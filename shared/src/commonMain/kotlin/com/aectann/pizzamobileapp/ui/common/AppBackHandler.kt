package com.aectann.pizzamobileapp.ui.common

import androidx.compose.runtime.Composable

/**
 * Platform back-navigation handler. On Android it intercepts the system back button
 * while [enabled]; on iOS there is no system back button, so the actual is a no-op.
 */
@Composable
expect fun AppBackHandler(enabled: Boolean, onBack: () -> Unit)
