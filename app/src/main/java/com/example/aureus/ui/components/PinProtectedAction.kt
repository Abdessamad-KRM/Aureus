package com.example.aureus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.ui.auth.viewmodel.PinViewModel
import kotlinx.coroutines.launch

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
        AlertDialog(
            onDismissRequest = {
                if (!loading) onDismiss()
            },
            title = { Text(title) },
            text = {
                Column {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        placeholder = { Text("••••") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (viewModel.errorMessage.value != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            viewModel.errorMessage.value!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (loading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator()
                    }
                }
            },
            confirmButton = {
                Button(
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
                        CircularProgressIndicator(
                            modifier = androidx.compose.ui.Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirmer")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!loading) onDismiss() },
                    enabled = !loading
                ) {
                    Text("Annuler")
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
