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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Dark Color Scheme pour l'application Aureus
 * Utilise la nouvelle palette de couleurs définie
 */
private val DarkColorScheme = darkColorScheme(
    // Primaires
    primary = DarkThemeColors.PrimaryNavyBlueDark,
    onPrimary = NeutralWhite,
    primaryContainer = DarkThemeColors.PrimaryMediumBlueDark,
    onPrimaryContainer = NeutralWhite,

    // Secondaires
    secondary = DarkThemeColors.SecondaryGoldDark,
    onSecondary = DarkThemeColors.PrimaryNavyBlueDark,
    secondaryContainer = DarkThemeColors.SecondaryDarkGoldDark,
    onSecondaryContainer = NeutralWhite,

    // Tertiaires
    tertiary = DarkThemeColors.PrimaryLightBlueDark,
    onTertiary = NeutralWhite,

    // Erreur
    error = DarkThemeColors.SemanticRedDark,
    onError = NeutralWhite,
    errorContainer = DarkThemeColors.SemanticRedDark,
    onErrorContainer = NeutralWhite,

    // Background & Surface
    background = DarkThemeColors.PrimaryNavyBlueDark,
    onBackground = DarkThemeColors.NeutralWhiteDark,
    surface = DarkThemeColors.PrimaryMediumBlueDark,
    onSurface = DarkThemeColors.NeutralWhiteDark,
    surfaceVariant = DarkThemeColors.PrimaryLightBlueDark,
    onSurfaceVariant = DarkThemeColors.NeutralWhiteDark,

    // Outline
    outline = DarkThemeColors.NeutralMediumGrayDark,
    outlineVariant = DarkThemeColors.NeutralMediumGrayDark.copy(alpha = 0.5f)
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

    val appColors = AppColors(darkTheme)

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APP COLOR SCHEME INTERFACE (PHASE 12)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Interface pour le schéma de couleurs de l'application
 * Permet un accès uniforme aux couleurs en mode clair et sombre
 */
interface AppColorScheme {
    val primaryNavyBlue: Color
    val primaryMediumBlue: Color
    val primaryLightBlue: Color
    val secondaryGold: Color
    val secondaryDarkGold: Color
    val neutralBlack: Color
    val neutralDarkGray: Color
    val neutralMediumGray: Color
    val neutralLightGray: Color
    val neutralWhite: Color
    val semanticGreen: Color
    val semanticRed: Color
    val semanticYellow: Color
    val semanticBlue: Color
    val isDark: Boolean
}

/**
 * Implémentation du schéma de couleurs
 */
private class AppColors(isDark: Boolean) : AppColorScheme {
    override val primaryNavyBlue = if (isDark) DarkThemeColors.PrimaryNavyBlueDark else PrimaryNavyBlue
    override val primaryMediumBlue = if (isDark) DarkThemeColors.PrimaryMediumBlueDark else PrimaryMediumBlue
    override val primaryLightBlue = if (isDark) DarkThemeColors.PrimaryLightBlueDark else PrimaryNavyBlue
    override val secondaryGold = if (isDark) DarkThemeColors.SecondaryGoldDark else SecondaryGold
    override val secondaryDarkGold = if (isDark) DarkThemeColors.SecondaryDarkGoldDark else SecondaryDarkGold
    override val neutralBlack = if (isDark) DarkThemeColors.NeutralBlackDark else NeutralWhite
    override val neutralDarkGray = if (isDark) DarkThemeColors.NeutralDarkGrayDark else NeutralDarkGray
    override val neutralMediumGray = if (isDark) DarkThemeColors.NeutralMediumGrayDark else NeutralMediumGray
    override val neutralLightGray = if (isDark) DarkThemeColors.NeutralLightGrayDark else NeutralLightGray
    override val neutralWhite = if (isDark) DarkThemeColors.NeutralWhiteDark else NeutralWhite
    override val semanticGreen = if (isDark) DarkThemeColors.SemanticGreenDark else SemanticGreen
    override val semanticRed = if (isDark) DarkThemeColors.SemanticRedDark else SemanticRed
    override val semanticYellow = if (isDark) DarkThemeColors.SemanticYellowDark else SemanticAmber
    override val semanticBlue = if (isDark) DarkThemeColors.SemanticBlueDark else PrimaryMediumBlue
    override val isDark = isDark
}

/**
 * LocalAppColors - CompositionLocal pour accéder au schéma de couleurs
 */
val LocalAppColors = androidx.compose.runtime.compositionLocalOf<AppColorScheme> {
    error("No AppColorScheme provided")
}