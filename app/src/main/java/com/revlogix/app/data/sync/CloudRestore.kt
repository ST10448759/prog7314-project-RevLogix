package com.revlogix.app.data.sync

import android.util.Log
import com.google.gson.Gson
import com.revlogix.app.data.local.AppDatabase
import com.revlogix.app.data.local.CustomPart
import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.remote.RetrofitClient

/**
 * Pulls a user's data down from the API after signing in on a phone that has nothing stored yet
 * (a new phone or a reinstall). It only runs when the local database has no vehicles, so it can
 * never overwrite changes that are still waiting to be synced.
 */
class CloudRestore(private val db: AppDatabase) {
    private val gson = Gson()

    suspend fun restoreIfEmpty(userId: Int) {
        if (db.vehicleDao().countForUser(userId) > 0) return

        try {
            val api = RetrofitClient.apiService

            // Parents first so the foreign keys are satisfied
            api.list(SyncTypes.VEHICLES).forEach {
                db.vehicleDao().insert(gson.fromJson(it, Vehicle::class.java).copy(userId = userId))
            }
            api.list(SyncTypes.FUEL_LOGS).forEach {
                db.fuelLogDao().insert(gson.fromJson(it, FuelLog::class.java))
            }
            api.list(SyncTypes.EXPENSES).forEach {
                db.expenseDao().insert(gson.fromJson(it, Expense::class.java))
            }
            api.list(SyncTypes.MAINTENANCE).forEach {
                db.maintenanceRecordDao().insert(gson.fromJson(it, MaintenanceRecord::class.java))
            }
            api.list(SyncTypes.PARTS).forEach {
                db.customPartDao().insert(gson.fromJson(it, CustomPart::class.java))
            }
            Log.i(TAG, "Restored data from the cloud")
        } catch (e: Exception) {
            // Offline or API asleep: the app still works, the restore is retried next sign-in
            Log.w(TAG, "Cloud restore skipped: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "CloudRestore"
    }
}
