package com.revlogix.app.data.repository

import com.revlogix.app.data.local.CustomPart
import com.revlogix.app.data.local.CustomPartDao
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.data.sync.SyncTypes
import kotlinx.coroutines.flow.Flow

class CustomPartRepository(
    private val dao: CustomPartDao,
    private val sync: SyncEnqueuer
) {
    fun getPartsForVehicle(vehicleId: Int): Flow<List<CustomPart>> = dao.getPartsForVehicle(vehicleId)

    suspend fun addPart(part: CustomPart): Long {
        val id = dao.insert(part)
        sync.created(SyncTypes.PARTS, id.toInt(), part.copy(partId = id.toInt()))
        return id
    }

    suspend fun updatePart(part: CustomPart) {
        dao.update(part)
        sync.updated(SyncTypes.PARTS, part.partId, part)
    }

    suspend fun deletePart(part: CustomPart) {
        dao.delete(part)
        sync.deleted(SyncTypes.PARTS, part.partId)
    }
}
