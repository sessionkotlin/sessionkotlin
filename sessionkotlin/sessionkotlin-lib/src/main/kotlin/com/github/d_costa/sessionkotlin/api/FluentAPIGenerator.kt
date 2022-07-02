package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class FluentAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "fluent") {

    private val superInterfacePostFix = "Branch"
    private val endClassName = ClassName(packageName, "End")
    private val endpointParameter = ParameterSpec
        .builder("e", SKMPEndpoint::class)
        .build()
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
        state: State,
        stateTransitions: List<Transition>,
        role: SKRole,
    ): MutableList<TypeSpec> {
        val className = getClassName(state.id, role)
        val classBuilder = createStateClass(className)

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
                classBuilder
                    .superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, stateTransitions, classBuilder)
            }
            StateClassification.Input -> {
                classBuilder
                    .superclass(SKInputEndpoint::class)
                    .addSuperclassConstructorParameter(endpointParameter.name)
                addFunctions(role, stateTransitions, classBuilder)
            }
        }

        classes.add(classBuilder.build())

        return classes
    }

    private fun createBranchInterface(className: ClassName): TypeSpec =
        TypeSpec.interfaceBuilder(className)
            .addModifiers(KModifier.SEALED)
            .build()

    private fun createStateClass(className: ClassName, passMessage: Boolean = false): TypeSpec.Builder =
        TypeSpec.classBuilder(className)
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

    /**
     * Create a branch function, add it to [classBuilder], and return a collection of classes representing
     * the next possible states.
     */
    private fun addBranchFunction(
        stateId: StateId,
        role: SKRole,
        transitions: List<Transition>,
        classBuilder: TypeSpec.Builder,
    ): Iterable<TypeSpec> {
        classBuilder.superclass(SKInputEndpoint::class)
            .addSuperclassConstructorParameter(endpointParameter.name)

        val classes = mutableListOf<TypeSpec>()

        val superInterfaceName = getClassName(stateId, role, superInterfacePostFix)
        val branchSuperInterface = createBranchInterface(superInterfaceName)
        classes.add(branchSuperInterface)

        val functionName = "branch"
        val functionBuilder = FunSpec.builder(functionName)
            .returns(superInterfaceName)
            .addModifiers(KModifier.SUSPEND)

        val functionBody = CodeBlock.builder()

        functionBody.addStatement("val %L = receive(${roleMap[commonSource(transitions)]})", msgProp.name)
        functionBody.beginControlFlow("return when(%L.label)", msgProp.name)

        for (t in transitions) {
            // Create a new intermediary class
            val intermediaryClassName = getClassName(stateId, role, "_${t.action.label.frontendName()}")
            val intermediaryClass = createStateClass(intermediaryClassName, true)
                .addSuperinterface(superInterfaceName)
                .superclass(SKCaseEndpoint::class)
                .addSuperclassConstructorParameter(endpointParameter.name)
                .addSuperclassConstructorParameter(msgParameter.name)

            functionBody.addStatement("%S -> %T(e, msg)", t.action.label.name, intermediaryClassName)

            // Function for the new class
            val function = FunSpec.builder(generateFunctionName(t.action))
                .addModifiers(KModifier.SUSPEND)
                .addParameters(generateFunctionParameters(t.action))
                .addCode(generateFunctionBody(t.action, getClassName(t.cont, role)))
                .returns(getClassName(t.cont, role))

            intermediaryClass.addFunction(function.build())
            classes.add(intermediaryClass.build())
        }

        val elseStatement = "else -> throw %T(\"This should not happen. branch: \${%L.label}\")"
        functionBody.addStatement(elseStatement, RuntimeException::class, msgProp.name)
        functionBody.endControlFlow()

        functionBuilder.addCode(functionBody.build())
        classBuilder.addFunction(functionBuilder.build())

        return classes
    }

    private fun commonSource(transitions: List<Transition>): SKRole {
        val sources = transitions.map { (it.action as ReceiveAction).from }.toSet()
        if (sources.size > 1)
            throw RuntimeException("Inconsistent external choice: [${sources.joinToString()}]")
        return sources.first()
    }

    /**
     * Create functions and add them to [classBuilder].
     */
    private fun addFunctions(role: SKRole, transitions: List<Transition>, classBuilder: TypeSpec.Builder) {
        for (t in transitions) {
            val nextClassName = getClassName(t.cont, role)
            val functionName = generateFunctionName(t.action)

            val function = FunSpec.builder(functionName)
                .returns(nextClassName)
                .addCode(generateFunctionBody(t.action, nextClassName))
                .addParameters(generateFunctionParameters(t.action))
                .addModifiers(KModifier.SUSPEND)
                .build()
            classBuilder.addFunction(function)
        }
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

    private fun generateFunctionName(action: Action): String =
        when (action) {
            is ReceiveAction -> "receive${action.label.frontendName()}From${action.from}"
            is SendAction -> "send${action.label.frontendName()}To${action.to}"
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

    private fun classifyState(stateId: StateId, transitions: List<Transition>): StateClassification {
        return if (transitions.all { it.action is ReceiveAction }) {
            if (transitions.size == 1)
                StateClassification.Input
            else
                StateClassification.ExternalChoice
        } else if (transitions.all { it.action is SendAction })
            StateClassification.Output
        else throw RuntimeException("State $stateId is not supported.")
    }
}

internal enum class StateClassification {
    Output, Input, ExternalChoice
}
