package com.denior.motus.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConvenientRowOfFABLikeSquareButtons(
    isEnabled: Boolean,
    onValueChanged: (Float) -> Unit,
    values: List<Float>,
    isRecommended: Float? = null,
    contentDescriptionForParameter: (Float) -> String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        values.forEachIndexed { index, value ->
            val isFirst = index == 0
            val isLast = index == values.size - 1

            val shape = when {
                isFirst -> CutCornerShape(
                    topStart = 12.dp, topEnd = 4.dp,
                    bottomStart = 12.dp, bottomEnd = 4.dp
                )

                isLast -> CutCornerShape(
                    topStart = 4.dp, topEnd = 12.dp,
                    bottomStart = 4.dp, bottomEnd = 12.dp
                )

                else -> RoundedCornerShape(4.dp)
            }

            val type = when {
                isRecommended?.let { it == value } == true -> TypesOfConviButs.RECOMMENDED
                isFirst || isLast -> TypesOfConviButs.PRIMARY
                else -> TypesOfConviButs.STANDARD
            }

            ConvenientFABLikeSquareButton(
                onClick = onValueChanged,
                value = value,
                shape = shape,
                type = type, modifier = Modifier.weight(1f),
                isEnabled = isEnabled,
                contentDescription = contentDescriptionForParameter(value)
                
            )
        }
    }
}

enum class TypesOfConviButs {
    PRIMARY,
    RECOMMENDED,
    STANDARD
}

@Composable
fun ConvenientFABLikeSquareButton(isEnabled: Boolean,
    onClick: (Float) -> Unit,
    value: Float,
    type: TypesOfConviButs,
    shape: Shape,
    modifier: Modifier = Modifier, contentDescription: String
) {
    val buttonModifier = Modifier
        .height(60.dp)
        .semantics { this.contentDescription = contentDescription }
        .then(modifier)
    when (type) {
        TypesOfConviButs.PRIMARY -> FilledIconButton(enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            onClick = { onClick(value) }
        ) {
            Text("$value")
        }

        TypesOfConviButs.STANDARD -> FilledTonalIconButton(enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            onClick = { onClick(value) }
        ) {
            Text("$value")
        }

        TypesOfConviButs.RECOMMENDED -> FilledTonalIconButton(enabled = isEnabled,
            colors = IconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = shape,
            modifier = buttonModifier,
            onClick = { onClick(value) }
        ) {
            Text("$value")
        }
    }
}

@Composable
@Preview
fun RowOfConvenientButtonsPrev() {
    ConvenientRowOfFABLikeSquareButtons(
        isEnabled = true, onValueChanged = { },
        values = listOf(30f, 45f, 60f, 90f),
        contentDescriptionForParameter = { float ->
            when (float) {
                0f -> "Set minimum speed"
                60f -> "Set maximum speed"
                else -> "Set speed to ${float.toInt()} RPM"
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ConvenientFABLikeSquareButtonPreview() {
    MaterialTheme {
        ConvenientFABLikeSquareButton(
            onClick = {},
            value = 45f,
            shape = RoundedCornerShape(12.dp),
            isEnabled = true,
            type = TypesOfConviButs.RECOMMENDED,
            modifier = Modifier,
            contentDescription = 0.0.toString()
        )
    }
}