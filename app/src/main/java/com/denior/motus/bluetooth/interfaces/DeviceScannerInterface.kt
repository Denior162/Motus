package com.denior.motus.bluetooth.interfaces

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface DeviceScannerInterface {
    val deviceList: StateFlow<Set<BluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}