package ru.aiss83.comunalexpenses2.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.SettingsManager

/**
 * ViewModel for managing user settings.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _settingsData = MutableStateFlow(SettingsData("", 0, 0))
    val settingsData: StateFlow<SettingsData> = _settingsData

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.settingsData.collect { data ->
                _settingsData.value = data
            }
        }
    }

    fun saveSettings(street: String, house: Int, flat: Int) {
        viewModelScope.launch {
            val settingsData = SettingsData(street, house, flat)
            settingsManager.saveSettings(settingsData)
        }
    }

    fun clearError() {
        // No-op for now
    }
}
