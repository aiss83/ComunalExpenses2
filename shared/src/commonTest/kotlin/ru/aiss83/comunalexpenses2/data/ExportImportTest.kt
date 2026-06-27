package ru.aiss83.comunalexpenses2.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unit tests for JSON export/import — round-trip with all fields including shared.
 */
@OptIn(ExperimentalUuidApi::class)
class ExportImportTest {

    @Test
    fun `toJsonModel includes shared field`() {
        val data = ResourceData(
            id = Uuid.random(),
            date = 1719000000000L,
            hotWater = 100,
            coldWater = 200,
            dayElectricity = 300,
            nightElectricity = 400,
            shared = true
        )
        val json = data.toJsonModel()
        assertTrue(json.shared)
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
    fun `export then import is round-trip with shared`() {
        val original = listOf(
            ResourceData(
                id = Uuid.random(),
                date = 1719000000000L,
                hotWater = 100,
                coldWater = 200,
                dayElectricity = 300,
                nightElectricity = 400,
                shared = true
            ),
            ResourceData(
                id = Uuid.random(),
                shared = false
            )
        )
        val json = exportToJson(original)
        val imported = importFromJson(json)
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
    fun `importFromJson returns empty list for invalid JSON`() {
        val result = importFromJson("this is not JSON")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `importFromJson returns empty list for empty string`() {
        val result = importFromJson("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `exportToJson produces valid JSON string`() {
        val data = listOf(
            ResourceData(
                id = Uuid.random(),
                date = 1719000000000L,
                hotWater = 500,
                coldWater = 600
            )
        )
        val json = exportToJson(data)
        assertTrue(json.contains("\"appName\""))
        assertTrue(json.contains("\"version\""))
        assertTrue(json.contains("\"records\""))
        assertTrue(json.contains("\"hotWater\""))
        // Pretty-print should have newlines
        assertTrue(json.contains("\n"))
    }

    @Test
    fun `importFromJson skips invalid UUID entries`() {
        val validId = Uuid.random().toString()
        val manualJson = """{"appName":"Test","version":1,"records":[
            {"id":"invalid-uuid","date":0,"hotWater":1,"coldWater":2,"dayElectricity":3,"nightElectricity":4},
            {"id":"$validId","date":1,"hotWater":5,"coldWater":6,"dayElectricity":7,"nightElectricity":8}
        ]}"""
        val result = importFromJson(manualJson)
        assertEquals(1, result.size, "Should skip invalid UUID and import only valid entry")
    }
}
