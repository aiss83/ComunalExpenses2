package ru.aiss83.comunalexpenses2.data

/**
 * Domain model for user settings (address information).
 */
data class SettingsData(
    val street: String = "",
    val house: Int = 0,
    val flat: Int = 0,
    val shareTemplate: String = DEFAULT_SHARE_TEMPLATE
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
         *
         * @param dayElectricity Day electricity reading
         * @param nightElectricity Night electricity reading
         * @param coldWater Cold water reading
         * @param hotWater Hot water reading
         * @param flatNumber User's flat number from settings
         * @return Processed string with keywords replaced
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

    /**
     * Returns true if at least one field is populated.
     */
    fun isNotEmpty(): Boolean = street.isNotBlank() || house > 0 || flat > 0 || shareTemplate != DEFAULT_SHARE_TEMPLATE

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
