package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DiaryPink,
    onPrimary = Color.White,
    primaryContainer = DiaryPinkContainer,
    onPrimaryContainer = DiaryOnPinkContainer,
    secondary = DiaryPinkDark,
    onSecondary = Color.White,
    secondaryContainer = DiaryPinkSubtle,
    onSecondaryContainer = DiaryOnPinkContainer,
    tertiary = DiaryPinkLight,
    onTertiary = Color.White,
    background = DiaryBackground,
    onBackground = DiaryTextPrimary,
    surface = DiarySurface,
    onSurface = DiaryTextPrimary,
    surfaceVariant = DiarySurfaceVariant,
    onSurfaceVariant = DiaryTextSecondary,
    outline = DiaryOutline,
    outlineVariant = DiaryOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DiaryPinkLight,
    onPrimary = Color.White,
    primaryContainer = DiaryOnPinkContainer,
    onPrimaryContainer = DiaryPinkContainer,
    secondary = DiaryPink,
    onSecondary = Color.White,
    background = DiaryDarkBackground,
    onBackground = DiaryDarkTextPrimary,
    surface = DiaryDarkSurface,
    onSurface = DiaryDarkTextPrimary,
    surfaceVariant = DiaryDarkSurfaceVariant,
    onSurfaceVariant = DiaryDarkTextSecondary,
    outline = DiaryDarkSurfaceVariant,
    outlineVariant = DiaryDarkSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

