package ru.aiss83.comunalexpenses2.data

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

/**
 * Repository for ResourceData operations.
 * Provides a clean API for the ViewModel to interact with the database.
 */
class ResourceDataRepository(private val dao: ResourceDataDao) {

    /**
     * Get all resources data as a reactive flow.
     */
    fun getAllResourcesData(): Flow<List<ResourceData>> = dao.getAllResourceData()

    /**
     * Get a single resource data record by ID.
     */
    fun getResourcesData(id: Uuid): Flow<ResourceData?> = dao.findResourceData(id)

    /**
     * Find resources data by date.
     */
    fun findResourcesData(date: Long): Flow<ResourceData?> = dao.findResourceDataByDate(date)

    /**
     * Insert a new resource data record.
     */
    suspend fun insertResourcesData(data: ResourceData) {
        dao.addResourceData(data)
    }

    /**
     * Update an existing resource data record.
     */
    suspend fun updateResourcesData(data: ResourceData) {
        dao.updateResourceData(data)
    }

    /**
     * Delete a resource data record by ID.
     */
    suspend fun deleteResourcesData(id: Uuid) {
        dao.deleteResourceData(id)
    }
}
