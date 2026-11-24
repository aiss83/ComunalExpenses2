package ru.aiss83.comunalexpenses2.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.aiss83.comunalexpenses2.data.ResourceData

/**
 * Tests for the pure chart/statistics math used by the statistics screen.
 */
@OptIn(ExperimentalUuidApi::class)
class StatsCalculationsTest {

    private fun record(date: Long, cold: Long = 0) =
        ResourceData(id = Uuid.random(), date = date, coldWater = cold)

    @Test
    fun `cumulative series keeps raw readings`() {
        val records = listOf(record(1, cold = 100), record(2, cold = 150))
        val series = metricSeries(records, { it.coldWater }, deltaMode = false)
        assertEquals(listOf(1L to 100L, 2L to 150L), series.map { it.date to it.value })
    }

    @Test
    fun `delta series computes consumption since the previous record`() {
        val records = listOf(record(1, cold = 100), record(2, cold = 150), record(3, cold = 170))
        val series = metricSeries(records, { it.coldWater }, deltaMode = true)
        assertEquals(listOf(0L, 50L, 20L), series.map { it.value })
    }

    @Test
    fun `delta series clamps negative differences to zero`() {
        val records = listOf(record(1, cold = 100), record(2, cold = 90))
        assertEquals(listOf(0L, 0L), metricSeries(records, { it.coldWater }, true).map { it.value })
    }

    @Test
    fun `delta series keeps the correct baseline before a period window`() {
        // The series is built over the full history and only afterwards filtered by period,
        // so the first visible point is measured against the record before the window.
        val records = listOf(record(1, cold = 100), record(2, cold = 150), record(3, cold = 170))
        val window = metricSeries(records, { it.coldWater }, true).drop(1)
        assertEquals(listOf(50L, 20L), window.map { it.value })
    }

    @Test
    fun `empty series has no summary`() {
        assertNull(summarize(emptyList()))
    }

    @Test
    fun `summary computes min max average total and last`() {
        val summary = requireNotNull(summarize(listOf(10L, 30L, 20L, 40L)))
        assertEquals(10L, summary.min)
        assertEquals(40L, summary.max)
        assertEquals(25L, summary.average)
        assertEquals(100L, summary.total)
        assertEquals(40L, summary.last)
    }

    @Test
    fun `summary of a single value repeats it`() {
        val summary = requireNotNull(summarize(listOf(7L)))
        assertEquals(7L, summary.min)
        assertEquals(7L, summary.max)
        assertEquals(7L, summary.average)
        assertEquals(7L, summary.total)
        assertEquals(7L, summary.last)
    }

    @Test
    fun `labels are downsampled keeping the first and the last`() {
        val labels = (1..100).map { "d$it" }
        val result = downsampleLabels(labels, maxLabels = 6)

        assertEquals(labels.size, result.size, "Result must stay index-aligned with the categories")
        assertEquals("d1", result.first())
        assertEquals("d100", result.last())
        assertTrue(result.count { it.isNotEmpty() } <= 7, "Too many labels drawn: ${result.count { it.isNotEmpty() }}")
    }

    @Test
    fun `short label lists are left untouched`() {
        val labels = listOf("a", "b", "c")
        assertEquals(labels, downsampleLabels(labels, maxLabels = 6))
    }

    @Test
    fun `non-positive label budget leaves labels untouched`() {
        val labels = listOf("a", "b")
        assertEquals(labels, downsampleLabels(labels, maxLabels = 0))
    }
}
