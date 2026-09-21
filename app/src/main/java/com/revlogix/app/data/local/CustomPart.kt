package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_parts",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomPart(
    @PrimaryKey(autoGenerate = true)
    val partId: Int = 0,
    val vehicleId: Int,
    val category: String,
    val partName: String,
    val brand: String,
    val partNumber: String,
    val serialNumber: String,
    val technicalSpecs: String,
    val vendor: String,
    val purchaseDate: Long?,
    val installDate: Long?,
    val cost: Double,
    val notes: String = ""
)