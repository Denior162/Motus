package com.denior.motus.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denior.motus.R
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.bluetooth.state.SearchState
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun OldDeviceFAB(viewModel: MotusViewModel) {
    val deviceList by viewModel.deviceList.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    var showDeviceList by remember { mutableStateOf(false) }
    val connectionState by viewModel.connectionState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val requiredPermissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )

    val hasAllPermissions = remember {
        mutableStateOf(requiredPermissions.all { perm ->
            androidx.core.content.ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        })
    }

    val containerColor = when {
        !hasAllPermissions.value -> MaterialTheme.colorScheme.tertiary
        connectionState is ConnectionState.Failed -> MaterialTheme.colorScheme.error
        connectionState is ConnectionState.Connected  -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    val fabIcon = when {
        !hasAllPermissions.value -> Icons.Default.Warning
        connectionState is ConnectionState.Failed -> Icons.Default.Warning
        connectionState is ConnectionState.Connected -> Icons.Default.Close
        connectionState is ConnectionState.Connecting || searchState == SearchState.Scanning -> null
        else -> Icons.Default.Search
    }

    val fabText: String = when {
        !hasAllPermissions.value -> stringResource(R.string.needs_permissions)
        connectionState is ConnectionState.Failed -> stringResource(R.string.connection_failed)
        connectionState is ConnectionState.Connected -> stringResource(R.string.change_device)
        searchState == SearchState.Scanning -> stringResource(R.string.scanning)
        else -> stringResource(R.string.select_device)
    }



    val fabSize by animateDpAsState(
        targetValue = if (connectionState is ConnectionState.Connecting || 
            searchState == SearchState.Scanning) 96.dp else 56.dp,
        label = "FAB size animation"
    )

    val permissionLauncher = rememberLauncherForActivityResult(

        ActivityResultContracts.RequestMultiplePermissions()

    ) { perms ->
        if (perms.all { it.value }) {
            viewModel.startScanning()
            showDeviceList = true
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            when {
                !hasAllPermissions.value -> {
                    permissionLauncher.launch(requiredPermissions.toTypedArray())
                }

                connectionState is ConnectionState.Failed -> {
                    viewModel.disconnect()
                    viewModel.clearDevices()
                    viewModel.startScanning()
                    showDeviceList = true
                }

                connectionState is ConnectionState.Connected -> {
                    viewModel.disconnect()
                    viewModel.clearDevices()
                    viewModel.stopScanning()
                    showDeviceList = true
                }

                searchState != SearchState.Scanning -> {
                    viewModel.startScanning()
                    showDeviceList = true
                }
            }
        },
        icon = {
            if (connectionState is ConnectionState.Connecting || searchState == SearchState.Scanning) {
                WigglingEyesIndicator(
                    modifier = Modifier.size(36.dp),  
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
            } else {
                fabIcon?.let { Icon(it, "Device Control") }
            }
        },
        text = { Text(fabText) },
        expanded = !(connectionState == ConnectionState.Connecting ||
                searchState == SearchState.Scanning),
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(fabSize)
    )
}