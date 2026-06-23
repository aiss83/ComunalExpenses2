package ru.aiss83.comunalexpenses2.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Android implementation: opens system file picker filtered to JSON/text files.
 */
@Composable
actual fun rememberJsonFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.let { stream ->
                    BufferedReader(InputStreamReader(stream)).use { it.readText() }
                }
                onFilePicked(content)
            } catch (_: Exception) {
                onFilePicked(null)
            }
        } else {
            onFilePicked(null)
        }
    }

    return {
        launcher.launch("application/json")
    }
}
