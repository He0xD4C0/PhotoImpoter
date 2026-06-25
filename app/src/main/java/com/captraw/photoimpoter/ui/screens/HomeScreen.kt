package com.captraw.photoimpoter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.captraw.photoimpoter.domain.DirectoryRule
import com.captraw.photoimpoter.domain.ImportMode
import com.captraw.photoimpoter.viewmodel.FolderSelectionRequest
import com.captraw.photoimpoter.viewmodel.PhotoImporterUiState
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PhotoImporterUiState,
    onSourcePickRequested: () -> Unit,
    onTargetPickRequested: () -> Unit,
    sourceLocationLabel: String,
    targetLocationLabel: String,
    onLaunchSystemPicker: (FolderSelectionRequest) -> Unit,
    onCancelFolderSelection: () -> Unit,
    onRuleSelected: (DirectoryRule) -> Unit,
    onModeSelected: (ImportMode) -> Unit,
    onScanRequested: () -> Unit,
    onNavigateToScanResult: () -> Unit
) {
    val pendingSelection = state.pendingFolderSelection

    if (pendingSelection != null) {
        BackHandler(onBack = onCancelFolderSelection)
        AlertDialog(
            onDismissRequest = onCancelFolderSelection,
            title = { Text(if (pendingSelection == FolderSelectionRequest.SOURCE) "Select Source Folder" else "Select Target Folder") },
            text = { Text("Open the system folder picker or cancel this selection.") },
            confirmButton = {
                Button(onClick = { onLaunchSystemPicker(pendingSelection) }) {
                    Text("Open Picker")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onCancelFolderSelection) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Photo Importer", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Select source and target folders")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onSourcePickRequested) { Text("Choose Source") }
                    OutlinedButton(onClick = onTargetPickRequested) { Text("Choose Target") }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selected locations", style = MaterialTheme.typography.titleMedium)
                        Text("Source: $sourceLocationLabel")
                        Text("Target: $targetLocationLabel")
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. Select directory rule")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DirectoryRule.entries.forEach { rule ->
                        FilterChip(
                            selected = state.directoryRule == rule,
                            onClick = { onRuleSelected(rule) },
                            label = { Text(rule.name) }
                        )
                    }
                }
                Text("Import mode")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImportMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.importMode == mode,
                            onClick = { onModeSelected(mode) },
                            label = { Text(mode.name) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            onScanRequested()
            onNavigateToScanResult()
        }, enabled = state.sourceTree != null && state.targetTree != null) {
            Text(if (state.scanInProgress) "Scanning..." else "Scan")
        }
    }
}