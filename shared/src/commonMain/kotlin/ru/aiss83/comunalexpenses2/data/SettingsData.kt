package ru.aiss83.comunalexpenses2.data

/**
 * Domain model for user settings (address information).
 */
data class SettingsData(
    val street: String = "",
    val house: Int = 0,
    val flat: Int = 0
)
