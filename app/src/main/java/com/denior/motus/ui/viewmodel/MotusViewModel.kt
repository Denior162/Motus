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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MotusViewModel @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val bluetoothConnectionManager: BluetoothConnectionManager
) : ViewModel() {

    val deviceList: StateFlow<List<BluetoothDevice>> = deviceScanner.deviceList
        .map { it.toList() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val connectionState: StateFlow<ConnectionState> = bluetoothConnectionManager.connectionState

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> get() = _searchState

    private var lastConnectAttempt: Long = 0
    private val debounceInterval = 2000

    private val _motorState = MutableStateFlow(MotorState())
    val motorState: StateFlow<MotorState> = _motorState

    fun updateRpm(newRpm: Float) {
        _motorState.update { currentState ->
            val clampedRpm = newRpm.coerceIn(1f, 60f)
            val newCommand = MotorCommand(
                targetAngle = currentState.angle.toInt(),
                rpm = clampedRpm.toInt()
            )
            sendMotorCommand(newCommand)
            currentState.copy(rpm = clampedRpm)
        }
    }

    fun updateAngle(newAngle: Float) {
        _motorState.update { currentState ->
            val clampedAngle = newAngle.coerceIn(-360f, 360f)
            val newCommand = MotorCommand(
                targetAngle = clampedAngle.toInt(),
                rpm = currentState.rpm.toInt()
            )
            sendMotorCommand(newCommand)
            currentState.copy(angle = clampedAngle)
        }
    }

    data class MotorState(
        val rpm: Float = 0f,
        val angle: Float = 90f
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
        _searchState.value = SearchState.Scanning
        deviceScanner.startScanning()

        viewModelScope.launch {
            delay(5000)
            _searchState.value = SearchState.Success
            delay(1000)
            _searchState.value = SearchState.Idle
        }
    }

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.Idle
    }

    fun connectToDevice(deviceAddress: String) {
        if (!isValidBluetoothAddress(deviceAddress)) {
            Log.e("MotusViewModel", "Invalid Bluetooth address: $deviceAddress")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastConnectAttempt < debounceInterval) {
            Log.d("MotusViewModel", "Connect attempt ignored due to debounce.")
            return
        }
        lastConnectAttempt = currentTime

        if (bluetoothConnectionManager.connectedDeviceAddress == deviceAddress &&
            connectionState.value == ConnectionState.Connected()) {
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

    private fun isValidBluetoothAddress(address: String): Boolean {
        val macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
        return Pattern.matches(macPattern, address)
    }
}