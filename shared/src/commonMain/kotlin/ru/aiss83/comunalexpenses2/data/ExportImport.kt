package ru.aiss83.comunalexpenses2.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.aiss83.comunalexpenses2.utils.normalizeToDayKey
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
            // Backups written before day keys existed contain real instants: normalise them so
            // the one-record-per-day invariant also holds for imported data.
            date = date.normalizeToDayKey(),
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
 * Outcome of parsing an import file.
 *
 * Keeps the distinction between "not a backup file at all" and "a backup with no usable
 * records" so the UI can show a meaningful message instead of a generic failure.
 */
sealed interface ImportParseResult {
    /** [records] are valid; [skippedInvalid] entries had malformed ids and were dropped. */
    data class Success(val records: List<ResourceData>, val skippedInvalid: Int) : ImportParseResult

    /** The text is not valid JSON or does not match the export format. */
    data object InvalidJson : ImportParseResult

    /** Valid container, but nothing importable inside (empty file or no records). */
    data object NoRecords : ImportParseResult
}

/**
 * Parse records from an export JSON string, skipping invalid entries.
 */
fun parseImportJson(jsonString: String): ImportParseResult {
    if (jsonString.isBlank()) return ImportParseResult.NoRecords

    val container = try {
        json.decodeFromString<ExportContainer>(jsonString)
    } catch (_: SerializationException) {
        return ImportParseResult.InvalidJson
    } catch (_: IllegalArgumentException) {
        return ImportParseResult.InvalidJson
    }

    val records = container.records.mapNotNull { it.toDomainModel() }
    if (records.isEmpty()) return ImportParseResult.NoRecords

    return ImportParseResult.Success(
        records = records,
        skippedInvalid = container.records.size - records.size
    )
}
