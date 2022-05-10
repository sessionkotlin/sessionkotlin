package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.parser.RefinementParser
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.SolverContext

internal class SatState(
    private val ctx: SolverContext,
) {
    private var formulaManager: FormulaManager = ctx.formulaManager
    private var boolMgr = formulaManager.booleanFormulaManager
    private var floatMgr = formulaManager.floatingPointFormulaManager
    private var intMgr = formulaManager.integerFormulaManager

    private val variables: MutableMap<String, Formula> = mutableMapOf()
    private val constraints: MutableList<BooleanFormula> = mutableListOf()

    fun addVariable(name: String) {
        variables[name] = formulaManager.integerFormulaManager.makeVariable(name)
    }

    fun addCondition(condition: String) {
        val ast = RefinementParser.parseToEnd(condition)
//        constraints.add(formulaManager.parse(ast.toSMT(variables)))
    }

    fun satisfiable(): Boolean {
        ctx.newProverEnvironment().use { prover ->
            for (constraint in constraints) {
                prover.addConstraint(constraint)
            }
            return !prover.isUnsat
        }
    }
}
