package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId AND isCompleted = 0")
    fun getActiveReminders(vehicleId: Int): Flow<List<Reminder>>
}