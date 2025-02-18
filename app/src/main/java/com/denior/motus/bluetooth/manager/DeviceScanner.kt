package com.denior.motus.bluetooth.manager

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.denior.motus.bluetooth.interfaces.DeviceScannerInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class DeviceScanner @Inject constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) : DeviceScannerInterface {

    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val _deviceList = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    override val deviceList: StateFlow<Set<BluetoothDevice>> get() = _deviceList
    private val scanPeriod: Long = 10000

    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Check permission at runtime.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            try {
                Log.d("DeviceScanner", """
                |Device found:
                |Address: ${result.device.address}
                |Name: ${result.device.name}
                |RSSI: ${result.rssi}
                |TX Power: ${result.txPower}
            """.trimMargin())
            } catch (se: SecurityException) {
                Log.e("DeviceScanner", "SecurityException: ${se.message}")
            }

            _deviceList.value = _deviceList.value.toMutableSet().apply { add(result.device) }
        }
    }

    override fun startScanning() {
        if (scanning) return

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("DeviceScanner", "Missing BLUETOOTH_SCAN permission")
            return
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(enableBtIntent)
            return
        }

        try {
            Log.d("DeviceScanner", "Starting BLE scan...")
            handler.postDelayed({
                stopScanning()
            }, scanPeriod)

            try {
                bluetoothLeScanner?.startScan(leScanCallback) ?: run {
                    Log.e("DeviceScanner", "BluetoothLeScanner is null")
                    return
                }
            } catch (se: SecurityException) {
                Log.e("DeviceScanner", "SecurityException: ${se.message}. BLUETOOTH_PRIVILEGED.")
                return
            }
            
            scanning = true
            _isScanning.value = true

        } catch (e: Exception) {
            Log.e("DeviceScanner", "Error starting scan: ${e.message}", e)
        }
    }

    override fun stopScanning() {
        if (!scanning) return

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("DeviceScanner", "Missing BLUETOOTH_SCAN permission")
            return
        }

        try {
            Log.d("DeviceScanner", "Stopping BLE scan...")
            bluetoothLeScanner?.stopScan(leScanCallback)
            scanning = false
            _isScanning.value = false

        } catch (e: Exception) {
            Log.e("DeviceScanner", "Error stopping scan: ${e.message}", e)
        }
    }
}