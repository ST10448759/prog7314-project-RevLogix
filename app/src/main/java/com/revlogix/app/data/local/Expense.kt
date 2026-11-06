package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Int = 0,
    val vehicleId: Int,
    val expenseDate: Long,
    val category: String,
    val amount: Double,
    val description: String,
    val receiptUri: String? = null
)