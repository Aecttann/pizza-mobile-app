package com.aectann.pizzamobileapp.ui.catalog.components

import kotlin.math.round

// Locale-independent "0.00" formatting. Kotlin's String.format uses the default
// locale, which renders a comma decimal separator on UA/EU devices. Rounds to the
// nearest cent (round() ties to even) before splitting whole and fractional parts.
internal fun formatPrice(value: Double): String {
    val cents = round(value * 100).toLong()
    val whole = cents / 100
    val frac = (cents % 100).toInt()
    return "$whole." + frac.toString().padStart(2, '0')
}
