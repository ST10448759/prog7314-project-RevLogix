package com.revlogix.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revlogix.app.R
import com.revlogix.app.auth.BiometricHelper
import com.revlogix.app.auth.findFragmentActivity
import com.revlogix.app.ui.viewmodel.SettingsViewModel
import com.revlogix.app.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val notice by viewModel.notice.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }

    // Resolved here because they are used inside callbacks
    val promptTitle = stringResource(R.string.biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_subtitle)
    val promptCancel = stringResource(R.string.biometric_cancel)
    val unavailableText = stringResource(R.string.biometric_unavailable)
    val enabledText = stringResource(R.string.biometric_enabled)

    val languageNames = mapOf(
        "en" to stringResource(R.string.language_en),
        "af" to stringResource(R.string.language_af),
        "zu" to stringResource(R.string.language_zu)
    )
    val currentLanguage = LocaleHelper.savedLanguage(context)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user?.fullName.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_language)) },
                supportingContent = { Text(languageNames[currentLanguage] ?: currentLanguage) },
                modifier = Modifier.clickable { showLanguageMenu = true }
            )
            if (showLanguageMenu) {
                AlertDialog(
                    onDismissRequest = { showLanguageMenu = false },
                    title = { Text(stringResource(R.string.settings_language_hint)) },
                    text = {
                        Column {
                            LocaleHelper.supported.forEach { code ->
                                TextButton(onClick = {
                                    showLanguageMenu = false
                                    if (code != currentLanguage) {
                                        viewModel.setLanguage(code)
                                        // Recreate so every screen picks up the new language
                                        (context as? Activity)?.recreate()
                                    }
                                }) { Text(languageNames[code] ?: code) }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_biometric)) },
                supportingContent = { Text(stringResource(R.string.settings_biometric_hint)) },
                trailingContent = {
                    Switch(
                        checked = user?.biometricEnabled ?: false,
                        onCheckedChange = { wantsOn ->
                            if (!wantsOn) {
                                viewModel.setBiometricEnabled(false)
                            } else if (!BiometricHelper.isAvailable(context)) {
                                Toast.makeText(context, unavailableText, Toast.LENGTH_LONG).show()
                            } else {
                                // Only switch it on after the user has proven they can pass the prompt
                                val activity = context.findFragmentActivity()
                                if (activity != null) {
                                    BiometricHelper.prompt(
                                        activity = activity,
                                        title = promptTitle,
                                        subtitle = promptSubtitle,
                                        cancelText = promptCancel,
                                        onSuccess = {
                                            viewModel.setBiometricEnabled(true)
                                            Toast.makeText(context, enabledText, Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { }
                                    )
                                }
                            }
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_sync)) },
                supportingContent = {
                    Text(
                        if (pendingCount == 0) stringResource(R.string.settings_sync_done)
                        else stringResource(R.string.settings_sync_pending, pendingCount)
                    )
                },
                trailingContent = {
                    TextButton(onClick = { viewModel.syncNow() }) {
                        Text(stringResource(R.string.settings_sync_now))
                    }
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                supportingContent = { notice?.let { Text(stringResource(it)) } },
                trailingContent = {
                    TextButton(onClick = { viewModel.sendTestNotification() }) {
                        Text(stringResource(R.string.settings_test_notification))
                    }
                }
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_sign_out))
            }
        }
    }
}
