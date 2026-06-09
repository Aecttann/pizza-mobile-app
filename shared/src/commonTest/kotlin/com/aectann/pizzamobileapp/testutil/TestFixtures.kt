package com.aectann.pizzamobileapp.testutil

import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.model.PizzaVariant
import com.aectann.pizzamobileapp.data.repository.PizzaRepository

fun samplePizza(
    id: String = "pepperoni-blast",
    name: String = "Pepperoni Blast",
    description: String = "Pepperoni pizza",
    imageUrl: String = "https://example.com/pizza.png",
    variants: List<PizzaVariant> = listOf(
        PizzaVariant(PizzaSize.S, 15.5),
        PizzaVariant(PizzaSize.M, 17.99),
        PizzaVariant(PizzaSize.L, 22.5),
    ),
    defaultSize: PizzaSize = PizzaSize.M,
): Pizza = Pizza(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    variants = variants,
    defaultSize = defaultSize,
)

/**
 * Records invocation count so tests can assert whether a network load was triggered,
 * and replays either a success payload or a failure.
 */
class FakePizzaRepository private constructor(
    private val result: Result<List<Pizza>>,
) : PizzaRepository {

    var callCount: Int = 0
        private set

    override suspend fun getPizzas(): List<Pizza> {
        callCount++
        return result.getOrThrow()
    }

    companion object {
        fun succeeding(pizzas: List<Pizza>): FakePizzaRepository =
            FakePizzaRepository(Result.success(pizzas))

        fun failing(error: Throwable): FakePizzaRepository =
            FakePizzaRepository(Result.failure(error))
    }
}
