package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.RecursionTag

public class UndefinedRecursionVariableException(i: RecursionTag) :
    SessionKotlinException("Recursion variable $i undefined.")
