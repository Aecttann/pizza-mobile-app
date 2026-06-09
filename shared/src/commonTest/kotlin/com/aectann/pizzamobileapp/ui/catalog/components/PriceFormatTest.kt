package com.aectann.pizzamobileapp.ui.catalog.components

import kotlin.test.Test
import kotlin.test.assertEquals

class PriceFormatTest {

    @Test
    fun formatsWholeNumberWithTwoDecimals() {
        assertEquals("20.00", formatPrice(20.0))
    }

    @Test
    fun padsSingleDigitFraction() {
        assertEquals("18.50", formatPrice(18.5))
    }

    @Test
    fun keepsTwoDecimalFraction() {
        assertEquals("17.99", formatPrice(17.99))
    }

    @Test
    fun roundsToNearestCent() {
        // 53.969999... is the Double result of 17.99 * 3; must render as "53.97".
        assertEquals("53.97", formatPrice(17.99 * 3))
        assertEquals("10.01", formatPrice(10.006))
    }

    @Test
    fun formatsZero() {
        assertEquals("0.00", formatPrice(0.0))
    }
}
