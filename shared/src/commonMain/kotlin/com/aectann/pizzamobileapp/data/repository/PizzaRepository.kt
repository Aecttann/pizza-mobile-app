package com.aectann.pizzamobileapp.data.repository

import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.remote.PizzaApi
import com.aectann.pizzamobileapp.data.remote.toDomainOrNull

interface PizzaRepository {
    suspend fun getPizzas(): List<Pizza>
}

class PizzaRepositoryImpl(
    private val api: PizzaApi = PizzaApi(),
) : PizzaRepository {
    override suspend fun getPizzas(): List<Pizza> =
        api.getPizzas().pizzas.mapNotNull { it.toDomainOrNull() }
}
