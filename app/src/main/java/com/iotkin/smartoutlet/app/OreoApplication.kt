package com.iotkin.smartoutlet.app

import android.app.Application

class OreoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = DefaultAppContainer(
            context = applicationContext
        )
    }
}