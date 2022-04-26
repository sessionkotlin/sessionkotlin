package org.david.sessionkotlin.dsl

public class RecursionTag internal constructor(
    private val name: String,
) {
    override fun toString(): String = name
}
