package com.captraw.photoimpoter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.captraw.photoimpoter.viewmodel.PhotoImporterUiState

@Composable
fun ScanResultScreen(
    state: PhotoImporterUiState,
    onStartImport: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Scan Result", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total files: ${state.scannedFiles.size}")
                Text("Plans: ${state.importPlans.size}")
                Text("Duplicate candidates: ${state.importPlans.count { it.duplicate }}")
            }
        }
        Button(onClick = onStartImport, enabled = state.importPlans.isNotEmpty()) {
            Text("Start Import")
        }
        Button(onClick = onBack) { Text("Back") }
    }
}