package com.denior.motus.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun WigglingEyesIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
) {
    val transition = rememberInfiniteTransition()

    val offsetX by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .progressSemantics()
            .size(50.dp)
    ) {
        val radius = size.width / 4
        val circleRadius = 4.dp.toPx()
        val centerPoint = center

        val leftEyeCenter = Offset(x = centerPoint.x - 20f + offsetX, y = centerPoint.y)
        val rightEyeCenter = Offset(x = centerPoint.x + 20f + offsetX, y = centerPoint.y)

//        val leftEyeCenter = Offset(x = centerPoint.x - 10 + offsetX, y = centerPoint.y)
//        val rightEyeCenter = Offset(x = centerPoint.x + 10 + offsetX, y = centerPoint.y)

        drawCircle(
            color = color,
            radius = circleRadius,
            center = leftEyeCenter
        )

        drawCircle(
            color = color,
            radius = circleRadius,
            center = rightEyeCenter
        )
    }
}

/*
@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
) {
    val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = strokeCap) }

    val transition = rememberInfiniteTransition()
    // The current rotation around the circle, so we know where to start the rotation from
    val currentRotation =
        transition.animateValue(
            0,
            RotationsPerCycle,
            Int.VectorConverter,
            infiniteRepeatable(
                animation =
                tween(
                    durationMillis = RotationDuration * RotationsPerCycle,
                    easing = LinearEasing
                )
            )
        )
    // How far forward (degrees) the base point should be from the start point
    val baseRotation =
        transition.animateFloat(
            0f,
            BaseRotationAngle,
            infiniteRepeatable(
                animation = tween(durationMillis = RotationDuration, easing = LinearEasing)
            )
        )
    // How far forward (degrees) both the head and tail should be from the base point
    val endAngle =
        transition.animateFloat(
            0f,
            JumpRotationAngle,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = HeadAndTailAnimationDuration + HeadAndTailDelayDuration
                    0f at 0 using CircularEasing
                    JumpRotationAngle at HeadAndTailAnimationDuration
                }
            )
        )
    val startAngle =
        transition.animateFloat(
            0f,
            JumpRotationAngle,
            infiniteRepeatable(
                animation =
                keyframes {
                    durationMillis = HeadAndTailAnimationDuration + HeadAndTailDelayDuration
                    0f at HeadAndTailDelayDuration using CircularEasing
                    JumpRotationAngle at durationMillis
                }
            )
        )
    Canvas(modifier.progressSemantics().size(CircularIndicatorDiameter)) {
        drawCircularIndicatorTrack(trackColor, stroke)

        val currentRotationAngleOffset = (currentRotation.value * RotationAngleOffset) % 360f

        // How long a line to draw using the start angle as a reference point
        val sweep = abs(endAngle.value - startAngle.value)

        // Offset by the constant offset and the per rotation offset
        val offset = StartAngleOffset + currentRotationAngleOffset + baseRotation.value
        drawIndeterminateCircularIndicator(
            startAngle.value + offset,
            strokeWidth,
            sweep,
            color,
            stroke
        )
    }
}*/

@Composable
@Preview
fun WigglingEyesIndicatorPrev(){
    WigglingEyesIndicator()
}