package com.github.d_costa.sessionkotlin.util

import com.github.d_costa.sessionkotlin.api.exception.RefinementException
import com.github.d_costa.sessionkotlin.dsl.SKRole

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal fun Map<SKRole, SKRole>.getOrKey(key: SKRole): SKRole = this.getOrDefault(key, key)

internal fun String.asClassname() =
    this.replace("\\s".toRegex(), "")
        .capitalized()

internal fun String.asPackageName() =
    this.trim()
        .replace("\\s".toRegex(), "_")
        .lowercase()

internal fun String.capitalized() =
    replaceFirstChar(Char::titlecase)

/**
 * Throws [RefinementException] if evaluation is false.
 */
public fun assertRefinement(refinement: String, evaluation: Boolean) {
    if (!evaluation)
        throw RefinementException(refinement)
}

internal fun hasWhitespace(label: String) =
    label.any { it.isWhitespace() }

internal fun <K, V> MutableMap<K, MutableList<V>>.merge(key: K, value: V) {
    if (key !in this) {
        put(key, mutableListOf())
    }
    getValue(key).add(value)
}

internal fun <K, V> MutableMap<K, MutableList<V>>.merge(key: K, value: MutableList<V>) {
    if (key !in this) {
        put(key, mutableListOf())
    }
    getValue(key).addAll(value)
}

internal fun <T, R> Iterable<T>.mapMutable(transform: (T) -> R): MutableList<R> =
    map(transform).toMutableList()
