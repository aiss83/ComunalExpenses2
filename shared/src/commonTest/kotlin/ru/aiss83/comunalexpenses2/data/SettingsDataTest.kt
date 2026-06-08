package ru.aiss83.comunalexpenses2.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SettingsData — keyword substitution, validation, formatting.
 */
class SettingsDataTest {

    @Test
    fun `applyShareTemplate substitutes all keywords`() {
        val template = "Day: \$day_e, Night: \$night_e, Cold: \$cold_w, Hot: \$hot_w, Flat: \$flat"
        val result = SettingsData.applyShareTemplate(
            template = template,
            dayElectricity = 100,
            nightElectricity = 50,
            coldWater = 200,
            hotWater = 75,
            flatNumber = 12
        )
        assertEquals("Day: 100, Night: 50, Cold: 200, Hot: 75, Flat: 12", result)
    }

    @Test
    fun `applyShareTemplate handles zero values`() {
        val template = "\$day_e/\$night_e/\$cold_w/\$hot_w/\$flat"
        val result = SettingsData.applyShareTemplate(
            template = template,
            dayElectricity = 0,
            nightElectricity = 0,
            coldWater = 0,
            hotWater = 0,
            flatNumber = 0
        )
        assertEquals("0/0/0/0/0", result)
    }

    @Test
    fun `applyShareTemplate uses default template when none provided`() {
        val result = SettingsData.applyShareTemplate(
            template = SettingsData.DEFAULT_SHARE_TEMPLATE,
            dayElectricity = 10,
            nightElectricity = 20,
            coldWater = 30,
            hotWater = 40,
            flatNumber = 5
        )
        assertTrue(result.contains("10"))
        assertTrue(result.contains("20"))
        assertTrue(result.contains("30"))
        assertTrue(result.contains("40"))
        assertTrue(result.contains("5"))
    }

    @Test
    fun `applyShareTemplate ignores unknown keywords`() {
        val template = "Unknown: \$unknown_kw, Day: \$day_e"
        val result = SettingsData.applyShareTemplate(
            template = template,
            dayElectricity = 42,
            nightElectricity = 0,
            coldWater = 0,
            hotWater = 0,
            flatNumber = 1
        )
        assertEquals("Unknown: \$unknown_kw, Day: 42", result)
    }

    @Test
    fun `isNotEmpty returns false for default settings`() {
        val settings = SettingsData()
        assertFalse(settings.isNotEmpty())
    }

    @Test
    fun `isNotEmpty returns true when street is set`() {
        val settings = SettingsData(street = "Lenina")
        assertTrue(settings.isNotEmpty())
    }

    @Test
    fun `isNotEmpty returns true when house is set`() {
        val settings = SettingsData(house = 42)
        assertTrue(settings.isNotEmpty())
    }

    @Test
    fun `isNotEmpty returns true when flat is set`() {
        val settings = SettingsData(flat = 15)
        assertTrue(settings.isNotEmpty())
    }

    @Test
    fun `formatAddress shows all fields when populated`() {
        val settings = SettingsData(street = "Lenina", house = 10, flat = 5)
        val address = settings.formatAddress()
        assertTrue(address.contains("Lenina"))
        assertTrue(address.contains("10"))
        assertTrue(address.contains("5"))
    }

    @Test
    fun `formatAddress skips empty fields`() {
        val settings = SettingsData(street = "Lenina", house = 0, flat = 0)
        val address = settings.formatAddress()
        assertEquals("Lenina", address)
    }

    @Test
    fun `formatAddress returns empty string for default settings`() {
        val settings = SettingsData()
        assertEquals("", settings.formatAddress())
    }
}
