package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extension function to convert a SQLDelight record to the domain model.
 *
 * Returns `null` for a row whose `recordId` is not a valid UUID (corrupted or foreign data):
 * one bad row must not take down the whole list flow, which previously threw and left the UI
 * with an error and no data.
 */
@OptIn(ExperimentalUuidApi::class)
private fun Resources_records.toResourceData(): ResourceData? {
    val id = runCatching { Uuid.parse(recordId) }.getOrNull() ?: return null
    return ResourceData(
        id = id,
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
                records.mapNotNull { it.toResourceData() }
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
    suspend fun addResourceData(data: ResourceData) = withContext(Dispatchers.IO) {
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
    suspend fun updateResourceData(data: ResourceData) = withContext(Dispatchers.IO) {
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
    suspend fun markAsShared(id: Uuid) = withContext(Dispatchers.IO) {
        queries.markAsShared(id.toString())
    }

    /**
     * Delete a resource data record by ID.
     */
    suspend fun deleteResourceData(id: Uuid) = withContext(Dispatchers.IO) {
        queries.deleteResourceData(id.toString())
    }

    /**
     * Delete all resource data records.
     */
    suspend fun deleteAllResourceData() = withContext(Dispatchers.IO) {
        queries.deleteAllResourceData()
    }

    /**
     * Bulk-import records in a single transaction for atomicity.
     *
     * Uses `INSERT OR REPLACE` so re-importing the same backup is idempotent instead of
     * failing with a primary-key violation and aborting the whole transaction.
     */
    suspend fun importRecords(records: List<ResourceData>) = withContext(Dispatchers.IO) {
        database.transaction {
            for (data in records) {
                queries.insertOrReplaceResourceData(
                    recordId = data.id.toString(),
                    date = data.date,
                    waterHot = data.hotWater,
                    waterCold = data.coldWater,
                    electricityDay = data.dayElectricity,
                    electricityNight = data.nightElectricity,
                    shared = if (data.shared) 1L else 0L
                )
            }
        }
    }
}
