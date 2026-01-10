package com.example.aureus.ui.auth.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.aureus.ui.theme.AureusTheme

/**
 * Previews for PIN Setup and Verification Screens
 */

// ============================================
// PIN Setup Screen Previews
// ============================================

@Preview(showBackground = true, showSystemUi = true, name = "PIN Setup - Create")
@Composable
fun PinSetupScreenPreview() {
    AureusTheme {
        PinSetupScreen(
            onPinSetupComplete = {},
            onNavigateBack = {}
        )
    }
}

// ============================================
// PIN Verification Screen Previews
// ============================================

@Preview(showBackground = true, showSystemUi = true, name = "PIN Verification - Full Screen")
@Composable
fun PinVerificationScreenPreview() {
    AureusTheme {
        PinVerificationScreen(
            onPinVerified = {},
            onCancel = {},
            title = "Confirmer le transfert",
            subtitle = "Entrez votre code PIN pour autoriser cette transaction"
        )
    }
}

@Preview(showBackground = true, name = "PIN Verification - Dialog")
@Composable
fun PinVerificationDialogPreview() {
    AureusTheme {
        PinVerificationDialog(
            onPinVerified = {},
            onDismiss = {},
            title = "Confirmer l'opération",
            subtitle = "Entrez votre code PIN de sécurité"
        )
    }
}

@Preview(showBackground = true, name = "PIN Dialog - Transaction")
@Composable
fun PinVerificationDialogTransactionPreview() {
    AureusTheme {
        PinVerificationDialog(
            onPinVerified = {},
            onDismiss = {},
            title = "Transfert de 5000 MAD",
            subtitle = "Confirmez cette transaction avec votre PIN"
        )
    }
}
