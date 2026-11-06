package com.revlogix.app.data.repository

import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.data.local.MaintenanceRecordDao
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.data.sync.SyncTypes
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(
    private val dao: MaintenanceRecordDao,
    private val sync: SyncEnqueuer
) {
    fun getRecordsForVehicle(vehicleId: Int): Flow<List<MaintenanceRecord>> = dao.getRecordsForVehicle(vehicleId)

    suspend fun addRecord(record: MaintenanceRecord): Long {
        val id = dao.insert(record)
        sync.created(SyncTypes.MAINTENANCE, id.toInt(), record.copy(maintenanceId = id.toInt()))
        return id
    }

    suspend fun updateRecord(record: MaintenanceRecord) {
        dao.update(record)
        sync.updated(SyncTypes.MAINTENANCE, record.maintenanceId, record)
    }

    suspend fun deleteRecord(record: MaintenanceRecord) {
        dao.delete(record)
        sync.deleted(SyncTypes.MAINTENANCE, record.maintenanceId)
    }
}
