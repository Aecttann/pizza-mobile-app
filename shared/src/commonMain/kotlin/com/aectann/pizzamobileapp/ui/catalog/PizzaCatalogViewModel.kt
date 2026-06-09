package com.aectann.pizzamobileapp.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.model.variantFor
import com.aectann.pizzamobileapp.data.repository.PizzaRepository
import com.aectann.pizzamobileapp.data.repository.PizzaRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PizzaCatalogUiState(
    val pizzas: List<Pizza> = emptyList(),
    // Size is a single global selection shared across all pizzas.
    val selectedSize: PizzaSize = PizzaSize.M,
    val quantities: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

private fun List<Pizza>.toLoadedCatalogState(): PizzaCatalogUiState =
    PizzaCatalogUiState(
        pizzas = this,
        quantities = associate { pizza -> pizza.id to 1 },
        isLoading = false,
    )

fun PizzaCatalogUiState.quantityFor(pizzaId: String): Int =
    quantities[pizzaId] ?: 1

fun PizzaCatalogUiState.totalPriceFor(pizzaId: String): Double {
    val pizza = pizzas.firstOrNull { it.id == pizzaId } ?: return 0.0
    val unitPrice = pizza.variantFor(selectedSize)?.price ?: return 0.0
    return unitPrice * quantityFor(pizzaId)
}

class PizzaCatalogViewModel(
    initialPizzas: List<Pizza>? = null,
    private val repository: PizzaRepository = PizzaRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialPizzas?.toLoadedCatalogState() ?: PizzaCatalogUiState())
    val uiState: StateFlow<PizzaCatalogUiState> = _uiState.asStateFlow()

    init {
        if (initialPizzas == null) {
            loadPizzas()
        }
    }

    private fun loadPizzas() {
        viewModelScope.launch {
            try {
                val pizzas = repository.getPizzas()
                _uiState.update {
                    pizzas.toLoadedCatalogState()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectSize(size: PizzaSize) {
        _uiState.update { state -> state.copy(selectedSize = size) }
    }

    fun increment(pizzaId: String) {
        _uiState.update { state ->
            val qty = state.quantityFor(pizzaId) + 1
            state.copy(quantities = state.quantities + (pizzaId to qty))
        }
    }

    fun decrement(pizzaId: String) {
        _uiState.update { state ->
            val qty = (state.quantityFor(pizzaId) - 1).coerceAtLeast(1)
            state.copy(quantities = state.quantities + (pizzaId to qty))
        }
    }
}
