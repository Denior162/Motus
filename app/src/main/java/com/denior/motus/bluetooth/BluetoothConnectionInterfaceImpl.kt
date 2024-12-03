package com.denior.motus.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.denior.motus.bluetooth.inerfaces.BluetoothConnectionInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID



class BluetoothConnectionInterfaceImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    override val connectionState: StateFlow<ConnectionStatus>,
    override val receivedPower: StateFlow<Float>?
) : BluetoothConnectionInterface {

    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("Bluetooth", "Connected to device: ${gatt.device.address}")
                    (connectionState as MutableStateFlow).value = ConnectionStatus.CONNECTED
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("Bluetooth", "Disconnected from device: ${gatt.device.address}")
                    (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
                }

                else -> {
                    Log.e("Bluetooth", "Unknown connection state: $newState")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
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
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Characteristic read: ${characteristic.uuid}")
                Log.d("Bluetooth", "Value: ${value.joinToString { it.toString(16) }}")
            } else {
                Log.e("Bluetooth", "Failed to read characteristic with status: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Successfully wrote characteristic: ${characteristic.uuid}")
            } else {
                Log.e("Bluetooth", "Failed to write characteristic with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d("Bluetooth", "Characteristic changed: ${characteristic.uuid}")
            Log.d("Bluetooth", "New value: ${value.joinToString { it.toString(16) }}")
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
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun sendPower(power: Float, value: ByteArray) {
        val gatt = bluetoothGatt
        if (gatt == null) {
            Log.e("Bluetooth", "Not connected to a GATT server")
            return
        }

        // Replace with the actual UUIDs of your service and characteristic
        val serviceUuid = UUID.fromString("YOUR_SERVICE_UUID")
        val characteristicUuid = UUID.fromString("YOUR_CHARACTERISTIC_UUID")

        val service = gatt.getService(serviceUuid)
        if (service == null) {
            Log.e("Bluetooth", "Service with UUID $serviceUuid not found")
            return
        }

        val characteristic = service.getCharacteristic(characteristicUuid)
        if (characteristic == null) {
            Log.e("Bluetooth", "Characteristic with UUID $characteristicUuid not found")
            return
        }

        // Ensure the characteristic supports write
        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            Log.e("Bluetooth", "Characteristic ${characteristic.uuid} does not support write")
            return
        }
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT




        try {
            gatt.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
                Log.d(
                    "Bluetooth",
                    "sendPower: Successfully initiated write of value ${
                        value.joinToString(" ") { "%02x".format(it) }
                    }"
                )
        } catch (e: Exception) {
            Log.e("Bluetooth", "sendPower: Exception during write", e)
        }
    }
}

//class BluetoothConnectionInterfaceImpl(
//    private val context: Context,
//    private val bluetoothAdapter: BluetoothAdapter,
//    override val connectionState: StateFlow<ConnectionStatus>
//) : BluetoothConnectionInterface {
//
//    private var bluetoothGatt: BluetoothGatt? = null
//
//    override fun connect(deviceAddress: String) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.e("Bluetooth", "Bluetooth permission not granted")
//            return
//        }
//
//        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
//        Log.d("Bluetooth", "Connecting to device: ${device.name} (${device.address})")
//
//        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
//            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//                when (newState) {
//                    BluetoothProfile.STATE_CONNECTED -> {
//                        Log.d("Bluetooth", "Connected to device: ${gatt.device.address}")
//                        (connectionState as MutableStateFlow).value = ConnectionStatus.CONNECTED
//                        gatt.discoverServices()
//                    }
//
//                    BluetoothProfile.STATE_DISCONNECTED -> {
//                        Log.d("Bluetooth", "Disconnected from device: ${gatt.device.address}")
//                        (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
//                    }
//                }
//            }
//
//            val gattCallback = object : BluetoothGattCallback() {
//
//                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        Log.d("Bluetooth", "Services discovered")
//
//                        val services = gatt.services
//                        for (service in services) {
//                            Log.d("Bluetooth", "Service UUID: ${service.uuid}")
//
//                            val characteristics = service.characteristics
//                            for (characteristic in characteristics) {
//                                Log.d("Bluetooth", "Characteristic UUID: ${characteristic.uuid}")
//
//                                if (characteristic.uuid == UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")) {
//                                    gatt.readCharacteristic(characteristic)
//                                }
//                            }
//                        }
//                    } else {
//                        Log.d("Bluetooth", "Failed to discover services, status: $status")
//                    }
//                }
//
//                override fun onCharacteristicWrite(
//                    gatt: BluetoothGatt,
//                    characteristic: BluetoothGattCharacteristic,
//                    status: Int
//                ) {
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        Log.d(
//                            "Bluetooth",
//                            "Successfully wrote characteristic: ${characteristic.uuid}"
//                        )
//                    } else {
//                        Log.d("Bluetooth", "Failed to write characteristic: ${characteristic.uuid}")
//                    }
//                }
//            }
//
//            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.d("Bluetooth", "Services discovered:")
//                    for (service in gatt.services) {
//                        Log.d("Bluetooth", "Service UUID: ${service.uuid}")
//                        for (characteristic in service.characteristics) {
//                            Log.d("Bluetooth", "  Characteristic UUID: ${characteristic.uuid}")
//                            Log.d("Bluetooth", "    Properties: ${characteristic.properties}")
//                        }
//                    }
//                } else {
//                    Log.e("Bluetooth", "Service discovery failed with status: $status")
//                }
//            }
//        }
//        )
//    }
//
//    override fun disconnect() {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        bluetoothGatt?.disconnect()
//        bluetoothGatt?.close()
//        bluetoothGatt = null
//        (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
//    }
//}
