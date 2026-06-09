package com.aectann.pizzamobileapp.data.remote

import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.repository.PizzaRepositoryImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PizzaApiTest {

    private fun jsonEngine(content: String, status: HttpStatusCode = HttpStatusCode.OK) =
        MockEngine {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

    private val validJson = """
        {
          "pizzas": [
            {
              "id": "midnight-harvest",
              "name": "Midnight Harvest",
              "description": "Veggie pizza",
              "image_url": "https://example.com/mh.png",
              "variants": [
                {"size": "S", "price": 12.0},
                {"size": "M", "price": 15.0},
                {"size": "L", "price": 18.5}
              ],
              "default_size": "M"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun parsesSuccessfulJsonResponse() = runTest {
        val api = PizzaApi(engine = jsonEngine(validJson))

        val response = api.getPizzas()

        assertEquals(1, response.pizzas.size)
        val dto = response.pizzas.first()
        assertEquals("midnight-harvest", dto.id)
        assertEquals("https://example.com/mh.png", dto.imageUrl)
        assertEquals("M", dto.defaultSize)
        assertEquals(3, dto.variants.size)
        api.close()
    }

    @Test
    fun ignoresUnknownJsonKeys() = runTest {
        val jsonWithExtras = """
            {
              "version": 2,
              "pizzas": [
                {
                  "id": "a",
                  "name": "A",
                  "description": "d",
                  "image_url": "u",
                  "calories": 900,
                  "variants": [{"size": "M", "price": 1.0, "currency": "USD"}],
                  "default_size": "M"
                }
              ]
            }
        """.trimIndent()
        val api = PizzaApi(engine = jsonEngine(jsonWithExtras))

        val response = api.getPizzas()

        assertEquals(1, response.pizzas.size)
        assertEquals("a", response.pizzas.first().id)
        api.close()
    }

    @Test
    fun networkFailurePropagates() = runTest {
        val engine = MockEngine { throw RuntimeException("network down") }
        val api = PizzaApi(engine = engine)

        assertFails { api.getPizzas() }
        api.close()
    }

    @Test
    fun httpErrorStatusPropagates() = runTest {
        val api = PizzaApi(engine = jsonEngine("Server Error", HttpStatusCode.InternalServerError))

        assertFails { api.getPizzas() }
        api.close()
    }

    @Test
    fun repositoryReturnsDomainModelsNotDtos() = runTest {
        val repository = PizzaRepositoryImpl(api = PizzaApi(engine = jsonEngine(validJson)))

        val pizzas = repository.getPizzas()

        assertEquals(1, pizzas.size)
        val pizza = pizzas.first()
        assertEquals("midnight-harvest", pizza.id)
        assertEquals("https://example.com/mh.png", pizza.imageUrl)
        assertEquals(PizzaSize.M, pizza.defaultSize)
        assertTrue(pizza.variants.any { it.size == PizzaSize.L && it.price == 18.5 })
    }

    @Test
    fun repositoryDropsPizzasWithNoValidVariants() = runTest {
        val mixedJson = """
            {
              "pizzas": [
                {
                  "id": "good",
                  "name": "Good",
                  "description": "d",
                  "image_url": "u",
                  "variants": [{"size": "M", "price": 1.0}],
                  "default_size": "M"
                },
                {
                  "id": "garbage",
                  "name": "Garbage",
                  "description": "d",
                  "image_url": "u",
                  "variants": [{"size": "XL", "price": 2.0}],
                  "default_size": "XL"
                }
              ]
            }
        """.trimIndent()
        val repository = PizzaRepositoryImpl(api = PizzaApi(engine = jsonEngine(mixedJson)))

        val pizzas = repository.getPizzas()

        assertEquals(listOf("good"), pizzas.map { it.id })
    }
}
