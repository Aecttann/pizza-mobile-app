package com.aectann.pizzamobileapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.repository.PizzaRepository
import com.aectann.pizzamobileapp.data.repository.PizzaRepositoryImpl
import com.aectann.pizzamobileapp.ui.catalog.PizzaCatalogScreen
import com.aectann.pizzamobileapp.ui.common.SystemBarsEffect
import com.aectann.pizzamobileapp.ui.common.SystemNavBarScrim
import com.aectann.pizzamobileapp.ui.splash.SplashScreen
import com.aectann.pizzamobileapp.ui.theme.ColorWhite
import com.aectann.pizzamobileapp.ui.theme.PizzaTheme

internal sealed interface Destination {
    data object Splash : Destination
    data class Catalog(val pizzas: List<Pizza>) : Destination
    data object LoadError : Destination
}

/**
 * Pure navigation decision for the splash phase. Returns the destination to switch to,
 * or null to stay on the current screen. The catalog only opens once the splash
 * animation has finished, and a successful load takes precedence over a failed one.
 */
internal fun resolveDestination(
    animationDone: Boolean,
    pizzas: List<Pizza>?,
    loadingFailed: Boolean,
): Destination? = when {
    !animationDone -> null
    pizzas != null -> Destination.Catalog(pizzas)
    loadingFailed -> Destination.LoadError
    else -> null
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun App(repository: PizzaRepository = remember { PizzaRepositoryImpl() }) {
    val context = LocalPlatformContext.current
    setSingletonImageLoaderFactory {
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }

    PizzaTheme {
        var destination by remember { mutableStateOf<Destination>(Destination.Splash) }
        var animationDone by remember { mutableStateOf(false) }
        var apiData by remember { mutableStateOf<List<Pizza>?>(null) }
        var loadingFailed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            try {
                apiData = repository.getPizzas()
            } catch (_: Exception) {
                loadingFailed = true
            }
        }

        LaunchedEffect(animationDone, apiData, loadingFailed) {
            resolveDestination(animationDone, apiData, loadingFailed)?.let { destination = it }
        }

        val onSplash = destination is Destination.Splash
        SystemBarsEffect(navigationBarsVisible = !onSplash)

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = destination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "appNavigation",
            ) { dest ->
                when (dest) {
                    Destination.Splash -> SplashScreen(
                        onAnimationFinished = { animationDone = true },
                    )
                    is Destination.Catalog -> PizzaCatalogScreen(initialPizzas = dest.pizzas)
                    Destination.LoadError -> AppLoadErrorScreen()
                }
            }

            if (!onSplash) {
                SystemNavBarScrim(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
private fun AppLoadErrorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorWhite),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Failed to load pizzas.\nPlease try again.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}
