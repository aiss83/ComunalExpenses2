package ru.aiss83.comunalexpenses2.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "resources_records")
class ResourceData {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name="recordId")
    var id: Long = 0    // Resources record ID

    @ColumnInfo(name = "date")
    var date: Date? = null  // Record creation date

    @ColumnInfo(name = "waterHot")
    var hotWater : Number = 0

    @ColumnInfo(name = "waterCold")
    var coldWater: Number = 0

    @ColumnInfo(name = "electricityDay")
    var dayElectricity: Number = 0

    @ColumnInfo(name = "electricityNight")
    var nightElectricity: Number = 0
}