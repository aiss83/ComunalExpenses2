package ru.aiss83.comunalexpenses2.data

import android.content.Context

/**
 * Android implementation: reads versionName from PackageInfo.
 */
actual fun getAppVersion(): String {
    val context = AppContextHolder.context ?: return "1.0"
    return try {
        val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pkgInfo.versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }
}
