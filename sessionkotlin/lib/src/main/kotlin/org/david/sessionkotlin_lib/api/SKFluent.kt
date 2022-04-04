package org.david.sessionkotlin_lib.api

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import org.david.sessionkotlin_lib.api.exception.LocalClassAPIException
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.RootEnv
import java.io.File

public abstract class SKFluent(g: RootEnv, r: Role) {
    init {
        println("hello")

        val dslPackage = "org.david.sessionkotlin_lib.api"
        val filename = "SKGenerated"
        val fileSpecBuilder = FileSpec.builder(
            packageName = "",
            fileName = filename
        )
        fileSpecBuilder.addComment("This is a generated file. Do not change it.")

        if (this::class.qualifiedName == null) {
            throw LocalClassAPIException()
        }

        fileSpecBuilder.addFunction(
            FunSpec.builder("hello")
                .receiver(this::class)
                .let {
                    it.addStatement("println(\"Hello World\")")
                    it
                }
                .build()
        )

        val directory = File("build/generated/sessionkotlin/main/kotlin")
        fileSpecBuilder.build().writeTo(directory)
    }
}
