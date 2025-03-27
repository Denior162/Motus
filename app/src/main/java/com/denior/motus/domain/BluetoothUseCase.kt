package com.denior.motus.domain

import com.denior.motus.bluetooth.manager.BluetoothConnectionManager
import com.denior.motus.bluetooth.manager.DeviceScanner
import com.denior.motus.bluetooth.state.SearchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothUseCase @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val bluetoothConnectionManager: BluetoothConnectionManager,
    private val externalScope: CoroutineScope
) {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.Idle
    }

    fun disconnect() {
        externalScope.launch(Dispatchers.IO) {
            bluetoothConnectionManager.disconnect()
        }
    }
}