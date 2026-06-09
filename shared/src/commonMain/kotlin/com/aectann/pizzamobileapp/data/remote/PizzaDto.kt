package com.aectann.pizzamobileapp.data.remote

import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.model.PizzaVariant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PizzasResponse(
    val pizzas: List<PizzaDto>,
)

@Serializable
data class PizzaDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("image_url") val imageUrl: String,
    val variants: List<VariantDto>,
    @SerialName("default_size") val defaultSize: String,
)

@Serializable
data class VariantDto(
    val size: String,
    val price: Double,
)

// Returns null instead of throwing when the backend sends sizes outside the known
// vocabulary. Variants with unrecognized sizes are dropped; a pizza left with no valid
// variant is itself dropped (returns null). An unrecognized default_size falls back to
// the first valid variant's size so a single bad field never discards a usable pizza.
fun PizzaDto.toDomainOrNull(): Pizza? {
    val mappedVariants = variants.mapNotNull { it.toDomainOrNull() }
    val firstSize = mappedVariants.firstOrNull()?.size ?: return null
    return Pizza(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        variants = mappedVariants,
        defaultSize = PizzaSize.fromApiValueOrNull(defaultSize) ?: firstSize,
    )
}

fun VariantDto.toDomainOrNull(): PizzaVariant? =
    PizzaSize.fromApiValueOrNull(size)?.let { mappedSize ->
        PizzaVariant(size = mappedSize, price = price)
    }
