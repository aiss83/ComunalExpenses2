package ru.aiss83.comunalexpenses2.utils

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import ru.aiss83.comunalexpenses2.data.AppContextHolder

/**
 * Android implementation: saves JSON bytes to Downloads folder via MediaStore.
 */
actual fun saveJsonToFile(bytes: ByteArray, filename: String) {
    val context = AppContextHolder.context ?: return

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(bytes)
            }
        }
    } catch (_: Exception) {
        // fail silently
    }
}
