package com.denior.motus.ui.screen

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.component.MotusTopBar
import com.denior.motus.ui.component.SuperDeviceFAB
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun MotorControlScreen(
    viewModel: MotusViewModel = hiltViewModel()
) {
    val motorState by viewModel.motorState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(topBar = { MotusTopBar() },
        floatingActionButton = {
            SuperDeviceFAB(
                viewModel = viewModel,
                permission = true
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MotorStatus(isConnected = isConnected)

            MotorSpeedSlider(
                value = motorState.rpm,
                enabled = isConnected,
                onValueChange = { viewModel.updateRpm(it) }
            )

            MotorAngleSlider(
                value = motorState.angle,
                enabled = isConnected,
                onValueChange = { viewModel.updateAngle(it) }
            )
            when (connectionState) {
                is ConnectionState.Connecting -> {
                }

                is ConnectionState.Failed -> {
                }

                is ConnectionState.Connected -> {
                }

                ConnectionState.Disconnected -> {
                }
            }
        }
    }
}

@Composable
fun MotorSpeedSlider(value: Float, enabled: Boolean, onValueChange: (Float)->Unit){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.speed_label, value.toInt()),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics {
                contentDescription = "Speed control slider"
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            valueRange = 1f..60f,
            steps = 15,
            enabled = enabled,
            modifier = Modifier.semantics {
                stateDescription = "Current speed: ${value.toInt()} RPM"
                contentDescription = "Motor speed control slider"
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1 RPM", style = MaterialTheme.typography.labelSmall)
            Text("60 RPM", style = MaterialTheme.typography.labelSmall)
        }
    }
    }
}

@Composable
fun MotorAngleSlider(value: Float, enabled: Boolean, onValueChange: (Float) -> Unit){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.angle_label, value.toInt()),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics {
                contentDescription = "Angle control slider"            }
        )
        Slider(
            value = value,
            onValueChange = { onValueChange(it)},
            valueRange = -360f..360f,
            steps = 36,
            enabled = enabled,
            modifier = Modifier.semantics {
                stateDescription = "Current angle: ${value.toInt()} degrees"
                contentDescription = "Motor angle control slider"
            }
        )
    }
}

@Composable
fun DeviceSelectionItem(device: BluetoothDevice, onConnect: (BluetoothDevice) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = { onConnect(device) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (ActivityCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Text(
                        text = "Permissions required",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Cannot access device details",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = device.name ?: "Unknown Device",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = { onConnect(device) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Connect"
                )
            }
        }
    }
}

@Composable
private fun MotorStatus(isConnected: Boolean) {
    val statusText = if (isConnected) {
        stringResource(R.string.connected)
    } else {
        stringResource(R.string.disconnected)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Motor status: $statusText"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.motor_status),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isConnected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = statusText,
                        color = if (isConnected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (!isConnected) {
                OutlinedButton(
                    onClick = { /* Handle connection retry */ },
                    modifier = Modifier.semantics {
                        contentDescription = "Retry connection"
                    }
                ) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}