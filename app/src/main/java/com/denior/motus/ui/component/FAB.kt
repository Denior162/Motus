package com.denior.motus.ui.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.screen.DeviceSelectionItem
import com.denior.motus.ui.state.SearchState
import com.denior.motus.ui.viewmodel.MotusViewModel

@Composable
fun SuperDeviceFAB(viewModel: MotusViewModel, permission: Boolean) {
    val deviceList by viewModel.deviceList.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    var showDeviceList by remember { mutableStateOf(false) }
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    val fabIcon = when {
        isConnected -> Icons.Default.Search
        searchState == SearchState.Scanning -> null
        else -> Icons.Default.Search
    }

    val fabText = when {
        isConnected -> "Change Device"
        searchState == SearchState.Scanning -> "Scanning..."
        else -> "Select Device"
    }

    val permissions = remember {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

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
            if (isConnected) {
                viewModel.disconnect()
                showDeviceList = true
            } else {
                if (permission) {
                    viewModel.startScanning()
                    showDeviceList = true
                } else {
                    permissionLauncher.launch(permissions)
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
        expanded = true
    )

    if (showDeviceList) {
        AlertDialog(
            onDismissRequest = {
                showDeviceList = false
                if (searchState == SearchState.Scanning) {
                    viewModel.stopScanning()
                }
            },
            title = { Text("Available Devices") },
            text = {
                LazyColumn {
                    items(deviceList) { device ->
                        DeviceSelectionItem(
                            device = device,
                            onConnect = {
                                viewModel.connectToDevice(it.address)
                                showDeviceList = false
                            }
                        )
                    }

                    if (deviceList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (searchState == SearchState.Scanning) {
                                    CircularProgressIndicator()
                                }
                                Text("No devices found")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDeviceList = false
                    if (searchState == SearchState.Scanning) {
                        viewModel.stopScanning()
                    }
                }) {
                    Text("Close")
                }
            }
        )
    }
}