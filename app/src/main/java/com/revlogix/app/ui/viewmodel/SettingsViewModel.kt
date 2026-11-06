package com.revlogix.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.R
import com.revlogix.app.data.local.SyncQueueDao
import com.revlogix.app.data.local.User
import com.revlogix.app.data.local.UserDao
import com.revlogix.app.data.remote.RetrofitClient
import com.revlogix.app.data.sync.SyncScheduler
import com.revlogix.app.util.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appContext: Context,
    private val userDao: UserDao,
    syncQueueDao: SyncQueueDao,
    userId: Int
) : ViewModel() {

    val user: StateFlow<User?> = userDao.getUserById(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** How many changes are still waiting to be pushed to the API. */
    val pendingSyncCount: StateFlow<Int> = syncQueueDao.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** String resource id of a short status message, or null. */
    private val _notice = MutableStateFlow<Int?>(null)
    val notice: StateFlow<Int?> = _notice

    fun setLanguage(languageCode: String) {
        // The saved code drives the app's resources; the Room copy keeps it with the profile
        LocaleHelper.saveLanguage(appContext, languageCode)
        viewModelScope.launch {
            user.value?.let {
                userDao.update(it.copy(preferredLanguage = languageCode))
            }
        }
        Log.i(TAG, "Language set to $languageCode")
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let {
                userDao.update(it.copy(biometricEnabled = enabled))
            }
            Log.i(TAG, "Biometric unlock enabled=$enabled")
        }
    }

    fun syncNow() {
        SyncScheduler.requestSync(appContext)
    }

    /** Asks the API to send a push notification to this phone (demonstrates FCM end to end). */
    fun sendTestNotification() {
        viewModelScope.launch {
            _notice.value = try {
                val response = RetrofitClient.apiService.sendTestNotification()
                if (response.isSuccessful) R.string.notice_test_sent else R.string.notice_test_failed
            } catch (e: Exception) {
                Log.w(TAG, "Test notification failed: ${e.message}")
                R.string.notice_test_failed
            }
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"

        fun provideFactory(
            appContext: Context,
            userDao: UserDao,
            syncQueueDao: SyncQueueDao,
            userId: Int
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(appContext, userDao, syncQueueDao, userId) as T
                }
            }
    }
}
