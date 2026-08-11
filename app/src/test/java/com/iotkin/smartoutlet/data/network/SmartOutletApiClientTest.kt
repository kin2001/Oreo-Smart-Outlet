package com.iotkin.smartoutlet.data.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class SmartOutletApiClientTest {

    private lateinit var server: MockWebServer

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun validSmartOutletResponseReturnsSuccess() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    validStatusJson(
                        deviceName = "SmartOutlet_ESP8266"
                    )
                )
        )

        val result = createApiClient().getStatus()

        assertTrue(
            result is DeviceStatusResult.Success
        )

        val success = result as DeviceStatusResult.Success

        assertEquals(
            "SmartOutlet_ESP8266",
            success.status.device
        )

        assertEquals(
            8080,
            success.status.apiPort
        )

        assertEquals(
            true,
            success.status.indicatorsEnabled
        )

        val request = server.takeRequest()

        assertEquals(
            "/api/status",
            request.path
        )

        assertEquals(
            "GET",
            request.method
        )
    }

    @Test
    fun differentDeviceNameReturnsInvalidDevice() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    validStatusJson(
                        deviceName = "UnknownDevice"
                    )
                )
        )

        val result = createApiClient().getStatus()

        assertTrue(
            result is DeviceStatusResult.InvalidDevice
        )

        val invalidDevice =
            result as DeviceStatusResult.InvalidDevice

        assertEquals(
            "UnknownDevice",
            invalidDevice.receivedDevice
        )
    }

    @Test
    fun firmwareErrorResponsePreservesMessage() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    """
                    {
                      "success": false,
                      "error": "Endpoint not found"
                    }
                    """.trimIndent()
                )
        )

        val result = createApiClient().getStatus()

        assertTrue(
            result is DeviceStatusResult.HttpError
        )

        val httpError =
            result as DeviceStatusResult.HttpError

        assertEquals(
            404,
            httpError.statusCode
        )

        assertEquals(
            "Endpoint not found",
            httpError.message
        )
    }

    @Test
    fun indicatorUpdateReturnsConfirmedState() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    """
                    {
                      "success": true,
                      "indicatorsEnabled": false
                    }
                    """.trimIndent()
                )
        )

        val result =
            createApiClient()
                .setIndicatorsEnabled(false)

        assertEquals(
            IndicatorApiResult.Success(
                confirmedEnabled = false
            ),
            result
        )

        val request = server.takeRequest()

        assertEquals(
            "/api/indicators",
            request.path
        )
        assertEquals("POST", request.method)
        assertTrue(
            request.body.readUtf8()
                .contains("\"enabled\":false")
        )
    }

    @Test
    fun mismatchedIndicatorConfirmationIsRejected() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    """
                    {
                      "success": true,
                      "indicatorsEnabled": true
                    }
                    """.trimIndent()
                )
        )

        val result =
            createApiClient()
                .setIndicatorsEnabled(false)

        assertTrue(
            result is
                IndicatorApiResult.InvalidResponse
        )
    }

    @Test
    fun delayedIndicatorResponseReturnsTimeout() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    """
                    {
                      "success": true,
                      "indicatorsEnabled": true
                    }
                    """.trimIndent()
                )
                .setBodyDelay(
                    2,
                    TimeUnit.SECONDS
                )
        )

        val timeoutClient =
            OkHttpClient.Builder()
                .connectTimeout(
                    200,
                    TimeUnit.MILLISECONDS
                )
                .readTimeout(
                    200,
                    TimeUnit.MILLISECONDS
                )
                .writeTimeout(
                    200,
                    TimeUnit.MILLISECONDS
                )
                .build()

        val result =
            createApiClient(
                okHttpClient = timeoutClient
            ).setIndicatorsEnabled(true)

        assertEquals(
            IndicatorApiResult.Timeout,
            result
        )
    }

    @Test
    fun delayedResponseReturnsTimeout() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    validStatusJson(
                        deviceName = "SmartOutlet_ESP8266"
                    )
                )
                .setBodyDelay(
                    2,
                    TimeUnit.SECONDS
                )
        )

        val timeoutClient = OkHttpClient.Builder()
            .connectTimeout(
                200,
                TimeUnit.MILLISECONDS
            )
            .readTimeout(
                200,
                TimeUnit.MILLISECONDS
            )
            .writeTimeout(
                200,
                TimeUnit.MILLISECONDS
            )
            .build()

        val result = createApiClient(
            okHttpClient = timeoutClient
        ).getStatus()

        assertTrue(
            result is DeviceStatusResult.Timeout
        )
    }

    private fun createApiClient(
        okHttpClient: OkHttpClient =
            OkHttpClient.Builder().build()
    ): SmartOutletApiClient {
        val contentType =
            "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(contentType)
            )
            .build()

        val service = retrofit.create(
            SmartOutletApiService::class.java
        )

        return SmartOutletApiClient(
            apiService = service,
            json = json
        )
    }

    private fun validStatusJson(
        deviceName: String
    ): String {
        return """
            {
              "device": "$deviceName",
              "success": true,
              "apiVersion": 1,
              "firmwareVersion": "1.0.0",
              "deviceId": "oreo-test-device",
              "uptimeSeconds": 120,
              "freeHeapBytes": 32000,
              "freeSketchSpaceBytes": 1048576,
              "resetReason": "Power on",
              "otaReady": true,
              "apiActive": true,
              "online": true,
              "apiPort": 8080,
              "ip": "192.168.8.113",
              "ssid": "Test WiFi",
              "rssi": -55,
              "timeValid": true,
              "philippineTime": "2026-07-25T12:00:00+08:00",
              "relay1": {
                "state": true,
                "schedule": {
                  "enabled": true,
                  "onHour": 18,
                  "onMinute": 0,
                  "offHour": 6,
                  "offMinute": 0
                }
              },
              "relay2": {
                "state": false,
                "schedule": {
                  "enabled": false,
                  "onHour": 17,
                  "onMinute": 45,
                  "offHour": 7,
                  "offMinute": 0
                }
              }
            }
        """.trimIndent()
    }
}
