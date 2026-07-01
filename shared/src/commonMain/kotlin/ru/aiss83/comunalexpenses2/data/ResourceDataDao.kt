package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extension function to convert SQLDelight record to domain model.
 */
@OptIn(ExperimentalUuidApi::class)
private fun Resources_records.toResourceData(): ResourceData {
    return ResourceData(
        id = Uuid.parse(recordId),
        date = date,
        hotWater = waterHot,
        coldWater = waterCold,
        dayElectricity = electricityDay,
        nightElectricity = electricityNight,
        shared = shared != 0L
    )
}

/**
 * DAO-like wrapper for ResourceData operations using SQLDelight.
 */
@OptIn(ExperimentalUuidApi::class)
class ResourceDataDao(private val database: ResourcesDatabase) {

    private val queries = database.resourcesRecordsQueries

    /**
     * Get all resources data ordered by date descending.
     */
    fun getAllResourceData(): Flow<List<ResourceData>> {
        return queries
            .getAllResourceData()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { records ->
                records.map { it.toResourceData() }
            }
    }

    /**
     * Find resource data by ID.
     */
    fun findResourceData(recordId: Uuid): Flow<ResourceData?> {
        return queries
            .findResourceDataById(recordId.toString())
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toResourceData() }
    }

    /**
     * Find resource data by date (epoch milliseconds).
     */
    fun findResourceDataByDate(dataDate: Long): Flow<ResourceData?> {
        return queries
            .findResourceDataByDate(dataDate)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toResourceData() }
    }

    /**
     * Insert a new resource data record.
     */
    fun addResourceData(data: ResourceData) {
        queries.insertResourceData(
            recordId = data.id.toString(),
            date = data.date,
            waterHot = data.hotWater,
            waterCold = data.coldWater,
            electricityDay = data.dayElectricity,
            electricityNight = data.nightElectricity,
            shared = if (data.shared) 1L else 0L
        )
    }

    /**
     * Update an existing resource data record.
     */
    fun updateResourceData(data: ResourceData) {
        queries.updateResourceData(
            date = data.date,
            waterHot = data.hotWater,
            waterCold = data.coldWater,
            electricityDay = data.dayElectricity,
            electricityNight = data.nightElectricity,
            shared = if (data.shared) 1L else 0L,
            recordId = data.id.toString()
        )
    }

    /**
     * Mark a record as shared.
     */
    fun markAsShared(id: Uuid) {
        queries.markAsShared(id.toString())
    }

    /**
     * Delete a resource data record by ID.
     */
    fun deleteResourceData(id: Uuid) {
        queries.deleteResourceData(id.toString())
    }

    /**
     * Delete all resource data records.
     */
    fun deleteAllResourceData() {
        queries.deleteAllResourceData()
    }
}
