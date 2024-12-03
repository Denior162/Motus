package com.denior.motus.ui.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.denior.motus.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotusTopBar(title: String? = null) {
    CenterAlignedTopAppBar(title = { Text(title ?: stringResource(R.string.app_name)) })
}