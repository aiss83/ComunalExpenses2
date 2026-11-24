package ru.aiss83.comunalexpenses2.utils

import platform.UIKit.UIActivityViewController

/**
 * iOS implementation of share functionality.
 * Uses UIActivityViewController to present the native share sheet.
 */
actual fun shareText(text: String, title: String) {
    presentShareSheet(UIActivityViewController(activityItems = listOf(text), applicationActivities = null))
}
