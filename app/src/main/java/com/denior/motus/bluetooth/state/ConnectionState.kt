package com.denior.motus.bluetooth.state

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object ConnectingToDevice : ConnectionState()
    data class Connected(
        val deviceName: String? = null,
    ) : ConnectionState()

    data class Failed(val error: String? = null) : ConnectionState()
}