package com.revlogix.app.data.repository

import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.local.VehicleDao
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
    fun getVehiclesForUser(userId: Int): Flow<List<Vehicle>> = vehicleDao.getVehiclesForUser(userId)
    fun getVehicleById(vehicleId: Int): Flow<Vehicle?> = vehicleDao.getVehicleById(vehicleId)
    suspend fun addVehicle(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)
}