package ru.aiss83.comunalexpenses2.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation: delegates to [BackHandler].
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
