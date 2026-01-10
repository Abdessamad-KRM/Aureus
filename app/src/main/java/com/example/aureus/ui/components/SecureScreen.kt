package com.example.aureus.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text as Material3Text
import androidx.compose.material3.AlertDialog as Material3AlertDialog
import androidx.compose.material3.TextButton as Material3TextButton

/**
 * Composable SecureScreen
 * Empêche le retour arrière dans les actions critiques
 *
 * Utilisation:
 * @Composable
 * fun MyScreen() {
 *     SecureScreen(preventBack = true) {
 *         Votre contenu UI
 *     }
 * }
 *
 * @param preventBack Si true, empêche le retour arrière (action critique)
 * @param onBackCallback Callback optionnel quand l'utilisateur essaie de revenir
 * @param content Le contenu de l'écran
 */
@Composable
fun SecureScreen(
    modifier: Modifier = Modifier,
    preventBack: Boolean = false,
    onBackCallback: () -> Unit = {},
    content: @Composable () -> Unit
) {
    SecureNavigationHandler(
        preventBack = preventBack,
        onBackCallback = onBackCallback
    )

    // Le contenu de l'écran enveloppé
    content()
}

/**
 * Handler de navigation sécurisée
 *
 * Gère les différents scénarios de retour arrière pour les écrans critiques:
 * - SMS Verification
 * - PIN Setup/Verification
 * - Phone Number Input (après Google Auth)
 * - Transactions en cours
 */
@Composable
private fun SecureNavigationHandler(
    preventBack: Boolean,
    onBackCallback: () -> Unit
) {
    BackHandler(enabled = preventBack) {
        // Ne rien faire - empêche le retour arrière
        // Appelle le callback pour les notifications supplémentaires si nécessaire
        onBackCallback()
    }
}

/**
 * Énumération des types d'écrans sécurisés
 */
enum class SecureScreenType {
    SMS_VERIFICATION,
    PHONE_NUMBER_INPUT,
    PIN_SETUP,
    PIN_VERIFICATION,
    TRANSACTION_IN_PROGRESS,
    ACCOUNT_SETTINGS,
    NONE // Non sécurisé
}

/**
 * Composable withSecurity pour les écrans nécessitant une protection
 *
 * @param screenType Type d'écran sécurisé
 * @param onBackAttempt Callback lors d'une tentative de retour (si non empêché)
 * @param content Contenu de l'écran
 */
@Composable
fun withSecurity(
    screenType: SecureScreenType = SecureScreenType.NONE,
    onBackAttempt: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val preventBack = when (screenType) {
        SecureScreenType.SMS_VERIFICATION,
        SecureScreenType.PHONE_NUMBER_INPUT,
        SecureScreenType.PIN_SETUP,
        SecureScreenType.PIN_VERIFICATION,
        SecureScreenType.TRANSACTION_IN_PROGRESS -> true

        SecureScreenType.ACCOUNT_SETTINGS -> true // Par défaut, les settings sont sécurisés

        SecureScreenType.NONE -> false
    }

    SecureScreen(
        preventBack = preventBack,
        onBackCallback = { if (!preventBack) onBackAttempt() }
    ) {
        content()
    }
}

/**
 * Énumération des types de transactions critiques
 */
enum class CriticalTransactionType {
    MONEY_TRANSFER,
    CARD_PAYMENT,
    BILL_PAYMENT,
    INTERNATIONAL_TRANSFER,
    NONE
}

/**
 * Composable pour les transactions critiques
 *
 * @param transactionType Type de transaction
 * @param inProgress Si la transaction est en cours
 * @param content Contenu de l'écran de transaction
 */
@Composable
fun SecureTransactionScreen(
    transactionType: CriticalTransactionType = CriticalTransactionType.NONE,
    inProgress: Boolean = false,
    content: @Composable () -> Unit
) {
    val preventBack = inProgress && transactionType != CriticalTransactionType.NONE

    SecureScreen(
        preventBack = preventBack
    ) {
        content()
    }
}

/**
 * Dialog de confirmation pour action critique
 *
 * Utilisez ce dialog pour confirmer les actions importantes avant de quitter
 */
@Composable
fun ActionExitConfirmationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit,
    actionTitle: String = "quitter",
    actionDescription: String = "Cette action est critique et ne peut être annulée."
) {
    if (!isVisible) return

    Material3AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Material3Text(
                text = "Êtes-vous sûr de vouloir $actionTitle?"
            )
        },
        text = {
            Material3Text(actionDescription)
        },
        confirmButton = {
            Material3TextButton(
                onClick = onConfirmExit
            ) {
                Material3Text(
                    text = "Oui, quitter",
                    color = com.example.aureus.ui.theme.SemanticRed
                )
            }
        },
        dismissButton = {
            Material3TextButton(
                onClick = onDismiss
            ) {
                Material3Text("Annuler")
            }
        }
    )
}