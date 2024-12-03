package com.denior.motus.bluetooth.state

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val deviceName: String? = null) : ConnectionState()
    data class Failed(val error:String? = null) : ConnectionState()
}