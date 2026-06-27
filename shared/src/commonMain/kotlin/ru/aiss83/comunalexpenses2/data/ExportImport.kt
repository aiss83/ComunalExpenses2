package ru.aiss83.comunalexpenses2.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * JSON-serializable representation of ResourceData for export/import.
 * Uses String for UUID and Long for date (epoch millis) for clean JSON output.
 */
@Serializable
data class ResourceDataJson(
    val id: String,
    val date: Long,
    val hotWater: Long,
    val coldWater: Long,
    val dayElectricity: Long,
    val nightElectricity: Long,
    val shared: Boolean = false
)

/**
 * Container for export/import of all records.
 */
@Serializable
data class ExportContainer(
    val appName: String = "CommunalExpenses",
    val version: Int = 1,
    val records: List<ResourceDataJson>
)

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Convert domain model to JSON-serializable model.
 */
@OptIn(ExperimentalUuidApi::class)
fun ResourceData.toJsonModel(): ResourceDataJson = ResourceDataJson(
    id = id.toString(),
    date = date,
    hotWater = hotWater,
    coldWater = coldWater,
    dayElectricity = dayElectricity,
    nightElectricity = nightElectricity,
    shared = shared
)

/**
 * Convert JSON model back to domain model.
 */
@OptIn(ExperimentalUuidApi::class)
fun ResourceDataJson.toDomainModel(): ResourceData? {
    val uuid = try {
        Uuid.parse(id)
    } catch (_: IllegalArgumentException) {
        null
    }
    return uuid?.let {
        ResourceData(
            id = it,
            date = date,
            hotWater = hotWater,
            coldWater = coldWater,
            dayElectricity = dayElectricity,
            nightElectricity = nightElectricity,
            shared = shared
        )
    }
}

/**
 * Export all records to pretty-printed JSON string.
 */
fun exportToJson(records: List<ResourceData>): String {
    val container = ExportContainer(
        records = records.map { it.toJsonModel() }
    )
    return json.encodeToString(container)
}

/**
 * Import records from JSON string.
 * Returns list of valid ResourceData, skipping invalid entries.
 */
fun importFromJson(jsonString: String): List<ResourceData> {
    return try {
        val container = json.decodeFromString<ExportContainer>(jsonString)
        container.records.mapNotNull { it.toDomainModel() }
    } catch (_: SerializationException) {
        emptyList()
    } catch (_: IllegalArgumentException) {
        emptyList()
    }
}
