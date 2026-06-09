package com.aectann.pizzamobileapp.ui.catalog

import com.aectann.pizzamobileapp.data.model.PizzaSize

// Stable test identifiers for the catalog UI. Kept in main code so production
// composables and UI tests share a single source of truth for the tags.
object CatalogTestTags {
    const val LOADING = "catalog_loading"
    const val ERROR = "catalog_error"
    const val PRICE = "order_price"
    const val QUANTITY = "order_quantity"
    const val INCREMENT = "order_increment"
    const val DECREMENT = "order_decrement"
    const val ADD = "order_add"

    fun sizePill(size: PizzaSize): String = "size_pill_${size.name}"
}
