package com.example.aureus.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *                          AUREUS COLOR PALETTE
 * ══════════════════════════════════════════════════════════════════════════════
 * Palette complète pour l'application bancaire Aureus
 * Design System: Prestige & Confiance
 */

// ═══════════════════════════════════════════════════════════════════════════════
// PRIMAIRES (Actions Principales)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Bleu marine profond - Couleur principale de la marque
 * Utilisé pour: Boutons principaux, Headers, Navigation, Branding
 */
val PrimaryNavyBlue = Color(0xFF1E3A5F)

/**
 * Bleu moyen - États actifs
 * Utilisé pour: États actifs, Hover sur éléments primaires, Tabs actives
 */
val PrimaryMediumBlue = Color(0xFF2C5F8D)

// ═══════════════════════════════════════════════════════════════════════════════
// SECONDAIRES (Accents Financiers)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Or - Accent premium
 * Utilisé pour: Éléments premium, Soldes positifs, Badges, Highlights
 */
val SecondaryGold = Color(0xFFD4AF37)

/**
 * Or foncé - États hover
 * Utilisé pour: Hover states sur éléments dorés, États pressed
 */
val SecondaryDarkGold = Color(0xFFC89F3C)

// ═══════════════════════════════════════════════════════════════════════════════
// SÉMANTIQUES (Feedback Utilisateur)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Vert - Positif/Succès
 * Utilisé pour: Transactions entrantes, Succès, Validation, Soldes positifs
 */
val SemanticGreen = Color(0xFF10B981)

/**
 * Rouge - Négatif/Erreur
 * Utilisé pour: Transactions sortantes, Erreurs, Alertes, Rejets
 */
val SemanticRed = Color(0xFFEF4444)

/**
 * Ambre - Avertissement
 * Utilisé pour: Avertissements, Solde faible, Actions requises
 */
val SemanticAmber = Color(0xFFF59E0B)

// ═══════════════════════════════════════════════════════════════════════════════
// NEUTRES (UI/Background)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Blanc - Backgrounds cards
 * Utilisé pour: Fond des cartes, Dialogs, Surfaces élevées
 */
val NeutralWhite = Color(0xFFFFFFFF)

/**
 * Gris très clair - Background général
 * Utilisé pour: Fond d'écran principal, Fond de sections
 */
val NeutralLightGray = Color(0xFFF8FAFC)

/**
 * Gris moyen - Textes secondaires
 * Utilisé pour: Textes secondaires, Descriptions, Labels
 */
val NeutralMediumGray = Color(0xFF64748B)

/**
 * Gris foncé - Textes principaux
 * Utilisé pour: Textes principaux, Titres, Contenus importants
 */
val NeutralDarkGray = Color(0xFF1E293B)

// ═══════════════════════════════════════════════════════════════════════════════
// ALIAS POUR COMPATIBILITÉ ASCENDANTE
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * @deprecated Utiliser PrimaryNavyBlue à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use PrimaryNavyBlue instead", ReplaceWith("PrimaryNavyBlue"))
val NavyBlue = PrimaryNavyBlue

/**
 * @deprecated Utiliser SecondaryGold à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use SecondaryGold instead", ReplaceWith("SecondaryGold"))
val Gold = SecondaryGold

/**
 * @deprecated Utiliser NeutralWhite à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use NeutralWhite instead", ReplaceWith("NeutralWhite"))
val White = NeutralWhite

/**
 * @deprecated Utiliser NeutralLightGray à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use NeutralLightGray instead", ReplaceWith("NeutralLightGray"))
val LightGray = NeutralLightGray

/**
 * @deprecated Utiliser NeutralMediumGray à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use NeutralMediumGray instead", ReplaceWith("NeutralMediumGray"))
val DarkGray = NeutralMediumGray

/**
 * @deprecated Utiliser PrimaryMediumBlue à la place
 * Conservé pour compatibilité avec le code existant
 */
@Deprecated("Use PrimaryMediumBlue instead", ReplaceWith("PrimaryMediumBlue"))
val LightNavy = PrimaryMediumBlue

// ═══════════════════════════════════════════════════════════════════════════════
// VARIANTES (Pour états et nuances)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Variantes avec opacité pour les états désactivés et overlays
 */
object ColorVariants {
    // Primary variants
    val PrimaryNavyBlue10 = PrimaryNavyBlue.copy(alpha = 0.1f)
    val PrimaryNavyBlue20 = PrimaryNavyBlue.copy(alpha = 0.2f)
    val PrimaryNavyBlue50 = PrimaryNavyBlue.copy(alpha = 0.5f)
    val PrimaryNavyBlue70 = PrimaryNavyBlue.copy(alpha = 0.7f)
    
    // Secondary variants
    val SecondaryGold10 = SecondaryGold.copy(alpha = 0.1f)
    val SecondaryGold20 = SecondaryGold.copy(alpha = 0.2f)
    val SecondaryGold50 = SecondaryGold.copy(alpha = 0.5f)
    
    // Semantic variants
    val SemanticGreen10 = SemanticGreen.copy(alpha = 0.1f)
    val SemanticGreen20 = SemanticGreen.copy(alpha = 0.2f)
    val SemanticRed10 = SemanticRed.copy(alpha = 0.1f)
    val SemanticRed20 = SemanticRed.copy(alpha = 0.2f)
    val SemanticAmber10 = SemanticAmber.copy(alpha = 0.1f)
    val SemanticAmber20 = SemanticAmber.copy(alpha = 0.2f)
    
    // Neutral variants
    val NeutralMediumGray50 = NeutralMediumGray.copy(alpha = 0.5f)
    val NeutralMediumGray70 = NeutralMediumGray.copy(alpha = 0.7f)
}

// ═══════════════════════════════════════════════════════════════════════════════
// GRADIENTS
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Gradients pour les fonds premium et les cards spéciales
 */
object AppGradients {
    val PrimaryGradient = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
    val GoldGradient = listOf(SecondaryGold, SecondaryDarkGold)
    val SuccessGradient = listOf(SemanticGreen, Color(0xFF059669))
    val PremiumGradient = listOf(PrimaryNavyBlue, SecondaryGold)
}

// ═══════════════════════════════════════════════════════════════════════════════
// DARK THEME COLORS (PHASE 12)
// ═══════════════════════════════════════════════════════════════════════════════
/**
 * Dark Theme Colors - Palette pour le mode sombre
 */
object DarkThemeColors {
    // Primary Colors
    val PrimaryNavyBlueDark = Color(0xFF0F172A)
    val PrimaryMediumBlueDark = Color(0xFF1E293B)
    val PrimaryLightBlueDark = Color(0xFF334155)

    // Secondary
    val SecondaryGoldDark = Color(0xFFD4AF37)
    val SecondaryDarkGoldDark = Color(0xFFB8960C)

    // Neutral Colors
    val NeutralBlackDark = Color(0xFF000000)
    val NeutralDarkGrayDark = Color(0xFF1F2937)
    val NeutralMediumGrayDark = Color(0xFF4B5563)
    val NeutralLightGrayDark = Color(0xFF9CA3AF)
    val NeutralWhiteDark = Color(0xFFE5E7EB)

    // Semantic Colors
    val SemanticGreenDark = Color(0xFF10B981)
    val SemanticRedDark = Color(0xFFEF4444)
    val SemanticYellowDark = Color(0xFFF59E0B)
    val SemanticBlueDark = Color(0xFF3B82F6)
}