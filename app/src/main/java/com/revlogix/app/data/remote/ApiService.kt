package com.revlogix.app.data.remote

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/** Body for registering this phone's FCM token with the API. */
data class DeviceRegistration(val token: String)

/** The API answers a receipt upload with the public URL of the stored image. */
data class BlobResponse(val url: String)

/**
 * REST endpoints of the hosted RevLogix API.
 * Every call carries the Firebase ID token (see AuthTokenInterceptor).
 *
 * "collection" is one of: vehicles, fuelLogs, expenses, maintenanceRecords, customParts.
 */
interface ApiService {
    @GET("api/data/{collection}")
    suspend fun list(@Path("collection") collection: String): List<JsonObject>

    @PUT("api/data/{collection}/{id}")
    suspend fun upsert(
        @Path("collection") collection: String,
        @Path("id") id: Int,
        @Body body: JsonObject
    ): Response<Unit>

    @DELETE("api/data/{collection}/{id}")
    suspend fun remove(
        @Path("collection") collection: String,
        @Path("id") id: Int
    ): Response<Unit>

    @POST("api/devices")
    suspend fun registerDevice(@Body body: DeviceRegistration): Response<Unit>

    @Multipart
    @POST("api/blobs")
    suspend fun uploadReceipt(@Part file: MultipartBody.Part): BlobResponse

    @POST("api/notifications/test")
    suspend fun sendTestNotification(): Response<Unit>
}
