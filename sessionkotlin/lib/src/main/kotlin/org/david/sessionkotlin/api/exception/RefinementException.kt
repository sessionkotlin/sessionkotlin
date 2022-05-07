package org.david.sessionkotlin.api.exception

public open class RefinementException(refinement: String) : SessionKotlinAPIException("Refinement not respected: [$refinement]")
