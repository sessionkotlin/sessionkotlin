package com.github.d_costa.sessionkotlin.dsl.exception

public class InvalidRefinementValueException(type: Class<*>) :
    SessionKotlinDSLException("Cannot use a variable of type $type as a refinement value.")
