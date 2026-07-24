package com.iotkin.smartoutlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.iotkin.smartoutlet.ui.navigation.AppNavHost
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OreoSmartOutletTheme {
                AppNavHost()
            }
        }
    }
}