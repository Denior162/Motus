package com.denior.motus.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.denior.motus.bluetooth.state.ConnectionState
import com.denior.motus.ui.viewmodel.MotusViewModel
import org.junit.Rule
import org.junit.Test

class ControlScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun controlScreen_shouldDisplaySpeedControl() {
        val mockMotorState = MotusViewModel.MotorState(rpm = 15f, angle = 10f)

        composeTestRule.setContent {
            ControlScreen(
                modifier = androidx.compose.ui.Modifier,
                connectionState = ConnectionState.Connected(),
                onSpeedChange = {},
                onAngleChange = {},
                isConnected = true,
                motorState = mockMotorState
            )
        }

        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText("19").performClick()
    }
}