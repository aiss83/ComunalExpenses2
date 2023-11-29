package ru.aiss83.comunalexpenses2

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ResourcesDataViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResourcesDataViewModel::class.java)) {
            return ResourcesDataViewModel(application) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel!")
    }
}