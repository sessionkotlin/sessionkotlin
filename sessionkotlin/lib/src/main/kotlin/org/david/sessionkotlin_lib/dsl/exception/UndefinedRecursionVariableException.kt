package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.RecursionTag

public class UndefinedRecursionVariableException(i: RecursionTag) :
    SessionKotlinDSLException("Recursion variable $i undefined.")
