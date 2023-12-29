package ru.aiss83.comunalexpenses2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.aiss83.comunalexpenses2.data.DataStoreManager

class SettingsViewModelFactory(private val dataStoreManager: DataStoreManager) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(dataStoreManager) as T
            } else
                throw IllegalArgumentException("Unknown ViewModel!")
        }
}