package ru.aiss83.comunalexpenses2.data

import com.russhwolf.settings.Settings

/**
 * Expected function to create platform-specific Settings.
 */
expect fun createSettings(): Settings

/**
 * Factory function to create SettingsManager.
 */
fun createSettingsManager(): SettingsManager {
    return SettingsManagerImpl(createSettings())
}
