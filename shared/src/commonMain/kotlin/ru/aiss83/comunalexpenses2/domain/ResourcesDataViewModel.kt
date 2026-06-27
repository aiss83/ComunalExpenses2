package ru.aiss83.comunalexpenses2.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.ResourceDataRepository
import ru.aiss83.comunalexpenses2.data.exportToJson
import ru.aiss83.comunalexpenses2.data.importFromJson
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    private val _importedCount = MutableStateFlow<Int?>(null)
    val importedCount: StateFlow<Int?> = _importedCount

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
                }
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

    fun clearImportedCount() {
        _importedCount.value = null
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
     * Observers should watch [importedCount] for the final inserted count.
     */
    fun importJson(jsonString: String) {
        val parsed = importFromJson(jsonString)
        if (parsed.isEmpty()) {
            _errorMessage.value = "No valid records found in JSON"
            return
        }
        viewModelScope.launch {
            try {
                for (record in parsed) {
                    repository.insertResourcesData(record)
                }
                _importedCount.value = parsed.size
            } catch (e: Exception) {
                _errorMessage.value = "Failed to import: ${e.message}"
            }
        }
    }
}
