package ru.aiss83.comunalexpenses2.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.create
import platform.Foundation.writeToFile

/**
 * iOS implementation: saves JSON bytes to Documents directory.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun saveJsonToFile(bytes: ByteArray, filename: String) {
    try {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDir = paths.firstOrNull() as? String ?: return
        val filePath = "$documentsDir/$filename"

        val json = NSString.create(string = bytes.decodeToString())
        json.writeToFile(
            path = filePath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    } catch (_: Exception) {
        // fail silently
    }
}
