package org.david.sessionkotlin_lib.dsl
//
// import com.squareup.kotlinpoet.*
// import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
// import org.david.sessionkotlin_lib.api.SKBuffer
// import org.david.sessionkotlin_lib.dsl.types.*
// import org.david.sessionkotlin_lib.util.remWhitespace
// import java.io.File
//
// public class RootEnv2(private val name: String) : GlobalEnv(emptySet(), emptySet()) {
//    private val endClassName = ClassName("", "End")
//    private val directory = File("build/generated/sessionkotlin/main/kotlin")
//
//    internal fun genLocals() {
//        val g = buildGlobalType(instructions)
//        genEndClass()
//        roles.forEach {
//            genLocals(g.project(it), it)
//        }
//    }
//
//    private fun genEndClass() {
//        val fileSpecBuilder = FileSpec.builder(
//            packageName = "",
//            fileName = endClassName.simpleName
//        )
//        fileSpecBuilder.addComment("This is a generated file. Do not change it.")
//        val classBuilder = TypeSpec.classBuilder(endClassName)
//        val directory = File("build/generated/sessionkotlin/main/kotlin")
//        fileSpecBuilder.addType(classBuilder.build())
//        fileSpecBuilder.build().writeTo(directory)
//    }
//
//    private fun buildClassName(r: Role, count: Int? = null, branch: String? = null) =
//        StringBuilder("${name}_$r")
//            .append(if (count != null) "_$count" else "")
//            .append(if (branch != null) "_$branch" else "")
//            .toString()
//
//    private fun genLocals(l: LocalType, r: SKRole) {
//        val className = ClassName("", buildClassName(r))
//
//        val fileSpecBuilder = FileSpec.builder(
//            packageName = "",
//            fileName = className.simpleName
//        ).addComment("This is a generated file. Do not change it.")
//
//        genLocals(fileSpecBuilder, l, r)
//
//        fileSpecBuilder.build().writeTo(directory)
//    }
//
//    private data class InterfaceClassPair(val interfaceClassName: ClassName, val className: ClassName)
//    private data class GenRet(val interfaceClassPair: InterfaceClassPair, val counter: Int)
//
//    private val initialStateCount = 1
//
//
//    private fun genLocals(
//        fileSpecBuilder: FileSpec.Builder,
//        l: LocalType,
//        r: SKRole,
//        stateCount: Int = 1,
//        recMap: Map<RecursionTag, InterfaceClassPair> = emptyMap(),
//        tag: RecursionTag? = null,
//        implementInterface: ClassName? = null,
//        branch: String? = null,
//    ): GenRet {
//
//        val interfaceName = ClassName("", buildClassName(r, stateCount, branch) + "Interface")
//        val className = ClassName("", buildClassName(r, stateCount, branch))
//        val newMapRec =
//            if (tag != null) recMap.plus(Pair(tag, InterfaceClassPair(interfaceName, className))) else recMap
//
//        val interfaceBuilder = TypeSpec
//            .interfaceBuilder(interfaceName)
//            .addModifiers(KModifier.SEALED)
//
//        if (implementInterface != null) {
//            interfaceBuilder.addSuperinterface(implementInterface)
//        }
//
//        val classBuilder = TypeSpec
//            .classBuilder(className)
//            .addSuperinterface(interfaceName)
//
//        if (stateCount > initialStateCount)
//            classBuilder.addModifiers(KModifier.PRIVATE)
//
//        fun genMsgPassing(
//            ret: GenRet,
//            methodName: String,
//            codeBlock: CodeBlock,
//            parameter: ParameterSpec?,
//        ) {
//            val abstractFunction = FunSpec.builder(methodName)
//                .returns(ret.interfaceClassPair.interfaceClassName)
//                .addModifiers(KModifier.ABSTRACT)
//            val function = FunSpec.builder(methodName)
//                .addModifiers(KModifier.OVERRIDE)
//                .returns(ret.interfaceClassPair.interfaceClassName)
//                .addCode(codeBlock)
//            if (parameter != null) {
//                abstractFunction.addParameter(parameter)
//                function.addParameter(parameter)
//            }
//
//            interfaceBuilder.addFunction(abstractFunction.build())
//            classBuilder.addFunction(function.build())
//
//            fileSpecBuilder.addType(interfaceBuilder.build())
//            fileSpecBuilder.addType(classBuilder.build())
//
//            fileSpecBuilder.build().writeTo(directory)
//        }
//
//        return when (l) {
//            LocalTypeEnd -> GenRet(InterfaceClassPair(endClassName, endClassName), stateCount)
//            is LocalTypeSend -> {
//                val ret = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, newMapRec)
//                val methodName = "sendTo${l.to}"
//                val codeBlock = CodeBlock.builder()
//                    .addStatement("println(\"Send to ${l.to}\")")
//                    .addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
//                    .build()
//                val parameter = if (l.type != Unit::class.java)
//                    ParameterSpec
//                        .builder("arg", l.type.kotlin)
//                        .build()
//                else null
//
//                genMsgPassing(ret, methodName, codeBlock, parameter)
//                GenRet(InterfaceClassPair(interfaceName, className), ret.counter)
//            }
//            is LocalTypeReceive -> {
//                val ret = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, newMapRec)
//                val methodName = "receiveFrom${l.from}"
//
//                val abstractFunction = FunSpec.builder(methodName)
//                    .returns(ret.interfaceClassPair.interfaceClassName)
//                    .addModifiers(KModifier.ABSTRACT)
//
//                val function = FunSpec.builder(methodName)
//                    .returns(ret.interfaceClassPair.interfaceClassName)
//                    .addModifiers(KModifier.OVERRIDE)
//                    .let {
//                        it.addStatement("println(\"Receive from ${l.from}\")")
//                        it.addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
//                        it
//                    }
//
//                if (l.type != Unit::class.java) {
//                    abstractFunction.addParameter("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
//                    function.addParameter("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
//                }
//
//                interfaceBuilder.addFunction(abstractFunction.build())
//                classBuilder.addFunction(function.build())
//
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//
//                fileSpecBuilder.build().writeTo(directory)
//                GenRet(InterfaceClassPair(interfaceName, className), ret.counter)
//            }
//            is LocalTypeRecursion -> GenRet(recMap.getValue(l.tag), stateCount)
//            is LocalTypeRecursionDefinition -> genLocals(fileSpecBuilder, l.cont, r, stateCount, recMap, l.tag)
//            is LocalTypeExternalChoice -> {
//                buildClassName(r, stateCount + 1, branch) + "_Branch"
//                val branchInterface = ClassName("", buildClassName(r, stateCount + 1, branch) + "_Branch")
//                val branchInterfaceBuilder = TypeSpec
//                    .interfaceBuilder(branchInterface)
//                    .addModifiers(KModifier.SEALED)
//
//                var count = stateCount + 1
//                for ((k, v) in l.cases) {
//                    val ret = genLocals(
//                        fileSpecBuilder,
//                        v,
//                        r,
//                        count,
//                        newMapRec,
//                        implementInterface = branchInterface,
//                        branch = k.remWhitespace()
//                    )
//                    count = ret.counter + 1
//                }
//
//                val methodName = "branch"
//
//                val abstractFunction = FunSpec.builder(methodName)
//                    .returns(branchInterface)
//                    .addModifiers(KModifier.ABSTRACT)
//
//                val function = FunSpec.builder(methodName)
//                    .returns(branchInterface)
//                    .addModifiers(KModifier.OVERRIDE)
//                    .let {
//                        it.addStatement("println(\"External choice\")")
//                        it.addStatement("TODO()")
//                        it
//                    }
//
//                interfaceBuilder.addFunction(abstractFunction.build())
//                classBuilder.addFunction(function.build())
//
//                fileSpecBuilder.addType(branchInterfaceBuilder.build())
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//
//                fileSpecBuilder.build().writeTo(directory)
//                GenRet(InterfaceClassPair(interfaceName, className), count)
//            }
//            is LocalTypeInternalChoice -> {
//                var counter = stateCount + 1
//                for ((k, v) in l.cases) {
//                    val ret = genLocals(fileSpecBuilder, v, r, counter, newMapRec)
//                    counter = ret.counter + 1
//
//                    val methodName = "branch$k"
//
//                    val abstractFunction = FunSpec.builder(methodName)
//                        .returns(ret.interfaceClassPair.interfaceClassName)
//                        .addModifiers(KModifier.ABSTRACT)
//
//                    val function = FunSpec.builder(methodName)
//                        .returns(ret.interfaceClassPair.interfaceClassName)
//                        .addModifiers(KModifier.OVERRIDE)
//                        .let {
//                            it.addStatement("println(\"Internal choice\")")
//                            it.addStatement("TODO()")
//                            it
//                        }
//
//                    interfaceBuilder.addFunction(abstractFunction.build())
//                    classBuilder.addFunction(function.build())
//                }
//                fileSpecBuilder.addType(interfaceBuilder.build())
//                fileSpecBuilder.addType(classBuilder.build())
//                fileSpecBuilder.build().writeTo(directory)
//                GenRet(InterfaceClassPair(interfaceName, className), counter)
//            }
//        }
//    }
// }
