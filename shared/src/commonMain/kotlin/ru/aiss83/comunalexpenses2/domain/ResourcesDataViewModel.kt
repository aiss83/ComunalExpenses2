package ru.aiss83.comunalexpenses2.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.aiss83.comunalexpenses2.data.ImportParseResult
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.ResourceDataRepository
import ru.aiss83.comunalexpenses2.data.exportToJson
import ru.aiss83.comunalexpenses2.data.parseImportJson
import ru.aiss83.comunalexpenses2.utils.clearWidgetData
import ru.aiss83.comunalexpenses2.utils.updateWidgetData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Outcome of an import attempt, consumed by the settings screen.
 */
sealed interface ImportResult {
    /** [imported] records were written, [skipped] malformed entries were ignored. */
    data class Success(val imported: Int, val skipped: Int) : ImportResult

    /** The picked file is not a valid export JSON. */
    data object InvalidFile : ImportResult

    /** The file parsed, but contained no usable records. */
    data object NoValidRecords : ImportResult

    /** The database rejected the import. */
    data object Failed : ImportResult
}

/**
 * ViewModel for managing utility meter readings (resource data).
 * Uses StateFlow instead of LiveData for KMP compatibility.
 */
@OptIn(ExperimentalUuidApi::class)
class ResourcesDataViewModel(
    private val repository: ResourceDataRepository
) : ViewModel() {

    private val _allResourcesData = MutableStateFlow<List<ResourceData>>(emptyList())
    val allResourcesData: StateFlow<List<ResourceData>> = _allResourcesData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

    init {
        observeAllResourcesData()
    }

    private fun observeAllResourcesData() {
        viewModelScope.launch {
            repository.getAllResourcesData()
                .catch { e ->
                    _errorMessage.value = "Failed to load data: ${e.message}"
                }
                .collect { data ->
                    _allResourcesData.value = data
                    syncHomeWidget(data)
                }
        }
    }

    /**
     * Keep the home-screen widget in sync with the newest record.
     * The DAO returns records ordered by date descending, so the first one is the latest.
     * Called after every data change (add / edit / delete / import) for a single source of truth.
     */
    private fun syncHomeWidget(data: List<ResourceData>) {
        val latest = data.firstOrNull()
        if (latest == null) {
            clearWidgetData()
        } else {
            updateWidgetData(
                coldWater = latest.coldWater,
                hotWater = latest.hotWater,
                dayElectricity = latest.dayElectricity,
                nightElectricity = latest.nightElectricity,
                date = latest.formatDate()
            )
        }
    }

    fun addResourcesData(data: ResourceData) {
        viewModelScope.launch {
            try {
                repository.insertResourcesData(data)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add data: ${e.message}"
            }
        }
    }

    fun updateResourceData(data: ResourceData) {
        viewModelScope.launch {
            try {
                repository.updateResourcesData(data)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update data: ${e.message}"
            }
        }
    }

    fun deleteResourceData(id: Uuid) {
        viewModelScope.launch {
            try {
                repository.deleteResourcesData(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete data: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    /**
     * Delete all records from the database.
     */
    fun deleteAllData() {
        viewModelScope.launch {
            try {
                repository.deleteAllResourcesData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete all data: ${e.message}"
            }
        }
    }

    /**
     * Re-insert a record for undo deletion.
     */
    fun undoDeleteResourceData(data: ResourceData) {
        viewModelScope.launch {
            try {
                repository.insertResourcesData(data)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to undo delete: ${e.message}"
            }
        }
    }

    /**
     * Mark a record as shared (locks it from editing/deletion).
     */
    fun markAsShared(id: Uuid) {
        viewModelScope.launch {
            try {
                repository.markAsShared(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to mark as shared: ${e.message}"
            }
        }
    }

    /**
     * Export all records as a JSON string for sharing.
     */
    fun exportJson(): String {
        return exportToJson(_allResourcesData.value)
    }

    /**
     * Import records from a JSON string.
     * Parsing is synchronous, insertion is async.
     * Observers should watch [importResult] — it is always set exactly once per call, so the
     * UI can reliably clear its progress state on success *and* on failure.
     */
    fun importJson(jsonString: String) {
        when (val parsed = parseImportJson(jsonString)) {
            ImportParseResult.InvalidJson -> {
                _importResult.value = ImportResult.InvalidFile
            }

            ImportParseResult.NoRecords -> {
                _importResult.value = ImportResult.NoValidRecords
            }

            is ImportParseResult.Success -> viewModelScope.launch {
                try {
                    repository.importRecords(parsed.records)
                    _importResult.value = ImportResult.Success(
                        imported = parsed.records.size,
                        skipped = parsed.skippedInvalid
                    )
                } catch (_: Exception) {
                    _importResult.value = ImportResult.Failed
                }
            }
        }
    }
}
