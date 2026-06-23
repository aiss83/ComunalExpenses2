package ru.aiss83.comunalexpenses2.data

/**
 * Application theme mode.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Domain model for user settings (address information and preferences).
 */
data class SettingsData(
    val street: String = "",
    val house: Int = 0,
    val flat: Int = 0,
    val shareTemplate: String = DEFAULT_SHARE_TEMPLATE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    companion object {
        /**
         * Default template with all available keywords.
         * Keywords: $day_e, $night_e, $cold_w, $hot_w, $flat
         */
        const val DEFAULT_SHARE_TEMPLATE =
            "Electricity: day \$day_e, night \$night_e\n" +
            "Water: cold \$cold_w, hot \$hot_w\n" +
            "Flat: \$flat"

        /**
         * Applies keyword substitution to the template using actual record values.
         */
        fun applyShareTemplate(
            template: String,
            dayElectricity: Long,
            nightElectricity: Long,
            coldWater: Long,
            hotWater: Long,
            flatNumber: Int
        ): String {
            return template
                .replace("\$day_e", dayElectricity.toString())
                .replace("\$night_e", nightElectricity.toString())
                .replace("\$cold_w", coldWater.toString())
                .replace("\$hot_w", hotWater.toString())
                .replace("\$flat", flatNumber.toString())
        }
    }

    fun isNotEmpty(): Boolean =
        street.isNotBlank() || house > 0 || flat > 0 || shareTemplate != DEFAULT_SHARE_TEMPLATE

    fun formatAddress(): String {
        val parts = mutableListOf<String>()
        if (street.isNotBlank()) parts += street
        if (house > 0) parts += "д. $house"
        if (flat > 0) parts += "кв. $flat"
        return parts.joinToString(", ")
    }
}
