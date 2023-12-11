package ru.aiss83.comunalexpenses2

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.annotation.DisplayContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.ResourcesDataRepository
import ru.aiss83.comunalexpenses2.data.ResourcesDataRoomDatabase
import java.util.UUID

class ResourcesDataViewModel(application: Application) : ViewModel() {
    val allResourcesData : LiveData<List<ResourceData>>
    private val repository: ResourcesDataRepository
    private val appContext = application.applicationContext

    init {
        val recordsDb = ResourcesDataRoomDatabase.getInstance(application)
        val recordsDao = recordsDb.resourcesDataDao()
        repository = ResourcesDataRepository(resourceDataDao = recordsDao)

        allResourcesData = repository.getAllResourcesData()
    }

    fun addResourcesData(data: ResourceData) {
        repository.insertResourcesData(data)
    }

    fun updateResourceData(data: ResourceData) {
        repository.updateResourcesData(data)
    }

    fun deleteResourceData(id: UUID) {
        repository.deleteResourcesData(id)
    }

    fun shareResourceData(id: UUID) {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            Toast.makeText(appContext, "Ready to share ${id.toString()}", Toast.LENGTH_LONG).show()
        }
    }
}