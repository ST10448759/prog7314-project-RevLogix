package com.revlogix.app.data.repository

import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.ExpenseDao
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.data.sync.SyncTypes
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val sync: SyncEnqueuer
) {
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> = dao.getExpensesForVehicle(vehicleId)

    suspend fun addExpense(expense: Expense): Long {
        val id = dao.insert(expense)
        sync.created(SyncTypes.EXPENSES, id.toInt(), expense.copy(expenseId = id.toInt()))
        return id
    }

    suspend fun deleteExpense(expense: Expense) {
        dao.delete(expense)
        sync.deleted(SyncTypes.EXPENSES, expense.expenseId)
    }
}
