package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_logs",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FuelLog(
    @PrimaryKey(autoGenerate = true)
    val fuelLogId: Int = 0,
    val vehicleId: Int,
    val fillUpDate: Long,
    val odometer: Int,
    val litres: Double,
    val totalCost: Double,
    val fuelType: String,
    val fullTank: Boolean,
    val notes: String = ""
)