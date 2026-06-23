package ru.aiss83.comunalexpenses2.utils

import androidx.compose.runtime.Composable

/**
 * Platform-specific file picker for JSON files.
 * Returns a trigger lambda that opens the system file picker.
 * When a file is selected, its content is passed to [onFilePicked].
 */
@Composable
expect fun rememberJsonFilePicker(onFilePicked: (String?) -> Unit): () -> Unit
