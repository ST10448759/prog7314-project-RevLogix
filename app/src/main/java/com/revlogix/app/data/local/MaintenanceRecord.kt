package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true)
    val maintenanceId: Int = 0,
    val vehicleId: Int,
    val serviceType: String,
    val serviceDate: Long,
    val odometer: Int,
    val cost: Double,
    val nextDueDate: Long?,
    val nextDueOdometer: Int?,
    val notes: String = "",
    // "completed", "upcoming" or "overdue" - recalculated by the health score logic later
    val status: String = "completed"
)