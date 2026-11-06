package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val vehicleId: Int = 0,
    val userId: Int,
    val make: String,
    val model: String,
    val year: Int,
    val bodyStyle: String,
    val registrationNumber: String,
    val currentOdometer: Int,
    val dateAdded: Long = System.currentTimeMillis()
)