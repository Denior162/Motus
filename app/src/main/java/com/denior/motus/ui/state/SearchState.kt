package com.denior.motus.ui.state

sealed class SearchState {
    data object Idle : SearchState()
    data object Scanning : SearchState()
    data object Success : SearchState()
data object Error : SearchState()
}