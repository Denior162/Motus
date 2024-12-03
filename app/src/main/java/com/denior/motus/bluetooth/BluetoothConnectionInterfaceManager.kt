package com.denior.motus.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.denior.motus.bluetooth.inerfaces.BluetoothConnectionInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BluetoothConnectionManager @Inject constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    override val receivedPower: StateFlow<Float>
) : BluetoothConnectionInterface {

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionStatus> get() = _connectionState
    private var bluetoothGatt: BluetoothGatt? = null

    override fun connect(deviceAddress: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                _connectionState.value = if (newState == BluetoothProfile.STATE_CONNECTED) {
                    ConnectionStatus.CONNECTED
                } else {
                    ConnectionStatus.DISCONNECTED
                }
            }
        })
    }

    override fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionStatus.DISCONNECTED
    }

    override fun sendPower(power: Float, value: ByteArray) {
        TODO("Not yet implemented")
    }
}
