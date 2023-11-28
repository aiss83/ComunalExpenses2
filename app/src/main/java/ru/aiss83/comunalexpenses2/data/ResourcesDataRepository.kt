package ru.aiss83.comunalexpenses2.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class ResourcesDataRepository(private val resourceDataDao: ResourceDataDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertResourcesData(data: ResourceData) {
        coroutineScope.launch(Dispatchers.IO) {
            resourceDataDao.addResourceData(data)
        }
    }

    fun deleteResourcesData(data: ResourceData) {
        coroutineScope.launch(Dispatchers.IO) {
            resourceDataDao.deleteResourceData(data)
        }
    }

    fun updateResourcesData(data: ResourceData) {
        coroutineScope.launch(Dispatchers.IO) {
            resourceDataDao.updateResourceData(data)
        }
    }

    fun findResourcesData(date: Date): LiveData<ResourceData?> =
        resourceDataDao.findResourceData(date)

    fun getResourcesData(id: UUID): LiveData<ResourceData?> =
        resourceDataDao.findResourceData(id)

    fun getAllResourcesData(): LiveData<List<ResourceData>> =
        resourceDataDao.getAllResourceData()

}