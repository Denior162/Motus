package com.denior.motus.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.component.MotorControls
import com.denior.motus.ui.component.MotusTopBar
import com.denior.motus.ui.component.OldDeviceFAB
import com.denior.motus.ui.viewmodel.MotusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotusApp(
    viewModel: MotusViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val motorState by viewModel.motorState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(
        topBar = {
            MotusTopBar(scrollBehavior = scrollBehavior, modifier = Modifier)
        },
        floatingActionButton = {
            OldDeviceFAB(
                viewModel = viewModel
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        ) { innerPadding ->
        MotorControls(
            modifier = Modifier.padding(innerPadding),
            rpm = motorState.rpm,
            angle = motorState.angle,
            onRpmChanged = { newSpeed ->
                if (isConnected) viewModel.setMotorSpeed(newSpeed)
            },
            onAngleChanged = { newAngle ->
                if (isConnected) viewModel.setMotorAngle(newAngle)
            },
            isEnabled = isConnected,
            connectionState = connectionState
        )
    }
}
