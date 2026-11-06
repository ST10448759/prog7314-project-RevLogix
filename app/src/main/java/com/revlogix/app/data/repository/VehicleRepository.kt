package com.revlogix.app.data.repository

import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.data.local.VehicleDao
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.data.sync.SyncTypes
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val sync: SyncEnqueuer
) {
    fun getVehiclesForUser(userId: Int): Flow<List<Vehicle>> = vehicleDao.getVehiclesForUser(userId)
    fun getVehicleById(vehicleId: Int): Flow<Vehicle?> = vehicleDao.getVehicleById(vehicleId)

    suspend fun addVehicle(vehicle: Vehicle): Long {
        val id = vehicleDao.insert(vehicle)
        sync.created(SyncTypes.VEHICLES, id.toInt(), vehicle.copy(vehicleId = id.toInt()))
        return id
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.update(vehicle)
        sync.updated(SyncTypes.VEHICLES, vehicle.vehicleId, vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.delete(vehicle)
        sync.deleted(SyncTypes.VEHICLES, vehicle.vehicleId)
    }
}
