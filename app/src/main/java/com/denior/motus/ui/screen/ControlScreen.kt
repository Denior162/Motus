package com.denior.motus.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.component.ConnectionStatusCard
import com.denior.motus.ui.component.MotusTopBar
import com.denior.motus.ui.component.OldDeviceFAB
import com.denior.motus.ui.component.ValueSelectorButtonRow
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun MotusApp(
    viewModel: MotusViewModel = hiltViewModel()
) {
    val motorState by viewModel.motorState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(topBar = {
        MotusTopBar()
    },
        floatingActionButton = {
            OldDeviceFAB(
                    viewModel = viewModel
                )
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
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
    isEnabled: Boolean,

    ) {
    val isAngleControlEnabled by remember(rpm, isEnabled) {
        derivedStateOf { isEnabled && rpm > 0 }
    }

    val sliderSteps = remember { 15 }
    val recommendedSpeed = remember { 19f }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            MotorControlSlider(
                value = rpm,
                isEnabled = isEnabled,
                onValueChange = onRpmChanged,
                valueRange = 0f..60f,
                steps = sliderSteps,
                labelResId = R.string.speed_label,
                minLabelResId = R.string.min_speed,
                maxLabelResId = R.string.max_speed,
                unit = "RPM"
            )
        }
        item {
            ValueSelectorButtonRow(
                onValueChanged = onRpmChanged,
                isEnabled = isEnabled,
                values = listOf(15f, 19f, 30f, 45f, 60f),
                isRecommended = recommendedSpeed,
                contentDescriptionForParameter = { float ->
                    when (float) {
                        0f -> "Set minimum speed"
                        60f -> "Set maximum speed"
                        else -> "Set speed to ${float.toInt()} RPM"
                    }
                }
            )
        }
        item {
            MotorControlSlider(
                value = angle,
                isEnabled = isAngleControlEnabled,
                onValueChange = onAngleChanged,
                valueRange = -180f..180f,
                steps = sliderSteps,
                labelResId = R.string.angle_label,
                minLabelResId = R.string.min_angle,
                maxLabelResId = R.string.max_angle,
                unit = "Degrees"
            )
        }
        item {
            ValueSelectorButtonRow(
                onValueChanged = onAngleChanged, isEnabled = isAngleControlEnabled,
                values = listOf(-360f, -180f, 180f, 360f),
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
}

@Composable
fun MotorControlSlider(
    value: Float,
    isEnabled: Boolean,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    labelResId: Int,
    minLabelResId: Int,
    maxLabelResId: Int,
    unit: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(labelResId, value.toInt()),
            style = MaterialTheme.typography.titleMedium,
        )

        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            valueRange = valueRange,
            steps = steps,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = "Current value: ${value.toInt()} $unit"
                },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(minLabelResId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(maxLabelResId),
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

@Preview(
    device = "spec:width=1440px,height=3360px,dpi=640,orientation=landscape", locale = "uk",
    showSystemUi = false, showBackground = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Composable
fun ControlScreenVariant(){
    ControlScreen(
        modifier = Modifier,
        connectionState = ConnectionState.Connected(),
        onSpeedChange = {},
        onAngleChange = {},
        isConnected = true,
        motorState = MotusViewModel.MotorState(rpm = 30f, angle = 0f)
    )
}