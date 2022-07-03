package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.github.d_costa.sessionkotlin.parser.RefinementParser
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class FluentAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "fluent") {

    private val superInterfacePostFix = "Branch"
    private val stateInterfacePostFix = "Interface"
    private val bufferParameterName = "buf"
    private val argParameterName = "arg"
    private val endClassName = ClassName(packageName, "End")

    private val msgParameter = ParameterSpec
        .builder("msg", SKMessage::class)
        .build()
    private val msgProp = PropertySpec.builder(msgParameter.name, SKMessage::class).build()

    init {
        val endFile = newFile("End").addType(
            TypeSpec.classBuilder(endClassName).primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(endpointParameter)
                    .build()
            ).addAnnotation(
                AnnotationSpec
                    .builder(suppressClassName)
                    .addMember("%S", "unused_parameter")
                    .build()
            ).build()
        ).build()
        files.add(endFile)

        roleMap.keys.forEach {
            val states = statesFromLocalType(globalEnv.project(it))
            val file = generateFluentAPI(states, it)
            files.add(file)
        }
    }

    private fun getClassName(stateId: StateId, role: SKRole, postFix: String = ""): ClassName =
        if (stateId == State.endStateIndex)
            endClassName
        else
            ClassName(packageName, buildClassname(role, stateId, postFix))

    private fun getInterfaceClassName(stateId: StateId, role: SKRole, postFix: String = ""): ClassName =
        if (stateId == State.endStateIndex)
            endClassName
        else
            ClassName(packageName, buildClassname(role, stateId, "$postFix$stateInterfacePostFix"))

    private fun generateFluentAPI(states: List<State>, role: SKRole): FileSpec {
        val file = newFile(buildClassname(role))
        file.addProperty(bindingsMapProperty)

        for (state in states) {
            val stateClasses = generateStateClasses(state, role)

            for (s in stateClasses)
                file.addType(s)
        }
        return file.build()
    }

    private fun generateStateClasses(
        state: State,
        role: SKRole,
    ): MutableList<TypeSpec> {
        val className = getClassName(state.id, role)
        val interfaceClassName = getInterfaceClassName(state.id, role)
        val classBuilder = createStateClass(state.id, className, interfaceClassName)

        val classes = mutableListOf<TypeSpec>()
        when (state) {
            EndState -> {
                return mutableListOf()
            }
            is ReceiveState -> {
                classBuilder.stateClass
                    .superclass(SKInputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, listOf(state.transition), classBuilder)
            }
            is SendState -> {
                classBuilder.stateClass
                    .superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, listOf(state.transition), classBuilder)
            }
            is InternalChoiceState -> {
                classBuilder.stateClass
                    .superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, state.transitions, classBuilder)
            }
            is ExternalChoiceState -> {
                classes.addAll(
                    addBranchFunction(
                        state,
                        role,
                        classBuilder
                    )
                )
            }
        }
        classes.add(classBuilder.stateClass.build())
        classes.add(classBuilder.stateInterface.build())

        return classes
    }

    private fun createBranchInterface(className: ClassName): TypeSpec =
        TypeSpec.interfaceBuilder(className)
            .addModifiers(KModifier.SEALED)
            .build()

    private data class StateInterfaceClass(val stateInterface: TypeSpec.Builder, val stateClass: TypeSpec.Builder)

    private fun createStateClass(
        stateId: StateId,
        className: ClassName,
        interfaceClassName: ClassName,
        passMessage: Boolean = false,
    ): StateInterfaceClass {
        val interfaceBuilder = TypeSpec.interfaceBuilder(interfaceClassName)
        val classBuilder = TypeSpec.classBuilder(className)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(endpointParameter)
                    .also { if (passMessage) it.addParameter(msgParameter) }
                    .build()
            ).addProperty(
                PropertySpec.builder(endpointParameter.name, endpointParameter.type)
                    .initializer(endpointParameter.name)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .also { if (stateId != State.initialStateIndex) it.addModifiers(KModifier.PRIVATE) }
            .addSuperinterface(interfaceClassName)

        return StateInterfaceClass(interfaceBuilder, classBuilder)
    }

    /**
     * Create a branch function, add it to [classBuilder], and return a collection of classes representing
     * the next possible states.
     */
    private fun addBranchFunction(
        state: ExternalChoiceState,
        role: SKRole,
        classBuilder: StateInterfaceClass,
    ): Iterable<TypeSpec> {

        classBuilder.stateClass.superclass(SKInputEndpoint::class)
            .addSuperclassConstructorParameter(endpointParameter.name)

        val classes = mutableListOf<TypeSpec>()

        val superInterfaceName = getClassName(state.id, role, superInterfacePostFix)
        val branchSuperInterface = createBranchInterface(superInterfaceName)
        classes.add(branchSuperInterface)

        val functionName = "branch"
        val functionBuilder = FunSpec.builder(functionName)
            .returns(superInterfaceName)
            .addModifiers(KModifier.SUSPEND)

        /**
         * branch() function, with the 'when' block
         */
        val functionBody = CodeBlock.builder()

        functionBody.addStatement("val %L = receive(${roleMap[state.from]})", msgProp.name)
        functionBody.beginControlFlow("return when(%L.label)", msgProp.name)

        for (t in state.transitions) {
            // Create a new intermediary class
            val intermediaryClassName = getClassName(state.id, role, "_${t.action.label.frontendName()}")
            val intermediaryInterfaceName = getInterfaceClassName(state.id, role, "_${t.action.label.frontendName()}")
            val intermediaryClassInterface =
                createStateClass(state.id, intermediaryClassName, intermediaryInterfaceName, passMessage = true)

            intermediaryClassInterface.stateInterface
                .addSuperinterface(superInterfaceName)

            intermediaryClassInterface.stateClass
                .superclass(SKCaseEndpoint::class)
                .addSuperclassConstructorParameter(endpointParameter.name)
                .addSuperclassConstructorParameter(msgParameter.name)

            functionBody.addStatement("%S -> %T(e, msg)", t.action.label.name, intermediaryClassName)

            /**
             * Function for the new class
             */
            val function = FunSpec.builder(generateFunctionName(t.action))
                .addModifiers(KModifier.SUSPEND)
                .addParameters(generateFunctionParameters(t.action))
                .returns(getInterfaceClassName(t.cont, role))

            val code = generateFunctionBody(t.action, getClassName(t.cont, role))
            addFunctionsToClassAndInterface(intermediaryClassInterface, function, code)

            classes.add(intermediaryClassInterface.stateClass.build())
            classes.add(intermediaryClassInterface.stateInterface.build())
        }

        val elseStatement = "else -> throw %T(\"This should not happen. branch: \${%L.label}\")"
        functionBody.addStatement(elseStatement, RuntimeException::class, msgProp.name)
        functionBody.endControlFlow()

        addFunctionsToClassAndInterface(classBuilder, functionBuilder, functionBody.build())

        return classes
    }

    /**
     * Create functions and add them to [classBuilders].
     */
    private fun addFunctions(role: SKRole, transitions: List<Transition>, classBuilders: StateInterfaceClass) {
        for (t in transitions) {
            val nextClassName = getClassName(t.cont, role)
            val nextInterfaceName = getInterfaceClassName(t.cont, role)
            val functionName = generateFunctionName(t.action)

            val function = FunSpec.builder(functionName)
                .returns(nextInterfaceName)
                .addParameters(generateFunctionParameters(t.action))
                .addModifiers(KModifier.SUSPEND)

            val code = generateFunctionBody(t.action, nextClassName)
            addFunctionsToClassAndInterface(classBuilders, function, code)
        }
    }

    private fun addFunctionsToClassAndInterface(
        classBuilders: StateInterfaceClass,
        functionWithoutCode: FunSpec.Builder,
        code: CodeBlock,
    ) {
        val initialModifiers = functionWithoutCode.modifiers.map { it } // copy

        classBuilders.stateInterface.addFunction(
            functionWithoutCode
                .addModifiers(KModifier.ABSTRACT)
                .build()
        )

        // Clear abstract
        functionWithoutCode.modifiers.clear()

        classBuilders.stateClass.addFunction(
            functionWithoutCode
                .addModifiers(initialModifiers)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(code).build()
        )
    }

    private fun generateFunctionParameters(action: Action): Iterable<ParameterSpec> {
        val p = generateFunctionParameter(action)
        return if (p == null)
            emptyList()
        else listOf(p)
    }

    private fun generateFunctionParameter(action: Action): ParameterSpec? =
        if (action.type == Unit::class.java) null
        else
            when (action) {
                is ReceiveAction -> {
                    ParameterSpec
                        .builder(bufferParameterName, SKBuffer::class.parameterizedBy(action.type.kotlin))
                        .build()
                }
                is SendAction -> {
                    ParameterSpec
                        .builder(argParameterName, action.type.kotlin)
                        .build()
                }
            }

    private fun generateFunctionBody(action: Action, nextClassName: ClassName): CodeBlock {
        val param = generateFunctionParameter(action)
        val codeBlock = CodeBlock.builder()

        when (action) {
            is ReceiveAction -> {
                if (param != null)
                    codeBlock.addStatement("receive(%L, %L)", roleMap[action.from], param.name)
                else
                    codeBlock.addStatement("receive(%L)", roleMap[action.from])

                addRefinementAssert(action, codeBlock)
            }

            is SendAction -> {
                addRefinementAssert(action, codeBlock)
                codeBlock.addStatement(
                    "send(%L, %L, %S)",
                    roleMap[action.to], param?.name ?: "Unit", action.label.name
                )
            }
        }
        codeBlock.addStatement("return %T(%N)", nextClassName, endpointParameter)
        return codeBlock.build()
    }

    private fun addRefinementAssert(action: SendAction, codeBlockBuilder: CodeBlock.Builder) {
        if (action.label.mentioned) {
            codeBlockBuilder.addStatement(
                "%L[%S] = %L.%M()",
                bindingsMapProperty.name,
                action.label.name,
                argParameterName,
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

    private fun addRefinementAssert(action: ReceiveAction, codeBlockBuilder: CodeBlock.Builder) {
        if (action.label.mentioned) {
            codeBlockBuilder.addStatement(
                "%L[%S] = %L.value.%M()",
                bindingsMapProperty.name,
                action.label.name,
                bufferParameterName,
                toValFunction
            )
        }
    }
}
