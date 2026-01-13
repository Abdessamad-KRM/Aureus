package com.example.aureus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.SemanticGreen
import com.example.aureus.ui.theme.PrimaryNavyBlue

/**
 * Password Storage Consent Dialog - Phase 2 Sécurité
 *
 * Demande le consentement explicite de l'utilisateur avant de stocker
 * ses identifiants de manière sécurisée (EncryptedSharedPreferences)
 *
 * Conformité RGPD et PCI-DSS:
 * - Consentement explicite obligatoire
 * - Explication claire du stockage sécurisé
 * - Possibilité de refuser sans bloquer l'inscription
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordStorageConsentDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    if (!isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = "Security Lock",
                tint = SemanticGreen,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Save your credentials securely?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "We can securely save your password to enable quick login in the future.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Security Features
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecurityFeatureRow(
                        icon = Icons.Default.Lock,
                        text = "AES-256 Encryption"
                    )
                    SecurityFeatureRow(
                        icon = Icons.Default.Security,
                        text = "Android Keystore Storage"
                    )
                    SecurityFeatureRow(
                        icon = Icons.Default.Lock,
                        text = "Only accessible on this device"
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Your consent is required to save these credentials. You can remove them at any time from Account Settings.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⚠️ Your password will be stored encrypted on this device only. Never share it with anyone.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAccept()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SemanticGreen
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Securely", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDecline()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't Save", color = PrimaryNavyBlue)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Row displaying a security feature
 */
@Composable
private fun SecurityFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SemanticGreen,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Consent State Manager
 *
 * Gère l'état du consentement et l'affichage du dialog
 */
class PasswordStorageConsentManager {
    var showDialog by mutableStateOf(false)
        private set

    var hasAlreadyConsented by mutableStateOf(false)
        private set

    var hasDeclined by mutableStateOf(false)
        private set

    /**
     * Demander le consentement si pas déjà fait
     */
    fun requestConsentIfNeeded(): Boolean {
        if (!hasAlreadyConsented && !hasDeclined) {
            showDialog = true
            return true
        }
        return false
    }

    /**
     * Accepter le consentement
     */
    fun acceptConsent() {
        hasAlreadyConsented = true
        hasDeclined = false
        showDialog = false
    }

    /**
     * Refuser le consentement
     */
    fun declineConsent() {
        hasDeclined = true
        hasAlreadyConsented = false
        showDialog = false
    }

    /**
     * Reset pour permettre de redemander le consentement
     * (ex: après changement dans settings)
     */
    fun resetConsent() {
        hasAlreadyConsented = false
        hasDeclined = false
    }

    /**
     * Marquer comme déjà consenti (ex: depuis SharedPreferences)
     */
    fun setConsentAlreadyGiven(consented: Boolean) {
        hasAlreadyConsented = consented
        hasDeclined = false
    }
}

/**
 * Remember le gestionnaire de consentement
 */
@Composable
fun rememberPasswordStorageConsentManager(): PasswordStorageConsentManager {
    return remember { PasswordStorageConsentManager() }
}