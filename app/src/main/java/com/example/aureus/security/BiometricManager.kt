package com.example.aureus.security

import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère l'authentification biométrique (Fingerprint, FaceID)
 */
@Singleton
class BiometricManager @Inject constructor(
    private val context: Context
) {

    private val _authResult = MutableStateFlow<BiometricResult>(BiometricResult.Idle)
    val authResult: StateFlow<BiometricResult> = _authResult.asStateFlow()

    /**
     * Vérifier si l'appareil supporte la biométrie
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)

        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoCredentials
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.Unsupported
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.Unknown
            else -> BiometricAvailability.Unknown
        }
    }

    /**
     * Lancer l'authentification biométrique
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Aureus Banking",
        subtitle: String = "Use your fingerprint to continue",
        description: String = "Touch the sensor to verify your identity",
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit = {},
        onError: (error: String) -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        // Vérifier disponibilité
        val availability = isBiometricAvailable()
        if (availability != BiometricAvailability.Available) {
            _authResult.value = BiometricResult.Error("Biometric not available: ${availability.name}")
            onError("Biometric not available")
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(context)

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        _authResult.value = BiometricResult.Success
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        _authResult.value = BiometricResult.Failed
                        onFailure()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        _authResult.value = BiometricResult.Error(errString.toString())
                        onError(errString.toString())
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            _authResult.value = BiometricResult.Error("Biometric error: ${e.message}")
            onError(e.message ?: "Unknown error")
        }
    }

    /**
     * Demander à l'utilisateur d'activer la biométrie
     */
    fun promptEnableBiometric(activity: FragmentActivity) {
        val intent = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }

    /**
     * Reset result to Idle
     */
    fun resetAuthResult() {
        _authResult.value = BiometricResult.Idle
    }
}

/**
 * Résultat de l'authentification biométrique
 */
sealed class BiometricResult {
    object Idle : BiometricResult()
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

/**
 * Disponibilité de la biométrie
 */
enum class BiometricAvailability {
    Available,
    NoHardware,
    HardwareUnavailable,
    NoCredentials,
    SecurityUpdateRequired,
    Unsupported,
    Unknown
}