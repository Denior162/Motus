package com.denior.motus.hilt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.denior.motus.bluetooth.BluetoothConnectionInterfaceImpl
import com.denior.motus.bluetooth.manager.BluetoothConnectionManager
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.bluetooth.manager.DeviceScanner
import com.denior.motus.bluetooth.interfaces.BluetoothConnectionInterface
import com.denior.motus.bluetooth.interfaces.DeviceScannerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter ?: throw IllegalStateException("Bluetooth is not supported on this device")
    }

    @Provides
    @Singleton
    fun provideBluetoothConnection(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): BluetoothConnectionInterface {
        return BluetoothConnectionInterfaceImpl(
            context, 
            bluetoothAdapter, 
            MutableStateFlow(ConnectionState.Idle)
        )
    }

    @Provides
    @Singleton
    fun provideBluetoothConnectionManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): BluetoothConnectionManager {
        return BluetoothConnectionManager(context, bluetoothAdapter)
    }

    @Provides
    @Singleton
    fun provideDeviceScanner(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): DeviceScannerInterface {
        return DeviceScanner(context, bluetoothAdapter)
    }
}