package ru.aiss83.comunalexpenses2.data

import kotlinx.coroutines.flow.Flow

interface DataStoreManager {
    suspend fun saveSettings(data: SettingsData)

    val settingsData: Flow<SettingsData>
}