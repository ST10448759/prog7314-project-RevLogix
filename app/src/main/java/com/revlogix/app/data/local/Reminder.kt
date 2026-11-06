package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MaintenanceRecord::class,
            parentColumns = ["maintenanceId"],
            childColumns = ["maintenanceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val reminderId: Int = 0,
    val vehicleId: Int,
    val maintenanceId: Int,
    val reminderDate: Long,
    val triggerOdometer: Int?,
    val isCompleted: Boolean = false
)