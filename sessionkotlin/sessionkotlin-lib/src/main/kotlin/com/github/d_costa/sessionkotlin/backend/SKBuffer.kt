package com.github.d_costa.sessionkotlin.backend

/**
 * A buffer that holds a payload of type [T].
 *
 * Trying to read [value] without first initializing it will cause
 * an [UninitializedPropertyAccessException].
 */
public class SKBuffer<T : Any> {
    public lateinit var value: T
}
