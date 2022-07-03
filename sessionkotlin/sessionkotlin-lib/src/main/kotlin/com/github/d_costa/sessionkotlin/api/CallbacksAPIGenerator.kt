package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.api.exception.NoInitialStateException
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.squareup.kotlinpoet.*

/**
 * pre: message labels are unique
 */
internal class CallbacksAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "callbacks") {
    private val callbacksInterfacePostfix = "Callbacks"
    private val callbacksEndpointPostfix = "CallbacksEndpoint"

    private val callbacksParameterName = "callbacks"
    private val startFunctionName = "start"
    private val variableName = "v"
    private val msgVariableName = "msg"

    /**
     * Maps StateIds to function names (inside the endpoint class)
     */
    private val stateFunctionMap = mutableMapOf<StateId, String>()

    init {
        roleMap.keys.forEach {
            println(it)
            val fsm = fsmFromLocalType(globalEnv.project(it))
            generateCallbacksAPI(fsm.asStates(), it)
            println(fsm.states)
            println(fsm.asStates())
            stateFunctionMap.clear()
        }
    }

    private fun generateCallbacksAPI(states: List<State>, role: SKRole) {
        val callbacksInterfaceName = getClassName(role, postFix = callbacksInterfacePostfix)
        val callbacksInterfaceBuilder = generateCallbacksInterface(callbacksInterfaceName)

        val callbacksEndpointClassName = getClassName(role, postFix = callbacksEndpointPostfix)
        val callbacksEndpointClassBuilder = generateEndpointClass(callbacksEndpointClassName, callbacksInterfaceName)
        val endpointClassFile = newFile(callbacksEndpointClassName.simpleName)

        val initialStateId = states.find { it.id == FSM.initialStateIndex }?.id ?: throw NoInitialStateException()

        for (s in states) {
            println(s)
            val stuffToAdd = processState(s, callbacksInterfaceBuilder, callbacksEndpointClassBuilder)
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
     * Return a function that implements the state's behaviour
     *
     */
    private fun processState(
        state: State,
        callbacksInterfaceBuilder: TypeSpec.Builder,
        callbacksEndpointClassBuilder: TypeSpec.Builder,
    ): Iterable<TypeSpec> {
        if (state.id in stateFunctionMap)
            return emptyList() // this state was already taken care of
        val stuffToAddToEndpointClassFile = mutableListOf<TypeSpec>()

        val stateFunctionName = getStateFunctionName(state.id)
        val stateFunctionBuilder = generateStateFunction(stateFunctionName)
        val codeBlockBuilder = CodeBlock.builder()

        when(state) {
            is ReceiveState -> {
                simpleState(state.transition, callbacksInterfaceBuilder, codeBlockBuilder)
            }
            is SendState -> {
                simpleState(state.transition, callbacksInterfaceBuilder, codeBlockBuilder)
            }
            is ExternalChoiceState -> {
                codeBlockBuilder.addStatement("val %L = receiveProtected(%L)", msgVariableName, roleMap.getValue(state.from))
                codeBlockBuilder.beginControlFlow("when(%L.label)", msgVariableName)

                for (t in state.transitions) {
                    val callbackName = generateFunctionName(t.action)
                    val callbackFunction = generateCallbackFunction(t.action, callbackName)
                    callbacksInterfaceBuilder.addFunction(callbackFunction)

                    codeBlockBuilder.beginControlFlow("%S ->", t.action.label.name)
                    codeBlockBuilder.addStatement("%L.%L(%L.payload as %T)", callbacksParameterName, callbackName, msgVariableName, t.action.type.kotlin)
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
                for (t in state.transitions) {
                    val enumConstant = getChoiceEnumConstant(state.id, t.action.label.name)
                    choiceEnumBuilder.addEnumConstant(enumConstant)

                    val callbackName = generateFunctionName(t.action)
                    val callbackFunction = generateCallbackFunction(t.action, callbackName)
                    callbacksInterfaceBuilder.addFunction(callbackFunction)

                    codeBlockBuilder.beginControlFlow("%T.%L ->", choiceEnumClassname, enumConstant)
                    codeBlockBuilder.addStatement("val %L = %L.%L()", msgVariableName, callbacksParameterName, callbackName)
                    codeBlockBuilder.addStatement("sendProtected(%L, %T(%S, %L))", roleMap.getValue((t.action as SendAction).to), SKMessage::class, t.action.label.name, msgVariableName)
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

    private fun simpleState(transition: Transition, callbacksInterfaceBuilder: TypeSpec.Builder, codeBlockBuilder: CodeBlock.Builder) {
        val callbackName = generateFunctionName(transition.action)
        val callbackFunction = generateCallbackFunction(transition.action, callbackName)
        callbacksInterfaceBuilder.addFunction(callbackFunction)

        addSimpleFunctionStatements(transition.action, codeBlockBuilder, callbackName)
        println(callbackName)
        codeBlockBuilder.addStatement("%L()", getStateFunctionName(transition.cont))
    }

    private fun generateStateFunction(name: String): FunSpec.Builder =
        FunSpec.builder(name)
            .addModifiers(KModifier.SUSPEND)

    private fun addSimpleFunctionStatements(action: Action, codeBlockBuilder: CodeBlock.Builder, callbackName: String)  {
        val msgVariableName = generateMessageLabel(action.label.name)

        when(action) {
            is ReceiveAction -> {
                codeBlockBuilder.addStatement("val %L = receiveProtected(%L)", msgVariableName, roleMap.getValue(action.from))
                codeBlockBuilder.addStatement("%L.%L(%L.payload as %T)", callbacksParameterName, callbackName, msgVariableName, action.type.kotlin)

            }
            is SendAction -> {
                codeBlockBuilder.addStatement("val %L = %L.%L()", msgVariableName, callbacksParameterName, callbackName)
                codeBlockBuilder.addStatement("sendProtected(%L, %T(%S, %L))", roleMap.getValue(action.to), SKMessage::class, action.label.name, msgVariableName)
            }
        }
    }

    private fun getStateFunctionName(stateId: StateId) =
        if (stateId == FSM.endStateIndex) "end" else "state$stateId"

    private fun generateMessageLabel(msgLabel: String) = "$msgVariableName$msgLabel"

    private fun generateCallbackFunction(action: Action, functionName: String): FunSpec =
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

}
