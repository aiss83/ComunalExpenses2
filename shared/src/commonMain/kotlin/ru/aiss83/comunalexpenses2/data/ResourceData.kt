package ru.aiss83.comunalexpenses2.data

import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.aiss83.comunalexpenses2.utils.formatEpochMillis

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
    val nightElectricity: Long = 0,
    val shared: Boolean = false
) {
    /** Format date as "dd.MM.yyyy" using shared utility. */
    fun formatDate(): String = date.formatEpochMillis()
}
