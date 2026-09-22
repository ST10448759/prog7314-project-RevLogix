package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.data.repository.ExpenseRepository
import com.revlogix.app.data.repository.FuelRepository
import com.revlogix.app.domain.model.FuelEconomyCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FuelExpenseViewModel(
    private val fuelRepository: FuelRepository,
    private val expenseRepository: ExpenseRepository,
    private val vehicleId: Int
) : ViewModel() {

    val fuelLogs: StateFlow<List<FuelLog>> = fuelRepository.getFuelLogsForVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = expenseRepository.getExpensesForVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageEconomy: StateFlow<Double?> = fuelLogs
        .map { FuelEconomyCalculator.calculateLitresPer100Km(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addFuelLog(odometer: Int, litres: Double, totalCost: Double, fuelType: String, fullTank: Boolean) {
        viewModelScope.launch {
            fuelRepository.addFuelLog(
                FuelLog(
                    vehicleId = vehicleId,
                    fillUpDate = System.currentTimeMillis(),
                    odometer = odometer,
                    litres = litres,
                    totalCost = totalCost,
                    fuelType = fuelType,
                    fullTank = fullTank
                )
            )
        }
    }

    fun addExpense(category: String, amount: Double, description: String) {
        viewModelScope.launch {
            expenseRepository.addExpense(
                Expense(
                    vehicleId = vehicleId,
                    expenseDate = System.currentTimeMillis(),
                    category = category,
                    amount = amount,
                    description = description
                )
            )
        }
    }

    companion object {
        fun provideFactory(
            fuelRepository: FuelRepository,
            expenseRepository: ExpenseRepository,
            vehicleId: Int
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FuelExpenseViewModel(fuelRepository, expenseRepository, vehicleId) as T
                }
            }
    }
}