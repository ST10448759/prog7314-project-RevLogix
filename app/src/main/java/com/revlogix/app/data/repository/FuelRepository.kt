package com.revlogix.app.data.repository

import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.data.local.FuelLogDao
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val dao: FuelLogDao) {
    fun getFuelLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>> = dao.getFuelLogsForVehicle(vehicleId)
    suspend fun addFuelLog(log: FuelLog): Long = dao.insert(log)
    suspend fun deleteFuelLog(log: FuelLog) = dao.delete(log)
}