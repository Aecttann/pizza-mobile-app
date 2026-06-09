package com.aectann.pizzamobileapp

import com.aectann.pizzamobileapp.testutil.samplePizza
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppNavigationTest {

    @Test
    fun staysOnSplashUntilAnimationFinishesEvenWhenDataReady() {
        val destination = resolveDestination(
            animationDone = false,
            pizzas = listOf(samplePizza()),
            loadingFailed = false,
        )

        assertNull(destination)
    }

    @Test
    fun staysOnSplashUntilAnimationFinishesEvenWhenLoadFailed() {
        val destination = resolveDestination(
            animationDone = false,
            pizzas = null,
            loadingFailed = true,
        )

        assertNull(destination)
    }

    @Test
    fun opensCatalogWhenAnimationDoneAndDataReady() {
        val pizzas = listOf(samplePizza(id = "a"), samplePizza(id = "b"))

        val destination = resolveDestination(
            animationDone = true,
            pizzas = pizzas,
            loadingFailed = false,
        )

        assertEquals(Destination.Catalog(pizzas), destination)
    }

    @Test
    fun opensErrorWhenAnimationDoneAndLoadFailed() {
        val destination = resolveDestination(
            animationDone = true,
            pizzas = null,
            loadingFailed = true,
        )

        assertEquals(Destination.LoadError, destination)
    }

    @Test
    fun successTakesPrecedenceOverFailure() {
        val pizzas = listOf(samplePizza())

        val destination = resolveDestination(
            animationDone = true,
            pizzas = pizzas,
            loadingFailed = true,
        )

        assertEquals(Destination.Catalog(pizzas), destination)
    }

    @Test
    fun waitsWhenAnimationDoneButNoOutcomeYet() {
        val destination = resolveDestination(
            animationDone = true,
            pizzas = null,
            loadingFailed = false,
        )

        assertNull(destination)
    }
}
