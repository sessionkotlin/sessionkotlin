package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.api.exception.NoInitialStateException
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.github.d_costa.sessionkotlin.parser.RefinementParser
import com.squareup.kotlinpoet.*
import java.util.*

/**
 * pre: message labels are unique
 */
internal class CallbacksAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "callbacks") {
    private val callbacksInterfacePostfix = "Callbacks"
    private val callbacksEndpointPostfix = "CallbacksEndpoint"

    private val callbacksParameterName = "callbacks"
    private val startFunctionName = "start"
    private val variableName = "v"

    init {
        roleMap.keys.forEach {
            val states = statesFromLocalType(globalEnv.project(it))

            /**
             * Maps StateIds to function names (inside the endpoint class)
             */
            val stateFunctionMap = mutableMapOf<StateId, String>()

            /**
             * Names of generated callback functions
             */
            val callbackFunctionNames = mutableSetOf<String>()

            generateCallbacksAPI(states, it, stateFunctionMap, callbackFunctionNames)
            stateFunctionMap.clear()
        }
    }

    private fun generateCallbacksAPI(
        states: List<State>,
        role: SKRole,
        stateFunctionMap: MutableMap<StateId, String>,
        callbackFunctionNames: MutableSet<String>,
    ) {
        val callbacksInterfaceName = getClassName(role, postFix = callbacksInterfacePostfix)
        val callbacksInterfaceBuilder = generateCallbacksInterface(callbacksInterfaceName)

        val callbacksEndpointClassName = getClassName(role, postFix = callbacksEndpointPostfix)
        val callbacksEndpointClassBuilder = generateEndpointClass(callbacksEndpointClassName, callbacksInterfaceName)
        val endpointClassFile = newFile(callbacksEndpointClassName.simpleName)

        val initialStateId = states.find { it.id == State.initialStateIndex }?.id ?: throw NoInitialStateException()

        callbacksEndpointClassBuilder.addProperty(bindingsMapProperty)

        for (s in states) {

            val stuffToAdd = processState(
                s, callbacksInterfaceBuilder, callbacksEndpointClassBuilder,
                stateFunctionMap, callbackFunctionNames
            )
            for (t in stuffToAdd)
                endpointClassFile.addType(t)
        }

        val codeBlock = CodeBlock.builder()
            .addStatement("%L()", stateFunctionMap.getValue(initialStateId))
            .build()

        val startFunction = generateStartFunction(codeBlock)
        callbacksEndpointClassBuilder.addFunction(startFunction)

        endpointClassFile.addType(callbacksEndpointClassBuilder.build())
        val callbacksFile = newFile(callbacksInterfaceName.simpleName).addType(callbacksInterfaceBuilder.build())

        files.add(endpointClassFile.build())
        files.add(callbacksFile.build())
    }

    private fun generateStartFunction(codeBlock: CodeBlock): FunSpec =
        FunSpec.builder(startFunctionName)
            .addModifiers(KModifier.SUSPEND)
            .addCode(codeBlock)
            .build()

    /**
     * Return an iterable of TypeSpecs that should get added to the endpoint class file
     */
    private fun processState(
        state: State,
        callbacksInterfaceBuilder: TypeSpec.Builder,
        callbacksEndpointClassBuilder: TypeSpec.Builder,
        stateFunctionMap: MutableMap<StateId, String>,
        callbackFunctionNames: MutableSet<String>,
    ): Iterable<TypeSpec> {
        if (state.id in stateFunctionMap)
            return emptyList() // this state was already taken care of
        val stuffToAddToEndpointClassFile = mutableListOf<TypeSpec>()

        val stateFunctionName = getStateFunctionName(state.id)
        val stateFunctionBuilder = generateStateFunction(stateFunctionName)
        val codeBlockBuilder = CodeBlock.builder()

        when (state) {
            is ReceiveState -> {
                simpleState(state.transition, callbacksInterfaceBuilder, codeBlockBuilder, callbackFunctionNames)
            }
            is SendState -> {
                simpleState(state.transition, callbacksInterfaceBuilder, codeBlockBuilder, callbackFunctionNames)
            }
            is ExternalChoiceState -> {
                codeBlockBuilder.addStatement(
                    "val %L = receiveProtected(%L)",
                    msgVariableName,
                    roleMap.getValue(state.from)
                )
                codeBlockBuilder.beginControlFlow("when(%L.label)", msgVariableName)

                for (t in state.transitions) {
                    val callbackName = generateFunctionName(t.action)
                    generateCallbackFunction(t.action, callbackName, callbackFunctionNames)
                        .ifPresent { callbacksInterfaceBuilder.addFunction(it) }

                    codeBlockBuilder.beginControlFlow("%S ->", t.action.label.name)
                    addRefinementAssert(t.action, codeBlockBuilder, msgVariableName)
                    codeBlockBuilder.addStatement(
                        "%L.%L(%L.payload as %T)",
                        callbacksParameterName,
                        callbackName,
                        msgVariableName,
                        t.action.type.kotlin
                    )
                    codeBlockBuilder.addStatement("%L()", getStateFunctionName(t.cont))
                    codeBlockBuilder.endControlFlow()
                }
                codeBlockBuilder.endControlFlow()
            }
            is InternalChoiceState -> {
                val choiceEnumClassname = getChoiceEnumClassName(state.id)
                val choiceEnumBuilder = TypeSpec.enumBuilder(choiceEnumClassname)

                val choiceCallbackName = "on${choiceEnumClassname.simpleName}"
                val choiceCallbackFunction = FunSpec.builder(choiceCallbackName)
                    .returns(choiceEnumClassname)
                    .addModifiers(KModifier.ABSTRACT)
                    .build()

                callbacksInterfaceBuilder.addFunction(choiceCallbackFunction)

                codeBlockBuilder.beginControlFlow("when(%L.%L())", callbacksParameterName, choiceCallbackName)
                for (t: SendTransition in state.transitions) {
                    val enumConstant = getChoiceEnumConstant(state.id, t.action.label.name)
                    choiceEnumBuilder.addEnumConstant(enumConstant)

                    val callbackName = generateFunctionName(t.action)
                    generateCallbackFunction(t.action, callbackName, callbackFunctionNames)
                        .ifPresent { callbacksInterfaceBuilder.addFunction(it) }

                    codeBlockBuilder.beginControlFlow("%T.%L ->", choiceEnumClassname, enumConstant)
                    codeBlockBuilder.addStatement(
                        "val %L = %L.%L()",
                        generateMessageLabel(t.action),
                        callbacksParameterName,
                        callbackName
                    )
                    addRefinementAssert(t.action, codeBlockBuilder)
                    codeBlockBuilder.addStatement(
                        "sendProtected(%L, %T(%S, %L))",
                        roleMap.getValue(t.action.to),
                        SKMessage::class,
                        t.action.label.name,
                        generateMessageLabel(t.action)
                    )
                    codeBlockBuilder.addStatement("%L()", getStateFunctionName(t.cont))
                    codeBlockBuilder.endControlFlow()
                }
                codeBlockBuilder.endControlFlow()
                stuffToAddToEndpointClassFile.add(choiceEnumBuilder.build())
            }
            EndState -> {
                codeBlockBuilder.addStatement("// Do nothing")
            }
        }

        stateFunctionBuilder.addCode(codeBlockBuilder.build())
        callbacksEndpointClassBuilder.addFunction(stateFunctionBuilder.build())

        stateFunctionMap[state.id] = stateFunctionName

        return stuffToAddToEndpointClassFile
    }

    private fun getChoiceEnumConstant(stateId: StateId, label: String): String = "Choice${stateId}_$label"

    private fun getChoiceEnumClassName(stateId: StateId): ClassName =
        ClassName(packageName, "Choice$stateId")

    private fun simpleState(
        transition: Transition,
        callbacksInterfaceBuilder: TypeSpec.Builder,
        codeBlockBuilder: CodeBlock.Builder,
        callbackFunctionNames: MutableSet<String>,
    ) {
        val callbackName = generateFunctionName(transition.action)
        generateCallbackFunction(transition.action, callbackName, callbackFunctionNames)
            .ifPresent { callbacksInterfaceBuilder.addFunction(it) }

        addSimpleFunctionStatements(transition.action, codeBlockBuilder, callbackName)
        codeBlockBuilder.addStatement("%L()", getStateFunctionName(transition.cont))
    }

    private fun generateStateFunction(name: String): FunSpec.Builder =
        FunSpec.builder(name)
            .addModifiers(KModifier.SUSPEND)

    private fun addSimpleFunctionStatements(action: Action, codeBlockBuilder: CodeBlock.Builder, callbackName: String) {
        val msgVariableName = generateMessageLabel(action)

        when (action) {
            is ReceiveAction -> {
                codeBlockBuilder.addStatement(
                    "val %L = receiveProtected(%L)",
                    msgVariableName,
                    roleMap.getValue(action.from)
                )
                addRefinementAssert(action, codeBlockBuilder)
                codeBlockBuilder.addStatement(
                    "%L.%L(%L.payload as %T)",
                    callbacksParameterName,
                    callbackName,
                    msgVariableName,
                    action.type.kotlin
                )
            }
            is SendAction -> {
                codeBlockBuilder.addStatement("val %L = %L.%L()", msgVariableName, callbacksParameterName, callbackName)
                addRefinementAssert(action, codeBlockBuilder)
                codeBlockBuilder.addStatement(
                    "sendProtected(%L, %T(%S, %L))",
                    roleMap.getValue(action.to),
                    SKMessage::class,
                    action.label.name,
                    msgVariableName
                )
            }
        }
    }

    private fun getStateFunctionName(stateId: StateId) =
        if (stateId == State.endStateIndex) "end" else "state$stateId"

    /**
     * Returns empty if a function with this name was already generated
     */
    private fun generateCallbackFunction(
        action: Action,
        functionName: String,
        callbackFunctionNames: MutableSet<String>,
    ): Optional<FunSpec> =
        if (functionName in callbackFunctionNames) Optional.empty()
        else Optional.of(
            when (action) {
                is ReceiveAction -> FunSpec.builder(functionName)
                    .addParameter(ParameterSpec.builder(variableName, action.type.kotlin).build())
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
                is SendAction -> FunSpec.builder(functionName)
                    .returns(action.type.kotlin)
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            }
        ).also { callbackFunctionNames.add(functionName) }

    /**
     * Return an interface builder to add callback definitions
     */
    private fun generateCallbacksInterface(interfaceClassName: ClassName): TypeSpec.Builder =
        TypeSpec.interfaceBuilder(interfaceClassName)

    /**
     * Return a class builder that extends SKMPEndpoint
     */
    private fun generateEndpointClass(className: ClassName, callbacksInterfaceName: ClassName): TypeSpec.Builder =
        TypeSpec.classBuilder(className)
            .superclass(SKMPEndpoint::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec
                            .builder(callbacksParameterName, callbacksInterfaceName)
                            .build()
                    )
                    .build()
            ).addProperty(
                PropertySpec.builder(callbacksParameterName, callbacksInterfaceName)
                    .initializer(callbacksParameterName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )

    private fun getClassName(role: SKRole, postFix: String = ""): ClassName =
        ClassName(packageName, buildClassname(role, postFix = postFix))

    private fun addRefinementAssert(action: SendAction, codeBlockBuilder: CodeBlock.Builder, variableName: String? = null) {
        if (action.label.mentioned) {
            codeBlockBuilder.addStatement(
                "%L[%S] = %L.%M()",
                bindingsMapProperty.name,
                action.label.name,
                variableName ?: generateMessageLabel(action),
                toValFunction
            )
        }
        if (action.condition.isNotBlank()) {
            codeBlockBuilder.addStatement(
                "%M(%S, %T.parseToEnd(%S).value(%L))",
                assertFunction,
                action.condition,
                RefinementParser::class,
                action.condition,
                bindingsMapProperty.name
            )
        }
    }

    private fun addRefinementAssert(action: ReceiveAction, codeBlockBuilder: CodeBlock.Builder, variableName: String? = null) {
        if (action.label.mentioned) {
            codeBlockBuilder.addStatement(
                "%L[%S] = (%L.payload as %T).%M()",
                bindingsMapProperty.name,
                action.label.name,
                variableName ?: generateMessageLabel(action),
                action.type.kotlin,
                toValFunction
            )
        }
    }
}
