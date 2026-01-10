package com.example.aureus.ui.onboarding

import com.example.aureus.R

/**
 * Data class representing an onboarding page
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieResId: Int
)

/**
 * Onboarding pages data
 */
object OnboardingData {
    val pages = listOf(
        OnboardingPage(
            title = "Sécurité & Protection",
            description = "Vos données sont protégées avec les dernières technologies de sécurité bancaire. Profitez d'une tranquillité d'esprit totale.",
            lottieResId = R.raw.secure
        ),
        OnboardingPage(
            title = "Gestion Simplifiée",
            description = "Gérez tous vos comptes bancaires en un seul endroit. Consultez vos soldes et transactions en temps réel.",
            lottieResId = R.raw.manage_money
        ),
        OnboardingPage(
            title = "Alertes Instantanées",
            description = "Recevez des notifications en temps réel pour chaque transaction. Restez toujours informé de l'activité de vos comptes.",
            lottieResId = R.raw.notifications
        )
    )
}
