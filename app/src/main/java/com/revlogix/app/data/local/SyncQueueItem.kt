package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local queue of operations waiting to be pushed to the API.
 * Supports the offline-first sync design from Part 1 (FR-09).
 */
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val syncId: Int = 0,
    val entityType: String,   // e.g. "FuelLog", "MaintenanceRecord"
    val entityId: Int,
    val operation: String,    // "CREATE", "UPDATE" or "DELETE"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING" // PENDING, SYNCED, FAILED
)

