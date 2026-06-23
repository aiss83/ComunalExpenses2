package ru.aiss83.comunalexpenses2.utils

import android.content.ContentValues
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import ru.aiss83.comunalexpenses2.data.AppContextHolder
import java.io.File

/**
 * Android implementation: saves JSON to Downloads folder and shares via intent.
 */
actual fun exportAndShareJson(json: String, filename: String, title: String) {
    val context = AppContextHolder.context ?: return

    try {
        // Save to Downloads via MediaStore
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
                stream.write(json.toByteArray(Charsets.UTF_8))
            }
        }

        // Share the file
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, title)
            if (uri != null) {
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                putExtra(Intent.EXTRA_TEXT, json)
            }
        }
        val chooser = Intent.createChooser(shareIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Exception) {
        // Fallback: share as text
        shareText(json, title)
    }
}
