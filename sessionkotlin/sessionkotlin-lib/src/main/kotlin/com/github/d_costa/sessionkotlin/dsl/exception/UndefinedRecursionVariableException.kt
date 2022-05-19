package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.RecursionTag

/**
 * Thrown when attempting to use recursion tag that is not defined.
 */
public class UndefinedRecursionVariableException(i: RecursionTag) :
    SessionKotlinDSLException("Recursion variable $i undefined.")
