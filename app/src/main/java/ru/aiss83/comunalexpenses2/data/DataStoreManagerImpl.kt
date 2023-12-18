package ru.aiss83.comunalexpenses2.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManagerImpl(
    private val dataStore: DataStore<Preferences>): DataStoreManager {

    private object PreferencesKeys {
        val STREET_KEY = stringPreferencesKey("street_line")
        val HOUSE_NUMBER_KEY = intPreferencesKey("house_number_line")
        val FLAT_NUMBER_KEY = intPreferencesKey("flat_number_key")
    }

    override suspend fun saveSettings(data: SettingsData) {
        /* We save all settings at once */
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STREET_KEY] = data.street
            preferences[PreferencesKeys.HOUSE_NUMBER_KEY] = data.house
            preferences[PreferencesKeys.FLAT_NUMBER_KEY] = data.flat
        }
    }

    override val settingsData: Flow<SettingsData> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                mapPreferences(preferences)
            }

    private fun mapPreferences(preferences: Preferences): SettingsData {
        val street = preferences[PreferencesKeys.STREET_KEY] ?: ""
        val house = preferences[PreferencesKeys.HOUSE_NUMBER_KEY] ?: 0
        val flat = preferences[PreferencesKeys.FLAT_NUMBER_KEY] ?: 0

        return SettingsData(street, house, flat)
    }
}