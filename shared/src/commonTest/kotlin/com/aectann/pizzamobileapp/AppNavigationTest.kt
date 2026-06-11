package com.aectann.pizzamobileapp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppNavigationTest {

    @Test
    fun staysOnSplashUntilAnimationFinishes() {
        val destination = resolveDestination(
            animationDone = false,
            networkGate = SplashNetworkGate.Released,
        )

        assertNull(destination)
    }

    @Test
    fun opensCatalogWhenAnimationDoneAndInitialRequestStillLoading() {
        val destination = resolveDestination(
            animationDone = true,
            networkGate = SplashNetworkGate.InitialRequest,
        )

        assertEquals(Destination.Catalog, destination)
    }

    @Test
    fun opensCatalogWhenAnimationDoneAndRequestReleased() {
        val destination = resolveDestination(
            animationDone = true,
            networkGate = SplashNetworkGate.Released,
        )

        assertEquals(Destination.Catalog, destination)
    }

    @Test
    fun staysOnSplashWhenOfflineBlocked() {
        val destination = resolveDestination(
            animationDone = true,
            networkGate = SplashNetworkGate.OfflineBlocked,
        )

        assertNull(destination)
    }

    @Test
    fun staysOnSplashWhileOfflineRetryIsRunning() {
        val destination = resolveDestination(
            animationDone = true,
            networkGate = SplashNetworkGate.RetryRequest,
        )

        assertNull(destination)
    }
}
