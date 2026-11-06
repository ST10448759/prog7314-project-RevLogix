package com.revlogix.app.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.revlogix.app.auth.FirebaseSession
import com.revlogix.app.data.local.AppDatabase
import com.revlogix.app.data.local.SyncQueueItem
import com.revlogix.app.data.remote.DeviceRegistration
import com.revlogix.app.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

/**
 * Pushes everything waiting in the sync_queue to the API, oldest first.
 * Order matters: a vehicle is always created before the fuel logs that belong to it.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (FirebaseSession.user() == null) {
            Log.d(TAG, "Nobody signed in, nothing to sync")
            return Result.success()
        }

        registerDeviceToken()

        val queueDao = AppDatabase.getDatabase(applicationContext).syncQueueDao()
        val pending = queueDao.getPendingItems()
        Log.i(TAG, "Sync started with ${pending.size} queued change(s)")

        for (item in pending) {
            val outcome = try {
                push(item)
            } catch (e: IOException) {
                // No connection / server asleep - keep the queue as it is and try again later
                Log.w(TAG, "Network problem while syncing: ${e.message}")
                return Result.retry()
            } catch (e: HttpException) {
                SyncPolicy.outcomeForHttp(e.code())
            }

            when (outcome) {
                SyncPolicy.Outcome.DONE -> queueDao.delete(item.syncId)
                SyncPolicy.Outcome.RETRY -> {
                    queueDao.update(item.copy(retryCount = item.retryCount + 1))
                    return Result.retry()
                }
                SyncPolicy.Outcome.GIVE_UP -> {
                    Log.e(TAG, "Server rejected ${item.entityType}/${item.entityId}, marking as failed")
                    queueDao.update(item.copy(status = "FAILED"))
                }
            }
        }

        Log.i(TAG, "Sync finished")
        return Result.success()
    }

    private suspend fun push(item: SyncQueueItem): SyncPolicy.Outcome {
        val api = RetrofitClient.apiService

        val response = if (item.operation == SyncTypes.DELETE) {
            api.remove(item.entityType, item.entityId)
        } else {
            val body = JsonParser.parseString(item.payloadJson).asJsonObject
            if (item.entityType == SyncTypes.EXPENSES) uploadReceiptIfNeeded(body)
            api.upsert(item.entityType, item.entityId, body)
        }

        val outcome = SyncPolicy.outcomeForHttp(response.code())
        if (outcome == SyncPolicy.Outcome.RETRY && SyncPolicy.hasExhaustedRetries(item.retryCount)) {
            return SyncPolicy.Outcome.GIVE_UP
        }
        return outcome
    }

    /**
     * Expenses can have a receipt photo saved on the phone. Before the expense itself is sent,
     * the photo goes to the API's blob storage and the local path is swapped for the URL.
     */
    private suspend fun uploadReceiptIfNeeded(body: JsonObject) {
        val path = body.get("receiptUri")?.takeIf { !it.isJsonNull }?.asString ?: return
        if (!path.startsWith("/")) return // already a URL

        val file = File(path)
        if (!file.exists()) {
            Log.w(TAG, "Receipt file missing, sending expense without it")
            body.remove("receiptUri")
            return
        }

        val part = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody("image/jpeg".toMediaType())
        )
        val uploaded = RetrofitClient.apiService.uploadReceipt(part)
        body.addProperty("receiptUri", uploaded.url)
        Log.d(TAG, "Receipt uploaded to blob storage")
    }

    /** Tells the API which phone to send push notifications to. Only re-sent when the token changes. */
    private suspend fun registerDeviceToken() {
        try {
            val token = Tasks.await(FirebaseMessaging.getInstance().token)
            val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val uid = FirebaseSession.user()?.uid ?: return
            val registeredKey = "fcm_registered_$uid"
            if (prefs.getString(registeredKey, null) == token) return

            val response = RetrofitClient.apiService.registerDevice(DeviceRegistration(token))
            if (response.isSuccessful) {
                prefs.edit().putString(registeredKey, token).apply()
                Log.i(TAG, "FCM token registered with the API")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register FCM token yet: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "SyncWorker"
        const val PREFS = "revlogix_prefs"
    }
}
