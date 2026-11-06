package com.revlogix.app.auth

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Fingerprint / face unlock using the AndroidX BiometricPrompt. BIOMETRIC_WEAK accepts any
 * enrolled fingerprint or face unlock the phone offers (strong biometrics count as well).
 */
object BiometricHelper {
    private const val TAG = "BiometricHelper"
    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        cancelText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.i(TAG, "Biometric authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.w(TAG, "Biometric error $errorCode: $errString")
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                // A single bad scan - the prompt stays open so the user can try again
                Log.d(TAG, "Biometric not recognised")
            }
        }

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(cancelText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(info)
    }
}

/** Compose gives us a Context; this walks up to the hosting activity. */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}
