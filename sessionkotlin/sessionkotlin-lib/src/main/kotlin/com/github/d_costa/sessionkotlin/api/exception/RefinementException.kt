package com.github.d_costa.sessionkotlin.api.exception

public open class RefinementException(refinement: String) : SessionKotlinAPIException("Refinement not respected: [$refinement]")
