package ru.aiss83.comunalexpenses2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "resources_records")
data class ResourceData(

    @PrimaryKey
    @ColumnInfo(name="recordId")
    val id: UUID = UUID.randomUUID(),    // Resources record ID

    @ColumnInfo(name = "date")
    var date: Date = Date(),  // Record creation date

    @ColumnInfo(name = "waterHot")
    var hotWater : Long = 0,

    @ColumnInfo(name = "waterCold")
    var coldWater: Long = 0,

    @ColumnInfo(name = "electricityDay")
    var dayElectricity: Long = 0,

    @ColumnInfo(name = "electricityNight")
    var nightElectricity: Long = 0
)