package ru.aiss83.comunalexpenses2.utils

/**
 * Platform-specific share function.
 * Shares text via native share sheet (Android) / activity view controller (iOS).
 */
expect fun shareText(text: String, title: String)
