package com.fittech.aigymplanner.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val label: String) {
    English("en", "English"),
    Uzbek("uz", "Uzbek"),
    Russian("ru", "Russian")
}

class LanguageViewModel : ViewModel() {
    private val _currentLanguage = MutableStateFlow(AppLanguage.English)
    val currentLanguage = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }
}
