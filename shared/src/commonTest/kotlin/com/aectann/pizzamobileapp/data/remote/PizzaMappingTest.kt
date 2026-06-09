package com.aectann.pizzamobileapp.data.remote

import com.aectann.pizzamobileapp.data.model.PizzaSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PizzaMappingTest {

    @Test
    fun pizzaDtoMapsAllFieldsIncludingSnakeCaseToDomain() {
        val dto = PizzaDto(
            id = "midnight-harvest",
            name = "Midnight Harvest",
            description = "Veggie pizza",
            imageUrl = "https://example.com/mh.png",
            variants = listOf(
                VariantDto(size = "S", price = 12.0),
                VariantDto(size = "L", price = 18.5),
            ),
            defaultSize = "L",
        )

        val pizza = dto.toDomainOrNull()

        assertEquals("midnight-harvest", pizza?.id)
        assertEquals("Midnight Harvest", pizza?.name)
        assertEquals("Veggie pizza", pizza?.description)
        assertEquals("https://example.com/mh.png", pizza?.imageUrl)
        assertEquals(PizzaSize.L, pizza?.defaultSize)
        assertEquals(2, pizza?.variants?.size)
        assertEquals(PizzaSize.S, pizza?.variants?.get(0)?.size)
        assertEquals(12.0, pizza?.variants?.get(0)?.price)
        assertEquals(PizzaSize.L, pizza?.variants?.get(1)?.size)
        assertEquals(18.5, pizza?.variants?.get(1)?.price)
    }

    @Test
    fun variantDtoMapsSizeAndPriceToDomain() {
        val variant = VariantDto(size = "M", price = 17.99).toDomainOrNull()

        assertEquals(PizzaSize.M, variant?.size)
        assertEquals(17.99, variant?.price)
    }

    @Test
    fun variantSizeLookupIsCaseInsensitive() {
        assertEquals(PizzaSize.L, PizzaSize.fromApiValueOrNull("l"))
        assertEquals(PizzaSize.M, PizzaSize.fromApiValueOrNull("m"))
        assertNull(PizzaSize.fromApiValueOrNull("XL"))
    }

    @Test
    fun variantDtoWithUnknownSizeIsDropped() {
        assertNull(VariantDto(size = "XL", price = 9.0).toDomainOrNull())
    }

    @Test
    fun pizzaDropsVariantsWithUnknownSizesButKeepsValidOnes() {
        val dto = PizzaDto(
            id = "a",
            name = "A",
            description = "d",
            imageUrl = "u",
            variants = listOf(
                VariantDto(size = "S", price = 10.0),
                VariantDto(size = "XL", price = 99.0),
                VariantDto(size = "M", price = 15.0),
            ),
            defaultSize = "M",
        )

        val pizza = dto.toDomainOrNull()

        assertEquals(listOf(PizzaSize.S, PizzaSize.M), pizza?.variants?.map { it.size })
    }

    @Test
    fun pizzaWithUnknownDefaultSizeFallsBackToFirstValidVariant() {
        val dto = PizzaDto(
            id = "a",
            name = "A",
            description = "d",
            imageUrl = "u",
            variants = listOf(VariantDto(size = "S", price = 10.0)),
            defaultSize = "HUGE",
        )

        assertEquals(PizzaSize.S, dto.toDomainOrNull()?.defaultSize)
    }

    @Test
    fun pizzaWithNoValidVariantsIsDropped() {
        val dto = PizzaDto(
            id = "a",
            name = "A",
            description = "d",
            imageUrl = "u",
            variants = listOf(VariantDto(size = "XL", price = 1.0)),
            defaultSize = "XL",
        )

        assertNull(dto.toDomainOrNull())
    }
}
