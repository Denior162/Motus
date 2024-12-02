package com.denior.motus.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.denior.motus.R


@Composable
fun MotusApp() {
    DeviceListScreen()
}

@Composable
fun DeviceListScreen(viewModel: MotusViewModel = hiltViewModel()) {
    val deviceList by viewModel.deviceList.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()

    Scaffold(
        topBar = { MotusTopBar() },
        floatingActionButton = { SearchFAB(searchState = searchState, viewModel = viewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Connection status: $connectionStatus",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            heartRate?.let {
                Text(
                    text = "Heart Rate: $it bpm",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } ?: run {
                Text(
                    text = "Heart Rate: Not available",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            ListOfDevices(deviceList = deviceList) { device ->
                viewModel.connectToDevice(device.address)
            }
        }
    }
}


enum class SearchState {
    IDLE,
    SCANNING,
    ERROR,
    SUCCESS
}


@Composable
fun SearchFAB(searchState: SearchState, viewModel: MotusViewModel) {
    val icon = getSearchIcon(searchState)
    val contentDescription = getSearchContentDescription(searchState)

    LargeFloatingActionButton(onClick = {
        when (searchState) {
            SearchState.IDLE, SearchState.ERROR -> viewModel.startScanning()
            SearchState.SCANNING -> viewModel.stopScanning()
            SearchState.SUCCESS -> {} // Можно добавить функционал для повторного сканирования
        }
    }) {
        if (searchState == SearchState.SCANNING) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Icon(imageVector = icon!!, contentDescription = contentDescription)
        }
    }
}


@Composable
fun ListOfDevices(
    deviceList: List<BluetoothDevice>, onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn (verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(deviceList) { index, device ->            DeviceElement(device = device, onClick = { onDeviceClick(device) })
        }
    }
}

@Composable
fun DeviceElement(
    device: BluetoothDevice, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)
        ) {
            Column {
                Text(
                    text = device.name ?: "Unknown name :-/",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun getSearchIcon(searchState: SearchState): ImageVector? {
    return when (searchState) {
        SearchState.IDLE -> Icons.Default.Search
        SearchState.SCANNING -> null // Иконка не нужна во время сканирования
        SearchState.ERROR -> Icons.Default.Warning
        SearchState.SUCCESS -> Icons.Default.Check
    }
}

@Composable
fun getSearchContentDescription(searchState: SearchState): String {
    return when (searchState) {
        SearchState.IDLE -> "Start Scanning"
        SearchState.SCANNING -> "Scanning in Progress"
        SearchState.ERROR -> "Error during Scanning"
        SearchState.SUCCESS -> "Scanning Completed Successfully"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotusTopBar(title: String? = null) {
    CenterAlignedTopAppBar(title = { Text(title ?: stringResource(R.string.app_name)) })
}

@Preview
@Composable
fun MotusAppPreview() {
    MotusApp()
}
