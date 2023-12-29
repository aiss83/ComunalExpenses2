package ru.aiss83.comunalexpenses2

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aiss83.comunalexpenses2.data.DataStoreManager
import ru.aiss83.comunalexpenses2.data.SettingsData

class SettingsViewModel(private val dataStore: DataStoreManager): ViewModel() {

    val settingsData = dataStore.settingsData
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun saveSettings(street: String, house: Int, flat: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            dataStore.saveSettings(SettingsData(street, house, flat))
        }
    }
}