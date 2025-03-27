package com.denior.motus.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class DismissibleTipData(
    val title: String,
    val description: String,
    val onAction: (() -> Unit)? = null
)

@Composable
fun DismissibleTipCard(
    title: String,
    description: String,
    onClose: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionLabel: String = "Действие",
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Filled.BluetoothDisabled,
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = modifier.padding(16.dp)) {
            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                   Row {
                       Icon(
                           modifier = modifier.padding(8.dp),
                           imageVector = imageVector, contentDescription = "Bluetooth disabled"
                       )
                       Text(
                           modifier = Modifier.padding(),
                           text = title,
                           style = MaterialTheme.typography.titleMedium
                       )
                   }
                    Text(
                        modifier = modifier.padding(start = 8.dp),
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    onAction?.let {
                        Row {
                            Button(shape = RoundedCornerShape(16.dp), onClick = onAction) {
                                Text(text = actionLabel)
                            }
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close tip")
                }
            }
        }
    }
}

@Composable
fun ForUserTips(
    modifier: Modifier = Modifier,
    tipIndex: Int
) {
    val tipList = remember {
        mutableStateListOf(
            DismissibleTipData(
                title = "Включить Bluetooth?",
                description = "Для правильной работы приложения включите Bluetooth",
                onAction = { /* Логика включения Bluetooth */ }
            ),
            DismissibleTipData(
                title = "Проверить устройство?",
                description = "Рекомендуется проверить соединение с устройством"
            )
        )
    }

    if (tipIndex < tipList.size) {
        val tip = tipList[tipIndex]
        DismissibleTipCard(
            title = tip.title,
            description = tip.description,
            onAction = tip.onAction,
            onClose = { tipList.remove(tip) }
        )
    }
}
