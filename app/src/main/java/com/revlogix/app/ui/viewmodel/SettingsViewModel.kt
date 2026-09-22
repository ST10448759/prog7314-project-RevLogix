package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.User
import com.revlogix.app.data.local.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userDao: UserDao) : ViewModel() {

    val user: StateFlow<User?> = userDao.getUserById(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            user.value?.let {
                userDao.update(it.copy(preferredLanguage = languageCode))
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let {
                userDao.update(it.copy(biometricEnabled = enabled))
            }
        }
    }

    companion object {
        fun provideFactory(userDao: UserDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(userDao) as T
                }
            }
    }
}