package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fuelLog: FuelLog): Long

    @Delete
    suspend fun delete(fuelLog: FuelLog)

    // Newest fill-up first, so the fuel economy calculation can grab
    // the two most recent full-tank entries easily.
    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY fillUpDate DESC")
    fun getFuelLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>>
}