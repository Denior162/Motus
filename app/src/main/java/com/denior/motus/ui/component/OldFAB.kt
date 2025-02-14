package com.denior.motus.ui.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.state.SearchState
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun OldDeviceFAB(viewModel: MotusViewModel, permission: Boolean, isConnected: Boolean) {
    val deviceList by viewModel.deviceList.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    var showDeviceList by remember { mutableStateOf(false) }
    val connectionState by viewModel.connectionState.collectAsState()

    val containerColor = when {
        !permission -> MaterialTheme.colorScheme.tertiary
        connectionState is ConnectionState.Failed -> MaterialTheme.colorScheme.error
        isConnected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    val fabIcon = when {
        !permission -> Icons.Default.Warning
        connectionState is ConnectionState.Failed -> Icons.Default.Warning
        isConnected -> Icons.Default.Search
        searchState == SearchState.Scanning -> null
        else -> Icons.Default.Search
    }

    val fabText: String = when {
        !permission -> stringResource(R.string.needs_permissions)
        connectionState is ConnectionState.Failed -> stringResource(R.string.connection_failed)
        isConnected -> stringResource(R.string.change_device)
        searchState == SearchState.Scanning -> stringResource(R.string.scanning)
        else -> stringResource(R.string.select_device)
    }

    val permissions = remember {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }

    var shouldRequestPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        shouldRequestPermissions = false
        if (perms.all { it.value }) {
            viewModel.startScanning()
            showDeviceList = true
        }
    }

    LaunchedEffect(shouldRequestPermissions) {
        if (shouldRequestPermissions) {
            permissionLauncher.launch(permissions)
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            when {
                connectionState is ConnectionState.Failed -> {
                    viewModel.disconnect()
                    viewModel.clearDevices()
                    viewModel.startScanning()
                    showDeviceList = true
                }
                isConnected -> {
                    viewModel.disconnect()
                    viewModel.clearDevices()
                    viewModel.stopScanning()
                    showDeviceList = true
                }
                !permission -> {
                    shouldRequestPermissions = true
                }
                searchState != SearchState.Scanning -> {
                    viewModel.startScanning()
                    showDeviceList = true
                }
            }
        },
        icon = {
            if (searchState == SearchState.Scanning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                fabIcon?.let { Icon(it, "Device Control") }
            }
        },
        text = { Text(fabText) },
        expanded = !isConnected,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}