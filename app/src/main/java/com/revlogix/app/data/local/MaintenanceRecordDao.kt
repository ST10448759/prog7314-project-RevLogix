package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MaintenanceRecord): Long

    @Update
    suspend fun update(record: MaintenanceRecord)

    @Delete
    suspend fun delete(record: MaintenanceRecord)

    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC")
    fun getRecordsForVehicle(vehicleId: Int): Flow<List<MaintenanceRecord>>
}