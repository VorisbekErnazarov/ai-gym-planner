package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fittech.aigymplanner.ui.theme.appColors
import com.fittech.aigymplanner.ui.theme.appStrings

@Composable
fun ProfileScreen() {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Text("${strings.profile} Screen (Coming Soon)", color = colors.textPrimary)
        }
    }
}
