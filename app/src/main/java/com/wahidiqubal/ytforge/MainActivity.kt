package com.wahidiqubal.ytforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wahidiqubal.ytforge.ui.AppRoot
import com.wahidiqubal.ytforge.ui.theme.YTForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YTForgeTheme {
                AppRoot()
            }
        }
    }
}
