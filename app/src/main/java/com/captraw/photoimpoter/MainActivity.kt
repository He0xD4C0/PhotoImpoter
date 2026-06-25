package com.captraw.photoimpoter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.captraw.photoimpoter.ui.theme.PhotoImpoterTheme
import com.captraw.photoimpoter.ui.app.PhotoImporterApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoImpoterTheme {
                PhotoImporterApp()
            }
        }
    }
}