package com.denior.motus.bluetooth.interfaces

import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.data.DeviceCharacteristics
import kotlinx.coroutines.flow.StateFlow

interface BluetoothConnectionInterface {
    val connectionState: StateFlow<ConnectionState>
    val characteristicsFlow: StateFlow<List<DeviceCharacteristics>>
    fun connect(deviceAddress: String)
    fun disconnect()
}