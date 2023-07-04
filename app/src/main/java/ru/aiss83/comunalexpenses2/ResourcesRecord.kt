package ru.aiss83.comunalexpenses2

import java.util.Date

data class ResourcesRecord(val date: Date,
                           val hotWater: Number,
                           val coldWater: Number,
                           val kWhDay: Number,
                           val kWhNight: Number)
