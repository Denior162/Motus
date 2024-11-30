package com.denior.motus.bluetooth.inerfaces

import com.denior.motus.bluetooth.ConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface BluetoothConnectionInterface {
    val connectionState: StateFlow<ConnectionStatus>
    fun connect(deviceAddress: String)
    fun disconnect()
}