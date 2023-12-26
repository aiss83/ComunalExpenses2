package ru.aiss83.comunalexpenses2.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

enum class SettingsDataSource {
    PREFERENCES_DATA_STORE
}
class DataStoreManagerFactory {
    fun create(source: SettingsDataSource, dataStore: DataStore<Preferences>): DataStoreManager {
        when (source) {
            SettingsDataSource.PREFERENCES_DATA_STORE -> return DataStoreManagerImpl(dataStore)
            else -> throw IllegalArgumentException("Unknown DataSource object")
        }

    }
}