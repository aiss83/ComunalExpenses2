package ru.aiss83.comunalexpenses2.utils

import platform.Foundation.NSArray
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * iOS implementation of share functionality.
 * Uses UIActivityViewController to present the native share sheet.
 */
actual fun shareText(text: String, title: String) {
    val activityItems = listOf(text)
    val activityViewController = UIActivityViewController(activityItems, null)
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
}
