package ru.aiss83.comunalexpenses2.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unit tests for JSON export/import — round-trip with all fields including shared.
 */
@OptIn(ExperimentalUuidApi::class)
class ExportImportTest {

    /** Day key of 10 March 2024. */
    private val dayKey = 1_710_028_800_000L

    private fun record(shared: Boolean = false) = ResourceData(
        id = Uuid.random(),
        date = dayKey,
        hotWater = 100,
        coldWater = 200,
        dayElectricity = 300,
        nightElectricity = 400,
        shared = shared
    )

    @Test
    fun `toJsonModel includes shared field`() {
        assertTrue(record(shared = true).toJsonModel().shared)
    }

    @Test
    fun `toDomainModel preserves shared flag`() {
        val json = ResourceDataJson(
            id = Uuid.random().toString(),
            date = 1719000000000L,
            hotWater = 50,
            coldWater = 100,
            dayElectricity = 150,
            nightElectricity = 200,
            shared = true
        )
        val domain = requireNotNull(json.toDomainModel())
        assertTrue(domain.shared)
    }

    @Test
    fun `toDomainModel returns null for invalid UUID`() {
        val json = ResourceDataJson(
            id = "not-a-valid-uuid",
            date = 0,
            hotWater = 0,
            coldWater = 0,
            dayElectricity = 0,
            nightElectricity = 0
        )
        assertEquals(null, json.toDomainModel())
    }

    @Test
    fun `export then import is a round trip with shared`() {
        val original = listOf(
            record(shared = true),
            ResourceData(id = Uuid.random(), shared = false)
        )
        val parsed = parseImportJson(exportToJson(original))
        val imported = assertIs<ImportParseResult.Success>(parsed).records

        assertEquals(original.size, imported.size)
        imported.zip(original).forEach { (importedRecord, originalRecord) ->
            assertEquals(originalRecord.id, importedRecord.id)
            assertEquals(originalRecord.date, importedRecord.date)
            assertEquals(originalRecord.hotWater, importedRecord.hotWater)
            assertEquals(originalRecord.coldWater, importedRecord.coldWater)
            assertEquals(originalRecord.dayElectricity, importedRecord.dayElectricity)
            assertEquals(originalRecord.nightElectricity, importedRecord.nightElectricity)
            assertEquals(originalRecord.shared, importedRecord.shared)
        }
    }

    @Test
    fun `import skips invalid ids and reports how many were skipped`() {
        val validId = Uuid.random().toString()
        val manualJson = """{"appName":"Test","version":1,"records":[
            {"id":"invalid-uuid","date":0,"hotWater":1,"coldWater":2,"dayElectricity":3,"nightElectricity":4},
            {"id":"$validId","date":1,"hotWater":5,"coldWater":6,"dayElectricity":7,"nightElectricity":8}
        ]}"""

        val parsed = assertIs<ImportParseResult.Success>(parseImportJson(manualJson))
        assertEquals(1, parsed.records.size)
        assertEquals(1, parsed.skippedInvalid)
    }

    @Test
    fun `malformed json is reported as invalid, not as an empty import`() {
        assertEquals(ImportParseResult.InvalidJson, parseImportJson("this is not JSON"))
        assertEquals(ImportParseResult.InvalidJson, parseImportJson("""{"appName":1}"""))
    }

    @Test
    fun `blank input and recordless backups are reported as no records`() {
        assertEquals(ImportParseResult.NoRecords, parseImportJson(""))
        assertEquals(ImportParseResult.NoRecords, parseImportJson("   "))
        assertEquals(
            ImportParseResult.NoRecords,
            parseImportJson("""{"appName":"Test","version":1,"records":[]}""")
        )
        // Valid container where every record has a broken id -> nothing importable.
        assertEquals(
            ImportParseResult.NoRecords,
            parseImportJson(
                """{"records":[{"id":"bad","date":0,"hotWater":0,"coldWater":0,"dayElectricity":0,"nightElectricity":0}]}"""
            )
        )
    }

    @Test
    fun `legacy instant dates are normalised to day keys on import`() {
        val validId = Uuid.random().toString()
        // 2024-03-10T09:15Z was written by a version that stored real instants.
        val legacyJson = """{"records":[{"id":"$validId","date":1710062100000,"hotWater":1,"coldWater":2,"dayElectricity":3,"nightElectricity":4}]}"""

        val parsed = assertIs<ImportParseResult.Success>(parseImportJson(legacyJson))
        assertEquals(dayKey, parsed.records.single().date)
    }

    @Test
    fun `two records of the same day collapse to one after import`() {
        val first = Uuid.random().toString()
        val second = Uuid.random().toString()
        val sameDayJson = """{"records":[
            {"id":"$first","date":$dayKey,"hotWater":1,"coldWater":1,"dayElectricity":1,"nightElectricity":1},
            {"id":"$second","date":$dayKey,"hotWater":2,"coldWater":2,"dayElectricity":2,"nightElectricity":2}
        ]}"""

        // Parsing keeps both (the database enforces one per day via INSERT OR REPLACE).
        val parsed = assertIs<ImportParseResult.Success>(parseImportJson(sameDayJson))
        assertEquals(2, parsed.records.size)
        assertEquals(setOf(dayKey), parsed.records.map { it.date }.toSet())
    }

    @Test
    fun `export produces a pretty printed container`() {
        val json = exportToJson(listOf(record()))
        assertTrue(json.contains("\"appName\""))
        assertTrue(json.contains("\"version\""))
        assertTrue(json.contains("\"records\""))
        assertTrue(json.contains("\"hotWater\""))
        // Pretty-print should have newlines
        assertTrue(json.contains("\n"))
    }

    @Test
    fun `empty export parses back to no records`() {
        assertEquals(ImportParseResult.NoRecords, parseImportJson(exportToJson(emptyList())))
    }
}
