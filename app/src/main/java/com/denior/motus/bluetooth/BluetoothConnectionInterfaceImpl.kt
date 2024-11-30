package com.denior.motus.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.denior.motus.bluetooth.inerfaces.BluetoothConnectionInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BluetoothConnectionInterfaceImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    override val connectionState: StateFlow<ConnectionStatus>
) : BluetoothConnectionInterface {

    private var bluetoothGatt: BluetoothGatt? = null

    override fun connect(deviceAddress: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        bluetoothGatt = gatt // Сохраняем ссылку на GATT
                        (connectionState as MutableStateFlow).value = ConnectionStatus.CONNECTED
                        gatt.discoverServices() // Discover services
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
                        bluetoothGatt = null
                    }
                    else -> {
                        // Обработка других состояний, если необходимо
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")) // Heart Rate Service
                    val characteristic = service?.getCharacteristic(UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")) // Heart Rate Measurement

                    if (characteristic != null) {
                        gatt.readCharacteristic(characteristic) // Чтение характеристики
                        gatt.setCharacteristicNotification(characteristic, true) // Включение уведомлений
                    } else {
                        Log.e("Bluetooth", "Characteristic not found")
                    }
                } else {
                    Log.e("Bluetooth", "Service discovery failed with status: $status")
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = characteristic.value
                    // Пример обработки данных о сердечном ритме
                    if (data.isNotEmpty()) {
                        val heartRate = data[1] // Обычно второй байт содержит значение сердечного ритма
                        Log.d("Bluetooth", "Heart Rate: $heartRate bpm")
                        // Здесь можно обновить состояние в ViewModel или UI
                    }
                } else {
                    Log.e("Bluetooth", "Failed to read characteristic with status: $status")
                }
            }


            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val data = characteristic.value
                Log.d("Bluetooth", "Characteristic changed: ${characteristic.uuid}, Data: ${data.joinToString()}")
            }
        })
    }

    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        (connectionState as MutableStateFlow).value = ConnectionStatus.DISCONNECTED
    }
}
