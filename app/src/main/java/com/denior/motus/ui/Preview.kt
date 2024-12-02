package com.denior.motus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun Preview() {
    Column {
        Text(text = "Adjust Engine Power")
        Button(onClick = { /* Set engine power to 25% */ }) {
            Text(text = "25% Power")
        }
        Button(onClick = { /* Set engine power to 50% */ }) {
            Text(text = "50% Power")
        }
        Button(onClick = { /* Set engine power to 75% */ }) {
            Text(text = "75% Power")
        }
        Button(onClick = { /* Set engine power to 100% */ }) {
            Text(text = "100% Power")
        }
    }
}
