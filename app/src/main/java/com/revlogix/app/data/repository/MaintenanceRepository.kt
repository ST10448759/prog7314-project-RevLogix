package com.revlogix.app.data.repository

import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.data.local.MaintenanceRecordDao
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val dao: MaintenanceRecordDao) {
    fun getRecordsForVehicle(vehicleId: Int): Flow<List<MaintenanceRecord>> = dao.getRecordsForVehicle(vehicleId)
    suspend fun addRecord(record: MaintenanceRecord): Long = dao.insert(record)
    suspend fun updateRecord(record: MaintenanceRecord) = dao.update(record)
    suspend fun deleteRecord(record: MaintenanceRecord) = dao.delete(record)
}