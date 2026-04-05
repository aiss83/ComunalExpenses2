package ru.aiss83.comunalexpenses2.utils

import android.content.Context
import android.content.Intent

/**
 * Android implementation of share functionality.
 * Uses Intent.ACTION_SEND to open the native share sheet.
 */
actual fun shareText(text: String, title: String) {
    val context = getAppContext()
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(shareIntent, title)
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

/**
 * Get application context from AppContextHolder.
 */
private fun getAppContext(): Context {
    return ru.aiss83.comunalexpenses2.data.AppContextHolder.context
        ?: throw IllegalStateException("AppContextHolder.context must be initialized")
}
