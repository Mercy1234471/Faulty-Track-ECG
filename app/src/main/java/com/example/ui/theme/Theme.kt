package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EcgColorScheme = darkColorScheme(
    primary = EcgYellow,
    onPrimary = EcgOnYellow,
    primaryContainer = EcgYellowContainer,
    onPrimaryContainer = EcgYellowLight,
    secondary = EcgYellowLight,
    onSecondary = EcgOnYellow,
    tertiary = EcgStatusInProgress,
    background = EcgDarkBackground,
    onBackground = EcgTextPrimary,
    surface = EcgDarkSurface,
    onSurface = EcgTextPrimary,
    surfaceVariant = EcgDarkSurfaceVariant,
    onSurfaceVariant = EcgTextSecondary,
    outline = EcgDarkCardBorder,
    error = EcgStatusRejected,
    onError = EcgWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // ECG Faulty Track Report features the iconic ECG Black & Yellow identity
    MaterialTheme(
        colorScheme = EcgColorScheme,
        typography = Typography,
        content = content
    )
}
