package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.Action
import com.github.d_costa.sessionkotlin.fsm.ReceiveAction
import com.github.d_costa.sessionkotlin.fsm.SendAction
import com.github.d_costa.sessionkotlin.parser.symbols.values.RefinedValue
import com.github.d_costa.sessionkotlin.util.asClassname
import com.github.d_costa.sessionkotlin.util.asPackageName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

internal open class NewAPIGenerator(val globalEnv: RootEnv, subPackage: String) {
    companion object {
        const val GENERATED_COMMENT = "This file was generated by sessionkotlin. Do not change it."
        const val msgVariableName = "msg"
        val bindingsMapPropertyType = LinkedHashMap::class.parameterizedBy(String::class, RefinedValue::class)
        val suppressClassName = ClassName("kotlin", "Suppress")
    }

    private val basePackageName: String = globalEnv.protocolName.asPackageName()

    private val protocolClassName = globalEnv.protocolName.asClassname()

    protected val roleMap = mutableMapOf<SKRole, ClassName>()

    protected val files = mutableListOf<FileSpec>()

    protected val packageName = "$basePackageName.$subPackage"

    protected val endpointParameter = ParameterSpec
        .builder("e", SKMPEndpoint::class)
        .build()

    protected val bindingsMapProperty = PropertySpec.builder(
        "bindings",
        bindingsMapPropertyType
    ).addModifiers(KModifier.PRIVATE)
        .initializer("%T()", bindingsMapPropertyType).build()

    protected val toValFunction = MemberName(
        "com.github.d_costa.sessionkotlin.parser.symbols.values",
        "toVal"
    )

    protected val assertFunction = MemberName(
        "com.github.d_costa.sessionkotlin.util",
        "assertRefinement"
    )

    init {
        genRoles()
    }

    private fun genRoles() {
        val fileSpecBuilder = FileSpec.builder(
            packageName = basePackageName,
            fileName = "${protocolClassName}Roles"
        )
        globalEnv.roles.forEach {
            val className = ClassName(basePackageName, it.toString())
            roleMap[it] = className
            fileSpecBuilder
                .addType(
                    TypeSpec.objectBuilder(className)
                        .superclass(SKGenRole::class)
                        .build()
                )
        }
        files.add(fileSpecBuilder.build())
    }

    internal fun writeTo(directory: File) {
        for (f in files) {
            f.writeTo(directory)
        }
    }

    protected fun buildClassname(r: SKRole, count: Int? = null, postFix: String = ""): String =
        StringBuilder("${protocolClassName}$r")
            .append(if (count != null) "$count" else "")
            .append(postFix)
            .toString()

    protected fun newFile(filename: String): FileSpec.Builder =
        FileSpec
            .builder(
                packageName = packageName,
                fileName = filename
            ).addFileComment(GENERATED_COMMENT)

    protected fun generateFunctionName(action: Action): String =
        when (action) {
            is ReceiveAction -> "receive${action.label.frontendName()}From${action.from}"
            is SendAction -> "send${action.label.frontendName()}To${action.to}"
        }

    private fun generateMessageLabel(msgLabel: String) = "$msgVariableName$msgLabel"
    protected fun generateMessageLabel(action: Action) = generateMessageLabel(action.label.name)
}

internal enum class StateClassification {
    Output, Input, ExternalChoice
}