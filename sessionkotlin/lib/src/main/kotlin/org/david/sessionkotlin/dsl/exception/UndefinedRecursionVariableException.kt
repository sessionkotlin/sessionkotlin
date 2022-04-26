package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.RecursionTag

public class UndefinedRecursionVariableException(i: RecursionTag) :
    SessionKotlinDSLException("Recursion variable $i undefined.")
