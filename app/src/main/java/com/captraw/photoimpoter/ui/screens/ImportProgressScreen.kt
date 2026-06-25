package com.captraw.photoimpoter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.captraw.photoimpoter.viewmodel.PhotoImporterUiState

@Composable
fun ImportProgressScreen(
    state: PhotoImporterUiState,
    onCancel: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val progress = if (state.progressTotal == 0) 0f else state.progressCompleted.toFloat() / state.progressTotal.toFloat()
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Import Progress", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current file: ${state.progressCurrentFile.ifBlank { "Waiting" }}")
                Text("Completed: ${state.progressCompleted}/${state.progressTotal}")
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("Speed: ${state.currentSpeed}")
                Text("ETA: ${state.eta}")
            }
        }
        Button(onClick = onCancel) { Text("Cancel") }
        Button(onClick = onNavigateToReport, enabled = state.importResult != null) { Text("Open Report") }
    }
}