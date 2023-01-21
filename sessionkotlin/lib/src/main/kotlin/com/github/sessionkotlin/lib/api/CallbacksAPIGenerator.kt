package com.github.sessionkotlin.lib.api

import com.github.sessionkotlin.lib.api.exception.NoInitialStateException
import com.github.sessionkotlin.lib.backend.endpoint.SKMPEndpoint
import com.github.sessionkotlin.lib.backend.message.SKDummyMessage
import com.github.sessionkotlin.lib.backend.message.SKMessage
import com.github.sessionkotlin.lib.dsl.RootEnv
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.fsm.Action
import com.github.sessionkotlin.lib.fsm.EndState
import com.github.sessionkotlin.lib.fsm.ExternalChoiceState
import com.github.sessionkotlin.lib.fsm.InternalChoiceState
import com.github.sessionkotlin.lib.fsm.ReceiveAction
import com.github.sessionkotlin.lib.fsm.ReceiveState
import com.github.sessionkotlin.lib.fsm.SendAction
import com.github.sessionkotlin.lib.fsm.SendState
import com.github.sessionkotlin.lib.fsm.State
import com.github.sessionkotlin.lib.fsm.StateId
import com.github.sessionkotlin.lib.fsm.Transition
import com.github.sessionkotlin.lib.fsm.statesFromLocalType
import com.squareup.kotlinpoet.*
import java.util.*

/**
 * pre: message labels are unique
 */
internal class CallbacksAPIGenerator(globalEnv: RootEnv) : AbstractAPIGenerator(globalEnv, "callbacks") {
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
                codeBlockBuilder.beginControlFlow(
                    "when(%L.label)",
                    msgVariableName
                )

                for (t in state.transitions) {
                    val callbackName = generateFunctionName(t.action)
                    generateCallbackFunction(t.action, callbackName, callbackFunctionNames)
                        .ifPresent { callbacksInterfaceBuilder.addFunction(it) }

                    codeBlockBuilder.beginControlFlow("%S ->", t.action.label.name)
                    addRefinementAssert(
                        t.action, codeBlockBuilder,
                        msgVariableName
                    )

                    addReceiveCallbackStatement(
                        codeBlockBuilder, callbackName, t.action,
                        msgVariableName
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
                for (t: Transition in state.transitions) {
                    val enumConstant = getChoiceEnumConstant(state.id, t.action.label.name)
                    choiceEnumBuilder.addEnumConstant(enumConstant)

                    val callbackName = generateFunctionName(t.action)
                    generateCallbackFunction(t.action, callbackName, callbackFunctionNames)
                        .ifPresent { callbacksInterfaceBuilder.addFunction(it) }

                    codeBlockBuilder.beginControlFlow("%T.%L ->", choiceEnumClassname, enumConstant)

                    addSimpleFunctionStatements(t.action, codeBlockBuilder, callbackName)

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

    private fun addReceiveCallbackStatement(
        codeBlockBuilder: CodeBlock.Builder,
        callbackName: String,
        action: Action,
        msgVariableName: String
    ) {
        if (action.type == Unit::class.java) {
            codeBlockBuilder.addStatement(
                "%L.%L(%T)",
                callbacksParameterName,
                callbackName,
                Unit::class
            )
        } else {
            codeBlockBuilder.addStatement(
                "%L.%L(%L.payload as %T)",
                callbacksParameterName,
                callbackName,
                msgVariableName,
                if (action.type.kotlin == Unit::class.java) Unit::class else action.type.kotlin
            )
        }
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
            .addModifiers(KModifier.SUSPEND, KModifier.PRIVATE)

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
                addReceiveCallbackStatement(codeBlockBuilder, callbackName, action, msgVariableName)
            }
            is SendAction -> {
                if (action.type == Unit::class.java) {
                    codeBlockBuilder.addStatement("%L.%L()", callbacksParameterName, callbackName)
                } else {
                    codeBlockBuilder.addStatement(
                        "val %L = %L.%L()",
                        msgVariableName,
                        callbacksParameterName,
                        callbackName
                    )
                }

                addRefinementAssert(action, codeBlockBuilder)

                if (action.type == Unit::class.java) {
                    codeBlockBuilder.addStatement(
                        "sendProtected(%L, %T(%S))",
                        roleMap.getValue(action.to),
                        SKDummyMessage::class,
                        action.label.name
                    )
                } else {
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

    private fun addRefinementAssert(
        action: SendAction,
        codeBlockBuilder: CodeBlock.Builder,
        variableName: String? = null,
    ) {
        if (action.label.mentioned) {
            codeBlockBuilder.addStatement(
                "%L[%S] = %L.%M()",
                bindingsMapProperty.name,
                action.label.name,
                variableName ?: generateMessageLabel(action),
                toValFunction
            )
        }
        if (action.condition != null) {
            addRefinementAssertion(codeBlockBuilder, action.condition)
        }
    }

    private fun addRefinementAssert(
        action: ReceiveAction,
        codeBlockBuilder: CodeBlock.Builder,
        variableName: String? = null,
    ) {
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
