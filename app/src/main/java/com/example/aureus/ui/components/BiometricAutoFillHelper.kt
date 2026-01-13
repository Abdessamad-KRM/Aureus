package com.example.aureus.ui.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Biometric Auto-Fill Helper - Phase 2 Sécurité
 *
 * Obforce l'authentification biométrique avant d'autoriser
 * l'auto-fill des identifiants pour le Quick Login
 *
 * Conformité PCI-DSS Section 8.2:
 * - Multi-factor authentication pour les opérations sensibles
 * - Something you have (device) + Something you are (biometric)
 */

/**
 * Vérifier si la biométrie est disponible et configurée
 */
fun canUseBiometric(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
}

/**
 * État de disponibilité biométrique
 */
enum class BiometricAvailability {
    AVAILABLE,
    NOT_AVAILABLE,
    NOT_ENROLLED,
    HARDWARE_UNAVAILABLE,
    NO_PERMISSION
}

/**
 * Obtenir l'état de disponibilité biométrique
 */
fun getBiometricAvailability(context: Context): BiometricAvailability {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.HARDWARE_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.HARDWARE_UNAVAILABLE
        else -> BiometricAvailability.NOT_AVAILABLE
    }
}

/**
 * Message d'erreur selon l'état biométrique
 */
fun getBiometricErrorMessage(availability: BiometricAvailability): String {
    return when (availability) {
        BiometricAvailability.AVAILABLE -> ""
        BiometricAvailability.NOT_ENROLLED -> "No biometric credentials enrolled. Please enable fingerprint or face recognition in device settings."
        BiometricAvailability.HARDWARE_UNAVAILABLE -> "Biometric hardware is not available on this device."
        BiometricAvailability.NOT_AVAILABLE -> "Biometric authentication is not available."
        BiometricAvailability.NO_PERMISSION -> "Biometric permission denied."
    }
}

/**
 * Exécuter l'authentification biométrique
 *
 * @return true si authentifiée, false sinon
 */
suspend fun authenticateWithBiometric(
    activity: FragmentActivity,
    title: String = "Quick Login Authentication",
    subtitle: String = "Verify your identity to access your account",
    description: String = "Use your fingerprint or face to authenticate",
    negativeButtonText: String = "Cancel"
): Boolean = suspendCancellableCoroutine { continuation ->
    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                continuation.resume(true)
            }

            override fun onAuthenticationFailed() {
                // Ne pas annuler la coroutine - l'utilisateur peut réessayer
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        continuation.resume(false)
                    }
                    else -> {
                        continuation.resumeWithException(
                            SecurityException("Biometric error: $errString")
                        )
                    }
                }
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(description)
        .setNegativeButtonText(negativeButtonText)
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)

    continuation.invokeOnCancellation {
        // Annuler le prompt biometric si la coroutine est cancelée
        try {
            biometricPrompt.cancelAuthentication()
        } catch (e: Exception) {
            // Ignorer les erreurs d'annulation
        }
    }
}

/**
 * Biometric Auth Handler pour Compose
 *
 * Gère l'authentification biométrique avec affichage des erreurs
 */
@Composable
fun rememberBiometricAuthHandler(
    snackbarHostState: SnackbarHostState? = null
): BiometricAuthHandler {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    return remember(snackbarHostState) {
        BiometricAuthHandler(
            activity = activity,
            snackbarHostState = snackbarHostState
        )
    }
}

/**
 * Handler pour l'authentification biométrique
 */
class BiometricAuthHandler(
    private val activity: FragmentActivity?,
    private val snackbarHostState: SnackbarHostState?
) {
    var isAuthenticated by mutableStateOf(false)
        private set

    var isAuthenticating by mutableStateOf(false)
        private set

    var availability by mutableStateOf(
        activity?.let { getBiometricAvailability(it) } ?: BiometricAvailability.NOT_AVAILABLE
    )
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Tenter l'authentification biométrique
     */
    suspend fun authenticate(
        title: String = "Quick Login Authentication",
        subtitle: String = "Verify your identity to access your account"
    ): Boolean {
        if (activity == null) {
            errorMessage = "Activity not available"
            showErrorSnackbar(errorMessage ?: "Authentication failed")
            return false
        }

        val currentAvailability = getBiometricAvailability(activity)
        availability = currentAvailability

        if (currentAvailability != BiometricAvailability.AVAILABLE) {
            errorMessage = getBiometricErrorMessage(currentAvailability)
            showErrorSnackbar(errorMessage ?: "Authentication failed")
            return false
        }

        isAuthenticating = true
        isAuthenticated = false
        errorMessage = null

        return try {
            val success = authenticateWithBiometric(
                activity = activity,
                title = title,
                subtitle = subtitle
            )
            isAuthenticated = success
            if (!success) {
                errorMessage = "Authentication cancelled"
            }
            success
        } catch (e: Exception) {
            errorMessage = "Authentication error: ${e.message}"
            showErrorSnackbar(errorMessage ?: "Authentication failed")
            false
        } finally {
            isAuthenticating = false
        }
    }

    /**
     * Réinitialiser l'état
     */
    fun reset() {
        isAuthenticated = false
        isAuthenticating = false
        errorMessage = null
    }

    private suspend fun showErrorSnackbar(message: String) {
        snackbarHostState?.showSnackbar(
            message = message,
            duration = SnackbarDuration.Long
        )
    }
}

/**
 * Vérifier si l'authentification biométrique est requise
 *
 * Retourne true si l'app doit exiger la biométrie pour l'auto-fill
 * (configurable via Remote Config ou preferences)
 */
fun isBiometricRequiredForAutoFill(context: Context): Boolean {
    // Par défaut, la biométrie est requise pour l'auto-fill
    // Peut être configurée via SharedPreferences ou Remote Config
    val prefs = context.getSharedPreferences("security_settings", Context.MODE_PRIVATE)
    return prefs.getBoolean("biometric_required_autofill", true)
}

/**
 * Activer/désactiver l'exigence biométrique pour l'auto-fill
 */
fun setBiometricRequirement(context: Context, required: Boolean) {
    val prefs = context.getSharedPreferences("security_settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("biometric_required_autofill", required).apply()
}