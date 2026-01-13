package com.example.aureus.ui.components

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.auth.screen.PinVerificationDialog
import com.example.aureus.ui.auth.viewmodel.PinViewModel

/**
 * ✅ PHASE 1 CORRECTION: Composable wrapper pour PIN-protected actions
 * Utilise ViewModel pour récupérer et vérifier le PIN depuis Firestore
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
    viewModel: PinViewModel = hiltViewModel()  // ✅ INJECTÉ!
) {
    var pin by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            pin = ""
            viewModel.reset()
        }
    }

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!loading) onDismiss()
            },
            title = { androidx.compose.material3.Text(title) },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        placeholder = { androidx.compose.material3.Text("••••") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = androidx.compose.foundation.layout.fillMaxWidth(),
                        singleLine = true
                    )

                    if (viewModel.errorMessage.value != null) {
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                        androidx.compose.material3.Text(
                            viewModel.errorMessage.value!!,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }

                    if (loading) {
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                        androidx.compose.material3.LinearProgressIndicator()
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (pin.length == 4 && !loading) {
                            loading = true
                            viewModel.viewModelScope.launch {
                                viewModel.verifyPinAndExecute(pin) { isValid ->
                                    loading = false
                                    if (isValid) {
                                        onSuccess()
                                        onDismiss()
                                    }
                                }
                            }
                        }
                    },
                    enabled = pin.length == 4 && !loading
                ) {
                    if (loading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = androidx.compose.foundation.layout.size(16.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        androidx.compose.material3.Text("Confirmer")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { if (!loading) onDismiss() },
                    enabled = !loading
                ) {
                    androidx.compose.material3.Text("Annuler")
                }
            }
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
 * ✅ PHASE 1 CORRECTION: Composable that handles PIN verification with state
 * Utilise ViewModel pour vérifier le PIN depuis Firestore
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
    viewModel: PinViewModel = hiltViewModel()  // ✅ INJECTÉ!
) {
    PinProtectedAction(
        showDialog = state.showDialog,
        onDismiss = { state.dismiss() },
        onSuccess = { state.onVerified() },
        title = state.title,
        subtitle = state.subtitle,
        viewModel = viewModel
    )
}
