package com.aectann.pizzamobileapp.ui.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.aectann.pizzamobileapp.ui.catalog.components.OrderBar
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class OrderBarTest {

    @Test
    fun priceLabelShowsFormattedTotal() = runComposeUiTest {
        setContent {
            MaterialTheme {
                OrderBar(quantity = 2, totalPrice = 35.98, onIncrement = {}, onDecrement = {})
            }
        }

        onNodeWithTag(CatalogTestTags.PRICE).assertTextEquals("$35.98")
    }

    @Test
    fun incrementAndDecrementInvokeCallbacks() = runComposeUiTest {
        var increments = 0
        var decrements = 0
        setContent {
            MaterialTheme {
                OrderBar(
                    quantity = 3,
                    totalPrice = 10.0,
                    onIncrement = { increments++ },
                    onDecrement = { decrements++ },
                )
            }
        }

        onNodeWithTag(CatalogTestTags.INCREMENT).performClick()
        onNodeWithTag(CatalogTestTags.INCREMENT).performClick()
        onNodeWithTag(CatalogTestTags.DECREMENT).performClick()

        assertEquals(2, increments)
        assertEquals(1, decrements)
    }

    @Test
    fun quantityReflectsHoistedState() = runComposeUiTest {
        setContent {
            var quantity by remember { mutableStateOf(1) }

            MaterialTheme {
                OrderBar(
                    quantity = quantity,
                    totalPrice = 0.0,
                    onIncrement = { quantity++ },
                    onDecrement = { quantity-- },
                )
            }
        }

        onNodeWithTag(CatalogTestTags.QUANTITY).assertTextEquals("1")
        onNodeWithTag(CatalogTestTags.INCREMENT).performClick()
        onNodeWithTag(CatalogTestTags.QUANTITY).assertTextEquals("2")
    }

    @Test
    fun narrowWidthStillExposesAllControls() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Box(Modifier.width(300.dp)) {
                    OrderBar(quantity = 1, totalPrice = 12.5, onIncrement = {}, onDecrement = {})
                }
            }
        }

        // Below the 360dp breakpoint OrderBar switches to the stacked layout; every
        // interactive control must remain present and visible.
        onNodeWithTag(CatalogTestTags.DECREMENT).assertIsDisplayed()
        onNodeWithTag(CatalogTestTags.QUANTITY).assertIsDisplayed()
        onNodeWithTag(CatalogTestTags.INCREMENT).assertIsDisplayed()
        onNodeWithTag(CatalogTestTags.PRICE).assertIsDisplayed()
        onNodeWithTag(CatalogTestTags.ADD).assertIsDisplayed()
    }
}
