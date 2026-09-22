package com.revlogix.app.data.remote

data class VehicleDto(
    val vehicleId: Int = 0,
    val userId: Int,
    val make: String,
    val model: String,
    val year: Int,
    val bodyStyle: String,
    val registrationNumber: String,
    val currentOdometer: Int
)