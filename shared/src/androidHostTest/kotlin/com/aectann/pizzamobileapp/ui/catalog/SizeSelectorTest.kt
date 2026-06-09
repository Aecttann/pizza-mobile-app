package com.aectann.pizzamobileapp.ui.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.ui.catalog.components.SizeSelector
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
class SizeSelectorTest {

    @Test
    fun rendersAllThreeSizes() = runComposeUiTest {
        setContent {
            MaterialTheme {
                SizeSelector(selectedSize = PizzaSize.M, onSizeSelected = {})
            }
        }

        PizzaSize.entries.forEach { size ->
            onNodeWithTag(CatalogTestTags.sizePill(size)).assertIsDisplayed()
        }
    }

    @Test
    fun clickingSizePillEmitsThatSize() = runComposeUiTest {
        val selected = mutableListOf<PizzaSize>()
        setContent {
            MaterialTheme {
                SizeSelector(selectedSize = PizzaSize.M, onSizeSelected = { selected += it })
            }
        }

        onNodeWithTag(CatalogTestTags.sizePill(PizzaSize.L)).performClick()
        onNodeWithTag(CatalogTestTags.sizePill(PizzaSize.S)).performClick()

        assertEquals(listOf(PizzaSize.L, PizzaSize.S), selected)
    }

    @Test
    fun selectionIsDrivenByHoistedState() = runComposeUiTest {
        var selectedSize by mutableStateOf(PizzaSize.S)
        setContent {
            MaterialTheme {
                SizeSelector(selectedSize = selectedSize, onSizeSelected = { selectedSize = it })
            }
        }

        onNodeWithTag(CatalogTestTags.sizePill(PizzaSize.L)).performClick()

        assertEquals(PizzaSize.L, selectedSize)
    }
}
