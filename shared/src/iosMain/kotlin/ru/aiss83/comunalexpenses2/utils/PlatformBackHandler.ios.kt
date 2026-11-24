package ru.aiss83.comunalexpenses2.utils

import androidx.compose.runtime.Composable

/**
 * iOS implementation: there is no system back button, so nothing to intercept.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op: iOS navigation is done with on-screen controls only.
}
