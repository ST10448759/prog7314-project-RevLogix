package com.revlogix.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/vehicles")
    suspend fun getVehicles(): List<VehicleDto>

    @POST("api/vehicles")
    suspend fun createVehicle(@Body vehicle: VehicleDto): VehicleDto
}