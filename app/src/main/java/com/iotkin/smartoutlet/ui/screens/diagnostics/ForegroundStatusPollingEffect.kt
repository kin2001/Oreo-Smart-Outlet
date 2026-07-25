package com.iotkin.smartoutlet.ui.screens.diagnostics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ForegroundStatusPollingEffect(
    viewModel: DiagnosticsViewModel
) {
    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        viewModel
    ) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        viewModel
                            .startForegroundPolling()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        viewModel
                            .stopForegroundPolling()
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(observer)

        if (
            lifecycleOwner.lifecycle
                .currentState
                .isAtLeast(
                    Lifecycle.State.STARTED
                )
        ) {
            viewModel.startForegroundPolling()
        }

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(observer)

            viewModel.stopForegroundPolling()
        }
    }
}