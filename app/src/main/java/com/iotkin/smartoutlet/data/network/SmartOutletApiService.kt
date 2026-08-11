package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.DeviceStatusResponse
import com.iotkin.smartoutlet.data.model.IndicatorSettingsRequest
import com.iotkin.smartoutlet.data.model.IndicatorSettingsResponse
import com.iotkin.smartoutlet.data.model.RelayCommandRequest
import com.iotkin.smartoutlet.data.model.RelayCommandResponse
import com.iotkin.smartoutlet.data.model.ScheduleUpdateRequest
import com.iotkin.smartoutlet.data.model.ScheduleUpdateResponse
import com.iotkin.smartoutlet.data.model.TimeSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SmartOutletApiService {

    @GET("api/status")
    suspend fun getStatus():
            Response<DeviceStatusResponse>

    @POST("api/time/sync")
    suspend fun requestTimeSync():
            Response<TimeSyncResponse>

    @POST("api/indicators")
    suspend fun setIndicatorsEnabled(
        @Body request: IndicatorSettingsRequest
    ): Response<IndicatorSettingsResponse>

    @POST("api/relay/1")
    suspend fun setRelay1(
        @Body request: RelayCommandRequest
    ): Response<RelayCommandResponse>

    @POST("api/relay/2")
    suspend fun setRelay2(
        @Body request: RelayCommandRequest
    ): Response<RelayCommandResponse>

    @POST("api/schedule/1")
    suspend fun updateSchedule1(
        @Body request: ScheduleUpdateRequest
    ): Response<ScheduleUpdateResponse>

    @POST("api/schedule/2")
    suspend fun updateSchedule2(
        @Body request: ScheduleUpdateRequest
    ): Response<ScheduleUpdateResponse>
}
