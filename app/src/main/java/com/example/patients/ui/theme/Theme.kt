package com.example.patients.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalPrimaryDark,
    onPrimary = MedicalOnPrimaryDark,
    primaryContainer = MedicalPrimaryContainerDark,
    onPrimaryContainer = MedicalOnPrimaryContainerDark,
    secondary = MedicalSecondaryDark,
    onSecondary = MedicalOnSecondaryDark,
    secondaryContainer = MedicalSecondaryContainerDark,
    onSecondaryContainer = MedicalOnSecondaryContainerDark,
    tertiary = MedicalTertiaryDark,
    onTertiary = MedicalOnTertiaryDark,
    tertiaryContainer = MedicalTertiaryContainerDark,
    onTertiaryContainer = MedicalOnTertiaryContainerDark,
    background = MedicalBackgroundDark,
    onBackground = MedicalOnBackgroundDark,
    surface = MedicalSurfaceDark,
    onSurface = MedicalOnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalPrimaryLight,
    onPrimary = MedicalOnPrimaryLight,
    primaryContainer = MedicalPrimaryContainerLight,
    onPrimaryContainer = MedicalOnPrimaryContainerLight,
    secondary = MedicalSecondaryLight,
    onSecondary = MedicalOnSecondaryLight,
    secondaryContainer = MedicalSecondaryContainerLight,
    onSecondaryContainer = MedicalOnSecondaryContainerLight,
    tertiary = MedicalTertiaryLight,
    onTertiary = MedicalOnTertiaryLight,
    tertiaryContainer = MedicalTertiaryContainerLight,
    onTertiaryContainer = MedicalOnTertiaryContainerLight,
    background = MedicalBackgroundLight,
    onBackground = MedicalOnBackgroundLight,
    surface = MedicalSurfaceLight,
    onSurface = MedicalOnSurfaceLight,
)

@Composable
fun PatientsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prefer our medical design palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
