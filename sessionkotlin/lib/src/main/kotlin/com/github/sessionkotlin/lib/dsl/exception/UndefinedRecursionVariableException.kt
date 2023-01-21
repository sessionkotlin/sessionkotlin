package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.RecursionTag

/**
 * Thrown when attempting to use recursion tag that is not defined.
 */
internal class UndefinedRecursionVariableException(i: RecursionTag) :
    SessionKotlinDSLException("Recursion variable $i undefined.")
