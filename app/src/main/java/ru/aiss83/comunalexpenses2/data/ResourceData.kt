package ru.aiss83.comunalexpenses2.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Entity(tableName = "resources_records")
@Parcelize
data class ResourceData (

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
) : Parcelable
