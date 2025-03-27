package com.denior.motus.ui.component

import android.os.Build
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.viewmodel.MotusViewModel
import kotlin.math.roundToInt

private object MotorConstants {
    const val MIN_RPM = 0f
    const val MAX_RPM = 60f
    const val RECOMMENDED_RPM = 19f
    const val MIN_ANGLE = -360f
    const val MAX_ANGLE = 360f
    const val SLIDER_STEPS = 11
}

private fun calculateButtonCount(
    availableWidthDp: Float,
    buttonWidthDp: Float = 48f,
    spacingDp: Float = 8f
): Int {
    require(availableWidthDp > 0) { "availableWidthDp must be positive" }
    var possibleCount = 1
    while (true) {
        val requiredWidth = possibleCount * buttonWidthDp + (possibleCount - 1) * spacingDp
        if (requiredWidth > availableWidthDp) break
        possibleCount++
    }
    return possibleCount - 1
}

@Composable
fun MotorControls(
    modifier: Modifier = Modifier,
    rpm: Float,
    angle: Float,
    onRpmChanged: (Float) -> Unit,
    onAngleChanged: (Float) -> Unit, viewModel: MotusViewModel
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val isSpeedControlEnabled = connectionState is ConnectionState.Connected

    val sliderSteps = remember { MotorConstants.SLIDER_STEPS }
    val recommendedSpeed = remember { MotorConstants.RECOMMENDED_RPM }

    val rpmValues = remember {
        generateEvenlySpacedValues(
            min = 10f,
            max = 60f,
            recommendedValue = recommendedSpeed,
            count = 6
        )
    }

    val dynamicCount = calculateButtonCount(330f)
    val angleValues = remember {
        generateEvenlySpacedValues(
            min = MotorConstants.MIN_ANGLE,
            max = MotorConstants.MAX_ANGLE,
            count = dynamicCount
        )
    }

    val motorControlState by remember(connectionState, rpm) {
        derivedStateOf {
            MotorControlState(
                isSpeedControlEnabled = isSpeedControlEnabled,
                isAngleControlEnabled = (isSpeedControlEnabled && rpm > 0)
            )
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass

    val margin = if (windowSizeClass == WindowWidthSizeClass.COMPACT) 16.dp else 24.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (windowSizeClass == WindowWidthSizeClass.COMPACT) 1 else 2),
        modifier = modifier
            .padding(horizontal = margin)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (windowSizeClass == WindowWidthSizeClass.COMPACT) {
            item {
                ConnectionStatusCard(
                    connectionState = connectionState,
                )
            }
        }

        item {
            MotorControlCard(
                isEnabled = motorControlState.isSpeedControlEnabled,
                value = rpm,
                valueRange = MotorConstants.MIN_RPM..MotorConstants.MAX_RPM,
                values = rpmValues,
                onValueChange = onRpmChanged,
                sliderSteps = sliderSteps,
                recommendedValue = recommendedSpeed,
                labelResId = R.string.speed_label,
                minLabelResId = R.string.min_speed,
                maxLabelResId = R.string.max_speed
            )
        }
        item {
            MotorControlCard(
                isEnabled = motorControlState.isAngleControlEnabled,
                value = angle,
                valueRange = MotorConstants.MIN_ANGLE..MotorConstants.MAX_ANGLE,
                values = angleValues,
                onValueChange = onAngleChanged,
                sliderSteps = sliderSteps,
                labelResId = R.string.angle_label,
                minLabelResId = R.string.min_angle,
                maxLabelResId = R.string.max_angle
            )
        }
//        items(
//                count = tipList.size,
//        key = { index -> tipList[index].hashCode() }
//        ) { index ->
//        ForUserTips(tipIndex = index)
//    }
    }
}

data class MotorControlState(
    val isSpeedControlEnabled: Boolean,
    val isAngleControlEnabled: Boolean
)

private fun generateEvenlySpacedValues(
    min: Float,
    max: Float,
    recommendedValue: Float? = null,
    count: Int
): List<Float> {
    require(count > 1) { "Count must be greater than 1" }

    val values = mutableSetOf<Float>()

    values.add(min)
    values.add(max)

    recommendedValue?.let {
        if (it in min..max) values.add(it)
    }

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
    maxLabelResId: Int
) {
    OutlinedCard(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {

            MotorControlSlider(
                value = value,
                isEnabled = isEnabled,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = sliderSteps,
                labelResId = labelResId,
                minLabelResId = minLabelResId,
                maxLabelResId = maxLabelResId,
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
                },
                modifier = Modifier
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
) {
    val view = LocalView.current

    remember(value) { value.roundToInt() }

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
            onValueChange = { newValue ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                }
                onValueChange(newValue.roundToInt().toFloat())
            },
            valueRange = valueRange,
            steps = steps,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(minLabelResId),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(maxLabelResId),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun ValueSelectorButtonRow(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onValueChanged: (Float) -> Unit,
    values: List<Float>,
    isRecommended: Float? = null,
    contentDescriptionForParameter: (Float) -> String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        values.forEachIndexed { index, value ->
            val isFirst = index == 0
            val isLast = index == values.size - 1

            val shape = when {
                isFirst -> RoundedCornerShape(
                    topStart = 16.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 8.dp
                )

                isLast -> RoundedCornerShape(
                    topStart = 8.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 16.dp
                )

                else -> RoundedCornerShape(8.dp)
            }

            val type = when {
                isRecommended?.let { it == value } == true -> TypesOfVB.RECOMMENDED
                isFirst || isLast -> TypesOfVB.PRIMARY
                else -> TypesOfVB.STANDARD
            }

            ValueButton(
                onClick = onValueChanged,
                value = value,
                shape = shape,
                type = type,
                modifier = modifier
                    .size(48.dp)
                    .weight(1f)
                    .aspectRatio(1f),
                isEnabled = isEnabled,
                contentDescription = contentDescriptionForParameter(value)

            )
        }
    }
}

enum class TypesOfVB {
    PRIMARY, RECOMMENDED, STANDARD
}

@Composable
fun ValueButton(
    isEnabled: Boolean,
    onClick: (Float) -> Unit,
    value: Float,
    type: TypesOfVB,
    shape: Shape,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    val context = LocalContext.current
    val view = LocalView.current
    ContextCompat.getSystemService(context, Vibrator::class.java)

    val buttonModifier =
        Modifier
            .semantics { this.contentDescription = contentDescription }
            .then(modifier)

    val handleClick = {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onClick(value)
    }

    val textContent = @Composable {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("${value.toInt()}")
        }
    }

    when (type) {
        TypesOfVB.PRIMARY -> FilledIconButton(
            enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = { handleClick() }
        ) { textContent() }

        TypesOfVB.STANDARD -> FilledIconButton(
            enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            onClick = { handleClick() }
        ) { textContent() }

        TypesOfVB.RECOMMENDED -> FilledIconButton(
            enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            onClick = { handleClick() }
        ) { textContent() }
    }
}

@Composable
@Preview(showBackground = true)
fun RowOfConvenientButtonsPrev() {
    ValueSelectorButtonRow(
        isEnabled = true,
        onValueChanged = { },
        values = listOf(15f, 19f, 30f, 45f, 60f, 90f),
        isRecommended = 19f,
        contentDescriptionForParameter = { float ->
            when (float) {
                0f -> "Set minimum speed"
                60f -> "Set maximum speed"
                else -> "Set speed to ${float.toInt()} RPM"
            }
        }
    )
}

@Preview
@Composable
fun ConvenientFABLikeSquareButtonPreview() {
    MaterialTheme {
        ValueButton(
            onClick = {},
            value = 45f,
            shape = RoundedCornerShape(16.dp),
            isEnabled = true,
            type = TypesOfVB.RECOMMENDED,
            modifier = Modifier,
            contentDescription = 0.0.toString()
        )
    }
}

@Preview(
    showBackground = true,
    fontScale = 2.0f, locale = "uk"
)
@Composable
fun MotorSpeedControlPreview() {
    MotorControlCard(
        isEnabled = true,
        value = 30f,
        valueRange = MotorConstants.MIN_RPM..MotorConstants.MAX_RPM,
        values = listOf(15f, MotorConstants.RECOMMENDED_RPM, 30f, 45f, MotorConstants.MAX_RPM),
        onValueChange = {},
        sliderSteps = MotorConstants.SLIDER_STEPS,
        recommendedValue = MotorConstants.RECOMMENDED_RPM,
        labelResId = R.string.speed_label,
        minLabelResId = R.string.min_speed,
        maxLabelResId = R.string.max_speed
    )
}


