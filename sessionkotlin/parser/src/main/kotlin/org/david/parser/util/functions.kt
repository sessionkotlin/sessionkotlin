package org.david.parser.util

import org.david.parser.exception.IncompatibleTypesException
import org.david.parser.exception.UnknownNumberClassException

internal fun <V> Map<String, V>.getOrThrow(name: String, throwable: () -> Throwable): V {
    try {
        return getValue(name)
    } catch (e: NoSuchElementException) {
        throw throwable()
    }
}

internal fun Number.isInteger() =
    when (this) {
        is Byte -> true
        is Short -> true
        is Int -> true
        is Long -> true
        is Float -> false
        is Double -> false
        else -> throw UnknownNumberClassException(this)
    }

internal operator fun Number.compareTo(o: Number): Int =
    if (this.isInteger() && o.isInteger()) {
        this.toLong().compareTo(o.toLong())
    } else if (!this.isInteger() && !o.isInteger()) {
        this.toDouble().compareTo(o.toDouble())
    } else {
        throw IncompatibleTypesException(this, o)
    }

internal operator fun Number.unaryMinus(): Number =
    when (this) {
        is Byte -> -this
        is Double -> -this
        is Float -> -this
        is Int -> -this
        is Long -> -this
        is Short -> -this
        else -> throw UnknownNumberClassException(this)
    }

internal operator fun Number.plus(o: Number): Number =
    if (this.isInteger() && o.isInteger()) {
        this.toLong().plus(o.toLong())
    } else if (!this.isInteger() && !o.isInteger()) {
        this.toDouble().plus(o.toDouble())
    } else {
        throw IncompatibleTypesException(this, o)
    }

internal operator fun Number.minus(o: Number): Number =
    if (this.isInteger() && o.isInteger()) {
        this.toLong().minus(o.toLong())
    } else if (!this.isInteger() && !o.isInteger()) {
        this.toDouble().minus(o.toDouble())
    } else {
        throw IncompatibleTypesException(this, o)
    }
