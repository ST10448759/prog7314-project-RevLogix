package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.remote.RetrofitClient
import com.revlogix.app.data.remote.VehicleDto
import com.revlogix.app.data.repository.UserRepository
import com.revlogix.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TEMP_USER_ID = 1

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    init {
        viewModelScope.launch { userRepository.ensurePlaceholderUser() }
    }

    val vehicles: StateFlow<List<Vehicle>> = vehicleRepository.getVehiclesForUser(TEMP_USER_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addVehicle(make: String, model: String, year: Int, bodyStyle: String, registrationNumber: String, odometer: Int) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                userId = TEMP_USER_ID, make = make, model = model, year = year,
                bodyStyle = bodyStyle, registrationNumber = registrationNumber, currentOdometer = odometer
            )
            vehicleRepository.addVehicle(vehicle)

            try {
                RetrofitClient.apiService.createVehicle(
                    VehicleDto(
                        userId = vehicle.userId, make = vehicle.make, model = vehicle.model,
                        year = vehicle.year, bodyStyle = vehicle.bodyStyle,
                        registrationNumber = vehicle.registrationNumber, currentOdometer = vehicle.currentOdometer
                    )
                )
            } catch (e: Exception) {
                // Network unavailable or API unreachable - fine, vehicle is safe in Room.
            }
        }
    }

    companion object {
        fun provideFactory(vehicleRepository: VehicleRepository, userRepository: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VehicleViewModel(vehicleRepository, userRepository) as T
                }
            }
    }
}