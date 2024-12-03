package com.denior.motus.ui

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denior.motus.bluetooth.ConnectionStatus
import com.denior.motus.bluetooth.DeviceScanner
import com.denior.motus.bluetooth.inerfaces.BluetoothConnectionInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotusViewModel @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val bluetoothConnectionManager: BluetoothConnectionInterface
) : ViewModel() {

    val deviceList: StateFlow<List<BluetoothDevice>> = deviceScanner.deviceList
        .map { it.toList() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val connectionStatus: StateFlow<ConnectionStatus> = bluetoothConnectionManager.connectionState

    private val _searchState = MutableStateFlow(SearchState.IDLE)
    val searchState: StateFlow<SearchState> get() = _searchState

    val receivedPower: StateFlow<Float>? = bluetoothConnectionManager.receivedPower

    private val _selectedPower = MutableStateFlow(0f)
    val selectedPower: StateFlow<Float> get() = _selectedPower

    private var lastConnectAttempt: Long = 0
    private val debounceInterval = 2000

    fun startScanning() {
        _searchState.value = SearchState.SCANNING
        deviceScanner.startScanning()

        viewModelScope.launch {
            delay(5000) // Завершаем сканирование через 5 секунд
            _searchState.value = SearchState.SUCCESS
            delay(1000)
            _searchState.value = SearchState.IDLE
        }
    }

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.IDLE
    }

    fun connectToDevice(deviceAddress: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastConnectAttempt < debounceInterval) {
            Log.d("MotusViewModel", "Connect attempt ignored due to debounce.")
            return
        }
        lastConnectAttempt = currentTime

        if (connectionStatus.value != ConnectionStatus.DISCONNECTED) {
            Log.d("MotusViewModel", "Cannot connect. Current state: ${connectionStatus.value}")
            return
        }

        Log.d("MotusViewModel", "Connecting to device: $deviceAddress")
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothConnectionManager.connect(deviceAddress)
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothConnectionManager.disconnect()
        }
    }

    fun updateSelectedPower(newPower: Float) {
        _selectedPower.value = newPower
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothConnectionManager.sendPower(
                newPower,
                value = TODO()
            )
        }
    }
}

