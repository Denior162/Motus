package com.denior.motus.domain

import android.bluetooth.BluetoothDevice
import com.denior.motus.bluetooth.manager.BluetoothConnectionManager
import com.denior.motus.bluetooth.manager.DeviceScanner
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.bluetooth.state.SearchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class BluetoothUseCase @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val bluetoothConnectionManager: BluetoothConnectionManager,
    private val externalScope: CoroutineScope
) {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> get() = _searchState

    private val _deviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val deviceList: StateFlow<List<BluetoothDevice>> get() = _deviceList

    private var lastConnectAttempt: Long = 0
    private val debounceInterval = 2000

    fun startScanning(targetDeviceAddress: String) {
        externalScope.launch(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                _searchState.value = SearchState.Scanning
                deviceScanner.startScanning()

                try {
                    withTimeout(10000) {
                        while (!_deviceList.value.any { it.address == targetDeviceAddress }) {
                            delay(100)
                        }
                        val endTime = System.currentTimeMillis()
                        val scanDuration = endTime - startTime
                        connectToDevice(targetDeviceAddress)
                        _searchState.value = SearchState.Success
                    }
                } catch (e: Exception) {
                    // Логируем ошибку
                    _searchState.value = SearchState.Error
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error
            } finally {
                deviceScanner.stopScanning()
                _searchState.value = SearchState.Idle
            }
        }
    }

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.Idle
    }

    fun connectToDevice(deviceAddress: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastConnectAttempt < debounceInterval) {
            return
        }
        lastConnectAttempt = currentTime

        externalScope.launch(Dispatchers.IO) {
            if (bluetoothConnectionManager.connectionState.value == ConnectionState.Connected()) {
                bluetoothConnectionManager.disconnect()
                delay(500)
            }
            bluetoothConnectionManager.connect(deviceAddress)
        }
    }

    fun disconnect() {
        externalScope.launch(Dispatchers.IO) {
            bluetoothConnectionManager.disconnect()
        }
    }
}