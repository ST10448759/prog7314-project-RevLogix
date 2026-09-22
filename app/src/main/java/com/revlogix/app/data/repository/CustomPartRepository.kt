package com.revlogix.app.data.repository

import com.revlogix.app.data.local.CustomPart
import com.revlogix.app.data.local.CustomPartDao
import kotlinx.coroutines.flow.Flow

class CustomPartRepository(private val dao: CustomPartDao) {
    fun getPartsForVehicle(vehicleId: Int): Flow<List<CustomPart>> = dao.getPartsForVehicle(vehicleId)
    suspend fun addPart(part: CustomPart): Long = dao.insert(part)
    suspend fun updatePart(part: CustomPart) = dao.update(part)
    suspend fun deletePart(part: CustomPart) = dao.delete(part)
}