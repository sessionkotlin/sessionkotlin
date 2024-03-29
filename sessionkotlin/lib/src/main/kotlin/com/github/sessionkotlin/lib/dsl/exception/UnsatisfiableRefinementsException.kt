package com.github.sessionkotlin.lib.dsl.exception

import org.sosy_lab.java_smt.api.BooleanFormula

/**
 * Thrown when a refinement is proved not be satisfiable.
 */
internal class UnsatisfiableRefinementsException(unsatExpressions: List<BooleanFormula>) :
    SessionKotlinDSLException(unsatExpressions.joinToString())
