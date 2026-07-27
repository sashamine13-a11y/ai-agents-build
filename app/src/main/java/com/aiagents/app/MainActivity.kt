package com.aiagents.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aiagents.app.ui.screens.ChatScreen
import com.aiagents.app.ui.screens.SettingsScreen
import com.aiagents.app.ui.theme.AIAgentsTheme
import com.aiagents.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIAgentsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MainViewModel = viewModel()) {
    val showSettings by viewModel.showSettings.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()

    ChatScreen(viewModel = viewModel)

    if (showSettings) {
        SettingsScreen(
            currentKeys = apiKeys,
            onSave = { viewModel.saveKeys(it) },
            onDismiss = { viewModel.toggleSettings() }
        )
    }
}
