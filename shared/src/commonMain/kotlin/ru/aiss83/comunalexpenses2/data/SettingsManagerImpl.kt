package ru.aiss83.comunalexpenses2.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Interface for settings storage using multiplatform-settings.
 */
interface SettingsManager {
    val settingsData: Flow<SettingsData>

    suspend fun saveSettings(data: SettingsData)
}

/**
 * Common implementation of SettingsManager using multiplatform-settings.
 * Wraps synchronous Settings API in a Flow-based interface.
 */
class SettingsManagerImpl(private val settings: Settings) : SettingsManager {

    companion object {
        private const val KEY_STREET = "street_line"
        private const val KEY_HOUSE = "house_number_line"
        private const val KEY_FLAT = "flat_number_key"
    }

    private val _settingsData = MutableStateFlow(getCurrentSettings())
    override val settingsData: Flow<SettingsData> = _settingsData.asStateFlow()

    private fun getCurrentSettings(): SettingsData {
        return SettingsData(
            street = settings.getString(KEY_STREET, ""),
            house = settings.getInt(KEY_HOUSE, 0),
            flat = settings.getInt(KEY_FLAT, 0)
        )
    }

    override suspend fun saveSettings(data: SettingsData) {
        settings.putString(KEY_STREET, data.street)
        settings.putInt(KEY_HOUSE, data.house)
        settings.putInt(KEY_FLAT, data.flat)
        // Notify observers about the change
        _settingsData.value = data
    }
}
