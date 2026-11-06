package com.revlogix.app.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules SyncWorker with WorkManager. The "network connected" constraint is what makes the
 * queue flush by itself as soon as the phone gets a connection back after being offline.
 */
object SyncScheduler {
    private const val TAG = "SyncScheduler"
    private const val ONE_TIME_NAME = "revlogix_sync_now"
    private const val PERIODIC_NAME = "revlogix_sync_periodic"

    private val needsNetwork = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Queue a sync run - it starts when the device is online. */
    fun requestSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(needsNetwork)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        Log.d(TAG, "Sync requested")
    }

    /** Safety net: also try every 15 minutes while there is a connection. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(needsNetwork)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
