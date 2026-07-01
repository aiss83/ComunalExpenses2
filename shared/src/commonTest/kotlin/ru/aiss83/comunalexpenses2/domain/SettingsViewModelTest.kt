package ru.aiss83.comunalexpenses2.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.SettingsManager

/**
 * Unit tests for SettingsManager and settings validation.
 * Tests the data layer directly — ViewModel can't be instantiated in commonTest.
 */
class SettingsViewModelTest {

    private class FakeSettingsManager : SettingsManager {
        private val _flow = MutableStateFlow(SettingsData())
        override val settingsData = _flow.asStateFlow()
        var lastSaved: SettingsData? = null

        override fun saveSettings(data: SettingsData) {
            _flow.value = data
            lastSaved = data
        }
    }

    // --- Validation logic (mirrors SettingsViewModel.saveSettings) ---

    private fun validateAndSave(manager: FakeSettingsManager, street: String, house: Int, flat: Int): String? {
        val trimmed = street.trim()
        if (trimmed.isBlank()) return "Street cannot be blank"
        if (house < 0) return "House must be non-negative, was $house"
        if (flat < 0) return "Flat must be non-negative, was $flat"
        manager.saveSettings(SettingsData(street = trimmed, house = house, flat = flat))
        return null
    }

    @Test
    fun `valid save persists data`() {
        val m = FakeSettingsManager()
        assertEquals(null, validateAndSave(m, "Lenina", 10, 5))
        assertEquals("Lenina", m.lastSaved?.street)
        assertEquals(10, m.lastSaved?.house)
    }

    @Test
    fun `trims street whitespace`() {
        val m = FakeSettingsManager()
        validateAndSave(m, "  Lenina  ", 1, 1)
        assertEquals("Lenina", m.lastSaved?.street)
    }

    @Test
    fun `blank street returns error`() {
        val error = validateAndSave(FakeSettingsManager(), "   ", 1, 1)
        assertTrue(error != null && error.contains("Street"))
    }

    @Test
    fun `empty street returns error`() {
        assertTrue(validateAndSave(FakeSettingsManager(), "", 1, 1) != null)
    }

    @Test
    fun `negative house returns error`() {
        val error = validateAndSave(FakeSettingsManager(), "Lenina", -1, 1)
        assertTrue(error != null && error.contains("House"))
    }

    @Test
    fun `negative flat returns error`() {
        val error = validateAndSave(FakeSettingsManager(), "Lenina", 1, -5)
        assertTrue(error != null && error.contains("Flat"))
    }

    @Test
    fun `zero house and flat is valid`() {
        assertEquals(null, validateAndSave(FakeSettingsManager(), "Lenina", 0, 0))
    }

    @Test
    fun `settings flow emits updates`() {
        val m = FakeSettingsManager()
        m.saveSettings(SettingsData(street = "Lenina", house = 1, flat = 1))
        assertEquals("Lenina", m.settingsData.value.street)
    }

    @Test
    fun `settings isNotEmpty detects content`() {
        assertFalse(SettingsData().isNotEmpty())
        assertTrue(SettingsData(street = "x").isNotEmpty())
        assertTrue(SettingsData(house = 1).isNotEmpty())
    }
}
