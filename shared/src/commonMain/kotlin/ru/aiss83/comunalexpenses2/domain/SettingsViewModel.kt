package ru.aiss83.comunalexpenses2.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.SettingsManager
import ru.aiss83.comunalexpenses2.data.ThemeMode

/**
 * ViewModel for managing user settings.
 *
 * Settings are loaded from persistent storage and exposed as a StateFlow.
 * Changes are written immediately to storage and broadcast to collectors.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    /**
     * Settings flow. Starts eager so the UI gets cached values immediately.
     */
    val settingsData: StateFlow<SettingsData> = settingsManager.settingsData
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsData())

    /**
     * Save settings with validation.
     *
     * @throws IllegalArgumentException if street is blank or house/flat are negative
     */
    fun saveSettings(
        street: String,
        house: Int,
        flat: Int,
        shareTemplate: String = SettingsData.DEFAULT_SHARE_TEMPLATE,
        themeMode: ThemeMode? = null
    ) {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(house >= 0) { "House must be non-negative, was $house" }
        require(flat >= 0) { "Flat must be non-negative, was $flat" }

        val current = settingsData.value
        settingsManager.saveSettings(
            SettingsData(
                street = street.trim(),
                house = house,
                flat = flat,
                shareTemplate = shareTemplate,
                themeMode = themeMode ?: current.themeMode
            )
        )
    }

    /**
     * Set theme mode only — no address validation needed, immediate apply.
     */
    fun setThemeMode(mode: ThemeMode) {
        val current = settingsData.value
        settingsManager.saveSettings(current.copy(themeMode = mode))
    }
}
