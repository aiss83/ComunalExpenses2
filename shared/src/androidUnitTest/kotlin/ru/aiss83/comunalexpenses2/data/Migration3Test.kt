package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import ru.aiss83.comunalexpenses2.utils.MILLIS_PER_DAY
import ru.aiss83.comunalexpenses2.utils.toDayKey

/**
 * Tests for the migration that introduced the one-record-per-day invariant.
 *
 * The migration is destructive by design: duplicate readings of the same day are collapsed to
 * the latest one, and stored instants are converted to day keys.
 */
class Migration3Test {

    private lateinit var driver: JdbcSqliteDriver
    private lateinit var database: ResourcesDatabase
    private val timeZone = TimeZone.currentSystemDefault()

    /** Schema as it was before this migration (version 3): no unique index on `date`. */
    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(
            null,
            """
            CREATE TABLE resources_records (
                recordId TEXT NOT NULL PRIMARY KEY,
                date INTEGER NOT NULL,
                waterHot INTEGER NOT NULL DEFAULT 0,
                waterCold INTEGER NOT NULL DEFAULT 0,
                electricityDay INTEGER NOT NULL DEFAULT 0,
                electricityNight INTEGER NOT NULL DEFAULT 0,
                shared INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
            0
        )
        database = ResourcesDatabase(driver)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    /** Instant of a local date+hour, i.e. what older versions stored. */
    private fun legacyInstant(day: Int, hour: Int): Long =
        LocalDate(2024, 3, day).atTime(hour, 0).toInstant(timeZone).toEpochMilliseconds()

    private fun insert(recordId: String, date: Long, hotWater: Long) {
        database.resourcesRecordsQueries.insertResourceData(
            recordId = recordId,
            date = date,
            waterHot = hotWater,
            waterCold = 0,
            electricityDay = 0,
            electricityNight = 0,
            shared = 0
        )
    }

    @Test
    fun `migration keeps one record per day and the latest of duplicates`() {
        val firstDayMorning = legacyInstant(day = 10, hour = 9)
        val firstDayEvening = legacyInstant(day = 10, hour = 21)
        val secondDay = legacyInstant(day = 11, hour = 9)
        insert("a", firstDayMorning, hotWater = 10)
        insert("b", firstDayEvening, hotWater = 20)
        insert("c", secondDay, hotWater = 30)

        ResourcesDatabase.Schema.migrate(driver, 3, ResourcesDatabase.Schema.version)

        val rows = database.resourcesRecordsQueries.getAllResourceData().executeAsList()
        assertEquals(2, rows.size, "duplicated day must be collapsed to one card")

        val firstDayKey = firstDayMorning.toDayKey(timeZone)
        val secondDayKey = secondDay.toDayKey(timeZone)
        assertEquals(setOf(firstDayKey, secondDayKey), rows.map { it.date }.toSet())

        // The later reading of the duplicated day survived.
        assertEquals(20L, rows.first { it.date == firstDayKey }.waterHot)
        assertEquals(30L, rows.first { it.date == secondDayKey }.waterHot)

        // Every stored date is a day key (UTC midnight) now.
        rows.forEach { row -> assertEquals(0L, row.date % MILLIS_PER_DAY) }
    }

    @Test
    fun `migration creates the unique index`() {
        insert("a", legacyInstant(day = 10, hour = 9), hotWater = 10)

        ResourcesDatabase.Schema.migrate(driver, 3, ResourcesDatabase.Schema.version)

        assertFails {
            insert("b", legacyInstant(day = 10, hour = 21).toDayKey(timeZone), hotWater = 1)
        }
    }
}
