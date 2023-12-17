package ru.aiss83.comunalexpenses2.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import java.util.prefs.Preferences

class DataStoreManagerImpl(
    private val settingsPreferencesStore: DataStore<Preferences>): DataStoreManager {

    private val STREET_KEY = "Street"
    override suspend fun saveSettings(data: SettingsData) {
//        settingsPreferencesStore.updateData { settingsPreferences ->
//            settingsPreferences[STREET_KEY] = data.street
//            settingsPreferences[]
//        }
    }

    override fun getSettings(): Flow<SettingsData> {
        TODO("Not yet implemented")
    }
}