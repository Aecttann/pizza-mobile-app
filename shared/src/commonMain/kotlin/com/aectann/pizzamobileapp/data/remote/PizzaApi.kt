package com.aectann.pizzamobileapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PizzaApi(
    engine: HttpClientEngine? = null,
    private val pizzasUrl: String = DEFAULT_PIZZAS_URL,
) {
    // Tolerate fields the client does not model so backend additions never crash parsing.
    private val configure: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val client: HttpClient =
        if (engine != null) HttpClient(engine, configure) else HttpClient(configure)

    suspend fun getPizzas(): PizzasResponse =
        client.get(pizzasUrl).body()

    fun close() {
        client.close()
    }

    companion object {
        const val DEFAULT_PIZZAS_URL = "https://oursongapp.com/api/pizzas"
    }
}
