package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Stubbed SSO for the practice build. Simulates the OpenID Connect /
 * Entra ID sign-in from Part 1 (FR-01) without a real Microsoft network
 * call. A real integration needs MSAL, an Azure App Registration, and a
 * signed redirect URI - external Azure Portal setup, not just code.
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun signInWithSso() {
        viewModelScope.launch {
            userRepository.ensurePlaceholderUser()
            _isLoggedIn.value = true
        }
    }

    fun signOut() {
        _isLoggedIn.value = false
    }

    companion object {
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(userRepository) as T
                }
            }
    }
}