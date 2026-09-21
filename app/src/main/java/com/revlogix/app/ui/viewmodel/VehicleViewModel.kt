package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.repository.UserRepository
import com.revlogix.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Fixed placeholder id until real SSO (FR-01) replaces it later.
const val TEMP_USER_ID = 1

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            userRepository.ensurePlaceholderUser()
        }
    }

    val vehicles: StateFlow<List<Vehicle>> = vehicleRepository.getVehiclesForUser(TEMP_USER_ID)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addVehicle(make: String, model: String, year: Int, bodyStyle: String, registrationNumber: String, odometer: Int) {
        viewModelScope.launch {
            vehicleRepository.addVehicle(
                Vehicle(
                    userId = TEMP_USER_ID,
                    make = make,
                    model = model,
                    year = year,
                    bodyStyle = bodyStyle,
                    registrationNumber = registrationNumber,
                    currentOdometer = odometer
                )
            )
        }
    }

    companion object {
        fun provideFactory(
            vehicleRepository: VehicleRepository,
            userRepository: UserRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VehicleViewModel(vehicleRepository, userRepository) as T
                }
            }
    }
}