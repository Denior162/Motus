package com.denior.motus.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
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
fun ValueSelectorButtonRow(
    isEnabled: Boolean,
    onValueChanged: (Float) -> Unit,
    values: List<Float>,
    isRecommended: Float? = null,
    contentDescriptionForParameter: (Float) -> String,
    modifier: Modifier = Modifier
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
                isRecommended?.let { it == value } == true -> TypesOfConviButs.RECOMMENDED
                isFirst || isLast -> TypesOfConviButs.PRIMARY
                else -> TypesOfConviButs.STANDARD
            }

            ConvenientFABLikeSquareButton(
                onClick = onValueChanged,
                value = value,
                shape = shape,
                type = type,
                modifier = modifier.weight(1f).aspectRatio(1f),
                isEnabled = isEnabled,
                contentDescription = contentDescriptionForParameter(value)

            )
        }
    }
}

enum class TypesOfConviButs {
    PRIMARY, RECOMMENDED, STANDARD
}

@Composable
fun ConvenientFABLikeSquareButton(
    isEnabled: Boolean,
    onClick: (Float) -> Unit,
    value: Float,
    type: TypesOfConviButs,
    shape: Shape,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    val buttonModifier =
        Modifier
            .semantics { this.contentDescription = contentDescription }
            .then(modifier)
    when (type) {
        TypesOfConviButs.PRIMARY -> FilledIconButton(enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            onClick = { onClick(value) }) {
            Text("${value.toInt()}")
        }

        TypesOfConviButs.STANDARD -> FilledTonalIconButton(enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            onClick = { onClick(value) }) {
            Text("${value.toInt()}")
        }

        TypesOfConviButs.RECOMMENDED -> FilledIconButton(enabled = isEnabled,
            shape = shape,
            modifier = buttonModifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            onClick = { onClick(value) }) {
            Text("${value.toInt()}")
        }
    }
}

@Composable
@Preview (showBackground = true)
fun RowOfConvenientButtonsPrev() {
    ValueSelectorButtonRow(isEnabled = true,
        onValueChanged = { },
        values = listOf(15f, 19f, 30f, 45f, 60f, 90f),
        isRecommended = 19f,
        contentDescriptionForParameter = { float ->
            when (float) {
                0f -> "Set minimum speed"
                60f -> "Set maximum speed"
                else -> "Set speed to ${float.toInt()} RPM"
            }
        })
}

@Preview
@Composable
fun ConvenientFABLikeSquareButtonPreview() {
    MaterialTheme {
        ConvenientFABLikeSquareButton(
            onClick = {},
            value = 45f,
            shape = RoundedCornerShape(16.dp),
            isEnabled = true,
            type = TypesOfConviButs.RECOMMENDED,
            modifier = Modifier,
            contentDescription = 0.0.toString()
        )
    }
}