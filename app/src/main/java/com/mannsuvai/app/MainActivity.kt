package com.mannsuvai.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Camera imports - assuming standard Android hardware Camera or CameraX setup
import android.hardware.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface {
                    // Main app content call
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    Text(text = "Welcome to Mann Suvai")
}
