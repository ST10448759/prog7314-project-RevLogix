package com.revlogix.app.data.repository

import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.data.local.FuelLogDao
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.data.sync.SyncTypes
import kotlinx.coroutines.flow.Flow

class FuelRepository(
    private val dao: FuelLogDao,
    private val sync: SyncEnqueuer
) {
    fun getFuelLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>> = dao.getFuelLogsForVehicle(vehicleId)

    suspend fun addFuelLog(log: FuelLog): Long {
        val id = dao.insert(log)
        sync.created(SyncTypes.FUEL_LOGS, id.toInt(), log.copy(fuelLogId = id.toInt()))
        return id
    }

    suspend fun deleteFuelLog(log: FuelLog) {
        dao.delete(log)
        sync.deleted(SyncTypes.FUEL_LOGS, log.fuelLogId)
    }
}
