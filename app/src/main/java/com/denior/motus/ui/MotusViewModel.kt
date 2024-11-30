package com.denior.motus.ui

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denior.motus.bluetooth.ConnectionStatus
import com.denior.motus.bluetooth.DeviceScanner
import com.denior.motus.bluetooth.inerfaces.BluetoothConnectionInterface
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> get() = _connectionStatus

    private val _searchState = MutableStateFlow(SearchState.IDLE)
    val searchState: StateFlow<SearchState> get() = _searchState

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> get() = _heartRate

    fun startScanning() {
        _searchState.value = SearchState.SCANNING
        deviceScanner.startScanning()

        viewModelScope.launch {
            delay(5000)
            _searchState.value = SearchState.SUCCESS
            delay(1000)
            _searchState.value = SearchState.IDLE
        }
    }

    fun stopScanning() {
        deviceScanner.stopScanning()
        _searchState.value = SearchState.IDLE
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    fun connectToDevice(deviceAddress: String) {
        Log.d("MotusViewModel", "Connecting to device: $deviceAddress")
        bluetoothConnectionManager.connect(deviceAddress)

        // Подписка на изменения статуса подключения
        viewModelScope.launch {
            bluetoothConnectionManager.connectionState.collect { status ->
                when (status) {
                    ConnectionStatus.CONNECTED -> _connectionStatus.value = ConnectionStatus.CONNECTED
                    ConnectionStatus.DISCONNECTED -> _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    ConnectionStatus.FAILED -> _connectionStatus.value = ConnectionStatus.FAILED // Добавьте статус ошибки
                    ConnectionStatus.CONNECTING -> TODO()
                }
            }
        }
    }


    fun disconnectDevice() {
        Log.d("MotusViewModel", "Disconnecting from device")
        bluetoothConnectionManager.disconnect()
    }

    fun updateHeartRate(value: Int) {
        Log.d("MotusViewModel", "Updating heart rate to: $value")
        _heartRate.value = value
    }

}
