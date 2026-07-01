package ru.aiss83.comunalexpenses2.utils

/**
 * Saves raw bytes to a file on the device.
 * Android: Downloads folder via MediaStore.
 * iOS: NSDocumentDirectory.
 */
expect fun saveJsonToFile(bytes: ByteArray, filename: String)
