package ru.aiss83.comunalexpenses2.data

/**
 * Domain model for user settings (address information).
 */
data class SettingsData(
    val street: String = "",
    val house: Int = 0,
    val flat: Int = 0
) {
    /**
     * Returns true if at least one field is populated.
     */
    fun isNotEmpty(): Boolean = street.isNotBlank() || house > 0 || flat > 0

    /**
     * Returns a formatted address string for display.
     */
    fun formatAddress(): String {
        val parts = mutableListOf<String>()
        if (street.isNotBlank()) parts += street
        if (house > 0) parts += "д. $house"
        if (flat > 0) parts += "кв. $flat"
        return parts.joinToString(", ")
    }
}
