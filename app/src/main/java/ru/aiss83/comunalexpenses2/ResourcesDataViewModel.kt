package ru.aiss83.comunalexpenses2

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.ResourcesDataRepository
import ru.aiss83.comunalexpenses2.data.ResourcesDataRoomDatabase
import java.util.UUID

class ResourcesDataViewModel(application: Application) : ViewModel() {
    val allResourcesData : LiveData<List<ResourceData>>
    private val repository: ResourcesDataRepository

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
        /* TODO: implement delete by UUID method in repository */
    }
}