package ru.aiss83.comunalexpenses2.utils

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the day-key model: a record's `date` is UTC midnight of its local calendar date,
 * and exactly one record may exist per day.
 */
class DateUtilsTest {

    private val yekaterinburg = TimeZone.of("Asia/Yekaterinburg") // UTC+5, no DST
    private val saoPaulo = TimeZone.of("America/Sao_Paulo") // UTC-3, no DST since 2019
    private val newYork = TimeZone.of("America/New_York") // DST on 2024-03-10

    /** Day key of 10 March 2024. */
    private val march10 = 1_710_028_800_000L

    @Test
    fun `positive offset instant maps to its local day`() {
        // 2024-03-10T02:00+05:00 == 2024-03-09T21:00Z, but locally it is still 10 March.
        assertEquals(march10, 1_710_018_000_000L.toDayKey(yekaterinburg))
    }

    @Test
    fun `negative offset instant maps to its local day`() {
        // 2024-03-10T00:30-03:00 == 2024-03-10T03:30Z
        assertEquals(march10, 1_710_041_400_000L.toDayKey(saoPaulo))
    }

    @Test
    fun `day key formats as its calendar date`() {
        // Formatting is UTC-based, so the key renders the same date on any device.
        assertEquals("10.03.2024", march10.formatDayKey())
    }

    @Test
    fun `normalising an existing day key is a no-op in any time zone`() {
        assertEquals(march10, march10.normalizeToDayKey(yekaterinburg))
        assertEquals(march10, march10.normalizeToDayKey(saoPaulo))
        assertEquals(march10, march10.normalizeToDayKey(newYork))
    }

    @Test
    fun `normalising a legacy instant yields the day key`() {
        // 2024-03-10T09:15Z — mid-day UTC, so it is the same local date in all real zones.
        assertEquals(march10, 1_710_062_100_000L.normalizeToDayKey(yekaterinburg))
        assertEquals(march10, 1_710_062_100_000L.normalizeToDayKey(saoPaulo))
    }

    @Test
    fun `dst transition day maps both sides to one key`() {
        // 2024-03-10 in New York is 23 hours long: 00:30 EST and 23:30 EDT.
        assertEquals(march10, 1_710_048_600_000L.toDayKey(newYork))
        assertEquals(march10, 1_710_127_800_000L.toDayKey(newYork))
    }

    @Test
    fun `day end is exactly one day later`() {
        assertEquals(march10 + MILLIS_PER_DAY, march10.dayEndExclusive())
    }

    @Test
    fun `range is inclusive at the start and exclusive at the end`() {
        val end = march10.dayEndExclusive()
        assertTrue(march10.isInDayKeyRange(march10, end))
        assertTrue((end - 1).isInDayKeyRange(march10, end))
        assertFalse((march10 - 1).isInDayKeyRange(march10, end))
        assertFalse(end.isInDayKeyRange(march10, end))
    }

    @Test
    fun `unbounded range accepts everything`() {
        assertTrue(0L.isInDayKeyRange(null, null))
        assertTrue(march10.isInDayKeyRange(null, null))
    }

    @Test
    fun `one-sided ranges work`() {
        assertTrue(march10.isInDayKeyRange(march10, null))
        assertFalse((march10 - 1).isInDayKeyRange(march10, null))
        assertTrue(march10.isInDayKeyRange(null, march10 + MILLIS_PER_DAY))
        assertFalse((march10 + MILLIS_PER_DAY).isInDayKeyRange(null, march10 + MILLIS_PER_DAY))
    }
}
