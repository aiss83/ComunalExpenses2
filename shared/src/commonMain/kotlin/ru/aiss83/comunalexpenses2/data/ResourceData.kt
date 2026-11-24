package ru.aiss83.comunalexpenses2.data

import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.aiss83.comunalexpenses2.utils.formatDayKey
import ru.aiss83.comunalexpenses2.utils.toDayKey

/**
 * Domain model for utility meter readings.
 *
 * Represents the readings of **one calendar day**: [date] is a day key (UTC midnight of the
 * local calendar date) and exactly one record may exist per day.
 */
@OptIn(ExperimentalUuidApi::class)
data class ResourceData(
    val id: Uuid = Uuid.random(),
    val date: Long = Clock.System.now().toEpochMilliseconds().toDayKey(),
    val hotWater: Long = 0,
    val coldWater: Long = 0,
    val dayElectricity: Long = 0,
    val nightElectricity: Long = 0,
    val shared: Boolean = false
) {
    /** Format the day key as "dd.MM.yyyy". */
    fun formatDate(): String = date.formatDayKey()
}
