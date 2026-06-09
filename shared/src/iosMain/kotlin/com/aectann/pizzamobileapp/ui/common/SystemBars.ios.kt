package com.aectann.pizzamobileapp.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun SystemBarsEffect(navigationBarsVisible: Boolean) {
    // iOS has no system navigation bar.
}

@Composable
actual fun SystemNavBarScrim(modifier: Modifier) {
    // iOS has no system navigation-bar buttons to scrim.
}
