package com.denior.motus.bluetooth.inerfaces

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface DeviceScannerInterface {
    val deviceList: StateFlow<Set<BluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}