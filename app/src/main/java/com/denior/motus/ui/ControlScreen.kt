package com.denior.motus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable

@Composable
fun MotorControl(
    connectionStatus: Boolean,
    selectedPower: Float,
    sendNewPowerToMotor: (Float) -> Unit,
    receivedPower: Float
) {
    Column {
        PowerSlider(
            onConnected = connectionStatus,
            selectedPower = selectedPower,
            onValueChange = sendNewPowerToMotor
        )
    }
}

@Composable
fun PowerSlider(
    onConnected: Boolean,
    selectedPower: Float,
    onValueChange: (Float) -> Unit
) {
    Slider(
        value = selectedPower,
        onValueChange = onValueChange,
        valueRange = -100f..100f,
        enabled = onConnected
    )
}

