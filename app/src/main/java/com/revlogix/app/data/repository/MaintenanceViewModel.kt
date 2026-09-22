package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.data.repository.MaintenanceRepository
import com.revlogix.app.domain.model.HealthScoreCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val repository: MaintenanceRepository,
    private val vehicleId: Int,
    private val currentOdometer: Int
) : ViewModel() {

    val records: StateFlow<List<MaintenanceRecord>> = repository.getRecordsForVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthScore: StateFlow<Int> = records
        .map { HealthScoreCalculator.calculateHealthScore(it, currentOdometer) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val badge: StateFlow<String> = healthScore
        .map { HealthScoreCalculator.badgeForScore(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Master Tuner")

    fun statusFor(record: MaintenanceRecord): String =
        HealthScoreCalculator.calculateStatus(record, currentOdometer)

    fun addRecord(
        serviceType: String,
        odometer: Int,
        cost: Double,
        nextDueInMonths: Int?,
        nextDueOdometer: Int?
    ) {
        viewModelScope.launch {
            val nextDueDate = nextDueInMonths?.let {
                System.currentTimeMillis() + (it.toLong() * 30L * 24 * 60 * 60 * 1000)
            }
            repository.addRecord(
                MaintenanceRecord(
                    vehicleId = vehicleId,
                    serviceType = serviceType,
                    serviceDate = System.currentTimeMillis(),
                    odometer = odometer,
                    cost = cost,
                    nextDueDate = nextDueDate,
                    nextDueOdometer = nextDueOdometer
                )
            )
        }
    }

    companion object {
        fun provideFactory(
            repository: MaintenanceRepository,
            vehicleId: Int,
            currentOdometer: Int
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MaintenanceViewModel(repository, vehicleId, currentOdometer) as T
                }
            }
    }
}