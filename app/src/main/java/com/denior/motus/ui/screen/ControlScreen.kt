package com.denior.motus.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.component.ConnectionStatusCard
import com.denior.motus.ui.component.ConvenientRowOfFABLikeSquareButtons
import com.denior.motus.ui.component.MotusTopBar
import com.denior.motus.ui.component.OldDeviceFAB
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun MotusApp(
    viewModel: MotusViewModel = hiltViewModel()
) {
    val motorState by viewModel.motorState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(topBar = { MotusTopBar() }, floatingActionButton = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            OldDeviceFAB(
                permission = false,
                isConnected = isConnected,
                viewModel = viewModel
            )
        }
    }
    ) { innerPadding ->
        ControlScreen(
            modifier = Modifier.padding(innerPadding),
            isConnected = isConnected,
            motorState = motorState,
            connectionState = connectionState,
            onSpeedChange = { newSpeed ->
                if (isConnected) viewModel.setMotorSpeed(newSpeed)
            },
            onAngleChange = { newAngle ->
                if (isConnected) viewModel.setMotorAngle(newAngle)
            }
        )
    }
}

@Composable
fun ControlScreen(
    modifier: Modifier,
    connectionState: ConnectionState,
    onSpeedChange: (Float) -> Unit,
    onAngleChange: (Float) -> Unit,
    isConnected: Boolean,
    motorState: MotusViewModel.MotorState
) {
    Column(
        modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConnectionStatusCard(connectionState = connectionState)
        MotorControl(
            rpm = motorState.rpm,
            angle = motorState.angle,
            onRpmChanged = onSpeedChange,
            onAngleChanged = onAngleChange,
            isEnabled = isConnected
        )
    }
}

@Composable
fun MotorControl(
    rpm: Float,
    angle: Float,
    onRpmChanged: (Float) -> Unit,
    onAngleChanged: (Float) -> Unit,
    isEnabled: Boolean
) {
    Column (verticalArrangement = Arrangement.spacedBy(16.dp)){
        MotorSpeedSlider(
            value = rpm, isEnabled = isEnabled, onValueChange = onRpmChanged
        )
        ConvenientRowOfFABLikeSquareButtons(
            onValueChanged = onRpmChanged,
            isEnabled = isEnabled,
            values = listOf(1f, 15f, 19f, 30f, 45f, 60f),
            isRecommended = 19f,
            contentDescriptionForParameter = { float ->
                when (float) {
                    0f -> "Set minimum speed"
                    60f -> "Set maximum speed"
                    else -> "Set speed to ${angle.toInt()} RPM"
                }
            }
        )
        MotorAngleSlider(
            value = angle, isEnabled = isEnabled, onValueChange = onAngleChanged
        )
        ConvenientRowOfFABLikeSquareButtons(
            onValueChanged = onAngleChanged, isEnabled = isEnabled,
            values = listOf(-360f, -180f, 0f, 180f, 360f),
            contentDescriptionForParameter = { float ->
                when (float) {
                    0f -> "Set neutral position"
                    360f, -360f -> "Set full rotation"
                    else -> "Set angle to ${angle.toInt()} degrees"
                }
            }
        )
    }
}

@Composable
fun MotorSpeedSlider(
    value: Float, isEnabled: Boolean, onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.speed_label, value.toInt()),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics {
                contentDescription = "Speed control slider"
            }
        )

        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            valueRange = 1f..60f,
            steps = 15,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = "Current speed: ${value.toInt()} RPM"
                    contentDescription = "Motor speed control slider"
                },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Text(
                modifier = Modifier.weight(1f), text =
                stringResource(R.string.min_speed),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.weight(1f), text =
                stringResource(R.string.max_speed),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun MotorAngleSlider(
    value: Float, isEnabled: Boolean, onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.angle_label, value.toInt()),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics {
                contentDescription = "Angle control slider"
            }
        )


        Slider(value = value,
            onValueChange = { onValueChange(it) },
            valueRange = -360f..360f,
            steps = 72,
            enabled = isEnabled,
            modifier = Modifier.semantics {
                stateDescription = "Current angle: ${value.toInt()} degrees"
                contentDescription = "Motor angle control slider"
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f), text =
                stringResource(R.string.min_angle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.weight(1f), text =
                (stringResource(R.string.max_angle)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ControlScreenPreview() {
    val mockMotorState = MotusViewModel.MotorState(rpm = 30f, angle = 0f)
    ControlScreen(
        modifier = Modifier,
        connectionState = ConnectionState.Connected(),
        onSpeedChange = {},
        onAngleChange = {},
        isConnected = true,
        motorState = mockMotorState
    )
}

@Preview(showBackground = true)
@Composable
fun MotorControlPreview() {
    MotorControl(
        rpm = 30f,
        angle = 0f,
        onRpmChanged = {},
        onAngleChanged = {},
        isEnabled = true
    )
}

@Preview(showBackground = true)
@Composable
fun ConnectionStatusCardPreview() {
    ConnectionStatusCard(connectionState = ConnectionState.Connected())
}

@Preview(showBackground = true)
@Composable
fun MotorSpeedSliderPreview() {
    MotorSpeedSlider(value = 30f, isEnabled = true, onValueChange = {})
}

@Preview(showBackground = true)
@Composable
fun MotorAngleSliderPreview() {
    MotorAngleSlider(value = 0f, isEnabled = true, onValueChange = {})
}