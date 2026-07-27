package com.aiagents.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aiagents.app.data.model.ApiKeys

@Composable
fun SettingsScreen(
    currentKeys: ApiKeys,
    onSave: (ApiKeys) -> Unit,
    onDismiss: () -> Unit
) {
    var gemini by remember { mutableStateOf(currentKeys.gemini) }
    var deepseek by remember { mutableStateOf(currentKeys.deepseek) }
    var qwen by remember { mutableStateOf(currentKeys.qwen) }
    var mistral by remember { mutableStateOf(currentKeys.mistral) }
    var showPasswords by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("API Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Enter at least one key. All keys are stored locally.",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = gemini,
                    onValueChange = { gemini = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIza...") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deepseek,
                    onValueChange = { deepseek = it },
                    label = { Text("DeepSeek API Key") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qwen,
                    onValueChange = { qwen = it },
                    label = { Text("Qwen API Key") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mistral,
                    onValueChange = { mistral = it },
                    label = { Text("Mistral API Key") },
                    placeholder = { Text("...") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showPasswords = !showPasswords }) {
                        Text(if (showPasswords) "Hide" else "Show")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(ApiKeys(gemini, deepseek, qwen, mistral)) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
