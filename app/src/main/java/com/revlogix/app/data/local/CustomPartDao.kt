package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(part: CustomPart): Long

    @Update
    suspend fun update(part: CustomPart)

    @Delete
    suspend fun delete(part: CustomPart)

    @Query("SELECT * FROM custom_parts WHERE vehicleId = :vehicleId ORDER BY category")
    fun getPartsForVehicle(vehicleId: Int): Flow<List<CustomPart>>
}