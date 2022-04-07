package org.david.sessionkotlin_lib.dsl

import com.squareup.kotlinpoet.*
import org.david.sessionkotlin_lib.dsl.types.*
import java.io.File

public class RootEnv(private val name: String) : GlobalEnv(emptySet(), emptySet()) {
    private val endClassName = ClassName("", "End")
    private val directory = File("build/generated/sessionkotlin/main/kotlin")

    internal fun genLocals() {
        val g = buildGlobalType(instructions)
        genEndClass()
        roles.forEach {
            genLocals(g.project(it), it)
        }
    }

    private fun genEndClass() {
        val fileSpecBuilder = FileSpec.builder(
            packageName = "",
            fileName = endClassName.simpleName
        )
        fileSpecBuilder.addComment("This is a generated file. Do not change it.")
        val classBuilder = TypeSpec.classBuilder(endClassName)
        val directory = File("build/generated/sessionkotlin/main/kotlin")
        fileSpecBuilder.addType(classBuilder.build())
        fileSpecBuilder.build().writeTo(directory)
    }

    private fun genLocals(l: LocalType, r: Role) {
        val className = ClassName("", "$name$r")

        val fileSpecBuilder = FileSpec.builder(
            packageName = "",
            fileName = className.simpleName
        ).addComment("This is a generated file. Do not change it.")

        genLocals(fileSpecBuilder, l, r, 1, emptyMap(), null)

        fileSpecBuilder.build().writeTo(directory)
    }

    private data class InterfaceClassPair(val interfaceClassName: ClassName, val className: ClassName)
    private data class GenRet(val interfaceClassPair: InterfaceClassPair, val counter: Int)

    private fun genLocals(
        fileSpecBuilder: FileSpec.Builder,
        l: LocalType,
        r: Role,
        stateCount: Int,
        recMap: Map<RecursionTag, InterfaceClassPair>,
        tag: RecursionTag? = null,
        implementInterface: ClassName? = null,
        branch: String? = null,
    ): GenRet {

        val interfaceName = ClassName("", "$name$r${stateCount}${branch ?: ""}Interface")
        val className = ClassName("", "$name$r$stateCount")
        val newMapRec =
            if (tag != null) recMap.plus(Pair(tag, InterfaceClassPair(interfaceName, className))) else recMap

        val interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
        if (implementInterface != null) {
            interfaceBuilder.addSuperinterface(implementInterface)
        }

        val classBuilder = TypeSpec
            .classBuilder(className)
            .addSuperinterface(interfaceName)
            .addModifiers(KModifier.PRIVATE)

        return when (l) {
            LocalTypeEnd -> GenRet(InterfaceClassPair(endClassName, endClassName), stateCount)
            is LocalTypeSend -> {
                val ret = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, newMapRec)
                val methodName = "sendTo${l.to}"

                val abstractFunction = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(ret.interfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(ret.interfaceClassPair.interfaceClassName)
                    .let {
                        it.addStatement("println(\"Send to ${l.to}\")")
                        it.addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
                        it
                    }
                interfaceBuilder.addFunction(abstractFunction.build())
                classBuilder.addFunction(function.build())

                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())

                GenRet(InterfaceClassPair(interfaceName, className), ret.counter)
            }
            is LocalTypeReceive -> {
                val ret = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, newMapRec)
                val methodName = "receiveFrom${l.from}"

                val abstractFunction = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(ret.interfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(ret.interfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.OVERRIDE)
                    .let {
                        it.addStatement("println(\"Receive from ${l.from}\")")
                        it.addStatement("return (${ret.interfaceClassPair.className.constructorReference()})()")
                        it
                    }

                interfaceBuilder.addFunction(abstractFunction.build())
                classBuilder.addFunction(function.build())

                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())

                fileSpecBuilder.build().writeTo(directory)
                GenRet(InterfaceClassPair(interfaceName, className), ret.counter)
            }
            is LocalTypeRecursion -> GenRet(recMap.getValue(l.tag), stateCount)
            is LocalTypeRecursionDefinition -> genLocals(fileSpecBuilder, l.cont, r, stateCount, recMap, l.tag)
            is LocalTypeExternalChoice -> {
                val branchInterface = ClassName("", "$name$r${stateCount + 1}Branch")
                val branchInterfaceBuilder = TypeSpec
                    .interfaceBuilder(branchInterface)
                    .addModifiers(KModifier.SEALED)

                var count = stateCount + 1
                for ((k, v) in l.cases) {
                    val ret = genLocals(fileSpecBuilder, v, r, count, newMapRec, implementInterface = branchInterface, branch = k)
                    count = ret.counter + 1
                }

                val methodName = "branch"

                val abstractFunction = FunSpec.builder(methodName)
                    .returns(branchInterface)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .returns(branchInterface)
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

                fileSpecBuilder.build().writeTo(directory)
                GenRet(InterfaceClassPair(interfaceName, className), count)
            }
            is LocalTypeInternalChoice -> {
                var counter = stateCount + 1
                for ((k, v) in l.cases) {
                    val ret = genLocals(fileSpecBuilder, v, r, counter, newMapRec, implementInterface = interfaceName)
                    counter = ret.counter + 1
                }
                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.build().writeTo(directory)
                GenRet(InterfaceClassPair(interfaceName, className), counter)
            }
        }
    }
}
