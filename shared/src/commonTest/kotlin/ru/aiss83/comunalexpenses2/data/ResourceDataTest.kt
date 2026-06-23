package ru.aiss83.comunalexpenses2.data

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Unit tests for ResourceData domain model — date formatting.
 */
@OptIn(ExperimentalUuidApi::class)
class ResourceDataTest {

    @Test
    fun `formatDate returns non-empty string`() {
        val data = ResourceData()
        val formatted = data.formatDate()
        assertTrue(formatted.isNotEmpty())
        assertTrue(formatted.contains("."), "Date should contain dots as separators")
    }

    @Test
    fun `formatDate has expected DD_MM_YYYY pattern`() {
        val data = ResourceData()
        val formatted = data.formatDate()
        val parts = formatted.split(".")
        assertTrue(parts.size == 3, "Should have 3 parts: day.month.year, got $formatted")
        // Each part should be parseable as int
        parts.forEach { it.toInt() }
    }

    @Test
    fun `formatDate day part is two digits`() {
        val data = ResourceData()
        val formatted = data.formatDate()
        val day = formatted.split(".")[0]
        assertTrue(day.length == 2, "Day should be zero-padded to 2 digits, got $day")
    }

    @Test
    fun `formatDate month part is two digits`() {
        val data = ResourceData()
        val formatted = data.formatDate()
        val month = formatted.split(".")[1]
        assertTrue(month.length == 2, "Month should be zero-padded to 2 digits, got $month")
    }
}
