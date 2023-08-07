package ru.aiss83.comunalexpenses2.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ResourcesRecord(val date: Date = android.icu.util.Calendar.getInstance().time,
                           val hotWater: Number = 0,
                           val coldWater: Number = 0,
                           val kWhDay: Number = 0,
                           val kWhNight: Number = 0) : Parcelable {
}
