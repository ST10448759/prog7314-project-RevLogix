package com.revlogix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revlogix.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    val user by viewModel.user.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings & Profile", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(user?.fullName ?: "Local Test User", style = MaterialTheme.typography.titleMedium)
                Text(user?.email ?: "test@revlogix.local", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("Language") },
            supportingContent = { Text(user?.preferredLanguage ?: "en") },
            modifier = Modifier.clickable { showLanguageMenu = true }
        )
        if (showLanguageMenu) {
            AlertDialog(
                onDismissRequest = { showLanguageMenu = false },
                title = { Text("Choose language") },
                text = {
                    Column {
                        listOf("en" to "English", "zu" to "isiZulu", "af" to "Afrikaans").forEach { (code, label) ->
                            TextButton(onClick = {
                                viewModel.setLanguage(code)
                                showLanguageMenu = false
                            }) { Text(label) }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Biometric unlock") },
            trailingContent = {
                Switch(
                    checked = user?.biometricEnabled ?: false,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                )
            }
        )

        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
    }
}