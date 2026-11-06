package com.revlogix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlogix.app.R

@Composable
fun LoginScreen(
    onSignIn: () -> Unit,
    isSigningIn: Boolean,
    errorRes: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.login_tagline), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onSignIn, enabled = !isSigningIn, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(
                    if (isSigningIn) R.string.login_signing_in else R.string.login_google
                )
            )
        }
        if (errorRes != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(errorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
