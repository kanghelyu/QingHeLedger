package com.qinghe.ledger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val QingHe = Color(0xFF0B7A66)
val QingHeDeep = Color(0xFF075A4D)
val WarmGold = Color(0xFFF4B860)
val ExpenseRed = Color(0xFFE76F51)
val IncomeGreen = Color(0xFF168C68)

private val LightColors = lightColorScheme(
    primary = QingHe,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F2E7),
    onPrimaryContainer = Color(0xFF073D34),
    secondary = Color(0xFF4B635C),
    tertiary = WarmGold,
    background = Color(0xFFF7F9F6),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EFEC),
    onSurface = Color(0xFF17201D),
    onSurfaceVariant = Color(0xFF66736E),
    error = ExpenseRed,
    outline = Color(0xFFB9C5C0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF73D8BC),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF075A4D),
    onPrimaryContainer = Color(0xFFB3F0DE),
    secondary = Color(0xFFB4CCC3),
    tertiary = Color(0xFFFFCF7D),
    background = Color(0xFF101714),
    surface = Color(0xFF17201D),
    surfaceVariant = Color(0xFF25302C),
    onSurface = Color(0xFFE4ECE8),
    onSurfaceVariant = Color(0xFFB9C5C0),
    error = Color(0xFFFFB4A2),
    outline = Color(0xFF87938E)
)

@Composable
fun QingHeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        }
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
