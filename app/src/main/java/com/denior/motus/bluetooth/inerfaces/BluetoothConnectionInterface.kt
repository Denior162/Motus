package com.denior.motus.bluetooth.inerfaces

import com.denior.motus.bluetooth.ConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface BluetoothConnectionInterface {
    val connectionState: StateFlow<ConnectionStatus>
    val receivedPower: StateFlow<Float>?
    fun connect(deviceAddress: String)
    fun disconnect()
    fun sendPower(power: Float, value: ByteArray)
}