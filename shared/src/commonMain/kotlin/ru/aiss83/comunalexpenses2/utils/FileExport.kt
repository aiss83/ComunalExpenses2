package ru.aiss83.comunalexpenses2.utils

/**
 * Platform-specific: saves JSON to file and shares it via system share sheet.
 */
expect fun exportAndShareJson(json: String, filename: String, title: String)
