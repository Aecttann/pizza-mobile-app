package com.aectann.pizzamobileapp.ui.catalog

import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.model.PizzaVariant
import com.aectann.pizzamobileapp.testutil.samplePizza
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogStateTest {

    @Test
    fun totalPriceForUnknownPizzaIdIsZero() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza(id = "known")),
            isLoading = false,
        )

        assertEquals(0.0, state.totalPriceFor("does-not-exist"))
    }

    @Test
    fun totalPriceWithDefaultQuantityEqualsSelectedVariantUnitPrice() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza(id = "a")),
            selectedSize = PizzaSize.M,
            isLoading = false,
        )

        // No stored quantity -> defaults to 1 -> total equals the M variant unit price.
        assertEquals(17.99, state.totalPriceFor("a"))
    }

    @Test
    fun totalPriceOnEmptyCatalogIsZero() {
        val state = PizzaCatalogUiState(pizzas = emptyList(), isLoading = false)

        assertEquals(0.0, state.totalPriceFor("anything"))
    }

    @Test
    fun totalPriceResolvesEverySize() {
        val pizza = samplePizza(
            id = "a",
            variants = listOf(
                PizzaVariant(PizzaSize.S, 10.0),
                PizzaVariant(PizzaSize.M, 20.0),
                PizzaVariant(PizzaSize.L, 30.0),
            ),
        )

        val expected = mapOf(
            PizzaSize.S to 10.0,
            PizzaSize.M to 20.0,
            PizzaSize.L to 30.0,
        )

        expected.forEach { (size, unitPrice) ->
            val state = PizzaCatalogUiState(
                pizzas = listOf(pizza),
                selectedSize = size,
                quantities = mapOf("a" to 1),
                isLoading = false,
            )
            assertEquals(unitPrice, state.totalPriceFor("a"))
        }
    }

    @Test
    fun totalPricePreservesTwoDecimalPrecision() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza(id = "a")),
            selectedSize = PizzaSize.M,
            quantities = mapOf("a" to 3),
            isLoading = false,
        )

        // 17.99 * 3 is not exactly representable as a Double; assert within sub-cent
        // tolerance to pin the value the UI rounds and shows to the user.
        assertEquals(53.97, state.totalPriceFor("a"), absoluteTolerance = 1e-9)
    }

    @Test
    fun quantityForReturnsStoredValueWhenPresent() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza(id = "a")),
            quantities = mapOf("a" to 4),
            isLoading = false,
        )

        assertEquals(4, state.quantityFor("a"))
    }
}
