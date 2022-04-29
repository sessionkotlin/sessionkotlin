package org.david.sessionkotlin.api

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.david.sessionkotlin.api.exception.NoMessageLabelException
import org.david.sessionkotlin.backend.SKBranch
import org.david.sessionkotlin.backend.SKBuffer
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.SKPayload
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.RootEnv
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.types.*
import org.david.sessionkotlin.util.asClassname
import org.david.sessionkotlin.util.capitalized
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
        APIGenerator(globalEnv.protocolName.asClassname(), it, globalType.project(it))
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
        .addAnnotation(
            AnnotationSpec
                .builder(ClassName("", "Suppress"))
                .addMember("%S", "unused_parameter")
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
                    .superclass(SKGenRole::class)
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

    private val callbacksInterfaceName = ClassName("", buildClassName(protocolName + "Callbacks", role))
    private val callbacksClassName = ClassName("", buildClassName(protocolName + "CallbacksClass", role))
    private val callbacksInterface = TypeSpec.interfaceBuilder(callbacksInterfaceName)
    private val callbacksInterfaceFile = FileSpec
        .builder(
            packageName = "",
            fileName = callbacksInterfaceName.simpleName
        ).addFileComment(GENERATED_COMMENT)
    private val callbacksParameterName = "callbacks"

    private fun genLocals(
        l: LocalType,
        stateIndex: Int,
        callbackCode: CodeBlock.Builder,
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

        if (stateIndex > initialStateCount) {
            classBuilder.addModifiers(KModifier.PRIVATE)
        }

        fun recursionVariable(tag: RecursionTag) = "var$tag"

        return when (l) {
            LocalTypeEnd -> GenRet(ICNames(endClassName, endClassName), stateIndex)
            is LocalTypeRecursion -> {
                callbackCode.addStatement("%L = true", recursionVariable(l.tag))
                GenRet(recursionMap.getValue(l.tag), stateIndex)
            }
            is LocalTypeRecursionDefinition -> {
                callbackCode.beginControlFlow("do")
                callbackCode.addStatement("var %L = %L", recursionVariable(l.tag), false)
                val ret = genLocals(l.cont, stateIndex, callbackCode, l.tag)
                callbackCode.endControlFlow()
                callbackCode.add("while(%L)", recursionVariable(l.tag))
                ret
            }
            is LocalTypeSend -> {
                classBuilder.superclass(SKOutputEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                val nextCallbackCode = CodeBlock.builder()
                val ret = genLocals(l.cont, stateIndex + 1, nextCallbackCode)
                val methodName = "sendTo${l.to}"
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("arg", l.type.kotlin)
                        .build()
                else null

                val codeBlock = CodeBlock.builder()
                    .let {
                        val pName = parameter?.name ?: "Unit"
                        it.addStatement("super.send(%L, %L, %S)", roleMap[l.to], pName, l.branchLabel)
                        it
                    }
                    .addStatement("return %T(e)", ret.interfaceClassPair.className)
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

                if (l.msgLabel != null) {
                    val name = "onSend${l.msgLabel.capitalized()}To${l.to}"

                    callbacksInterface.addFunction(
                        FunSpec.builder(name)
                            .addModifiers(KModifier.ABSTRACT)
                            .returns(l.type.kotlin)
                            .build()
                    )
                    if (l.branchLabel != null) {
                        callbackCode.add(
                            CodeBlock.builder()
                                .addStatement(
                                    "super.sendProtected(%L, %T(\"%L\"))",
                                    roleMap[l.to],
                                    SKBranch::class,
                                    l.branchLabel
                                )
                                .build()
                        )
                    }
                    callbackCode.add(
                        CodeBlock.builder()
                            .addStatement(
                                "super.sendProtected(%L, %T(%L.%L()))",
                                roleMap[l.to],
                                SKPayload::class,
                                callbacksParameterName, name
                            )
                            .build()
                    )
                    callbackCode.add(nextCallbackCode.build())
                } else {
                    throw NoMessageLabelException(l.asString())
                }

                GenRet(ICNames(interfaceName, className), ret.counter)
            }
            is LocalTypeReceive -> {
                classBuilder.superclass(SKInputEndpoint::class)
                    .addSuperclassConstructorParameter("e")
                val nextCallbackCode = CodeBlock.builder()
                val ret = genLocals(l.cont, stateIndex + 1, nextCallbackCode)
                val methodName = "receiveFrom${l.from}"
                val parameter = if (l.type != Unit::class.java)
                    ParameterSpec
                        .builder("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
                        .build()
                else null

                val codeBlock = CodeBlock.builder()
                    .let {
                        if (parameter != null)
                            it.addStatement("super.receive(%L, %L)", roleMap[l.from], parameter.name)
                        else
                            it.addStatement(
                                "super.receive(%L, %T())", roleMap[l.from],
                                SKBuffer::class.asClassName().parameterizedBy(Unit::class.asClassName())
                            )
                        it
                    }
                    .addStatement("return %T(e)", ret.interfaceClassPair.className)
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

                if (l.msgLabel != null) {
                    val name = "onReceive${l.msgLabel.capitalized()}From${l.from}"

                    callbacksInterface.addFunction(
                        FunSpec.builder(name)
                            .addModifiers(KModifier.ABSTRACT)
                            .let {
                                if (parameter != null)
                                    it.addParameter(ParameterSpec.builder("value", l.type.kotlin).build())
                                it
                            }
                            .build()
                    )
                    callbackCode.add(
                        CodeBlock.builder()
                            .addStatement(
                                "%L.%L((super.receiveProtected(%L) as %T).payload)",
                                callbacksParameterName, name,
                                roleMap[l.from],
                                SKPayload::class.parameterizedBy(l.type.kotlin)
                            )
                            .build()
                    )
                    callbackCode.add(nextCallbackCode.build())
                } else {
                    throw NoMessageLabelException(l.asString())
                }

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
                callbackCode.beginControlFlow(
                    "when((super.receiveProtected(%L) as %T).label)",
                    roleMap[l.to]!!, SKBranch::class
                )

                for ((k, v) in l.branches) {
                    val nextCallbackCode = CodeBlock.builder()
                    callbackCode.add("\"%L\" -> ", k)
                    callbackCode.beginControlFlow("")

                    val ret = genLocals(
                        v,
                        newIndex,
                        nextCallbackCode,
                        superInterface = branchInterfaceName,
                        branch = k
                    )
                    callbackCode.add(nextCallbackCode.build())
                    callbackCode.endControlFlow()

                    whenBlock.addStatement("\"%L\" -> %T(e)", k, ret.interfaceClassPair.className)
                    ret.interfaceClassPair.className
                    newIndex = ret.counter + 1
                }
                callbackCode.endControlFlow()

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
                        it.addStatement("else -> throw %T(\"This shouldn't happen\")", RuntimeException::class)
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
                classBuilder.superclass(SKEndpoint::class)
//                    .addSuperclassConstructorParameter("e")
                var counter = stateIndex + 1
                val choiceEnumClassname = ClassName("", "Choice$stateIndex")
                val choiceEnum = TypeSpec.enumBuilder(choiceEnumClassname)
                val choiceFunctionName = "onChoose$stateIndex"
                callbacksInterface.addFunction(
                    FunSpec.builder(choiceFunctionName)
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(choiceEnumClassname)
                        .build()
                )
                callbackCode.beginControlFlow("when(%L.%L())", callbacksParameterName, choiceFunctionName)

                for ((k, v) in l.branches) {
                    val nextCallbackCode = CodeBlock.builder()
                    val enumConstant = "Choice${stateIndex}_$k"
                    callbackCode.add("%T.%L -> ", choiceEnumClassname, enumConstant)
                    callbackCode.beginControlFlow("")
                    choiceEnum.addEnumConstant(enumConstant)
                    val ret = genLocals(v, counter, nextCallbackCode)
                    callbackCode.add(nextCallbackCode.build())
                    callbackCode.endControlFlow()

                    counter = ret.counter + 1

                    val methodName = "branch$k"

                    val abstractFunction = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

                    val function = FunSpec.builder(methodName)
                        .returns(ret.interfaceClassPair.interfaceClassName)
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .let {
                            it.addStatement("return %T(e)", ret.interfaceClassPair.className)
                            it
                        }

                    interfaceBuilder.addFunction(abstractFunction.build())
                    classBuilder.addFunction(function.build())
                }
                callbackCode.endControlFlow()

                callbacksInterfaceFile.addType(choiceEnum.build())
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())
                GenRet(ICNames(interfaceName, className), counter)
            }
        }
    }

    fun writeTo(outputDirectory: File) {
        val codeBlock = CodeBlock.builder()
        genLocals(localType, initialStateCount, codeBlock)
        fileSpecBuilder.build().writeTo(outputDirectory)

        val classBuilder = TypeSpec
            .classBuilder(callbacksClassName)
            .superclass(SKMPEndpoint::class)
            .addFunction(
                FunSpec.builder("start")
                    .addModifiers(KModifier.SUSPEND)
                    .addCode(codeBlock.build()).build()
            )
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
        val suppressUnchecked = AnnotationSpec.builder(ClassName("", "Suppress"))
            .addMember("%S", "unchecked_cast")
            .build()
        FileSpec
            .builder(
                packageName = "",
                fileName = callbacksClassName.simpleName
            ).addFileComment(GENERATED_COMMENT)
            .addAnnotation(suppressUnchecked)
            .addType(classBuilder.build())
            .build().writeTo(outputDirectory)

        callbacksInterfaceFile
            .addType(callbacksInterface.build())
            .build()
            .writeTo(outputDirectory)
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
