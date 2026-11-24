package ru.aiss83.comunalexpenses2.domain

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.SettingsManager
import ru.aiss83.comunalexpenses2.data.ThemeMode

/**
 * Result of saving settings — success or validation error.
 */
sealed class SaveSettingsResult {
    data object Success : SaveSettingsResult()
    data class Error(val message: String) : SaveSettingsResult()
}

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
     * Settings flow. The manager already exposes a StateFlow holding the persisted values,
     * so the first composition sees real data (no default-value flash that used to reset
     * the input fields when the theme changed).
     */
    val settingsData: StateFlow<SettingsData> = settingsManager.settingsData

    private val _saveResult = MutableStateFlow<SaveSettingsResult?>(null)
    val saveResult: StateFlow<SaveSettingsResult?> = _saveResult.asStateFlow()

    /**
     * Save settings with validation.
     * Returns result via [saveResult] StateFlow — never throws.
     */
    fun saveSettings(
        street: String,
        house: Int,
        flat: Int,
        shareTemplate: String = SettingsData.DEFAULT_SHARE_TEMPLATE,
        themeMode: ThemeMode? = null
    ) {
        val trimmedStreet = street.trim()
        if (trimmedStreet.isBlank()) {
            _saveResult.value = SaveSettingsResult.Error("Street cannot be blank")
            return
        }
        if (house < 0) {
            _saveResult.value = SaveSettingsResult.Error("House must be non-negative, was $house")
            return
        }
        if (flat < 0) {
            _saveResult.value = SaveSettingsResult.Error("Flat must be non-negative, was $flat")
            return
        }

        val current = settingsData.value
        settingsManager.saveSettings(
            SettingsData(
                street = trimmedStreet,
                house = house,
                flat = flat,
                shareTemplate = shareTemplate,
                themeMode = themeMode ?: current.themeMode
            )
        )
        _saveResult.value = SaveSettingsResult.Success
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    /**
     * Set theme mode only — no address validation needed, immediate apply.
     */
    fun setThemeMode(mode: ThemeMode) {
        val current = settingsData.value
        settingsManager.saveSettings(current.copy(themeMode = mode))
    }
}
