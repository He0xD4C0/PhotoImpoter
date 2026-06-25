package com.captraw.photoimpoter.ui.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.documentfile.provider.DocumentFile
import com.captraw.photoimpoter.data.FileImportRepository
import com.captraw.photoimpoter.data.MetadataExtractorExifReader
import com.captraw.photoimpoter.data.PlatformFileScanner
import com.captraw.photoimpoter.domain.ImportMode
import com.captraw.photoimpoter.domain.ImportPlan
import com.captraw.photoimpoter.domain.ImportResult
import com.captraw.photoimpoter.domain.DirectoryRule
import com.captraw.photoimpoter.ui.screens.HomeScreen
import com.captraw.photoimpoter.ui.screens.ImportProgressScreen
import com.captraw.photoimpoter.ui.screens.ImportReportScreen
import com.captraw.photoimpoter.ui.screens.ScanResultScreen
import com.captraw.photoimpoter.viewmodel.FolderSelectionRequest
import com.captraw.photoimpoter.viewmodel.PhotoImporterViewModel

@Composable
fun PhotoImporterApp() {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val scanner = remember { PlatformFileScanner(context, MetadataExtractorExifReader(context)) }
    val exifReader = remember { MetadataExtractorExifReader(context) }
    val importRepository = remember(context) { FileImportRepository(context, scanner, exifReader) }
    val viewModel = remember(importRepository) { PhotoImporterViewModel(importRepository) }
    val sourceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.onSourceTreeSelected(uri)
        }
        viewModel.clearFolderSelectionRequest()
    }
    val targetLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.onTargetTreeSelected(uri)
        }
        viewModel.clearFolderSelectionRequest()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing)
        ) {
            NavHost(navController = navController, startDestination = PhotoImporterRoute.Home.route) {
                composable(PhotoImporterRoute.Home.route) {
                    val state = viewModel.uiState.collectAsStateWithLifecycle().value
                    HomeScreen(
                        state = state,
                        onSourcePickRequested = viewModel::requestSourceTree,
                        onTargetPickRequested = viewModel::requestTargetTree,
                        sourceLocationLabel = describeTreeLocation(context, state.sourceTree),
                        targetLocationLabel = describeTreeLocation(context, state.targetTree),
                        onLaunchSystemPicker = { request ->
                            when (request) {
                                FolderSelectionRequest.SOURCE -> sourceLauncher.launch(null)
                                FolderSelectionRequest.TARGET -> targetLauncher.launch(null)
                            }
                        },
                        onCancelFolderSelection = viewModel::clearFolderSelectionRequest,
                        onRuleSelected = viewModel::setDirectoryRule,
                        onScanRequested = viewModel::scan,
                        onNavigateToScanResult = { navController.navigate(PhotoImporterRoute.ScanResult.route) },
                        onModeSelected = viewModel::setImportMode
                    )
                }
                composable(PhotoImporterRoute.ScanResult.route) {
                    ScanResultScreen(
                        state = viewModel.uiState.collectAsStateWithLifecycle().value,
                        onStartImport = {
                            viewModel.startImport()
                            navController.navigate(PhotoImporterRoute.ImportProgress.route)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(PhotoImporterRoute.ImportProgress.route) {
                    ImportProgressScreen(
                        state = viewModel.uiState.collectAsStateWithLifecycle().value,
                        onCancel = viewModel::cancelImport,
                        onNavigateToReport = { navController.navigate(PhotoImporterRoute.Report.route) }
                    )
                }
                composable(PhotoImporterRoute.Report.route) {
                    ImportReportScreen(
                        state = viewModel.uiState.collectAsStateWithLifecycle().value,
                        onBackToHome = {
                            navController.popBackStack(PhotoImporterRoute.Home.route, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}

private enum class PhotoImporterRoute(val route: String) {
    Home("home"),
    ScanResult("scan_result"),
    ImportProgress("import_progress"),
    Report("report")
}

private fun describeTreeLocation(context: android.content.Context, uri: Uri?): String {
    if (uri == null) return "Not selected"
    val treeName = DocumentFile.fromTreeUri(context, uri)?.name
    return if (treeName.isNullOrBlank()) uri.toString() else treeName
}