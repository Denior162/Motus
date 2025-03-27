package com.denior.motus.bluetooth.manager

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.denior.motus.bluetooth.interfaces.BluetoothConnectionInterface
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.data.DeviceCharacteristics
import com.denior.motus.data.model.MotorCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

/**
 * Manages Bluetooth GATT connections and interactions.
 *
 * @property context The application [Context] used for permissions and establishing GATT connections.
 * @property bluetoothAdapter The [BluetoothAdapter] used to connect to remote devices.
 */
class BluetoothConnectionManager @Inject constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
) : BluetoothConnectionInterface {
    companion object {
        private const val TAG = "BluetoothConnectionManager"
        private const val CONNECTION_TIMEOUT = 10_000L

        private object Services {
            val MOTOR_SERVICE: UUID = UUID.fromString("00001815-0000-1000-8000-00805f9b34fb")
        }

        private object Characteristics {
            val MOTOR: UUID = UUID.fromString("02001525-1212-efde-1523-785feabcd123")
        }
    }

    val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> get() = _connectionState

    private val _characteristicsFlow = MutableStateFlow<List<DeviceCharacteristics>>(emptyList())
    override val characteristicsFlow:
            StateFlow<List<DeviceCharacteristics>> = _characteristicsFlow

    private var _connectedDeviceAddress: String? = null
    val connectedDeviceAddress: String? get() = _connectedDeviceAddress
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionJob: Job? = null

    private fun BluetoothGattCharacteristic.hasNotifyProperty(): Boolean {
        return properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handleConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            handleServicesDiscovered(
                gatt, status, ByteArray(0)
            )
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == Characteristics.MOTOR) {
                Log.d(TAG, "Received feedback from device: ${value.contentToString()}")
            }
            handleCharacteristicChanged(characteristic, value)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            handleCharacteristicWrite(characteristic, status)
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission
                .BLUETOOTH_CONNECT
        ) == PackageManager
            .PERMISSION_GRANTED && context.checkSelfPermission(
            Manifest
                .permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleMissingPermissions(operation: String) {
        Log.e(TAG, "Missing Bluetooth permissions for operation: $operation")
        _connectionState.value = ConnectionState.Failed("Missing Bluetooth permissions")
    }

    /**
     * Initiates a connection to a remote Bluetooth device with the specified [deviceAddress].
     *
     * If the device is not bonded, an attempt to bond is made. This method updates [_connectionState].
     *
     * @param deviceAddress The MAC address of the remote device to connect to.
     */
    override fun connect(deviceAddress: String) {
        if (!validateBluetoothState(deviceAddress)) return
        if (!hasBluetoothPermissions()) {
            handleMissingPermissions("connect")
            return
        }

        try {
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device not bonded, attempting to create bond")
                if (hasBluetoothPermissions()) {
                    device.createBond()
                } else {
                    handleMissingPermissions("createBond")
                }
            }

            _connectedDeviceAddress = deviceAddress
            _connectionState.value = ConnectionState.ConnectingToDevice

            connectionJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(CONNECTION_TIMEOUT) {
                        bluetoothGatt = device.connectGatt(
                            context,
                            false,
                            gattCallback,
                            BluetoothDevice.TRANSPORT_LE
                        )
                    }
                } catch (e: Exception) {
                    _connectionState.value =
                        ConnectionState.Failed(e.message ?: "Connection timeout")
                    disconnect()
                }
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("connect")
        }
    }

    /**
     * Disconnects from the currently connected Bluetooth device, if any,
     * and cleans up the GATT resources. Resets connection and characteristics state.
     */
    override fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            _connectedDeviceAddress = null
            _connectionState.value = ConnectionState.Idle
            _characteristicsFlow.value = emptyList()
        } catch (e: SecurityException) {
            handleMissingPermissions("disconnect")
        }
    }

    /**
     * Validates that Bluetooth is enabled before attempting a connection.
     *
     * @param deviceAddress The MAC address of the remote device to validate.
     * @return true if Bluetooth is enabled; false otherwise.
     */
    private fun validateBluetoothState(deviceAddress: String): Boolean {
        Log.d(TAG, "Attempting to connect to device: $deviceAddress")
        try {
            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Connection failed: Bluetooth is disabled")
                _connectionState.value = ConnectionState.Failed("Bluetooth is disabled")
                return false
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("validateBluetoothState")
            return false
        }
        return true
    }

    /**
     * Retrieves a [BluetoothGattCharacteristic] by its [serviceUUID] and [characteristicUUID].
     * Returns null if permissions are missing or if the characteristic is not found.
     *
     * @param serviceUUID The UUID of the service containing the characteristic.
     * @param characteristicUUID The UUID of the characteristic to retrieve.
     * @return The requested [BluetoothGattCharacteristic], or null if unavailable.
     */
    private fun getCharacteristic(
        serviceUUID: UUID,
        characteristicUUID: UUID
    ): BluetoothGattCharacteristic? {
        if (!hasBluetoothPermissions()) {
            handleMissingPermissions("getCharacteristic")
            return null
        }

        try {
            return bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        } catch (e: SecurityException) {
            handleMissingPermissions("getCharacteristic")
            return null
        }
    }

    /**
     * Handles changes to the GATT connection state. If successfully connected,
     * initiates service discovery.
     *
     * @param gatt The [BluetoothGatt] instance for the connection.
     * @param status The connection status code.
     * @param newState The new connection state (e.g., connected or disconnected).
     */
    private fun handleConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        try {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Connection failed with status: $status")
                _connectionState.value =
                    ConnectionState.Failed("Connection failed with status: $status")
                return
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server, discovering services...")
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionState.value = ConnectionState.Idle
                    _characteristicsFlow.value = emptyList()
                    bluetoothGatt?.close()
                }

                else -> {
                    Log.w(TAG, "Unknown connection state: $newState")
                    _connectionState.value = ConnectionState
                        .Failed("Unknown state: $newState")
                }
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("handleConnectionStateChange")
            disconnect()
        }
    }

    /**
     * Handles changes to the GATT connection state. If successfully connected,
     * initiates service discovery.
     *
     * @param gatt The [BluetoothGatt] instance for the connection.
     * @param status The connection status code.
     */
    private fun handleServicesDiscovered(gatt: BluetoothGatt, status: Int, value: ByteArray) {
        try {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = ConnectionState.Connected(gatt.device.name)
                val characteristics = gatt.services?.flatMap { service ->
                    service.characteristics?.map { characteristic ->
                        if (characteristic.hasNotifyProperty()) {
                            gatt.setCharacteristicNotification(characteristic, true)
                        }
                        DeviceCharacteristics(
                            uuid = characteristic.uuid.toString(),
                            value = value
                        )
                    } ?: emptyList()
                } ?: emptyList()

                _characteristicsFlow.value = characteristics
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
                disconnect()
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("handleServicesDiscovered")
            disconnect()
        }
    }

    /**
     * Triggered when a characteristic's value changes (e.g., via notification).
     *
     * @param characteristic The [BluetoothGattCharacteristic] that changed.
     * @param value The new byte array representing the updated data.
     */
    private fun handleCharacteristicChanged(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        try {
            Log.d(
                TAG, """
            |Characteristic changed:
            |UUID: ${characteristic.uuid}
            |New value: ${value.contentToString()}
        """.trimMargin()
            )

            val deviceCharacteristic = DeviceCharacteristics(
                uuid = characteristic.uuid.toString(),
                value = value
            )

            _characteristicsFlow.value = _characteristicsFlow.value.map {
                if (it.uuid == deviceCharacteristic.uuid) deviceCharacteristic else it
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("handleCharacteristicChanged")
        }
    }

    /**
     * Handles the result of a characteristic write operation. If unsuccessful due to
     * insufficient authentication, attempts to bond with the device.
     *
     * @param characteristic The [BluetoothGattCharacteristic] written to.
     * @param status The status of the write operation.
     */
    private fun handleCharacteristicWrite(
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                Log.d(TAG, "Write successful for ${characteristic.uuid}")
            }

            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                Log.e(TAG, "Authentication required, attempting to bond")
                _connectedDeviceAddress?.let { address ->
                    try {
                        if (hasBluetoothPermissions()) {
                            bluetoothAdapter.getRemoteDevice(address).createBond()
                        } else {
                            handleMissingPermissions("createBond")
                        }
                    } catch (e: SecurityException) {
                        handleMissingPermissions("createBond")
                    }
                }
            }

            else -> {
                Log.e(TAG, "Characteristic write failed with status: $status")
            }
        }
    }

    /**
     * Sends a [MotorCommand] to the remote device by writing to the motor characteristic.
     * Ensures that the RPM and angle are within a safe range before converting to a byte array.
     *
     * @param command The [MotorCommand] to be sent, containing the target angle and RPM.
     */
    fun sendMotorCommand(command: MotorCommand) {
        if (!hasBluetoothPermissions()) {
            handleMissingPermissions("sendMotorCommand")
            return
        }

        if (connectionState.value !is ConnectionState.Connected) {
            Log.e(TAG, "Cannot send motor command: device not connected")
            return
        }

        try {
            val characteristic = getCharacteristic(Services.MOTOR_SERVICE, Characteristics.MOTOR)
            characteristic?.let { it ->
                val safeRpm = command.rpm.coerceIn(1, 60)
                val safeAngle = command.targetAngle.coerceIn(-360, 360)
                val data = MotorCommand(safeAngle, safeRpm).toByteArray()

                Log.d(TAG, "Sending command: ${data.joinToString { "%02X".format(it) }}")

                try {
                    bluetoothGatt?.writeCharacteristic(
                        it,
                        data,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                } catch (e: SecurityException) {
                    handleMissingPermissions("writeCharacteristic")
                    Log.e(TAG, "SecurityException while writing characteristic", e)
                }
            }
        } catch (e: SecurityException) {
            handleMissingPermissions("getCharacteristic")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending command: ${e.message}")
        }
    }
}