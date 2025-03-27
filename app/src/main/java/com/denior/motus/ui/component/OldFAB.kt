package com.denior.motus.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.bluetooth.state.SearchState
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun OldDeviceFAB(viewModel: MotusViewModel, modifier: Modifier = Modifier) {
    val searchState by viewModel.searchState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val requiredPermissions = remember {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }

    val hasPermissions = remember {
        mutableStateOf(
            requiredPermissions.all {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) ==
                        PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val containerColor = when {
        !hasPermissions.value -> MaterialTheme.colorScheme.tertiary
        connectionState is ConnectionState.Failed -> MaterialTheme.colorScheme.error
        connectionState is ConnectionState.Connected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when {
        !hasPermissions.value -> Icons.Default.Warning
        connectionState is ConnectionState.Failed -> Icons.Default.Warning
        connectionState is ConnectionState.Connected -> Icons.Default.BluetoothConnected
        connectionState is ConnectionState.ConnectingToDevice ||
                searchState == SearchState.Scanning -> null

        else -> Icons.Default.Bluetooth
    }

    val text: String = when {
        !hasPermissions.value -> stringResource(R.string.needs_permissions)
        connectionState is ConnectionState.Failed -> stringResource(R.string.connection_failed)
        connectionState is ConnectionState.Connected -> stringResource(R.string.change_device)
        searchState == SearchState.Scanning -> stringResource(R.string.scanning)
        else -> "Connect"
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.all { it.value }) {
            viewModel.startScanning()
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            when {
                !hasPermissions.value -> permissionLauncher.launch(requiredPermissions)


                connectionState is ConnectionState.Failed -> {
                    viewModel.disconnect()
                    viewModel.clearDevices()
                }

                connectionState is ConnectionState.Connected -> {
                    with(viewModel) {
                        disconnect()
                        clearDevices()
                        stopScanning()
                    }
                }

                searchState != SearchState.Scanning -> {
                    viewModel.startScanning()
                }
            }
        },
        icon = {
            if (icon == null) {
                WigglingEyesIndicator()
            } else {
                Icon(icon, "Device Control")

            }
        },
        text = { Text(text) },
        containerColor = containerColor
    )
}