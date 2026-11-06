package com.revlogix.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revlogix.app.data.local.CustomPart
import com.revlogix.app.data.repository.CustomPartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomPartViewModel(
    private val repository: CustomPartRepository,
    private val vehicleId: Int
) : ViewModel() {

    val parts: StateFlow<List<CustomPart>> = repository.getPartsForVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPart(
        category: String,
        partName: String,
        brand: String,
        partNumber: String,
        serialNumber: String,
        technicalSpecs: String,
        vendor: String,
        cost: Double
    ) {
        viewModelScope.launch {
            repository.addPart(
                CustomPart(
                    vehicleId = vehicleId,
                    category = category,
                    partName = partName,
                    brand = brand,
                    partNumber = partNumber,
                    serialNumber = serialNumber,
                    technicalSpecs = technicalSpecs,
                    vendor = vendor,
                    purchaseDate = System.currentTimeMillis(),
                    installDate = System.currentTimeMillis(),
                    cost = cost
                )
            )
        }
    }

    companion object {
        fun provideFactory(repository: CustomPartRepository, vehicleId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CustomPartViewModel(repository, vehicleId) as T
                }
            }
    }
}