// package com.github.d_costa.sessionkotlin.api
//
// import com.github.d_costa.sessionkotlin.backend.SKBuffer
// import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
// import com.github.d_costa.sessionkotlin.backend.message.SKDummyMessage
// import com.github.d_costa.sessionkotlin.backend.message.SKMessage
// import com.github.d_costa.sessionkotlin.dsl.RecursionTag
// import com.github.d_costa.sessionkotlin.dsl.RootEnv
// import com.github.d_costa.sessionkotlin.dsl.SKRole
// import com.github.d_costa.sessionkotlin.dsl.types.*
// import com.github.d_costa.sessionkotlin.fsm.*
// import com.github.d_costa.sessionkotlin.fsm.CLT
// import com.github.d_costa.sessionkotlin.fsm.CLTEnd
// import com.github.d_costa.sessionkotlin.fsm.CLTRecursion
// import com.github.d_costa.sessionkotlin.fsm.CLTSend
// import com.github.d_costa.sessionkotlin.fsm.CLTTagged
// import com.github.d_costa.sessionkotlin.parser.RefinementParser
// import com.github.d_costa.sessionkotlin.parser.symbols.values.RefinedValue
// import com.github.d_costa.sessionkotlin.util.asClassname
// import com.github.d_costa.sessionkotlin.util.asPackageName
// import com.github.d_costa.sessionkotlin.util.capitalized
// import com.squareup.kotlinpoet.*
// import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
// import java.io.File
//
// private const val GENERATED_COMMENT = "This file was generated by sessionkotlin. Do not change it."
// private const val endSimpleClassName = "End"
// private val suppressClassName = ClassName("kotlin", "Suppress")
//
// private fun buildClassName(protocolName: String, r: SKRole, count: Int? = null) =
//    StringBuilder("${protocolName}$r")
//        .append(if (count != null) "$count" else "")
//        .toString()
//
// private fun callbacks(basePackageName: String) = "$basePackageName.callback"
// private fun fluent(basePackageName: String) = "$basePackageName.fluent"
//
// private data class ICNames(val interfaceClassName: ClassName, val className: ClassName)
// private data class FunSpecs(val abstractSpec: FunSpec, val overrideSpec: FunSpec)
// private data class GenRet(val interfaceClassPair: ICNames, val counter: Int)
//
// private val roleMap = mutableMapOf<SKRole, ClassName>()
//
// internal fun generateAPI(globalEnv: RootEnv, genCallbacksAPI: Boolean) {
//    val basePackageName = globalEnv.protocolName.asPackageName()
//    val protocolClassName = globalEnv.protocolName.asClassname()
//    val outputDirectory = File("build/generated/sessionkotlin/main/kotlin")
//    genRoles(globalEnv.roles, protocolClassName, outputDirectory, basePackageName) // populates roleMap
//    globalEnv.roles.forEach {
//        APIGenerator(
//            protocolClassName, basePackageName, it,
//            globalEnv.project(it), genCallbacksAPI
//        ).writeTo(outputDirectory)
//    }
//    genEndClass(outputDirectory, fluent(basePackageName))
// }
//
// private const val endpointVariable = "e"
//
// private fun genEndClass(outputDirectory: File, packageName: String) {
//    val endClassName = ClassName(packageName, endSimpleClassName)
//    val fileSpecBuilder = FileSpec.builder(
//        packageName = packageName,
//        fileName = endClassName.simpleName
//    )
//    fileSpecBuilder.addFileComment(GENERATED_COMMENT)
//    val classBuilder = TypeSpec.classBuilder(endClassName)
//        .primaryConstructor(
//            FunSpec.constructorBuilder()
//                .addParameter(
//                    ParameterSpec
//                        .builder(endpointVariable, SKMPEndpoint::class)
//                        .build()
//                )
//                .build()
//        )
//        .addAnnotation(
//            AnnotationSpec
//                .builder(suppressClassName)
//                .addMember("%S", "unused_parameter")
//                .build()
//        )
//    fileSpecBuilder.addType(classBuilder.build())
//    fileSpecBuilder.build().writeTo(outputDirectory)
// }
//
// private fun genRoles(roles: Set<SKRole>, protocolName: String, outputDirectory: File, packageName: String) {
//    val fileSpecBuilder = FileSpec.builder(
//        packageName = packageName,
//        fileName = "${protocolName}Roles"
//    )
//    fileSpecBuilder.addFileComment(GENERATED_COMMENT)
//    roles.forEach {
//        val className = ClassName(packageName, it.toString())
//        roleMap[it] = className
//        fileSpecBuilder
//            .addType(
//                TypeSpec.objectBuilder(className)
//                    .superclass(SKGenRole::class)
//                    .build()
//            )
//    }
//    fileSpecBuilder.build().writeTo(outputDirectory)
// }
//
// private class APIGenerator(
//    private val protocolName: String,
//    basePackage: String,
//    val role: SKRole,
//    private val localType: LocalType,
//    private val genCallbacksAPI: Boolean,
// ) {
//
//    companion object {
//        const val initialStateCount = 1
//    }
//
//    private val callbacksPackage = callbacks(basePackage)
//    private val fluentPackage = fluent(basePackage)
//    private val bindingsVariableName = "bindings"
//    private val caseMsgVariable = "msg"
//    private val bindingsValueType = LinkedHashMap::class.parameterizedBy(String::class, RefinedValue::class)
//    private val recursionMap: MutableMap<RecursionTag, ICNames> = mutableMapOf()
//    private val callbacksInterfaceName = ClassName(callbacksPackage, buildClassName(protocolName + "Callbacks", role))
//    private val callbacksClassName =
//        ClassName(callbacksPackage, buildClassName(protocolName + "CallbackEndpoint", role))
//    private val callbacksInterface = TypeSpec.interfaceBuilder(callbacksInterfaceName)
//    private val callbacksInterfaceFile = FileSpec
//        .builder(
//            packageName = callbacksPackage,
//            fileName = callbacksInterfaceName.simpleName
//        ).addFileComment(GENERATED_COMMENT)
//    private val callbacksParameterName = "callbacks"
//    private val assertFunction = MemberName(
//        "com.github.d_costa.sessionkotlin.util",
//        "assertRefinement"
//    )
//    private val toValFunction = MemberName(
//        "com.github.d_costa.sessionkotlin.parser.symbols.values",
//        "toVal"
//    )
//    private val fileSpecBuilder = FileSpec
//        .builder(
//            packageName = fluentPackage,
//            fileName = buildClassName(protocolName, role)
//        ).addFileComment(GENERATED_COMMENT)
//        .addProperty(
//            PropertySpec.builder(
//                bindingsVariableName,
//                bindingsValueType
//            ).addModifiers(KModifier.PRIVATE)
//                .initializer("%T()", bindingsValueType).build()
//        )
//    val endClassName = ClassName(fluentPackage, endSimpleClassName)
//
//    fun recursionVariable(tag: RecursionTag) = "recursionTag${tag.hashCode()}"
//    fun msgVariable(label: String) = "msg$label"
//
//    private fun genLocals(
//        l: LocalType,
//        stateIndex: Int,
//        callbackCode: CodeBlock.Builder,
//        superInterface: ClassName? = null,
//        branch: String? = null,
//    ): GenRet {
//        // Fluent API
//        val interfaceSuffix = "_" + (branch ?: "Interface")
//        val interfaceName = ClassName(fluentPackage, buildClassName(protocolName, role, stateIndex) + interfaceSuffix)
//        val className = ClassName(fluentPackage, buildClassName(protocolName, role, stateIndex))
//        if (l is CLTTagged && l.recursionTag != null) {
//            recursionMap[l.recursionTag] = ICNames(interfaceName, className)
//        }
//        val interfaceBuilder = TypeSpec
//            .interfaceBuilder(interfaceName)
//
//        if (superInterface != null) {
//            interfaceBuilder.addSuperinterface(superInterface)
//        }
//
//        val classBuilder = TypeSpec
//            .classBuilder(className)
//            .addSuperinterface(interfaceName)
//            .primaryConstructor(
//                FunSpec.constructorBuilder()
//                    .addParameter(
//                        ParameterSpec
//                            .builder(endpointVariable, SKMPEndpoint::class)
//                            .build()
//                    )
//                    .build()
//            ).addProperty(
//                PropertySpec.builder(endpointVariable, SKMPEndpoint::class)
//                    .initializer(endpointVariable)
//                    .addModifiers(KModifier.PRIVATE)
//                    .build()
//            )
//
//        if (stateIndex > initialStateCount) {
//            classBuilder.addModifiers(KModifier.PRIVATE)
//        }
//        // End Fluent API
//
//        return when (l) {
//            is CLTEnd -> GenRet(ICNames(endClassName, endClassName), stateIndex)
//            is CLTRecursion -> {
//                callbackCode.addStatement("%L = true", recursionVariable(l.tag))
//                GenRet(recursionMap.getValue(l.tag), stateIndex)
//            }
// //            is LocalTypeRecursionDefinition -> {
// //                callbackCode.beginControlFlow("do")
// //                callbackCode.addStatement("var %L = %L", recursionVariable(l.tag), false)
// //                val ret = genLocals(l.cont, stateIndex, callbackCode, l.tag)
// //                callbackCode.endControlFlow()
// //                callbackCode.add("while(%L)", recursionVariable(l.tag))
// //                ret
// //            }
//            is CLTSend -> {
//                classBuilder.superclass(SKOutputEndpoint::class)
//                    .addSuperclassConstructorParameter(endpointVariable)
//                val nextCallbackCode = CodeBlock.builder()
//                val ret = genLocals(l.cont, stateIndex + 1, nextCallbackCode)
//                val methodName = "sendTo${l.to}"
//                val parameter = if (l.type != Unit::class.java)
//                    ParameterSpec
//                        .builder("arg", l.type.kotlin)
//                        .build()
//                else null
//
//                val codeBlock = CodeBlock.builder()
//                    .also {
//                        val pName = parameter?.name ?: "Unit"
//                        if (l.msgLabel.mentioned) {
//                            it.addStatement(
//                                "%L[%S] = %L.%M()",
//                                bindingsVariableName,
//                                l.msgLabel.label,
//                                pName,
//                                toValFunction
//                            )
//                        }
//                        if (l.condition.isNotBlank()) {
//                            it.addStatement(
//                                "%M(%S, %T.parseToEnd(%S).value(%L))",
//                                assertFunction,
//                                l.condition,
//                                RefinementParser::class,
//                                l.condition,
//                                bindingsVariableName
//                            )
//                        }
//                        it.addStatement("send(%L, %L)", roleMap[l.to], pName)
//                    }
//                    .addStatement("return %T(e)", ret.interfaceClassPair.className)
//                    .build()
//
//                val functions = genMsgPassing(
//                    methodName, codeBlock, parameter, ret.interfaceClassPair.interfaceClassName
//                )
//                interfaceBuilder.addFunction(functions.abstractSpec)
//                classBuilder.addFunction(functions.overrideSpec)
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//
//                if (genCallbacksAPI) {
//                    val callbackFunction =
//                        FunSpec.builder("onSend${l.msgLabel.label.capitalized()}To${l.to}")
//                            .addModifiers(KModifier.ABSTRACT)
//                            .returns(l.type.kotlin)
//                            .build()
//
//                    callbacksInterface.addFunction(callbackFunction)
//                    callbackCode.add(
//                        CodeBlock.builder()
//                            .addStatement(
//                                "val %L = %L.%N()",
//                                msgVariable(l.msgLabel.label), callbacksParameterName, callbackFunction
//                            )
//                            .also {
//                                if (l.msgLabel.mentioned) {
//                                    it.addStatement(
//                                        "%L[%S] = %L.%M()",
//                                        bindingsVariableName,
//                                        l.msgLabel.label,
//                                        msgVariable(l.msgLabel.label),
//                                        toValFunction
//                                    )
//                                }
//                            }
//                            .also {
//                                if (l.condition.isNotBlank()) {
//                                    it.addStatement(
//                                        "%M(%S, %T.parseToEnd(%S).value(%L))",
//                                        assertFunction,
//                                        l.condition,
//                                        RefinementParser::class,
//                                        l.condition,
//                                        bindingsVariableName
//                                    )
//                                }
//                            }
//                            .also {
//                                if (parameter == null) {
//                                    it.addStatement(
//                                        "sendProtected(%L, %T())",
//                                        roleMap[l.to], SKDummyMessage::class
//                                    )
//                                } else {
//                                    it.addStatement(
//                                        "sendProtected(%L, %T(%L, %S))",
//                                        roleMap[l.to], SKMessage::class, msgVariable(l.msgLabel.label),
//                                    )
//                                }
//                            }
//                            .build()
//                    )
//                    callbackCode.add(nextCallbackCode.build())
//                }
//                GenRet(ICNames(interfaceName, className), ret.counter)
//            }
//            is CLTReceive -> {
//                classBuilder.addSuperclassConstructorParameter(endpointVariable)
//                if (branch != null) {
//                    classBuilder.superclass(SKCaseEndpoint::class)
//                    addCaseMsgVariable(classBuilder)
//                } else {
//                    classBuilder.superclass(SKInputEndpoint::class)
//                }
//
//                val nextCallbackCode = CodeBlock.builder()
//                val ret = genLocals(l.cont, stateIndex + 1, nextCallbackCode)
//                val methodName = "receiveFrom${l.from}"
//                val bufferParam = if (l.type != Unit::class.java)
//                    ParameterSpec
//                        .builder("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
//                        .build()
//                else null
//
//                val codeBlock = CodeBlock.builder()
//                    .also {
//                        if (bufferParam == null) {
//                            it.addStatement("receive(%L)", roleMap[l.from])
//                        } else {
//                            it.addStatement("receive(%L, %L)", roleMap[l.from], bufferParam.name)
//                            if (l.msgLabel.mentioned) {
//                                it.addStatement(
//                                    "%L[%S] = %L.value.%M()",
//                                    bindingsVariableName,
//                                    l.msgLabel.label,
//                                    bufferParam.name,
//                                    toValFunction
//                                )
//                            }
//                        }
//                    }
//                    .addStatement("return %T(e)", ret.interfaceClassPair.className)
//                    .build()
//
//                val functions = genMsgPassing(
//                    methodName,
//                    codeBlock,
//                    bufferParam,
//                    ret.interfaceClassPair.interfaceClassName
//                )
//                interfaceBuilder.addFunction(functions.abstractSpec)
//                classBuilder.addFunction(functions.overrideSpec)
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//
//                if (genCallbacksAPI) {
//                    val callbackFunction = FunSpec.builder("onReceive${l.msgLabel.label.capitalized()}From${l.from}")
//                        .addModifiers(KModifier.ABSTRACT)
//                        .also {
//                            if (bufferParam != null)
//                                it.addParameter(ParameterSpec.builder("v", l.type.kotlin).build())
//                        }
//                        .build()
//                    callbacksInterface.addFunction(callbackFunction)
//                    callbackCode.add(
//                        CodeBlock.builder()
//                            .also {
//                                if (bufferParam == null) {
//                                    it.addStatement(
//                                        "receiveProtected(%L)",
//                                        roleMap[l.from],
//                                    )
//                                } else {
//                                    if (branch == null) {
//                                        it.addStatement(
//                                            "val %L = (receiveProtected(%L) as %T).payload",
//                                            msgVariable(l.msgLabel.label),
//                                            roleMap[l.from],
//                                            SKMessage::class.parameterizedBy(l.type.kotlin)
//                                        )
//                                    } else {
//                                        it.addStatement(
//                                            "val %L = %L.payload as %T",
//                                            msgVariable(l.msgLabel.label),
//                                            caseMsgVariable,
//                                            l.type.kotlin
//                                        )
//                                    }
//                                }
//                            }
//                            .also {
//                                if (l.msgLabel.mentioned) {
//                                    it.addStatement(
//                                        "%L[%S] = %L.%M()",
//                                        bindingsVariableName,
//                                        l.msgLabel.label,
//                                        msgVariable(l.msgLabel.label),
//                                        toValFunction
//                                    )
//                                }
//                            }
//                            .also {
//                                if (bufferParam == null) {
//                                    it.addStatement(
//                                        "%L.%N()",
//                                        callbacksParameterName,
//                                        callbackFunction
//                                    )
//                                } else {
//                                    it.addStatement(
//                                        "%L.%N(%L)",
//                                        callbacksParameterName,
//                                        callbackFunction,
//                                        msgVariable(l.msgLabel.label)
//                                    )
//                                }
//                            }
//                            .build()
//                    )
//                    callbackCode.add(nextCallbackCode.build())
//                }
//
//                GenRet(ICNames(interfaceName, className), ret.counter)
//            }
//            is CLTExternalChoice -> {
//                classBuilder.addSuperclassConstructorParameter(endpointVariable)
//                if (branch != null) {
//                    classBuilder.superclass(SKCaseEndpoint::class)
//                    addCaseMsgVariable(classBuilder)
//                } else {
//                    classBuilder.superclass(SKInputEndpoint::class)
//                }
//
//                val branchInterfaceName = ClassName(
//                    fluentPackage,
//                    buildClassName(protocolName, role, stateIndex + 1) + "Branch"
//                )
//                val branchInterfaceBuilder = TypeSpec
//                    .interfaceBuilder(branchInterfaceName)
//                    .addModifiers(KModifier.SEALED)
//
//                var newIndex = stateIndex + 1
//                val whenBlock = CodeBlock.builder()
//                callbackCode.addStatement(
//                    "val %L = receiveProtected(%L)",
//                    caseMsgVariable, roleMap[l.of]!!
//                )
//                callbackCode.beginControlFlow("when(%L.branch)", caseMsgVariable)
//
// //                for (b in l.branches) {
// //                    val nextCallbackCode = CodeBlock.builder()
// //                    callbackCode.add("%S -> ", k)
// //                    callbackCode.beginControlFlow("")
// //
// //                    val ret = genLocals(
// //                        v,
// //                        newIndex,
// //                        nextCallbackCode,
// //                        superInterface = branchInterfaceName,
// //                        branch = k
// //                    )
// //                    callbackCode.add(nextCallbackCode.build())
// //                    callbackCode.endControlFlow()
// //
// //                    whenBlock.addStatement("%S -> %T(e, msg)", k, ret.interfaceClassPair.className)
// //                    ret.interfaceClassPair.className
// //                    newIndex = ret.counter + 1
// //                }
//                val elseStatement = "else -> throw %T(\"This should not happen. branch: \${%L.branch}\")"
//                callbackCode.addStatement(elseStatement, RuntimeException::class, caseMsgVariable)
//                callbackCode.endControlFlow()
//
//                whenBlock.addStatement(elseStatement, RuntimeException::class, caseMsgVariable)
//
//                val methodName = "branch"
//                val abstractFunction = FunSpec.builder(methodName)
//                    .returns(branchInterfaceName)
//                    .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
//
//                val function = FunSpec.builder(methodName)
//                    .returns(branchInterfaceName)
//                    .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
//                    .also {
//                        it.addStatement(
//                            "val %L = receive(${roleMap[l.of]})",
//                            caseMsgVariable
//                        )
//                        it.beginControlFlow("return when(%L.branch)", caseMsgVariable)
//                        it.addCode(whenBlock.build())
//                        it.endControlFlow()
//                    }
//
//                interfaceBuilder.addFunction(abstractFunction.build())
//                classBuilder.addFunction(function.build())
//
//                fileSpecBuilder.addType(branchInterfaceBuilder.build())
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//                GenRet(ICNames(interfaceName, className), newIndex)
//            }
//            is CLTInternalChoice -> {
//                classBuilder.superclass(SKLinearEndpoint::class)
//                var counter = stateIndex + 1
//                val choiceEnumClassname = ClassName(callbacksPackage, "Choice$stateIndex")
//                val choiceEnum = TypeSpec.enumBuilder(choiceEnumClassname)
//                val choiceFunctionName = "onChoose$stateIndex"
//                callbacksInterface.addFunction(
//                    FunSpec.builder(choiceFunctionName)
//                        .addModifiers(KModifier.ABSTRACT)
//                        .returns(choiceEnumClassname)
//                        .build()
//                )
//                callbackCode.beginControlFlow("when(%L.%L())", callbacksParameterName, choiceFunctionName)
//
// //                for ((k, v) in l.branches) {
// //                    val nextCallbackCode = CodeBlock.builder()
// //                    val enumConstant = "Choice${stateIndex}_$k"
// //                    callbackCode.add("%T.%L -> ", choiceEnumClassname, enumConstant)
// //                    callbackCode.beginControlFlow("")
// //                    choiceEnum.addEnumConstant(enumConstant)
// //                    val ret = genLocals(v, counter, nextCallbackCode)
// //                    callbackCode.add(nextCallbackCode.build())
// //                    callbackCode.endControlFlow()
// //
// //                    counter = ret.counter + 1
// //
// //                    val methodName = "branch$k"
// //
// //                    val abstractFunction = FunSpec.builder(methodName)
// //                        .returns(ret.interfaceClassPair.interfaceClassName)
// //                        .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
// //
// //                    val function = FunSpec.builder(methodName)
// //                        .returns(ret.interfaceClassPair.interfaceClassName)
// //                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
// //                        .also {
// //                            it.addStatement("return %T(e)", ret.interfaceClassPair.className)
// //                        }
// //
// //                    interfaceBuilder.addFunction(abstractFunction.build())
// //                    classBuilder.addFunction(function.build())
// //                }
//                callbackCode.endControlFlow()
//
//                callbacksInterfaceFile.addType(choiceEnum.build())
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//                GenRet(ICNames(interfaceName, className), counter)
//            }
//        }
//    }
//
//    private fun addCaseMsgVariable(classBuilder: TypeSpec.Builder) {
//        // use msg from branch
//        classBuilder.addSuperclassConstructorParameter(caseMsgVariable)
//        classBuilder.addProperty(
//            PropertySpec.builder(caseMsgVariable, SKMessage::class)
//                .initializer(caseMsgVariable)
//                .addModifiers(KModifier.PRIVATE)
//                .build()
//        )
//        classBuilder.primaryConstructor(
//            (classBuilder.build().primaryConstructor?.toBuilder() ?: FunSpec.constructorBuilder())
//                .addParameter(
//                    ParameterSpec
//                        .builder(caseMsgVariable, SKMessage::class)
//                        .build()
//                )
//                .build()
//        )
//    }
//
//    fun writeTo(outputDirectory: File) {
//        val codeBlock = CodeBlock.builder()
//        codeBlock.addStatement(
//            "val %L = %T()",
//            bindingsVariableName,
//            bindingsValueType
//        )
//        genLocals(localType, initialStateCount, codeBlock)
//        fileSpecBuilder.build().writeTo(outputDirectory)
//
//        val callbacksClassBuilder = TypeSpec
//            .classBuilder(callbacksClassName)
//            .superclass(SKMPEndpoint::class)
//            .addFunction(
//                FunSpec.builder("start")
//                    .addModifiers(KModifier.SUSPEND)
//                    .addCode(codeBlock.build()).build()
//            )
//            .primaryConstructor(
//                FunSpec.constructorBuilder()
//                    .addParameter(
//                        ParameterSpec
//                            .builder(callbacksParameterName, callbacksInterfaceName)
//                            .build()
//                    )
//                    .build()
//            ).addProperty(
//                PropertySpec.builder(callbacksParameterName, callbacksInterfaceName)
//                    .initializer(callbacksParameterName)
//                    .addModifiers(KModifier.PRIVATE)
//                    .build()
//            )
//
//        if (genCallbacksAPI) {
//            val suppressUnchecked = AnnotationSpec.builder(suppressClassName)
//                .addMember("%S", "unchecked_cast")
//                .addMember("%S", "unused_variable")
//                .build()
//
//            FileSpec
//                .builder(
//                    packageName = callbacksPackage,
//                    fileName = callbacksClassName.simpleName
//                ).addFileComment(GENERATED_COMMENT)
//                .addAnnotation(suppressUnchecked)
//                .addType(callbacksClassBuilder.build())
//                .build().writeTo(outputDirectory)
//
//            callbacksInterfaceFile
//                .addType(callbacksInterface.build())
//                .build()
//                .writeTo(outputDirectory)
//        }
//    }
// }
//
// private fun genMsgPassing(
//    methodName: String,
//    codeBlock: CodeBlock,
//    parameter: ParameterSpec?,
//    nextInterface: ClassName,
// ): FunSpecs {
//    val abstractFunction = FunSpec.builder(methodName)
//        .returns(nextInterface)
//        .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
//    val function = FunSpec.builder(methodName)
//        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
//        .returns(nextInterface)
//        .addCode(codeBlock)
//    if (parameter != null) {
//        abstractFunction.addParameter(parameter)
//        function.addParameter(parameter)
//    }
//    return FunSpecs(abstractFunction.build(), function.build())
// }
