package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to use variable with type not supported as a refinement value.
 */
public class InvalidRefinementValueException(type: Class<*>) :
    SessionKotlinDSLException("Cannot use a variable of type $type as a refinement value.")
