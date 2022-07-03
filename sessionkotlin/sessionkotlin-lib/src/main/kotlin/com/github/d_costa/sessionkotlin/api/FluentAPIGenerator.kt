package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class FluentAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "fluent") {

    private val superInterfacePostFix = "Branch"
    private val stateInterfacePostFix = "Interface"
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
            val fsm = fsmFromLocalType(globalEnv.project(it))
            val file = generateFluentAPI(fsm, it)
            files.add(file)
        }
    }

    private fun getClassName(stateId: StateId, role: SKRole, postFix: String = ""): ClassName =
        if (stateId == FSM.endStateIndex)
            endClassName
        else
            ClassName(packageName, buildClassname(role, stateId, postFix))

    private fun getInterfaceClassName(stateId: StateId, role: SKRole, postFix: String = ""): ClassName =
        if (stateId == FSM.endStateIndex)
            endClassName
        else
            ClassName(packageName, buildClassname(role, stateId, "$postFix$stateInterfacePostFix"))


    private fun generateFluentAPI(fsm: FSM, role: SKRole): FileSpec {
        val file = newFile(buildClassname(role))

        for (state in fsm.states) {
            if (state.id == FSM.endStateIndex)
                continue
            val stateTransitions = fsm.transitions.getOrDefault(state.id, emptyList())
            val stateClasses = generateStateClasses(state, stateTransitions, role)

            for (s in stateClasses)
                file.addType(s)
        }
        return file.build()
    }

    private fun generateStateClasses(
        state: SimpleState,
        stateTransitions: List<SimpleTransition>,
        role: SKRole,
    ): MutableList<TypeSpec> {
        val className = getClassName(state.id, role)
        val interfaceClassName = getInterfaceClassName(state.id, role)
        val classBuilder = createStateClass(state.id, className, interfaceClassName)

        val classes = mutableListOf<TypeSpec>()
        when (classifyState(state.id, stateTransitions)) {
            StateClassification.ExternalChoice -> {
                classes.addAll(
                    addBranchFunction(
                        state.id,
                        role,
                        stateTransitions,
                        classBuilder
                    )
                )
            }
            StateClassification.Output -> {
                classBuilder.stateClass
                    .superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, stateTransitions, classBuilder)
            }
            StateClassification.Input -> {
                classBuilder.stateClass
                    .superclass(SKInputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, stateTransitions, classBuilder)
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
        className: ClassName, interfaceClassName: ClassName,
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
            .also { if (stateId != FSM.initialStateIndex) it.addModifiers(KModifier.PRIVATE) }
            .addSuperinterface(interfaceClassName)

        return StateInterfaceClass(interfaceBuilder, classBuilder)
    }

    /**
     * Create a branch function, add it to [classBuilder], and return a collection of classes representing
     * the next possible states.
     */
    private fun addBranchFunction(
        stateId: StateId,
        role: SKRole,
        transitions: List<SimpleTransition>,
        classBuilder: StateInterfaceClass,
    ): Iterable<TypeSpec> {

        classBuilder.stateClass.superclass(SKInputEndpoint::class)
            .addSuperclassConstructorParameter(endpointParameter.name)

        val classes = mutableListOf<TypeSpec>()

        val superInterfaceName = getClassName(stateId, role, superInterfacePostFix)
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

        functionBody.addStatement("val %L = receive(${roleMap[commonSource(transitions)]})", msgProp.name)
        functionBody.beginControlFlow("return when(%L.label)", msgProp.name)

        for (t in transitions) {
            // Create a new intermediary class
            val intermediaryClassName = getClassName(stateId, role, "_${t.action.label.frontendName()}")
            val intermediaryInterfaceName = getInterfaceClassName(stateId, role, "_${t.action.label.frontendName()}")
            val intermediaryClassInterface =
                createStateClass(stateId, intermediaryClassName, intermediaryInterfaceName, passMessage = true)

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

    private fun commonSource(transitions: List<SimpleTransition>): SKRole { // TODO delete
        val sources = transitions.map { (it.action as ReceiveAction).from }.toSet()
        if (sources.size > 1)
            throw RuntimeException("Inconsistent external choice: [${sources.joinToString()}]")
        return sources.first()
    }

    /**
     * Create functions and add them to [classBuilder].
     */
    private fun addFunctions(role: SKRole, transitions: List<SimpleTransition>, classBuilders: StateInterfaceClass) {
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
                        .builder("buf", SKBuffer::class.parameterizedBy(action.type.kotlin))
                        .build()
                }
                is SendAction -> {
                    ParameterSpec
                        .builder("arg", action.type.kotlin)
                        .build()
                }
            }

    private fun generateFunctionBody(action: Action, nextClassName: ClassName): CodeBlock {
        val param = generateFunctionParameter(action)
        val codeBlock = CodeBlock.builder()

        when (action) {
            is ReceiveAction -> if (param != null)
                codeBlock.addStatement("receive(%L, %L)", roleMap[action.from], param.name)
            else
                codeBlock.addStatement("receive(%L)", roleMap[action.from])

            is SendAction -> codeBlock.addStatement(
                "send(%L, %L, %S)",
                roleMap[action.to],
                param?.name ?: "Unit",
                action.label.name
            )
        }
        codeBlock.addStatement("return %T(%N)", nextClassName, endpointParameter)
        return codeBlock.build()
    }
}
