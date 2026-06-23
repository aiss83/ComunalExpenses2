package ru.aiss83.comunalexpenses2.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for utility meter readings.
 * Represents a single record of resource consumption (water, electricity).
 */
@OptIn(ExperimentalUuidApi::class)
data class ResourceData(
    val id: Uuid = Uuid.random(),
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val hotWater: Long = 0,
    val coldWater: Long = 0,
    val dayElectricity: Long = 0,
    val nightElectricity: Long = 0
) {
    /**
     * Convert epoch milliseconds to a formatted date string.
     */
    fun formatDate(): String {
        val instant = Instant.fromEpochMilliseconds(date)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year
        return "$day.$month.$year"
    }
}
