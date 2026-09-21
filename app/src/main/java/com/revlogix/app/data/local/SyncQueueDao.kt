package com.revlogix.app.data.local

import androidx.room.*

@Dao
interface SyncQueueDao {
    @Insert
    suspend fun insert(item: SyncQueueItem): Long

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<SyncQueueItem>

    @Update
    suspend fun update(item: SyncQueueItem)

    @Query("DELETE FROM sync_queue WHERE syncId = :syncId")
    suspend fun delete(syncId: Int)
}