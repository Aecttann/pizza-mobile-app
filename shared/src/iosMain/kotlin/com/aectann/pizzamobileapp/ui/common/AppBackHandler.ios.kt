package com.aectann.pizzamobileapp.ui.common

import androidx.compose.runtime.Composable

// iOS has no hardware/system back button; zoom-out is driven by tap and pinch only.
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
