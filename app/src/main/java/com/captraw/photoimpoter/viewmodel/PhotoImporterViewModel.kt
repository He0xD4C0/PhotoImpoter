package com.captraw.photoimpoter.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captraw.photoimpoter.data.FileImportRepository
import com.captraw.photoimpoter.domain.DirectoryRule
import com.captraw.photoimpoter.domain.ImportMode
import com.captraw.photoimpoter.domain.ImportPlan
import com.captraw.photoimpoter.domain.ImportResult
import com.captraw.photoimpoter.domain.MediaFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FolderSelectionRequest {
    SOURCE,
    TARGET
}

data class PhotoImporterUiState(
    val sourceTree: Uri? = null,
    val targetTree: Uri? = null,
    val pendingFolderSelection: FolderSelectionRequest? = null,
    val directoryRule: DirectoryRule = DirectoryRule.YYYY_MM_DD,
    val importMode: ImportMode = ImportMode.COPY,
    val scannedFiles: List<MediaFile> = emptyList(),
    val importPlans: List<ImportPlan> = emptyList(),
    val scanInProgress: Boolean = false,
    val importInProgress: Boolean = false,
    val progressCurrentFile: String = "",
    val progressCompleted: Int = 0,
    val progressTotal: Int = 0,
    val currentSpeed: String = "0 MB/s",
    val eta: String = "--",
    val importResult: ImportResult? = null,
    val errorMessage: String? = null
)

class PhotoImporterViewModel(
    private val repository: FileImportRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoImporterUiState())
    val uiState: StateFlow<PhotoImporterUiState> = _uiState.asStateFlow()

    private var importJob: Job? = null

    fun requestSourceTree() {
        _uiState.value = _uiState.value.copy(pendingFolderSelection = FolderSelectionRequest.SOURCE)
    }

    fun requestTargetTree() {
        _uiState.value = _uiState.value.copy(pendingFolderSelection = FolderSelectionRequest.TARGET)
    }

    fun clearFolderSelectionRequest() {
        _uiState.value = _uiState.value.copy(pendingFolderSelection = null)
    }

    fun setDirectoryRule(rule: DirectoryRule) {
        _uiState.value = _uiState.value.copy(directoryRule = rule)
    }

    fun setImportMode(mode: ImportMode) {
        _uiState.value = _uiState.value.copy(importMode = mode)
    }

    fun onSourceTreeSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(sourceTree = uri)
    }

    fun onTargetTreeSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(targetTree = uri)
    }

    fun scan() {
        val sourceTree = _uiState.value.sourceTree ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scanInProgress = true, errorMessage = null)
            val scannedFiles = repository.scan(sourceTree)
            val targetTree = requireNotNull(_uiState.value.targetTree) { "Target tree must be selected before scanning" }
            val plans = repository.buildPlans(scannedFiles, targetTree, _uiState.value.directoryRule, _uiState.value.importMode)
            _uiState.value = _uiState.value.copy(
                scanInProgress = false,
                scannedFiles = scannedFiles,
                importPlans = plans,
                progressTotal = plans.size
            )
        }
    }

    fun startImport() {
        val plans = _uiState.value.importPlans
        if (plans.isEmpty()) return
        importJob?.cancel()
        importJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                importInProgress = true,
                importResult = null,
                progressCompleted = 0,
                progressCurrentFile = "",
                currentSpeed = "0 MB/s",
                eta = "--"
            )
            val targetTree = requireNotNull(_uiState.value.targetTree) { "Target tree must be selected before importing" }
            val result = repository.importPlans(
                plans = plans,
                targetRoot = targetTree,
                mode = _uiState.value.importMode,
                onProgress = { currentFile, completed, total, currentSpeed, eta ->
                    _uiState.value = _uiState.value.copy(
                        progressCurrentFile = currentFile,
                        progressCompleted = completed,
                        progressTotal = total,
                        currentSpeed = currentSpeed,
                        eta = eta
                    )
                }
            )
            _uiState.value = _uiState.value.copy(importInProgress = false, importResult = result)
        }
    }

    fun cancelImport() {
        importJob?.cancel()
        _uiState.value = _uiState.value.copy(importInProgress = false)
    }
}