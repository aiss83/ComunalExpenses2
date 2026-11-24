package ru.aiss83.comunalexpenses2.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** Length of a day in milliseconds. Day keys are UTC midnights, so this is always exact. */
const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

/**
 * Converts a real instant to the **day key** stored in the database: UTC midnight of the local
 * calendar date that contains this instant.
 *
 * This is the unit of the domain: a record is "the readings of one day", and exactly one
 * record may exist per day (enforced by a UNIQUE index). Using UTC midnight keeps the key
 * time-zone independent — the same record keeps rendering as the same calendar date after a
 * time-zone change, unlike a stored local-midnight instant.
 *
 * Do **not** apply this twice: for negative UTC offsets an existing key is still the previous
 * local day. Use [normalizeToDayKey] when the input may already be a key.
 */
fun Long.toDayKey(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(timeZone)
        .date
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()

/**
 * Formats a day key as "dd.MM.yyyy".
 * Formatted in UTC on purpose: the key already encodes the calendar date.
 */
fun Long.formatDayKey(): String {
    val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    return "$day.$month.${localDateTime.year}"
}

/**
 * Normalises a value that may be either a real instant (legacy records, old backups) or an
 * already normalised day key. Day keys are exact multiples of [MILLIS_PER_DAY].
 */
fun Long.normalizeToDayKey(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    if (this % MILLIS_PER_DAY == 0L) this else toDayKey(timeZone)

/** Exclusive upper bound of the day key's range: the next day's key. */
fun Long.dayEndExclusive(): Long = this + MILLIS_PER_DAY

/**
 * Date-range predicate used by the home screen filter.
 * [fromDayKey] is inclusive, [toDayEndExclusive] is exclusive; `null` means "unbounded".
 */
fun Long.isInDayKeyRange(fromDayKey: Long?, toDayEndExclusive: Long?): Boolean {
    val afterStart = fromDayKey == null || this >= fromDayKey
    val beforeEnd = toDayEndExclusive == null || this < toDayEndExclusive
    return afterStart && beforeEnd
}
