package ru.aiss83.comunalexpenses2.data

import platform.Foundation.NSBundle

/**
 * iOS implementation: reads CFBundleShortVersionString from Info.plist.
 */
actual fun getAppVersion(): String {
    return try {
        val info = NSBundle.mainBundle.infoDictionary
        (info?.get("CFBundleShortVersionString") as? String) ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }
}
