package com.fittech.aigymplanner.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.fittech.aigymplanner.viewmodel.AppLanguage

enum class AppTheme {
    DarkGreen, DarkBlue, Light, DarkAmber
}

data class AppColors(
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val primary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val heroEnd: Color = Color.Transparent
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = Color(0xFF0F1923),
        surface = Color(0xFF162A1C),
        cardBg = Color(0xFF1A2B21),
        primary = Color(0xFF2ECC71),
        textPrimary = Color.White,
        textSecondary = Color(0xFF9BA3AF),
        border = Color(0xFF2A3E2A)
    )
}

val GreenPrimary    = Color(0xFF2ECC71)
val GreenDark       = Color(0xFF1A6B3A)
val TextPrimary     = Color(0xFFFFFFFF)
val TextSecondary   = Color(0xFF9BA3AF)
val BottomNavBorder = Color(0xFF2A3E2A)

// Theme Definitions
private val DarkGreenTheme = AppColors(
    background = Color(0xFF0F1923),
    surface = Color(0xFF162A1C),
    cardBg = Color(0xFF1A2B21),
    primary = Color(0xFF2ECC71),
    textPrimary = Color.White,
    textSecondary = Color(0xFF9BA3AF),
    border = Color(0xFF2A3E2A),
    heroEnd = Color(0xFF1E3A22)
)

private val DarkBlueTheme = AppColors(
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF16213E),
    cardBg = Color(0xFF0F3460),
    primary = Color(0xFF7C83FD),
    textPrimary = Color.White,
    textSecondary = Color(0xFFAAAAAA),
    border = Color(0xFF1A1A2E),
    heroEnd = Color(0xFF1A1A2E)
)

private val LightTheme = AppColors(
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    cardBg = Color.White,
    primary = Color(0xFF2ECC71),
    textPrimary = Color.Black,
    textSecondary = Color(0xFF666666),
    border = Color(0xFFDDDDDD),
    heroEnd = Color(0xFFE8F5E9)
)

private val DarkAmberTheme = AppColors(
    background = Color(0xFF1A1200),
    surface = Color(0xFF2C1E00),
    cardBg = Color(0xFF3D2B00),
    primary = Color(0xFFF1C40F),
    textPrimary = Color.White,
    textSecondary = Color(0xFFBBBBBB),
    border = Color(0xFF1A1200),
    heroEnd = Color(0xFF1A1200)
)

@Composable
fun AIGymPlannerTheme(
    theme: AppTheme = AppTheme.DarkGreen,
    language: AppLanguage = AppLanguage.English,
    content: @Composable () -> Unit
) {
    val appColors = when (theme) {
        AppTheme.DarkGreen -> DarkGreenTheme
        AppTheme.DarkBlue -> DarkBlueTheme
        AppTheme.Light -> LightTheme
        AppTheme.DarkAmber -> DarkAmberTheme
    }

    val appStrings = when (language) {
        AppLanguage.English -> EnStrings
        AppLanguage.Uzbek -> UzStrings
        AppLanguage.Russian -> RuStrings
    }

    val colorScheme = if (theme == AppTheme.Light) {
        lightColorScheme(primary = appColors.primary, background = appColors.background, surface = appColors.surface)
    } else {
        darkColorScheme(primary = appColors.primary, background = appColors.background, surface = appColors.surface)
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppStrings provides appStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
