package com.revlogix.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Single sign-on with a Google account. Android's Credential Manager shows the account picker,
 * and the Google ID token it returns is exchanged for a Firebase session. The first time an
 * account signs in, Firebase creates it - so this one flow covers both register and log in.
 */
class GoogleSignInHelper(context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /** The web client ID is generated from google-services.json; 0 means the file was not added. */
    private fun webClientId(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }

    fun isConfigured(context: Context): Boolean = webClientId(context) != null

    /** [activityContext] must be an Activity so the account picker can be shown. */
    suspend fun signIn(activityContext: Context): FirebaseUser? {
        val clientId = webClientId(activityContext) ?: run {
            Log.e(TAG, "default_web_client_id missing - add google-services.json")
            return null
        }

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false) // also offer accounts that have not used the app yet
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val credential = credentialManager.getCredential(activityContext, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)
            val result = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            Log.i(TAG, "Signed in with Google")
            return result.user
        }

        Log.w(TAG, "Unexpected credential type: ${credential.type}")
        return null
    }

    suspend fun signOut() {
        FirebaseSession.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "Could not clear credential state: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "GoogleSignInHelper"
    }
}
