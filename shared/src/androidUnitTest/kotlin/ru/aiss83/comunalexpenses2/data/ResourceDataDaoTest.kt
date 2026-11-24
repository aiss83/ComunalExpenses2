package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * DAO tests against a real in-memory SQLite database (JVM).
 * These cover the behaviour that unit tests of pure functions cannot: actual SQL semantics.
 */
@OptIn(ExperimentalUuidApi::class)
class ResourceDataDaoTest {

    private lateinit var driver: JdbcSqliteDriver
    private lateinit var dao: ResourceDataDao

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ResourcesDatabase.Schema.create(driver)
        dao = ResourceDataDao(ResourcesDatabase(driver))
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun record(
        id: Uuid = Uuid.random(),
        date: Long = 1_700_000_000_000L,
        coldWater: Long = 10
    ) = ResourceData(id = id, date = date, coldWater = coldWater)

    @Test
    fun `importing the same backup twice is idempotent`() = runBlocking {
        val records = listOf(
            record(),
            record(date = 1_700_100_000_000L)
        )

        dao.importRecords(records)
        // Re-importing the same ids used to throw a primary-key violation and abort the
        // whole transaction — the user saw "Failed to import" instead of a successful import.
        dao.importRecords(records)

        assertEquals(2, dao.getAllResourceData().first().size)
    }

    @Test
    fun `import replaces an existing record with the same id`() = runBlocking {
        val id = Uuid.random()
        dao.addResourceData(record(id = id, coldWater = 10))

        dao.importRecords(listOf(record(id = id, coldWater = 99)))

        val stored = dao.findResourceData(id).first()
        assertEquals(99L, stored?.coldWater)
        assertEquals(1, dao.getAllResourceData().first().size)
    }

    @Test
    fun `importing two records of the same day keeps the last one`() = runBlocking {
        val day = 1_710_028_800_000L

        dao.importRecords(
            listOf(
                record(date = day, coldWater = 1),
                record(date = day, coldWater = 2)
            )
        )

        val stored = dao.getAllResourceData().first()
        assertEquals(1, stored.size, "one card per day")
        assertEquals(2L, stored.single().coldWater)
    }

    @Test
    fun `inserting a second record for the same day is rejected`() = runBlocking {
        val day = 1_710_028_800_000L
        dao.addResourceData(record(date = day))

        assertFails { dao.addResourceData(record(date = day)) }
        assertEquals(1, dao.getAllResourceData().first().size)
    }

    @Test
    fun `rows with a corrupted id are skipped instead of breaking the list`() = runBlocking {
        dao.addResourceData(record(coldWater = 5))
        // Simulates foreign/corrupted data: one bad row used to throw from the flow mapper and
        // leave the whole screen empty with an error dialog.
        driver.execute(
            null,
            """INSERT INTO resources_records
               (recordId, date, waterHot, waterCold, electricityDay, electricityNight, shared)
               VALUES ('not-a-uuid', 1710028800000, 0, 0, 0, 0, 0)""".trimIndent(),
            0
        )

        val stored = dao.getAllResourceData().first()
        assertEquals(1, stored.size)
        assertEquals(5L, stored.single().coldWater)
    }

    @Test
    fun `records are ordered by date descending`() = runBlocking {
        dao.addResourceData(record(date = 1_000L, coldWater = 1))
        dao.addResourceData(record(date = 3_000L, coldWater = 3))
        dao.addResourceData(record(date = 2_000L, coldWater = 2))

        assertEquals(listOf(3L, 2L, 1L), dao.getAllResourceData().first().map { it.coldWater })
    }
}
