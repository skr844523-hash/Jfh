package com.nexora.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NexoraDarkColorScheme = darkColorScheme(
    primary = NexoraPrimaryPurple,
    onPrimary = NexoraDarkBackground,
    primaryContainer = NexoraSurfaceVariant,
    onPrimaryContainer = NexoraElectricCyan,
    secondary = NexoraElectricCyan,
    onSecondary = NexoraDarkBackground,
    secondaryContainer = NexoraSurfaceVariant,
    onSecondaryContainer = NexoraPrimaryPurple,
    tertiary = NexoraAccentPink,
    onTertiary = NexoraDarkBackground,
    background = NexoraDarkBackground,
    onBackground = NexoraTextPrimary,
    surface = NexoraSurface,
    onSurface = NexoraTextPrimary,
    surfaceVariant = NexoraSurfaceVariant,
    onSurfaceVariant = NexoraTextSecondary,
    error = NexoraErrorRed,
    onError = NexoraDarkBackground
)

@Composable
fun NexoraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexoraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
