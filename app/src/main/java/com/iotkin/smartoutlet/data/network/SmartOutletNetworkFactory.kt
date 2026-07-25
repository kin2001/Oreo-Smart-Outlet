package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.DeviceAddress
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object SmartOutletNetworkConfig {
    const val CONNECT_TIMEOUT_SECONDS = 3L
    const val READ_TIMEOUT_SECONDS = 5L
    const val WRITE_TIMEOUT_SECONDS = 5L
    const val CALL_TIMEOUT_SECONDS = 8L
}

class SmartOutletNetworkFactory(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = false
    }
) {

    fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(
                SmartOutletNetworkConfig.CONNECT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                SmartOutletNetworkConfig.READ_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                SmartOutletNetworkConfig.WRITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .callTimeout(
                SmartOutletNetworkConfig.CALL_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun createApi(
        address: DeviceAddress
    ): SmartOutletApiService {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(address.toBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(
                json.asConverterFactory(contentType)
            )
            .build()
            .create(SmartOutletApiService::class.java)
    }
    fun createClient(
        address: DeviceAddress
    ): SmartOutletApiClient {
        return SmartOutletApiClient(
            apiService = createApi(address),
            json = json
        )
    }
}