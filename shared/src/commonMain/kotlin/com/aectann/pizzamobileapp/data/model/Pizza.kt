package com.aectann.pizzamobileapp.data.model

data class Pizza(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val variants: List<PizzaVariant>,
    val defaultSize: PizzaSize,
)

data class PizzaVariant(
    val size: PizzaSize,
    val price: Double,
)

enum class PizzaSize {
    S, M, L;

    companion object {
        // Case-insensitive lookup that returns null for values outside the known set,
        // instead of throwing like valueOf. Used to keep API mapping crash-safe.
        fun fromApiValueOrNull(raw: String): PizzaSize? =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }
}

fun Pizza.variantFor(size: PizzaSize): PizzaVariant? =
    variants.firstOrNull { it.size == size }
