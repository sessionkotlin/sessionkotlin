package com.github.sessionkotlin.lib.api

import com.github.sessionkotlin.lib.backend.endpoint.SKMPEndpoint
import com.github.sessionkotlin.lib.dsl.RootEnv
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.fsm.Action
import com.github.sessionkotlin.lib.fsm.ReceiveAction
import com.github.sessionkotlin.lib.fsm.SendAction
import com.github.sessionkotlin.lib.util.asClassname
import com.github.sessionkotlin.lib.util.asPackageName
import com.github.sessionkotlin.parser.RefinementCondition
import com.github.sessionkotlin.parser.symbols.*
import com.github.sessionkotlin.parser.symbols.values.IntegerValue
import com.github.sessionkotlin.parser.symbols.values.RealValue
import com.github.sessionkotlin.parser.symbols.values.RefinedValue
import com.github.sessionkotlin.parser.symbols.values.StringValue
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

internal open class AbstractAPIGenerator(private val globalEnv: RootEnv, subPackage: String) {
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
        "com.github.sessionkotlin.parser.symbols.values",
        "toVal"
    )

    private val assertFunction = MemberName(
        "com.github.sessionkotlin.lib.util",
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

    private fun generateMessageLabel(msgLabel: String) = "${msgVariableName}$msgLabel"
    protected fun generateMessageLabel(action: Action) = generateMessageLabel(action.label.name)

    protected fun addRefinementAssertion(
        codeBlockBuilder: CodeBlock.Builder,
        condition: RefinementCondition,
    ) {
        codeBlockBuilder.addStatement(
            "%M(%S, %L.value(%L))",
            assertFunction,
            condition.plain,
            condition.expression.toCodeBlock(),
            bindingsMapProperty.name
        )
    }

    private fun Term.toCodeBlock(): CodeBlock =
        when (this) {
            is Name -> CodeBlock.of("%T(%S)", Name::class, id)
            is Const -> CodeBlock.of(
                "%T(%T(%L))",
                Const::class,
                when (v) {
                    is IntegerValue -> IntegerValue::class
                    is RealValue -> RealValue::class
                    is StringValue -> StringValue::class
                },
                v.value
            )
            is Minus -> CodeBlock.of("%T(%L, %L)", Minus::class, t1.toCodeBlock(), t2.toCodeBlock())
            is Plus -> CodeBlock.of("%T(%L, %L)", Plus::class, t1.toCodeBlock(), t2.toCodeBlock())
            is Neg -> CodeBlock.of("%T(%L)", Neg::class, t)
        }

    private fun BooleanExpression.toCodeBlock(): CodeBlock =
        when (this) {
            True -> CodeBlock.of("%L", True)
            False -> CodeBlock.of("%L", False)
            is And -> CodeBlock.of("%T(%L, %L)", And::class, c1.toCodeBlock(), c2.toCodeBlock())
            is Or -> CodeBlock.of("%T(%L, %L)", Or::class, c1.toCodeBlock(), c2.toCodeBlock())
            is Impl -> CodeBlock.of("%T(%L, %L)", Impl::class, c1.toCodeBlock(), c2.toCodeBlock())
            is Not -> CodeBlock.of("%T(%L)", Not::class, c)
            is Eq -> CodeBlock.of("%T(%L, %L)", Eq::class, e1.toCodeBlock(), e2.toCodeBlock())
            is Greater -> CodeBlock.of("%T(%L, %L)", Greater::class, e1.toCodeBlock(), e2.toCodeBlock())
            is GreaterEq -> CodeBlock.of("%T(%L, %L)", GreaterEq::class, e1.toCodeBlock(), e2.toCodeBlock())
            is Lower -> CodeBlock.of("%T(%L, %L)", Lower::class, e1.toCodeBlock(), e2.toCodeBlock())
            is LowerEq -> CodeBlock.of("%T(%L, %L)", LowerEq::class, e1.toCodeBlock(), e2.toCodeBlock())
            is Neq -> CodeBlock.of("%T(%L, %L)", Neq::class, e1.toCodeBlock(), e2.toCodeBlock())
        }
}