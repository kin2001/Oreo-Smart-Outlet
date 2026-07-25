package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.DeviceAddress
import okhttp3.HttpUrl

fun DeviceAddress.toBaseUrl(): HttpUrl {
    return HttpUrl.Builder()
        .scheme("http")
        .host(host.trim())
        .port(port)
        .build()
}