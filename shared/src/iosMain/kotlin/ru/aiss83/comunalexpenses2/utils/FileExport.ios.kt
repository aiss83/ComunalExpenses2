package ru.aiss83.comunalexpenses2.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController

/**
 * iOS implementation: saves JSON to Documents and shares via UIActivityViewController.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun exportAndShareJson(json: String, filename: String, title: String) {
    try {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDir = paths.firstOrNull() as? String ?: return
        val filePath = "$documentsDir/$filename"

        val nsString: NSString = NSString.create(string = json)
        nsString.writeToFile(
            path = filePath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )

        // Share the file
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )
        presentShareSheet(activityVC)
    } catch (_: Exception) {
        // Fallback: share as text
        shareText(json, title)
    }
}
