package com.fittech.aigymplanner.viewmodel

import androidx.lifecycle.ViewModel
import com.fittech.aigymplanner.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel : ViewModel() {
    private val _currentTheme = MutableStateFlow(AppTheme.DarkGreen)
    val currentTheme = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }
}
