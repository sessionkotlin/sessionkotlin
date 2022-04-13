package org.david.sessionkotlin_lib.api

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.david.sessionkotlin_lib.backend.SKBuffer
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

internal fun generateAPI(globalEnv: RootEnv) {
    val outputDirectory = File("build/generated/sessionkotlin/main/kotlin")
    val globalType = globalEnv.asGlobalType()
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
    fileSpecBuilder.addComment(GENERATED_COMMENT)
    val classBuilder = TypeSpec.classBuilder(endClassName)
    fileSpecBuilder.addType(classBuilder.build())
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
        ).addComment(GENERATED_COMMENT)

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
        if (stateIndex > initialStateCount)
            classBuilder.addModifiers(KModifier.PRIVATE)

        return when (l) {
            LocalTypeEnd -> GenRet(ICNames(endClassName, endClassName), stateIndex)
            is LocalTypeRecursion -> GenRet(recursionMap.getValue(l.tag), stateIndex)
            is LocalTypeRecursionDefinition -> genLocals(l.cont, stateIndex, l.tag)
            is LocalTypeSend -> {
                val ret = genLocals(l.cont, stateIndex + 1)
                val methodName = "sendTo${l.to}"
                val codeBlock = CodeBlock.builder()
                    .addStatement("println(\"Send to ${l.to}\")")
                    .addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
                    .build()
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("arg", l.type.kotlin)
                        .build()
                else null

                val functions = genMsgPassing(methodName, codeBlock, parameter, ret.interfaceClassPair.interfaceClassName)
                interfaceBuilder.addFunction(functions.abstractSpec)
                classBuilder.addFunction(functions.overrideSpec)
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), ret.counter)
            }
            is LocalTypeReceive -> {
                val ret = genLocals(l.cont, stateIndex + 1)
                val methodName = "receiveFrom${l.from}"
                val codeBlock = CodeBlock.builder()
                    .addStatement("println(\"Receive from ${l.from}\")")
                    .addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
                    .build()
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
                        .build()
                else null

                val functions = genMsgPassing(methodName, codeBlock, parameter, ret.interfaceClassPair.interfaceClassName)
                interfaceBuilder.addFunction(functions.abstractSpec)
                classBuilder.addFunction(functions.overrideSpec)
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), ret.counter)
            }
            is LocalTypeExternalChoice -> {
                val branchInterfaceName = ClassName(
                    "",
                    buildClassName(protocolName, role, stateIndex + 1) + "_Branch"
                )
                val branchInterfaceBuilder = TypeSpec
                    .interfaceBuilder(branchInterfaceName)
                    .addModifiers(KModifier.SEALED)

                var newIndex = stateIndex + 1
                for ((k, v) in l.cases) {
                    val ret = genLocals(
                        v,
                        newIndex,
                        superInterface = branchInterfaceName,
                        branch = k
                    )
                    newIndex = ret.counter + 1
                }

                val methodName = "branch"
                val abstractFunction = FunSpec.builder(methodName)
                    .returns(branchInterfaceName)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .returns(branchInterfaceName)
                    .addModifiers(KModifier.OVERRIDE)
                    .let {
                        it.addStatement("println(\"External choice\")")
                        it.addStatement("TODO()")
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
                var counter = stateIndex + 1
                for ((k, v) in l.cases) {
                    val ret = genLocals(v, counter)
                    counter = ret.counter + 1

                    val methodName = "branch$k"

                    val abstractFunction = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.ABSTRACT)

                    val function = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.OVERRIDE)
                        .let {
                            it.addStatement("println(\"Internal choice\")")
                            it.addStatement("TODO()")
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

private fun APIGenerator.genMsgPassing(
    methodName: String,
    codeBlock: CodeBlock,
    parameter: ParameterSpec?,
    nextInterface: ClassName,
): FunSpecs {
    val abstractFunction = FunSpec.builder(methodName)
        .returns(nextInterface)
        .addModifiers(KModifier.ABSTRACT)
    val function = FunSpec.builder(methodName)
        .addModifiers(KModifier.OVERRIDE)
        .returns(nextInterface)
        .addCode(codeBlock)
    if (parameter != null) {
        abstractFunction.addParameter(parameter)
        function.addParameter(parameter)
    }
    return FunSpecs(abstractFunction.build(), function.build())
}
