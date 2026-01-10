package com.example.aureus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Dark Color Scheme pour l'application Aureus
 * Utilise la nouvelle palette de couleurs définie
 */
private val DarkColorScheme = darkColorScheme(
    // Primaires
    primary = PrimaryNavyBlue,
    onPrimary = NeutralWhite,
    primaryContainer = PrimaryMediumBlue,
    onPrimaryContainer = NeutralWhite,
    
    // Secondaires
    secondary = SecondaryGold,
    onSecondary = PrimaryNavyBlue,
    secondaryContainer = SecondaryDarkGold,
    onSecondaryContainer = NeutralWhite,
    
    // Tertiaires
    tertiary = PrimaryMediumBlue,
    onTertiary = NeutralWhite,
    
    // Erreur
    error = SemanticRed,
    onError = NeutralWhite,
    errorContainer = SemanticRed,
    onErrorContainer = NeutralWhite,
    
    // Background & Surface
    background = NeutralDarkGray,
    onBackground = NeutralWhite,
    surface = PrimaryNavyBlue,
    onSurface = NeutralWhite,
    surfaceVariant = PrimaryMediumBlue,
    onSurfaceVariant = NeutralWhite,
    
    // Outline
    outline = NeutralMediumGray,
    outlineVariant = ColorVariants.NeutralMediumGray50
)

/**
 * Light Color Scheme pour l'application Aureus
 * Schéma de couleurs par défaut de l'application
 */
private val LightColorScheme = lightColorScheme(
    // Primaires
    primary = PrimaryNavyBlue,
    onPrimary = NeutralWhite,
    primaryContainer = ColorVariants.PrimaryNavyBlue10,
    onPrimaryContainer = PrimaryNavyBlue,
    
    // Secondaires
    secondary = SecondaryGold,
    onSecondary = PrimaryNavyBlue,
    secondaryContainer = ColorVariants.SecondaryGold10,
    onSecondaryContainer = SecondaryDarkGold,
    
    // Tertiaires
    tertiary = PrimaryMediumBlue,
    onTertiary = NeutralWhite,
    tertiaryContainer = ColorVariants.PrimaryNavyBlue10,
    onTertiaryContainer = PrimaryMediumBlue,
    
    // Erreur
    error = SemanticRed,
    onError = NeutralWhite,
    errorContainer = ColorVariants.SemanticRed10,
    onErrorContainer = SemanticRed,
    
    // Background & Surface
    background = NeutralLightGray,
    onBackground = NeutralDarkGray,
    surface = NeutralWhite,
    onSurface = NeutralDarkGray,
    surfaceVariant = NeutralLightGray,
    onSurfaceVariant = NeutralMediumGray,
    
    // Outline
    outline = NeutralMediumGray,
    outlineVariant = ColorVariants.NeutralMediumGray50,
    
    // Inverse (pour snackbars, etc.)
    inverseSurface = PrimaryNavyBlue,
    inverseOnSurface = NeutralWhite,
    inversePrimary = SecondaryGold
)

/**
 * Thème principal de l'application Aureus
 * 
 * @param darkTheme Active le mode sombre si true
 * @param dynamicColor Active les couleurs dynamiques Android 12+ (désactivé par défaut pour cohérence)
 * @param content Contenu de l'application
 */
@Composable
fun AureusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color désactivé par défaut pour maintenir la cohérence de la marque
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
        content = content
    )
}