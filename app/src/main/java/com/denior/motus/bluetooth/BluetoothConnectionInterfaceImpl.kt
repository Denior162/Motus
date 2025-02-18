package com.denior.motus.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.denior.motus.bluetooth.interfaces.BluetoothConnectionInterface
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.data.DeviceCharacteristics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class BluetoothConnectionInterfaceImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    override val connectionState: StateFlow<ConnectionState>,
) : BluetoothConnectionInterface {

    private val _characteristicsFlow = MutableStateFlow<List<DeviceCharacteristics>>(emptyList())
    override val characteristicsFlow: StateFlow<List<DeviceCharacteristics>> = _characteristicsFlow

    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            try {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("Bluetooth", "Connected to device: ${gatt.device.address}")
                        (connectionState as MutableStateFlow).value = ConnectionState.Connected()
                        gatt.discoverServices()
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("Bluetooth", "Disconnected from device: ${gatt.device.address}")
                        (connectionState as MutableStateFlow).value = ConnectionState.NotConnected
                    }

                    else -> {
                        Log.e("Bluetooth", "Unknown connection state: $newState")
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Security exception during connection state change", e)
                (connectionState as MutableStateFlow).value = ConnectionState.NotConnected
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("Bluetooth", "Services discovered:")
                    gatt.services.forEach { service ->
                        Log.d("Bluetooth", "Service UUID: ${service.uuid}")
                        service.characteristics.forEach { characteristic ->
                            Log.d("Bluetooth", "  Characteristic UUID: ${characteristic.uuid}")
                            Log.d("Bluetooth", "    Properties: ${characteristic.properties}")
                            if (characteristic.uuid == UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")) {
                                gatt.readCharacteristic(characteristic)
                            }
                        }
                    }
                } else {
                    Log.e("Bluetooth", "Service discovery failed with status: $status")
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Security exception during service discovery", e)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("Bluetooth", "Characteristic read: ${characteristic.uuid}")
                    Log.d("Bluetooth", "Value: ${value.joinToString { it.toString(16) }}")
                } else {
                    Log.e("Bluetooth", "Failed to read characteristic with status: $status")
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Security exception during characteristic read", e)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("Bluetooth", "Successfully wrote characteristic: ${characteristic.uuid}")
                } else {
                    Log.e("Bluetooth", "Failed to write characteristic with status: $status")
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Security exception during characteristic write", e)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            try {
                Log.d("Bluetooth", "Characteristic changed: ${characteristic.uuid}")
                Log.d("Bluetooth", "New value: ${value.joinToString { it.toString(16) }}")

                val deviceCharacteristic = DeviceCharacteristics(
                    uuid = characteristic.uuid.toString(),
                    value = value
                )

                _characteristicsFlow.update { currentList ->
                    val newList = currentList.toMutableList()
                    val index = newList.indexOfFirst { it.uuid == deviceCharacteristic.uuid }
                    if (index != -1) {
                        newList[index] = deviceCharacteristic
                    } else {
                        newList.add(deviceCharacteristic)
                    }
                    newList
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Security exception during characteristic changed", e)
            }
        }
    }

    override fun connect(deviceAddress: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Bluetooth", "Bluetooth permission not granted")
            return
        }

        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        Log.d("Bluetooth", "Connecting to device: ${device.name} (${device.address})")

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    override fun disconnect() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Bluetooth", "Bluetooth permission not granted")
            return
        }
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            (connectionState as MutableStateFlow).value = ConnectionState.NotConnected
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Security exception during disconnect", e)
        }
    }
}