package com.denior.motus.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denior.motus.ui.component.MotorControls
import com.denior.motus.ui.component.MotusTopBar
import com.denior.motus.ui.component.OldDeviceFAB
import com.denior.motus.ui.viewmodel.MotusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotusApp(
    viewModel: MotusViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.motorState.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {

            MotusTopBar(scrollBehavior = scrollBehavior, modifier = Modifier)
        },
        floatingActionButton = {
            OldDeviceFAB(
                viewModel
            )

        }

    ) { innerPadding ->
        MotorControls(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxWidth(),
            rpm = uiState.rpm,
            angle = uiState.angle,
            onRpmChanged = { newSpeed ->
                viewModel.setMotorSpeed(newSpeed)
            },
            onAngleChanged = { newAngle ->
                viewModel.setMotorAngle(newAngle)
            },
            viewModel = viewModel,
        )
    }
}
