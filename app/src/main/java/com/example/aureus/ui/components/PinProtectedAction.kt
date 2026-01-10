package com.example.aureus.ui.components

import androidx.compose.runtime.*
import com.example.aureus.ui.auth.screen.PinVerificationDialog

/**
 * Composable wrapper for PIN-protected actions
 * 
 * Usage example:
 * ```
 * var showPinDialog by remember { mutableStateOf(false) }
 * 
 * Button(onClick = { showPinDialog = true }) {
 *     Text("Transfer Money")
 * }
 * 
 * PinProtectedAction(
 *     showDialog = showPinDialog,
 *     onDismiss = { showPinDialog = false },
 *     title = "Confirm Transfer",
 *     subtitle = "Enter your PIN to authorize this transaction",
 *     onSuccess = {
 *         showPinDialog = false
 *         // Execute the sensitive action
 *         performTransfer()
 *     }
 * )
 * ```
 */
@Composable
fun PinProtectedAction(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    title: String = "Confirmer l'opération",
    subtitle: String = "Entrez votre code PIN de sécurité",
    correctPin: String = "1234" // TODO: Get from secure storage
) {
    if (showDialog) {
        PinVerificationDialog(
            onPinVerified = onSuccess,
            onDismiss = onDismiss,
            title = title,
            subtitle = subtitle,
            correctPin = correctPin
        )
    }
}

/**
 * State holder for PIN-protected actions
 */
class PinProtectedActionState {
    var showDialog by mutableStateOf(false)
        private set
    
    var title by mutableStateOf("Confirmer l'opération")
        private set
    
    var subtitle by mutableStateOf("Entrez votre code PIN de sécurité")
        private set
    
    private var onSuccessCallback: (() -> Unit)? = null
    
    /**
     * Request PIN verification for an action
     */
    fun requestPin(
        title: String = "Confirmer l'opération",
        subtitle: String = "Entrez votre code PIN de sécurité",
        onSuccess: () -> Unit
    ) {
        this.title = title
        this.subtitle = subtitle
        this.onSuccessCallback = onSuccess
        showDialog = true
    }
    
    /**
     * Handle successful PIN verification
     */
    fun onVerified() {
        onSuccessCallback?.invoke()
        dismiss()
    }
    
    /**
     * Dismiss the PIN dialog
     */
    fun dismiss() {
        showDialog = false
        onSuccessCallback = null
    }
}

/**
 * Remember a PIN protected action state
 */
@Composable
fun rememberPinProtectedActionState(): PinProtectedActionState {
    return remember { PinProtectedActionState() }
}

/**
 * Composable that handles PIN verification with state
 * 
 * Usage example:
 * ```
 * val pinState = rememberPinProtectedActionState()
 * 
 * Button(onClick = {
 *     pinState.requestPin(
 *         title = "Confirm Transfer",
 *         subtitle = "Enter PIN to transfer 5000 MAD"
 *     ) {
 *         // This code runs after successful PIN verification
 *         performTransfer()
 *     }
 * }) {
 *     Text("Transfer Money")
 * }
 * 
 * PinProtectedActionHandler(state = pinState)
 * ```
 */
@Composable
fun PinProtectedActionHandler(
    state: PinProtectedActionState,
    correctPin: String = "1234" // TODO: Get from secure storage
) {
    PinProtectedAction(
        showDialog = state.showDialog,
        onDismiss = { state.dismiss() },
        onSuccess = { state.onVerified() },
        title = state.title,
        subtitle = state.subtitle,
        correctPin = correctPin
    )
}
