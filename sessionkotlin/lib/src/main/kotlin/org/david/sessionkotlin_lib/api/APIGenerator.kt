package org.david.sessionkotlin_lib.api

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.david.sessionkotlin_lib.backend.*
import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.RootEnv
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.types.*
import java.io.File

private const val GENERATED_COMMENT = "This is a generated file. Do not change it."
private val endClassName = ClassName("", "End")
private fun buildClassName(protocolName: String, r: SKRole, count: Int? = null) =
    StringBuilder("${protocolName}_$r")
        .append(if (count != null) "_$count" else "")
        .toString()

private data class ICNames(val interfaceClassName: ClassName, val className: ClassName)
private data class FunSpecs(val abstractSpec: FunSpec, val overrideSpec: FunSpec)
private data class GenRet(val interfaceClassPair: ICNames, val counter: Int)

private val roleMap = mutableMapOf<SKRole, ClassName>()

internal fun generateAPI(globalEnv: RootEnv) {
    val outputDirectory = File("build/generated/sessionkotlin/main/kotlin")
    val globalType = globalEnv.asGlobalType()
    genRoles(globalEnv.roles, outputDirectory) // populates roleMap
    globalEnv.roles.forEach {
        APIGenerator(globalEnv.name, it, globalType.project(it))
            .writeTo(outputDirectory)
    }
    genEndClass(outputDirectory)
}

private fun genEndClass(outputDirectory: File) {
    val fileSpecBuilder = FileSpec.builder(
        packageName = "",
        fileName = endClassName.simpleName
    )
    fileSpecBuilder.addFileComment(GENERATED_COMMENT)
    val classBuilder = TypeSpec.classBuilder(endClassName)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder("e", SKMPEndpoint::class)
                        .build()
                )
                .build()
        )
    fileSpecBuilder.addType(classBuilder.build())
    fileSpecBuilder.build().writeTo(outputDirectory)
}

private fun genRoles(roles: Set<SKRole>, outputDirectory: File) {
    val fileSpecBuilder = FileSpec.builder(
        packageName = "",
        fileName = "Roles"
    )
    fileSpecBuilder.addFileComment(GENERATED_COMMENT)
    roles.forEach {
        val className = ClassName("", it.toString())
        roleMap[it] = className
        fileSpecBuilder
            .addType(
                TypeSpec.objectBuilder(className)
                    .addSuperinterface(SKGenRole::class)
                    .build()
            )
    }
    fileSpecBuilder.build().writeTo(outputDirectory)
}

private class APIGenerator(
    private val protocolName: String,
    val role: SKRole,
    private val localType: LocalType,
) {

    companion object {
        const val initialStateCount = 1
    }

    private val recursionMap: MutableMap<RecursionTag, ICNames> = mutableMapOf()
    private val fileSpecBuilder = FileSpec
        .builder(
            packageName = "",
            fileName = buildClassName(protocolName, role)
        ).addFileComment(GENERATED_COMMENT)

    private fun genLocals(
        l: LocalType,
        stateIndex: Int,
        tag: RecursionTag? = null,
        superInterface: ClassName? = null,
        branch: String? = null,
    ): GenRet {
        val suffix = "_" + (branch ?: "Interface")
        val interfaceName = ClassName("", buildClassName(protocolName, role, stateIndex) + suffix)
        val className = ClassName("", buildClassName(protocolName, role, stateIndex))
        if (tag != null) {
            recursionMap[tag] = ICNames(interfaceName, className)
        }
        val interfaceBuilder = TypeSpec
            .interfaceBuilder(interfaceName)
            .addModifiers(KModifier.SEALED)
        if (superInterface != null) {
            interfaceBuilder.addSuperinterface(superInterface)
        }

        val classBuilder = TypeSpec
            .classBuilder(className)
            .addSuperinterface(interfaceName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec
                            .builder("e", SKMPEndpoint::class)
                            .build()
                    )
                    .build()
            ).addProperty(
                PropertySpec.builder("e", SKMPEndpoint::class)
                    .initializer("e")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )

        if (stateIndex > initialStateCount)
            classBuilder.addModifiers(KModifier.PRIVATE)

        return when (l) {
            LocalTypeEnd -> GenRet(ICNames(endClassName, endClassName), stateIndex)
            is LocalTypeRecursion -> GenRet(recursionMap.getValue(l.tag), stateIndex)
            is LocalTypeRecursionDefinition -> genLocals(l.cont, stateIndex, l.tag)
            is LocalTypeSend -> {
                classBuilder.superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                val ret = genLocals(l.cont, stateIndex + 1)
                val methodName = "sendTo${l.to}"
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("arg", l.type.kotlin)
                        .build()
                else null

                val codeBlock = CodeBlock.builder()
                    .let {
                        val pName = parameter?.name ?: "Unit"
                        it.addStatement("super.send(${roleMap[l.to]}, $pName)")
                        it
                    }
                    .addStatement("return (${ret.interfaceClassPair.className.constructorReference()})(e)")
                    .build()

                val functions = genMsgPassing(
                    methodName,
                    codeBlock,
                    parameter,
                    ret.interfaceClassPair.interfaceClassName
                )
                interfaceBuilder.addFunction(functions.abstractSpec)
                classBuilder.addFunction(functions.overrideSpec)
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), ret.counter)
            }
            is LocalTypeReceive -> {
                classBuilder.superclass(SKInputEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                val ret = genLocals(l.cont, stateIndex + 1)
                val methodName = "receiveFrom${l.from}"
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
                        .build()
                else null

                val codeBlock = CodeBlock.builder()
                    .let {
                        val pName = parameter?.name ?: "Unit"
                        it.addStatement("super.receive(${roleMap[l.from]}, $pName)")
                        it
                    }
                    .addStatement("return (${ret.interfaceClassPair.className.constructorReference()})(e)")
                    .build()

                val functions = genMsgPassing(
                    methodName,
                    codeBlock,
                    parameter,
                    ret.interfaceClassPair.interfaceClassName
                )
                interfaceBuilder.addFunction(functions.abstractSpec)
                classBuilder.addFunction(functions.overrideSpec)
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), ret.counter)
            }
            is LocalTypeExternalChoice -> {
                classBuilder.superclass(SKExternalEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                val branchInterfaceName = ClassName(
                    "",
                    buildClassName(protocolName, role, stateIndex + 1) + "_Branch"
                )
                val branchInterfaceBuilder = TypeSpec
                    .interfaceBuilder(branchInterfaceName)
                    .addModifiers(KModifier.SEALED)

                var newIndex = stateIndex + 1
                val whenBlock = CodeBlock.builder()
                for ((k, v) in l.cases) {
                    val ret = genLocals(
                        v,
                        newIndex,
                        superInterface = branchInterfaceName,
                        branch = k
                    )
                    whenBlock.addStatement("\"$k\" -> (${ret.interfaceClassPair.className.constructorReference()})(e)")
                    ret.interfaceClassPair.className
                    newIndex = ret.counter + 1
                }

                val methodName = "branch"
                val abstractFunction = FunSpec.builder(methodName)
                    .returns(branchInterfaceName)
                    .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

                val function = FunSpec.builder(methodName)
                    .returns(branchInterfaceName)
                    .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                    .let {
                        it.beginControlFlow("return when(super.receiveBranch(${roleMap[l.to]}))")
                        it.addCode(whenBlock.build())
                        it.addStatement("else -> throw RuntimeException(\"This shouldn't happen\")")
                        it.endControlFlow()
                        it
                    }

                interfaceBuilder.addFunction(abstractFunction.build())
                classBuilder.addFunction(function.build())

                fileSpecBuilder.addType(branchInterfaceBuilder.build())
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), newIndex)
            }
            is LocalTypeInternalChoice -> {
                classBuilder.superclass(SKInternalEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                var counter = stateIndex + 1
                for ((k, v) in l.cases) {
                    val ret = genLocals(v, counter)
                    counter = ret.counter + 1

                    val methodName = "branch$k"

                    val abstractFunction = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

                    val function = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .let {
                            it.addStatement("super.sendBranch(TODO())")
                            it.addStatement("return (${ret.interfaceClassPair.className.constructorReference()})(e)")
                            it
                        }

                    interfaceBuilder.addFunction(abstractFunction.build())
                    classBuilder.addFunction(function.build())
                }
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), counter)
            }
        }
    }

    fun writeTo(outputDirectory: File) {
        genLocals(localType, initialStateCount)
        fileSpecBuilder.build().writeTo(outputDirectory)
    }
}

private fun genMsgPassing(
    methodName: String,
    codeBlock: CodeBlock,
    parameter: ParameterSpec?,
    nextInterface: ClassName,
): FunSpecs {
    val abstractFunction = FunSpec.builder(methodName)
        .returns(nextInterface)
        .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
    val function = FunSpec.builder(methodName)
        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
        .returns(nextInterface)
        .addCode(codeBlock)
    if (parameter != null) {
        abstractFunction.addParameter(parameter)
        function.addParameter(parameter)
    }
    return FunSpecs(abstractFunction.build(), function.build())
}
