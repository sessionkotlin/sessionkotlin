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

    private fun genLocals(
        fileSpecBuilder: FileSpec.Builder,
        l: LocalType,
        r: Role,
        stateCount: Int,
        recMap: Map<RecursionTag, InterfaceClassPair>,
        tag: RecursionTag? = null,
    ): InterfaceClassPair {

        val interfaceName = ClassName("", "$name$r${stateCount}Interface")
        val className = ClassName("", "$name$r$stateCount")

        val interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
        val classBuilder = TypeSpec
            .classBuilder(className)
            .addSuperinterface(interfaceName)
            .addModifiers(KModifier.PRIVATE)

        return when (l) {
            LocalTypeEnd -> InterfaceClassPair(endClassName, endClassName)
            is LocalTypeSend -> {
                val m = if (tag != null) recMap.plus(Pair(tag, InterfaceClassPair(interfaceName, className))) else recMap
                val nextInterfaceClassPair = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, m)
                val methodName = "sendTo${l.to}"

                val abstractFunction = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(nextInterfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(nextInterfaceClassPair.interfaceClassName)
                    .let {
                        it.addStatement("println(\"Send to ${l.to}\")")
                        it.addStatement("return (${nextInterfaceClassPair.className.constructorReference()})()")
                        it
                    }
                interfaceBuilder.addFunction(abstractFunction.build())
                classBuilder.addFunction(function.build())

                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())

                InterfaceClassPair(interfaceName, className)
            }
            is LocalTypeReceive -> {
                val m = if (tag != null) recMap.plus(Pair(tag, InterfaceClassPair(interfaceName, className))) else recMap
                val nextInterfaceClassPair = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, m)
                val methodName = "receiveFrom${l.from}"

                val abstractFunction = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(nextInterfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.ABSTRACT)

                val function = FunSpec.builder(methodName)
                    .addParameter("arg", l.type.kotlin)
                    .returns(nextInterfaceClassPair.interfaceClassName)
                    .addModifiers(KModifier.OVERRIDE)
                    .let {
                        it.addStatement("println(\"Receive from ${l.from}\")")
                        it.addStatement("return (${nextInterfaceClassPair.className.constructorReference()})()")
                        it
                    }

                interfaceBuilder.addFunction(abstractFunction.build())
                classBuilder.addFunction(function.build())

                fileSpecBuilder.addType(interfaceBuilder.build())
                fileSpecBuilder.addType(classBuilder.build())

                fileSpecBuilder.build().writeTo(directory)
                InterfaceClassPair(interfaceName, className)
            }
            is LocalTypeRecursion -> recMap.getValue(l.tag)
            is LocalTypeRecursionDefinition -> genLocals(fileSpecBuilder, l.cont, r, stateCount, recMap, l.tag)
            is LocalTypeExternalChoice -> {
                TODO()
            }
            is LocalTypeInternalChoice -> {
                TODO()
            }
        }
    }
}
