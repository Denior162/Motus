package com.denior.motus.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.denior.motus.bluetooth.inerfaces.DeviceScannerInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class DeviceScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?
) : DeviceScannerInterface {

    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val _deviceList = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    override val deviceList: StateFlow<Set<BluetoothDevice>> get() = _deviceList

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            _deviceList.value = _deviceList.value.toMutableSet().apply { add(result.device) }
        }
    }

    override fun startScanning() {
        bluetoothLeScanner?.startScan(leScanCallback)
    }

    override fun stopScanning() {
        bluetoothLeScanner?.stopScan(leScanCallback)
    }
}


