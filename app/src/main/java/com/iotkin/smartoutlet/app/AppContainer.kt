package com.iotkin.smartoutlet.app

import android.content.Context

interface AppContainer {
    val applicationContext: Context
}

class DefaultAppContainer(
    context: Context
) : AppContainer {

    override val applicationContext: Context =
        context.applicationContext
}