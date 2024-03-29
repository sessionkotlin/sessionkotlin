package com.github.sessionkotlin.lib.util

import com.github.sessionkotlin.lib.api.exception.RefinementException
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

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
public fun assertRefinement(plain: String, evaluation: Boolean) {
    if (!evaluation)
        throw RefinementException(plain)
}

internal fun String.hasWhitespace() = this.any { it.isWhitespace() }

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

internal fun createKeyManagers(keyStoreFilename: String, keystorePassword: String, keyPassword: String): Collection<KeyManager> {
    val keyStore = KeyStore.getInstance("JKS")
    val stream = loadResource(keyStoreFilename)
    keyStore.load(stream, keystorePassword.toCharArray())
    stream.close()

    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(keyStore, keyPassword.toCharArray())
    return kmf.keyManagers.toList()
}

internal fun createTrustManagers(trustStoreFilename: String, keystorePassword: String): Collection<TrustManager> {
    val trustStore = KeyStore.getInstance("JKS")
    val stream = loadResource(trustStoreFilename)
    trustStore.load(stream, keystorePassword.toCharArray())
    stream.close()

    val tmf = TrustManagerFactory.getInstance("SunX509")
    tmf.init(trustStore)
    return tmf.trustManagers.toList()
}

internal fun loadResource(filename: String): InputStream {
    val o = object : Any() {}
    return o.javaClass.classLoader.getResource(filename)?.openStream() ?: throw RuntimeException("Error reading $filename")
}
