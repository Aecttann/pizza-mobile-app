package com.aectann.pizzamobileapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.repository.PizzaLoadFailure
import com.aectann.pizzamobileapp.data.repository.PizzaRepository
import com.aectann.pizzamobileapp.data.repository.PizzaRepositoryImpl
import com.aectann.pizzamobileapp.data.repository.toPizzaLoadFailure
import com.aectann.pizzamobileapp.ui.catalog.PizzaCatalogScreen
import com.aectann.pizzamobileapp.ui.common.SystemBarsEffect
import com.aectann.pizzamobileapp.ui.common.SystemNavBarScrim
import com.aectann.pizzamobileapp.ui.splash.SplashScreen
import com.aectann.pizzamobileapp.ui.theme.PizzaTheme
import kotlinx.coroutines.CompletableDeferred

internal sealed interface Destination {
    data object Splash : Destination
    data object Catalog : Destination
}

internal enum class SplashNetworkGate {
    InitialRequest,
    RetryRequest,
    OfflineBlocked,
    Released,
}

/**
 * Pure navigation decision for the splash phase. Returns the destination to switch to,
 * or null to stay on the current screen. The catalog opens after the splash animation
 * without waiting for pizza data, except while an explicit offline retry is blocking it.
 */
internal fun resolveDestination(
    animationDone: Boolean,
    networkGate: SplashNetworkGate,
): Destination? = when {
    !animationDone -> null
    networkGate == SplashNetworkGate.OfflineBlocked -> null
    networkGate == SplashNetworkGate.RetryRequest -> null
    else -> Destination.Catalog
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
        var loadAttempt by remember { mutableStateOf(0) }
        var networkGate by remember { mutableStateOf(SplashNetworkGate.InitialRequest) }
        val pizzaPrefetch = remember(loadAttempt) {
            CompletableDeferred<Result<List<Pizza>>>()
        }

        LaunchedEffect(loadAttempt) {
            val retryingFromOffline = networkGate == SplashNetworkGate.OfflineBlocked
            networkGate = if (retryingFromOffline) {
                SplashNetworkGate.RetryRequest
            } else {
                SplashNetworkGate.InitialRequest
            }
            val result = runCatching { repository.getPizzas() }
            pizzaPrefetch.complete(result)
            val failure = result.exceptionOrNull()?.toPizzaLoadFailure()
            networkGate = if (failure == PizzaLoadFailure.NetworkUnavailable && destination is Destination.Splash) {
                SplashNetworkGate.OfflineBlocked
            } else {
                SplashNetworkGate.Released
            }
        }

        LaunchedEffect(animationDone, networkGate) {
            resolveDestination(animationDone, networkGate)?.let { destination = it }
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
                    Destination.Catalog -> PizzaCatalogScreen(initialLoad = pizzaPrefetch)
                }
            }

            if (!onSplash) {
                SystemNavBarScrim(modifier = Modifier.align(Alignment.BottomCenter))
            }

            if (onSplash && networkGate == SplashNetworkGate.OfflineBlocked) {
                NoInternetDialog(
                    retryInProgress = false,
                    onRetry = { loadAttempt++ },
                )
            } else if (onSplash && networkGate == SplashNetworkGate.RetryRequest) {
                NoInternetDialog(
                    retryInProgress = true,
                    onRetry = {},
                )
            }
        }
    }
}

@Composable
private fun NoInternetDialog(
    retryInProgress: Boolean,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "No internet connection")
        },
        text = {
            Text(text = "Check your connection and try again.")
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                enabled = !retryInProgress,
            ) {
                if (retryInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text(text = "Try again")
                }
            }
        },
    )
}
