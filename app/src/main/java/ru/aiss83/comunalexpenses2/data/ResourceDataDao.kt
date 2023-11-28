package ru.aiss83.comunalexpenses2.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.Date
import java.util.UUID

@Dao
interface ResourceDataDao {

    /**
     * Get all available resources data
     */
    @Query("SELECT * FROM resources_records ORDER BY date DESC")
    fun getAllResourceData() : LiveData<List<ResourceData>>

    /**
     * Find specified resources data record by it's Id
     * @param recordId Id of data resource record
     */
    @Query("SELECT * FROM resources_records WHERE recordId = (:recordId)")
    fun findResourceData(recordId: UUID): LiveData<ResourceData?>

    /**
     * Find resources data record specified by date
     * @param dataDate Date of data issued
     */
    @Query("SELECT * FROM resources_records WHERE date = :dataDate")
    fun findResourceData(dataDate: Date): LiveData<ResourceData?>

    /**
     * Add resources data record to a database
     */
    @Insert
    fun addResourceData(data: ResourceData)

    /**
     * Delete resources data record from database
     */
    @Delete
    fun deleteResourceData(data: ResourceData)

    /**
     * Update resources data record in database
     */
    @Update
    fun updateResourceData(data: ResourceData)
}