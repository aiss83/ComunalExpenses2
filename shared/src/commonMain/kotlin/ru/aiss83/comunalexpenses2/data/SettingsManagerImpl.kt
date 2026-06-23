package ru.aiss83.comunalexpenses2.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface for settings storage using multiplatform-settings.
 */
interface SettingsManager {
    val settingsData: Flow<SettingsData>
    fun saveSettings(data: SettingsData)
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
        private const val KEY_SHARE_TEMPLATE = "share_template"
        private const val KEY_THEME_MODE = "theme_mode"
    }

    private val _settingsData = MutableStateFlow(getCurrentSettings())
    override val settingsData: Flow<SettingsData> = _settingsData.asStateFlow()

    private fun getCurrentSettings(): SettingsData {
        val themeModeStr = settings.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        return SettingsData(
            street = settings.getString(KEY_STREET, ""),
            house = settings.getInt(KEY_HOUSE, 0),
            flat = settings.getInt(KEY_FLAT, 0),
            shareTemplate = settings.getString(KEY_SHARE_TEMPLATE, SettingsData.DEFAULT_SHARE_TEMPLATE),
            themeMode = themeMode
        )
    }

    override fun saveSettings(data: SettingsData) {
        settings.putString(KEY_STREET, data.street)
        settings.putInt(KEY_HOUSE, data.house)
        settings.putInt(KEY_FLAT, data.flat)
        settings.putString(KEY_SHARE_TEMPLATE, data.shareTemplate)
        settings.putString(KEY_THEME_MODE, data.themeMode.name)
        _settingsData.value = data
    }
}
