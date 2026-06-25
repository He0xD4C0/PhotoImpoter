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
fun ImportReportScreen(
    state: PhotoImporterUiState,
    onBackToHome: () -> Unit
) {
    val result = state.importResult
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Import Report", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Success: ${result?.successCount ?: 0}")
                Text("Duplicates: ${result?.duplicateCount ?: 0}")
                Text("Failures: ${result?.failureCount ?: 0}")
            }
        }
        Text("Failed files")
        Text(result?.errors?.joinToString(separator = "\n").orEmpty().ifBlank { "None" })
        Button(onClick = onBackToHome) { Text("Back to Home") }
    }
}