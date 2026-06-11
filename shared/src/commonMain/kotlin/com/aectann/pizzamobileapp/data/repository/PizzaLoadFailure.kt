package com.aectann.pizzamobileapp.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException

enum class PizzaLoadFailure {
    NetworkUnavailable,
    Server,
    Unknown,
}

fun Throwable.toPizzaLoadFailure(): PizzaLoadFailure {
    val root = rootCause()
    return when {
        this is ResponseException || root is ResponseException -> PizzaLoadFailure.Server
        this is SerializationException || root is SerializationException -> PizzaLoadFailure.Server
        root.looksLikeNetworkUnavailable() -> PizzaLoadFailure.NetworkUnavailable
        else -> PizzaLoadFailure.Unknown
    }
}

private tailrec fun Throwable.rootCause(): Throwable =
    cause?.takeIf { it !== this }?.rootCause() ?: this

private fun Throwable.looksLikeNetworkUnavailable(): Boolean {
    val text = listOfNotNull(message, cause?.message)
        .joinToString(separator = " ")
        .lowercase()

    return listOf(
        "unable to resolve host",
        "failed to connect",
        "network is unreachable",
        "no route to host",
        "software caused connection abort",
        "could not connect",
        "internet",
        "offline",
    ).any { marker -> marker in text }
}
