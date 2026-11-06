package com.revlogix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlogix.app.R
import com.revlogix.app.auth.BiometricHelper
import com.revlogix.app.auth.findFragmentActivity

/**
 * Shown on launch when the user has turned on biometric unlock.
 * The prompt opens automatically; the button lets them try again after cancelling.
 */
@Composable
fun BiometricLockScreen(
    onUnlocked: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val title = stringResource(R.string.biometric_prompt_title)
    val subtitle = stringResource(R.string.biometric_prompt_subtitle)
    val cancel = stringResource(R.string.biometric_cancel)
    val unavailableText = stringResource(R.string.biometric_unavailable)
    var message by remember { mutableStateOf<String?>(null) }

    fun showPrompt() {
        val activity = context.findFragmentActivity() ?: return
        if (!BiometricHelper.isAvailable(context)) {
            message = unavailableText
            return
        }
        BiometricHelper.prompt(
            activity = activity,
            title = title,
            subtitle = subtitle,
            cancelText = cancel,
            onSuccess = onUnlocked,
            onError = { message = it }
        )
    }

    LaunchedEffect(Unit) { showPrompt() }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.lock_title), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { showPrompt() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.lock_unlock))
        }
        message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Way out if biometrics were removed from the phone after being enabled in the app
        TextButton(onClick = onSignOut) { Text(stringResource(R.string.settings_sign_out)) }
    }
}
