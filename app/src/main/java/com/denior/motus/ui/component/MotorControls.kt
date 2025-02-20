package com.denior.motus.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState


@Composable
fun MotorControls(
    modifier: Modifier = Modifier,
    rpm: Float,
    angle: Float,
    onRpmChanged: (Float) -> Unit,
    onAngleChanged: (Float) -> Unit,
    isEnabled: Boolean,
    connectionState: ConnectionState,
    ) {
    val isAngleControlEnabled by remember(rpm, isEnabled) {
        derivedStateOf { isEnabled && rpm > 0 }
    }

    val sliderSteps = remember { 15 }
    val recommendedSpeed = remember { 19f }

    val rpmValues = remember {
        generateEvenlySpacedValues(
            min = 10f,
            max = 60f,
            recommendedValue = recommendedSpeed,
            count = 6
        )
    }

    val angleValues = remember {
        generateEvenlySpacedValues(
            min = -360f,
            max = 360f,
            count = 5
        )
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ConnectionStatusCard(connectionState = connectionState)
        }
        item {
            MotorControlCard(
                isEnabled = isEnabled,
                value = rpm,
                onValueChange = onRpmChanged,
                sliderSteps = sliderSteps,
                recommendedValue = recommendedSpeed,
                valueRange = 0f..60f,
                labelResId = R.string.speed_label,
                minLabelResId = R.string.min_speed,
                maxLabelResId = R.string.max_speed,
                values = rpmValues,
                unit = "RPM"
            )
        }
        item {
            MotorControlCard(
                isEnabled = isAngleControlEnabled,
                value = angle,
                onValueChange = onAngleChanged,
                sliderSteps = sliderSteps,
                valueRange = -360f..360f,
                labelResId = R.string.angle_label,
                minLabelResId = R.string.min_angle,
                maxLabelResId = R.string.max_angle,
                values = angleValues,
                unit = "Degrees"
            )
        }
    }
}


private fun generateEvenlySpacedValues(
    min: Float,
    max: Float,
    recommendedValue: Float? = null,
    count: Int
): List<Float> {
    require(count > 1) { "Count must be greater than 1" }

    val values = mutableSetOf<Float>()

    // Добавляем минимальное и максимальное значения
    values.add(min)
    values.add(max)

    // Добавляем рекомендуемое значение, если оно есть
    recommendedValue?.let {
        if (it in min..max) values.add(it)
    }

    // Вычисляем оставшиеся равномерно распределенные значения
    val remainingCount = count - values.size
    if (remainingCount > 0) {
        val step = (max - min) / (count - 1)
        for (i in 1 until count - 1) {
            val value = min + (step * i)
            values.add(value)
        }
    }

    return values.sorted()
}

@Composable
fun MotorControlCard(
    isEnabled: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    values: List<Float>,
    onValueChange: (Float) -> Unit,
    sliderSteps: Int,
    recommendedValue: Float? = null,
    labelResId: Int,
    minLabelResId: Int,
    maxLabelResId: Int,
    unit: String
) {
    OutlinedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            MotorControlSlider(
                value = value,
                isEnabled = isEnabled,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = sliderSteps,
                labelResId = labelResId,
                minLabelResId = minLabelResId,
                maxLabelResId = maxLabelResId,
                unit = unit
            )
            ValueSelectorButtonRow(
                onValueChanged = onValueChange,
                isEnabled = isEnabled,
                values = values,
                isRecommended = recommendedValue,
                contentDescriptionForParameter = { float ->
                    when (float) {
                        0f -> "Set minimum speed"
                        60f -> "Set maximum speed"
                        else -> "Set speed to ${float.toInt()} RPM"
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
            )

            Text(
                text = stringResource(maxLabelResId),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MotorSpeedControlPreview() {
    MotorControlCard(
        isEnabled = true,
        value = 30f,
        onValueChange = {},
        sliderSteps = 15,
        recommendedValue = 19f,
        valueRange = 0f..60f,
        labelResId = R.string.speed_label,
        minLabelResId = R.string.min_speed,
        maxLabelResId = R.string.max_speed,
        values = listOf(15f, 19f, 30f, 45f, 60f),
        unit = "RPM"
    )
}

@Preview(showBackground = true)
@Composable
fun MotorControlPreview() {
    MotorControls(
        rpm = 30f,
        angle = 0f,
        onRpmChanged = {},
        onAngleChanged = {},
        isEnabled = true,
        connectionState = ConnectionState.Connected()
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
fun ControlScreenVariant() {
    MotorControls(
        rpm = 30f,
        angle = 0f,
        onRpmChanged = {},
        onAngleChanged = {},
        isEnabled = true,
        connectionState = ConnectionState.Connected()
    )
}

