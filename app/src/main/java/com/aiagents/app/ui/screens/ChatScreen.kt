package com.aiagents.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aiagents.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: MainViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAgent by viewModel.selectedAgent.collectAsState()
    val agents = viewModel.availableAgents
    val listState = rememberLazyListState()
    val error by viewModel.error.collectAsState()

    var input by remember { mutableStateOf(TextFieldValue()) }
    var useMultiAgent by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachFile(it) }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    error?.let {
        LaunchedEffect(it) {
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Agents") },
                actions = {
                    IconButton(onClick = { viewModel.toggleSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (agents.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        agents.forEach { agent ->
                            val isSelected = selectedAgent?.id == agent.id
                            val color = Color(agent.color)
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                                color = if (isSelected) color.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                    2.dp, color
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .clickable { viewModel.selectAgent(agent) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(agent.icon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        agent.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = useMultiAgent,
                            onCheckedChange = { useMultiAgent = it }
                        )
                        Text("Multi", style = MaterialTheme.typography.bodySmall)
                    }

                    IconButton(onClick = { filePicker.launch("*/*") }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "File")
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (input.text.isNotBlank()) {
                                viewModel.sendMessage(input.text, useMultiAgent)
                                input = TextFieldValue()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    val isError = msg.role == "error"
                    val backgroundColor = when {
                        isError -> MaterialTheme.colorScheme.errorContainer
                        isUser -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = alignment
                    ) {
                        Text(
                            text = msg.content,
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(backgroundColor)
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}
