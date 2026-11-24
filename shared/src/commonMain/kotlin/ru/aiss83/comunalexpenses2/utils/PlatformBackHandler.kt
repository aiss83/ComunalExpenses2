package ru.aiss83.comunalexpenses2.utils

import androidx.compose.runtime.Composable

/**
 * Handles the platform "back" gesture: Android system back, no-op on iOS.
 *
 * Declared as `expect` so screens in `commonMain` can override back navigation
 * without importing Android-only APIs.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
