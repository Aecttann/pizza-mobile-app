package com.aectann.pizzamobileapp.ui.catalog

import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.testutil.FakePizzaRepository
import com.aectann.pizzamobileapp.testutil.samplePizza
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PizzaCatalogViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoadingWhenNoInitialPizzasProvided() = runTest(dispatcher) {
        val repository = FakePizzaRepository.succeeding(listOf(samplePizza()))

        val viewModel = PizzaCatalogViewModel(repository = repository)

        // loadPizzas() is scheduled on Main (StandardTestDispatcher) but not yet executed.
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.pizzas.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun successfulLoadPopulatesPizzasAndClearsLoading() = runTest(dispatcher) {
        val pizzas = listOf(samplePizza(id = "a"), samplePizza(id = "b"))
        val repository = FakePizzaRepository.succeeding(pizzas)

        val viewModel = PizzaCatalogViewModel(repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(pizzas, state.pizzas)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun repositoryErrorSetsErrorAndClearsLoading() = runTest(dispatcher) {
        val repository = FakePizzaRepository.failing(RuntimeException("boom"))

        val viewModel = PizzaCatalogViewModel(repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
        assertTrue(state.pizzas.isEmpty())
    }

    @Test
    fun initialPizzasSkipNetworkLoadAndYieldLoadedState() = runTest(dispatcher) {
        val pizzas = listOf(samplePizza())
        val repository = FakePizzaRepository.succeeding(listOf(samplePizza(id = "from-network")))

        val viewModel = PizzaCatalogViewModel(initialPizzas = pizzas, repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(pizzas, state.pizzas)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun quantitiesAreInitializedToOnePerPizzaAfterLoad() = runTest(dispatcher) {
        val pizzas = listOf(samplePizza(id = "a"), samplePizza(id = "b"), samplePizza(id = "c"))
        val repository = FakePizzaRepository.succeeding(pizzas)

        val viewModel = PizzaCatalogViewModel(repository = repository)
        advanceUntilIdle()

        val quantities = viewModel.uiState.value.quantities
        assertEquals(setOf("a", "b", "c"), quantities.keys)
        assertTrue(quantities.values.all { it == 1 })
    }

    @Test
    fun selectSizeUpdatesSelectedSize() = runTest(dispatcher) {
        val viewModel = PizzaCatalogViewModel(
            initialPizzas = listOf(samplePizza()),
            repository = FakePizzaRepository.succeeding(emptyList()),
        )

        viewModel.selectSize(PizzaSize.L)

        assertEquals(PizzaSize.L, viewModel.uiState.value.selectedSize)
    }

    @Test
    fun incrementRaisesQuantityForGivenPizza() = runTest(dispatcher) {
        val viewModel = PizzaCatalogViewModel(
            initialPizzas = listOf(samplePizza(id = "a")),
            repository = FakePizzaRepository.succeeding(emptyList()),
        )

        viewModel.increment("a")
        viewModel.increment("a")

        assertEquals(3, viewModel.uiState.value.quantityFor("a"))
    }

    @Test
    fun decrementDoesNotGoBelowOne() = runTest(dispatcher) {
        val viewModel = PizzaCatalogViewModel(
            initialPizzas = listOf(samplePizza(id = "a")),
            repository = FakePizzaRepository.succeeding(emptyList()),
        )

        viewModel.decrement("a")
        viewModel.decrement("a")

        assertEquals(1, viewModel.uiState.value.quantityFor("a"))
    }

    @Test
    fun incrementThenDecrementReturnsToPreviousQuantity() = runTest(dispatcher) {
        val viewModel = PizzaCatalogViewModel(
            initialPizzas = listOf(samplePizza(id = "a")),
            repository = FakePizzaRepository.succeeding(emptyList()),
        )

        viewModel.increment("a")
        viewModel.increment("a")
        viewModel.decrement("a")

        assertEquals(2, viewModel.uiState.value.quantityFor("a"))
    }
}
