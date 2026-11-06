package com.revlogix.app

import android.app.Application
import android.util.Log
import com.revlogix.app.data.sync.SyncScheduler
import com.revlogix.app.notifications.NotificationHelper

class RevLogixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "RevLogix ${BuildConfig.VERSION_NAME} starting")
        NotificationHelper.createChannel(this)
        SyncScheduler.schedulePeriodic(this)
    }

    private companion object {
        const val TAG = "RevLogixApplication"
    }
}
