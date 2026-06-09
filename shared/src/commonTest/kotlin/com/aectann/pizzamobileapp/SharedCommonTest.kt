package com.aectann.pizzamobileapp

import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.model.PizzaVariant
import com.aectann.pizzamobileapp.ui.catalog.PizzaCatalogUiState
import com.aectann.pizzamobileapp.ui.catalog.quantityFor
import com.aectann.pizzamobileapp.ui.catalog.totalPriceFor
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun totalPriceUsesSelectedSizePriceAndQuantity() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza()),
            selectedSize = PizzaSize.L,
            quantities = mapOf("pepperoni-blast" to 3),
            isLoading = false,
        )

        assertEquals(67.5, state.totalPriceFor("pepperoni-blast"))
    }

    @Test
    fun quantityDefaultsToOneWhenPizzaHasNoStoredQuantity() {
        val state = PizzaCatalogUiState()

        assertEquals(1, state.quantityFor("unknown"))
    }

    @Test
    fun totalPriceReturnsZeroWhenSelectedVariantIsMissing() {
        val state = PizzaCatalogUiState(
            pizzas = listOf(samplePizza(variants = listOf(PizzaVariant(PizzaSize.S, 15.5)))),
            selectedSize = PizzaSize.M,
            quantities = mapOf("pepperoni-blast" to 2),
            isLoading = false,
        )

        assertEquals(0.0, state.totalPriceFor("pepperoni-blast"))
    }

    private fun samplePizza(
        variants: List<PizzaVariant> = listOf(
            PizzaVariant(PizzaSize.S, 15.5),
            PizzaVariant(PizzaSize.M, 17.99),
            PizzaVariant(PizzaSize.L, 22.5),
        ),
    ): Pizza = Pizza(
        id = "pepperoni-blast",
        name = "Pepperoni Blast",
        description = "Pepperoni pizza",
        imageUrl = "https://example.com/pizza.png",
        variants = variants,
        defaultSize = PizzaSize.M,
    )
}
