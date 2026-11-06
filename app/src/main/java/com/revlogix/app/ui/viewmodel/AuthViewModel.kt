package com.revlogix.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.R
import com.revlogix.app.auth.FirebaseSession
import com.revlogix.app.auth.GoogleSignInHelper
import com.revlogix.app.data.local.User
import com.revlogix.app.data.repository.UserRepository
import com.revlogix.app.data.sync.CloudRestore
import com.revlogix.app.data.sync.SyncScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Where the user is in the sign-in flow. */
sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    /** Signed in, but biometric unlock is switched on and has not been passed yet. */
    data class Locked(val user: User) : AuthState
    data class Ready(val user: User) : AuthState
}

/**
 * Real SSO (FR-01): Google account via Credential Manager -> Firebase Auth.
 * Replaces the stubbed sign-in from the Part 2 prototype.
 */
class AuthViewModel(
    private val appContext: Context,
    private val userRepository: UserRepository,
    private val googleSignIn: GoogleSignInHelper,
    private val cloudRestore: CloudRestore
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    /** String resource id of the latest error, or null. */
    private val _errorRes = MutableStateFlow<Int?>(null)
    val errorRes: StateFlow<Int?> = _errorRes

    private val _signingIn = MutableStateFlow(false)
    val signingIn: StateFlow<Boolean> = _signingIn

    init {
        viewModelScope.launch { restoreSession() }
    }

    /** Firebase keeps the session between launches, so an existing user goes straight in. */
    private suspend fun restoreSession() {
        val firebaseUser = FirebaseSession.user()
        if (firebaseUser == null) {
            Log.d(TAG, "No saved session")
            _state.value = AuthState.SignedOut
            return
        }
        val user = userRepository.getOrCreate(
            firebaseUser.uid, firebaseUser.displayName.orEmpty(), firebaseUser.email.orEmpty()
        )
        Log.i(TAG, "Restored session for local user ${user.userId}")
        _state.value = if (user.biometricEnabled) AuthState.Locked(user) else AuthState.Ready(user)
        afterSignIn(user)
    }

    fun signInWithGoogle(activityContext: Context) {
        if (_signingIn.value) return
        viewModelScope.launch {
            _signingIn.value = true
            _errorRes.value = null
            try {
                if (!googleSignIn.isConfigured(appContext)) {
                    _errorRes.value = R.string.login_not_configured
                    return@launch
                }
                val firebaseUser = googleSignIn.signIn(activityContext)
                if (firebaseUser == null) {
                    _errorRes.value = R.string.login_failed
                    return@launch
                }
                val user = userRepository.getOrCreate(
                    firebaseUser.uid, firebaseUser.displayName.orEmpty(), firebaseUser.email.orEmpty()
                )
                _state.value = AuthState.Ready(user)
                afterSignIn(user)
            } catch (e: Exception) {
                // Includes the user backing out of the account picker
                Log.w(TAG, "Sign-in did not complete: ${e.message}")
                _errorRes.value = R.string.login_failed
            } finally {
                _signingIn.value = false
            }
        }
    }

    private suspend fun afterSignIn(user: User) {
        cloudRestore.restoreIfEmpty(user.userId)
        SyncScheduler.requestSync(appContext)
    }

    fun unlock() {
        val current = _state.value
        if (current is AuthState.Locked) _state.value = AuthState.Ready(current.user)
    }

    fun signOut() {
        viewModelScope.launch {
            googleSignIn.signOut()
            _state.value = AuthState.SignedOut
            Log.i(TAG, "Signed out")
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"

        fun provideFactory(
            appContext: Context,
            userRepository: UserRepository,
            googleSignIn: GoogleSignInHelper,
            cloudRestore: CloudRestore
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(appContext, userRepository, googleSignIn, cloudRestore) as T
                }
            }
    }
}
