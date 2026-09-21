package com.revlogix.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY expenseDate DESC")
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>>
}