package org.david.sessionkotlin_lib.dsl

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import org.david.sessionkotlin_lib.api.SKBuffer
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

    private fun genLocals(
        fileSpecBuilder: FileSpec.Builder,
        l: LocalType,
        r: Role,
        stateCount: Int,
        recMap: Map<RecursionTag, ClassName>,
        tag: RecursionTag? = null,
    ): ClassName {
        val className = ClassName("", "$name$r$stateCount")

        val classBuilder = TypeSpec.classBuilder(className)

        return when (l) {
            LocalTypeEnd -> endClassName
            is LocalTypeSend -> {
                println(tag)
                val m = if (tag != null) recMap.plus(Pair(tag, className)) else recMap
                val nextClassName = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, m)
                val f = FunSpec.builder("sendTo${l.to}")
                    .addParameter("arg", l.type.kotlin)
                    .returns(nextClassName)
                    .let {
                        it.addStatement("println(\"Send to ${l.to}\")")
                        it.addStatement("return (${nextClassName.constructorReference()})()")
                        it
                    }
                classBuilder.addFunction(f.build())
                fileSpecBuilder.addType(classBuilder.build())

                className
            }
            is LocalTypeReceive -> {
                println(tag)

                val m = if (tag != null) recMap.plus(Pair(tag, className)) else recMap
                val nextClassName = genLocals(fileSpecBuilder, l.cont, r, stateCount + 1, m)

                val f = FunSpec.builder("receiveFrom${l.from}")
                    .addParameter("buf", SKBuffer::class.parameterizedBy(l.type.kotlin))
                    .returns(nextClassName)
                    .let {
                        it.addStatement("println(\"Receive from ${l.from}\")")
                        it.addStatement("return (${nextClassName.constructorReference()})()")
                        it
                    }
                classBuilder.addFunction(f.build())
                val directory = File("build/generated/sessionkotlin/main/kotlin")
                fileSpecBuilder.addType(classBuilder.build())
                fileSpecBuilder.build().writeTo(directory)
                className
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
