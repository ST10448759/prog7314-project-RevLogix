package com.revlogix.app.data.sync

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.revlogix.app.data.local.SyncQueueDao
import com.revlogix.app.data.local.SyncQueueItem

/**
 * Called by the repositories after every local write. The change is stored in the sync_queue
 * table (so it survives the app being closed) and a sync run is requested.
 */
class SyncEnqueuer(
    private val queueDao: SyncQueueDao,
    private val context: Context
) {
    private val gson = Gson()

    suspend fun created(type: String, id: Int, entity: Any) = enqueue(type, id, SyncTypes.CREATE, gson.toJson(entity))

    suspend fun updated(type: String, id: Int, entity: Any) = enqueue(type, id, SyncTypes.UPDATE, gson.toJson(entity))

    suspend fun deleted(type: String, id: Int) = enqueue(type, id, SyncTypes.DELETE, "{}")

    private suspend fun enqueue(type: String, id: Int, operation: String, payload: String) {
        queueDao.insert(
            SyncQueueItem(entityType = type, entityId = id, operation = operation, payloadJson = payload)
        )
        Log.d(TAG, "Queued $operation for $type/$id")
        SyncScheduler.requestSync(context)
    }

    private companion object {
        const val TAG = "SyncEnqueuer"
    }
}
