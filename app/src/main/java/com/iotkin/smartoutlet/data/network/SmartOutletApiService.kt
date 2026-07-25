package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.DeviceStatusResponse
import retrofit2.Response
import retrofit2.http.GET

interface SmartOutletApiService {

    @GET("api/status")
    suspend fun getStatus(): Response<DeviceStatusResponse>
}