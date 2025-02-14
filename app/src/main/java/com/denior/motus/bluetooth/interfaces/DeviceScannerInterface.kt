package com.denior.motus.bluetooth.interfaces

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface DeviceScannerInterface {
    val deviceList: StateFlow<Set<BluetoothDevice>>
    val isScanning: StateFlow<Boolean>
    fun startScanning()
    fun stopScanning()
}