package com.revlogix.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userId: Int
) : ViewModel() {

    val vehicles: StateFlow<List<Vehicle>> = vehicleRepository.getVehiclesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addVehicle(make: String, model: String, year: Int, bodyStyle: String, registrationNumber: String, odometer: Int) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                userId = userId, make = make, model = model, year = year,
                bodyStyle = bodyStyle, registrationNumber = registrationNumber, currentOdometer = odometer
            )
            // Saved in Room first (works offline); the repository queues it for the API
            val id = vehicleRepository.addVehicle(vehicle)
            Log.d(TAG, "Vehicle $id saved locally")
        }
    }

    companion object {
        private const val TAG = "VehicleViewModel"

        fun provideFactory(vehicleRepository: VehicleRepository, userId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VehicleViewModel(vehicleRepository, userId) as T
                }
            }
    }
}
