package com.aectann.pizzamobileapp.ui.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.data.repository.PizzaRepository
import com.aectann.pizzamobileapp.testutil.FakePizzaRepository
import com.aectann.pizzamobileapp.testutil.samplePizza
import com.aectann.pizzamobileapp.ui.theme.PizzaTheme
import kotlinx.coroutines.awaitCancellation
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

private class NeverReturningRepository : PizzaRepository {
    override suspend fun getPizzas(): List<Pizza> = awaitCancellation()
}

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PizzaCatalogScreenTest {

    @Test
    fun showsLoadingIndicatorWhileLoading() = runComposeUiTest {
        val viewModel = PizzaCatalogViewModel(repository = NeverReturningRepository())

        setContent {
            MaterialTheme {
                PizzaCatalogScreen(
                    viewModel = viewModel,
                )
            }
        }

        onNodeWithTag(CatalogTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun showsErrorWhenLoadFails() = runComposeUiTest {
        val viewModel = PizzaCatalogViewModel(
            repository = FakePizzaRepository.failing(RuntimeException("boom")),
        )

        setContent {
            MaterialTheme {
                PizzaCatalogScreen(
                    viewModel = viewModel,
                )
            }
        }

        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag(CatalogTestTags.ERROR).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag(CatalogTestTags.ERROR).assertIsDisplayed()
    }

    @Test
    fun showsCatalogControlsWhenLoaded() = runComposeUiTest {
        val pizzas = listOf(samplePizza(id = "pepperoni-blast"))
        val viewModel = PizzaCatalogViewModel(
            initialPizzas = pizzas,
            repository = FakePizzaRepository.succeeding(emptyList()),
        )

        setContent {
            PizzaTheme {
                PizzaCatalogScreen(
                    viewModel = viewModel,
                )
            }
        }

        // The order bar lives in a vertical-scroll column below the carousel, so it may
        // sit outside the test viewport; assert presence in the tree rather than display.
        onNodeWithTag(CatalogTestTags.PRICE).assertExists()
        onNodeWithTag(CatalogTestTags.INCREMENT).assertExists()
        onNodeWithTag(CatalogTestTags.DECREMENT).assertExists()
        onNodeWithTag(CatalogTestTags.sizePill(PizzaSize.M)).assertExists()
    }
}
