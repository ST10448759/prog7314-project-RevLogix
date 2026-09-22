package com.revlogix.app.data.repository

import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> = dao.getExpensesForVehicle(vehicleId)
    suspend fun addExpense(expense: Expense): Long = dao.insert(expense)
    suspend fun deleteExpense(expense: Expense) = dao.delete(expense)
}