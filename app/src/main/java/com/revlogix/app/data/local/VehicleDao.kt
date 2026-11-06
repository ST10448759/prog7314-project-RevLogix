package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)

    @Query("SELECT * FROM vehicles WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getVehiclesForUser(userId: Int): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId")
    fun getVehicleById(vehicleId: Int): Flow<Vehicle?>

    // Used to decide whether a fresh install should restore data from the cloud
    @Query("SELECT COUNT(*) FROM vehicles WHERE userId = :userId")
    suspend fun countForUser(userId: Int): Int
}
