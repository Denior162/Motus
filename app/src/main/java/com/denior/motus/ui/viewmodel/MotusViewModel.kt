package com.denior.motus.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denior.motus.bluetooth.manager.BluetoothConnectionManager
import com.denior.motus.bluetooth.manager.DeviceScanner
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.data.model.MotorCommand
import com.denior.motus.ui.state.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class MotusViewModel @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val bluetoothConnectionManager: BluetoothConnectionManager
) : ViewModel() {

    private val targetDeviceAddress = "F0:F5:BD:C9:66:1E"

    private val _deviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val deviceList: StateFlow<List<BluetoothDevice>> = _deviceList.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = bluetoothConnectionManager.connectionState

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> get() = _searchState

    private var lastConnectAttempt: Long = 0
    private val debounceInterval = 2000

    private val _motorState = MutableStateFlow(MotorState())
    val motorState: StateFlow<MotorState> = _motorState

    fun setMotorSpeed(rpm: Float) {
        viewModelScope.launch {
            val clampedRpm = rpm.coerceIn(0f, 60f)
            _motorState.update { it.copy(rpm = clampedRpm) }
        }
    }

    fun setMotorAngle(degrees: Float) {
        viewModelScope.launch {
            _motorState.update { currentState ->
                val clampedAngle = degrees.coerceIn(-360f, 360f)
                val newCommand = MotorCommand(
                    targetAngle = clampedAngle.toInt(),
                    rpm = currentState.rpm.toInt()
                )
                try {
                    sendMotorCommand(newCommand)
                } catch (e: Exception) {
                    Log.e("MotusViewModel", "Failed to set motor angle: ${e.message}")
                }
                currentState.copy(angle = clampedAngle)
            }
        }
    }

    data class MotorState(
        val rpm: Float = 0f,
        val angle: Float = 0f
    )

    private fun sendMotorCommand(command: MotorCommand) {
        viewModelScope.launch(Dispatchers.IO) {
            if (connectionState.value !is ConnectionState.Connected) {
                Log.w("MotusViewModel", "Cannot send command: device not connected")
                return@launch
            }

            try {
                bluetoothConnectionManager.sendMotorCommand(command)
            } catch (e: Exception) {
                Log.e("MotusViewModel", "Error sending motor command: ${e.message}")
            }
        }
    }

    fun startScanning() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("MotusViewModel", "Starting scan, looking for device: $targetDeviceAddress")
                _searchState.value = SearchState.Scanning
                deviceScanner.startScanning()

                try {
                    withTimeout(10000) {
                        while (!deviceList.value.any { it.address == targetDeviceAddress }) {
                            Log.d("MotusViewModel", "Current device list: ${
                                deviceList.value.joinToString {
                                    it.address
                                }
                            }")
                            delay(100)
                        }
                        Log.d("MotusViewModel", "Target device found!")
                        deviceList.value.find { it.address == targetDeviceAddress }?.let {
                            connectToDevice(it)
                        }
                    }
                    _searchState.value = SearchState.Success
                } catch (e: Exception) {
                    Log.e("MotusViewModel", "Scanning failed: ${e.message}, devices found: ${
                        deviceList.value.size
                    }")
                    _searchState.value = SearchState.Error
                }
            } catch (e: Exception) {
                Log.e("MotusViewModel", "Unexpected error during scanning: ${e.message}", e)
                _searchState.value = SearchState.Error
            } finally {
                Log.d("MotusViewModel", "Stopping scan...")
                deviceScanner.stopScanning()
                _searchState.value = SearchState.Idle
            }
        }
    }

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.Idle
    }

    private fun connectToDevice(deviceAddress: String = targetDeviceAddress) {

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastConnectAttempt < debounceInterval) {
            Log.d("MotusViewModel", "Connect attempt ignored due to debounce.")
            return
        }
        lastConnectAttempt = currentTime

        if (bluetoothConnectionManager.connectedDeviceAddress == deviceAddress &&
            connectionState.value == ConnectionState.Connected()
        ) {
            Log.d("MotusViewModel", "Already connected to this device: $deviceAddress")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (connectionState.value == ConnectionState.Connected()) {
                bluetoothConnectionManager.disconnect()
                delay(500)
            }
            Log.d("MotusViewModel", "Connecting to device: $deviceAddress")
            bluetoothConnectionManager.connect(deviceAddress)
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MotusViewModel", "Disconnecting from device")
            bluetoothConnectionManager.disconnect()
        }
    }

    fun clearDevices() {
        _searchState.value = SearchState.Idle
    }
}