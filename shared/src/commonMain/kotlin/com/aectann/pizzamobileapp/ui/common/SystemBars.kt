package com.aectann.pizzamobileapp.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Toggles system navigation-bar visibility per app phase. On Android the bar is hidden
 * while [navigationBarsVisible] is false (used during the splash) and restored otherwise.
 * iOS has no system navigation bar, so the actual is a no-op.
 */
@Composable
expect fun SystemBarsEffect(navigationBarsVisible: Boolean)

/**
 * Gradient scrim drawn behind the system navigation bar so its buttons stay legible over
 * light content. On Android the height tracks the navigation-bar inset and the gradient
 * starts transparent at the top, deepening toward the bottom. iOS has no navigation-bar
 * buttons, so the actual is a no-op.
 */
@Composable
expect fun SystemNavBarScrim(modifier: Modifier = Modifier)
