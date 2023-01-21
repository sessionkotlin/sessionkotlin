package com.github.sessionkotlin.lib.api.exception

/**
 * Thrown when a refinement expression is not respected.
 */
public class RefinementException(refinement: String) : SessionKotlinAPIException("Refinement not respected: [$refinement]")
