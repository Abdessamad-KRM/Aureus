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
// Note: PinVerificationDialog and PinVerificationScreen need to be created
// ============================================

@Preview(showBackground = true, showSystemUi = true, name = "PIN Verification - Full Screen")
@Composable
fun PinVerificationScreenPreview() {
    AureusTheme {
        // TODO: Create PinVerificationScreen composable
        androidx.compose.material3.Text("PIN Verification Preview")
    }
}

@Preview(showBackground = true, name = "PIN Verification - Dialog")
@Composable
fun PinVerificationDialogPreview() {
    AureusTheme {
        // TODO: Create PinVerificationDialog composable
        androidx.compose.material3.Text("PIN Dialog Preview")
    }
}

@Preview(showBackground = true, name = "PIN Dialog - Transaction")
@Composable
fun PinVerificationDialogTransactionPreview() {
    AureusTheme {
        androidx.compose.material3.Text("PIN Dialog Transaction Preview")
    }
}
