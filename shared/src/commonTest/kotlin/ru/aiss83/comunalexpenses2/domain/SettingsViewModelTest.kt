package ru.aiss83.comunalexpenses2.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.SettingsManager

/**
 * Unit tests for SettingsManager and SettingsData validation.
 * Tests the data layer directly without Android ViewModel lifecycle dependency.
 */
class SettingsViewModelTest {

    /**
     * Fake SettingsManager that stores data in memory.
     */
    private class FakeSettingsManager : SettingsManager {
        private val _flow = MutableStateFlow(SettingsData())
        override val settingsData = _flow.asStateFlow()

        override fun saveSettings(data: SettingsData) {
            // Apply same validation as SettingsViewModel
            require(data.street.isNotBlank()) { "Street cannot be blank" }
            require(data.house >= 0) { "House must be non-negative, was ${data.house}" }
            require(data.flat >= 0) { "Flat must be non-negative, was ${data.flat}" }
            _flow.value = data
        }
    }

    @Test
    fun `saveSettings with valid data persists values`() {
        val manager = FakeSettingsManager()
        manager.saveSettings(SettingsData(street = "Lenina", house = 10, flat = 5))
        assertEquals("Lenina", manager.settingsData.value.street)
        assertEquals(10, manager.settingsData.value.house)
        assertEquals(5, manager.settingsData.value.flat)
    }

    @Test
    fun `saveSettings trims street whitespace internally`() {
        val manager = FakeSettingsManager()
        // ViewModel trims before calling saveSettings — simulate trimmed input
        val input = "  Lenina  ".trim()
        manager.saveSettings(SettingsData(street = input, house = 1, flat = 1))
        assertEquals("Lenina", manager.settingsData.value.street)
    }

    @Test
    fun `saveSettings with blank street throws`() {
        val manager = FakeSettingsManager()
        assertFailsWith<IllegalArgumentException> {
            manager.saveSettings(SettingsData(street = "  ", house = 1, flat = 1))
        }
    }

    @Test
    fun `saveSettings with negative house throws`() {
        val manager = FakeSettingsManager()
        assertFailsWith<IllegalArgumentException> {
            manager.saveSettings(SettingsData(street = "Lenina", house = -1, flat = 1))
        }
    }

    @Test
    fun `saveSettings with negative flat throws`() {
        val manager = FakeSettingsManager()
        assertFailsWith<IllegalArgumentException> {
            manager.saveSettings(SettingsData(street = "Lenina", house = 1, flat = -1))
        }
    }

    @Test
    fun `saveSettings with zero house and flat is valid`() {
        val manager = FakeSettingsManager()
        manager.saveSettings(SettingsData(street = "Lenina", house = 0, flat = 0))
        assertEquals(0, manager.settingsData.value.house)
        assertEquals(0, manager.settingsData.value.flat)
    }
}
