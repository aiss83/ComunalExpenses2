package ru.aiss83.comunalexpenses2.utils

import androidx.compose.runtime.Composable

/**
 * iOS implementation: returns a no-op trigger.
 * File picking on iOS requires additional UIKit setup — use text import as fallback.
 */
@Composable
actual fun rememberJsonFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    return {
        // Not implemented for iOS — use export/import via share sheet instead
        onFilePicked(null)
    }
}
