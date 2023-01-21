package com.github.sessionkotlin.lib.dsl.types

import com.github.sessionkotlin.lib.dsl.exception.InvalidRefinementValueException
import com.github.sessionkotlin.parser.symbols.*
import com.github.sessionkotlin.parser.symbols.values.StringValue
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.SolverContext

/**
 * State used when testing global protocol satisfiability.
 */
internal class SatState(
    private val ctx: SolverContext,
) {
    private enum class SmtSort(val value: String) {
        STRING("String"), INT("Int"), REAL("Real");

        override fun toString() = value
    }

    private data class SMTVariable(val id: String, val sort: SmtSort)
    private data class UnsupportedVariable(val id: String, val type: Class<*>)

    private var formulaManager: FormulaManager = ctx.formulaManager
    private var variables: MutableSet<SMTVariable> = mutableSetOf()
    private var constraints: MutableList<String> = mutableListOf()
    private var unsupportedVariable: MutableSet<UnsupportedVariable> = mutableSetOf()

    fun addVariable(id: String, type: Class<*>) {
        val sort: SmtSort = when (type) {
            Long::class.javaObjectType -> SmtSort.INT
            Int::class.javaObjectType -> SmtSort.INT
            Short::class.javaObjectType -> SmtSort.INT
            Byte::class.javaObjectType -> SmtSort.INT
            Double::class.javaObjectType -> SmtSort.REAL
            String::class.javaObjectType -> SmtSort.STRING
            else -> throw InvalidRefinementValueException(type)
        }

        variables.add(SMTVariable(id, sort))
    }

    fun addUnsupportedVariableType(id: String, type: Class<*>) {
        unsupportedVariable.add(UnsupportedVariable(id, type))
    }

    fun addCondition(condition: BooleanExpression) {
        constraints.add(condition.toSMT())
    }

    private fun BooleanExpression.toSMT(): String =
        when (this) {
            True -> "true"
            False -> "false"

            is Impl -> "(=> ${c1.toSMT()} ${c2.toSMT()})"
            is Not -> "(not ${c.toSMT()})"
            is And -> "(and ${c1.toSMT()} ${c2.toSMT()})"
            is Or -> "(or ${c1.toSMT()} ${c2.toSMT()})"

            is Eq -> "(= ${e1.toSMT()} ${e2.toSMT()})"
            is Neq -> "(not (= ${e1.toSMT()} ${e2.toSMT()}))"
            is Greater -> "(> ${e1.toSMT()} ${e2.toSMT()})"
            is GreaterEq -> "(>= ${e1.toSMT()} ${e2.toSMT()})"
            is Lower -> "(< ${e1.toSMT()} ${e2.toSMT()})"
            is LowerEq -> "(<= ${e1.toSMT()} ${e2.toSMT()})"
        }

    private fun Term.toSMT(): String =
        when (this) {
            is Const -> when (v) {
                is StringValue -> "\"${v.value}\""
                else -> "${v.value}"
            }
            is Minus -> "(- ${t1.toSMT()} ${t2.toSMT()})"
            is Name -> {
                val unsupported = unsupportedVariable.find { it.id == id }
                if (unsupported != null) {
                    // id was declared but is not supported
                    throw InvalidRefinementValueException(unsupported.type)
                }
                id
            }
            is Neg -> "(- ${t.toSMT()})"
            is Plus -> "(+ ${t1.toSMT()} ${t2.toSMT()})"
        }

    fun satisfiable(): List<BooleanFormula> {
        ctx.newProverEnvironment(SolverContext.ProverOptions.GENERATE_UNSAT_CORE).use { prover ->
            if (constraints.isNotEmpty()) {
                val declaration = StringBuilder()

                for (v in variables) {
                    declaration.append("(declare-fun ${v.id} () ${v.sort})")
                }

                declaration.append("(assert (and ${constraints.joinToString(" ")}))")

                prover.addConstraint(formulaManager.parse(declaration.toString()))

                if (prover.isUnsat) {
                    return prover.unsatCore
                }
            }
            return emptyList()
        }
    }

    fun clone(): SatState {
        val s = SatState(ctx)
        s.variables = variables.toMutableSet()
        s.constraints = constraints.toMutableList()
        return s
    }
}
