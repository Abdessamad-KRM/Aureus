package com.example.aureus.ui.auth.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Phone Auth ViewModel
 * Gère l'authentification par téléphone (SMS)
 * Supporte deux cas:
 * 1. Nouveau utilisateur (création de compte avec téléphone)
 * 2. Utilisateur existant (Google/email) -> lier le téléphone
 */
@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _phoneAuthState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val phoneAuthState: StateFlow<PhoneAuthState> = _phoneAuthState.asStateFlow()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Indique si on lie un compte existant ou on en crée un nouveau
    private var isLinkingExistingUser = false

    /**
     * Vérifier si un utilisateur est déjà connecté (Google/email)
     */
    fun isUserAlreadyLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Définir si on lie un compte existant
     */
    fun setLinkingExistingUser(isLinking: Boolean) {
        isLinkingExistingUser = isLinking
        Log.d("PhoneAuthViewModel", "Mode linking: $isLinking")
    }

    /**
     * Envoyer le code de vérification SMS au numéro de téléphone
     */
    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ) {
        Log.d("PhoneAuthViewModel", "Envoi du code SMS à: $phoneNumber (existingUser: $isLinkingExistingUser)")
        _phoneAuthState.value = PhoneAuthState.SendingCode

        firebaseAuthManager.verifyPhoneNumber(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = { verificationId ->
                Log.d("PhoneAuthViewModel", "Code SMS envoyé. verificationId: $verificationId")
                storedVerificationId = verificationId
                _phoneAuthState.value = PhoneAuthState.CodeSent(
                    verificationId = verificationId
                )
            },
            onVerificationCompleted = { credential ->
                Log.d("PhoneAuthViewModel", "Vérification automatique réussie")
                handleVerificationSuccess(credential)
            },
            onVerificationFailed = { exception ->
                Log.e("PhoneAuthViewModel", "Échec d'envoi SMS: ${exception.message}")
                _phoneAuthState.value = PhoneAuthState.Error(
                    message = exception.message ?: "Échec de l'envoi du SMS. Vérifiez le numéro."
                )
            }
        )
    }

    /**
     * Vérifier le code SMS entré par l'utilisateur
     */
    fun verifyOtpCode(code: String) {
        val verificationId = storedVerificationId
        if (verificationId == null) {
            _phoneAuthState.value = PhoneAuthState.Error(
                message = "Aucun code de vérification en attente"
            )
            return
        }

        Log.d("PhoneAuthViewModel", "Vérification du code OTP (existingUser: $isLinkingExistingUser)")
        _phoneAuthState.value = PhoneAuthState.Verifying

        viewModelScope.launch {
            try {
                if (isLinkingExistingUser && auth.currentUser != null) {
                    // Cas: Utilisateur existe déjà (Google/email) -> lier le téléphone
                    linkPhoneToExistingAccount(code, verificationId)
                } else {
                    // Cas: Nouveau utilisateur -> créer compte avec téléphone
                    verifyPhoneForNewAccount(code, verificationId)
                }
            } catch (e: Exception) {
                Log.e("PhoneAuthViewModel", "Erreur verification", e)
                _phoneAuthState.value = PhoneAuthState.Error(
                    message = e.message ?: "Erreur lors de la vérification"
                )
            }
        }
    }

    /**
     * Lier le téléphone à un compte existant (Google/email)
     */
    private suspend fun linkPhoneToExistingAccount(code: String, verificationId: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuthManager.linkPhoneCredential(credential)

            if (result.isSuccess) {
                Log.d("PhoneAuthViewModel", "Téléphone lié avec succès au compte existant")
                _phoneAuthState.value = PhoneAuthState.Success(
                    user = result.getOrNull(),
                    isNewUser = false
                )
            } else {
                val exception = result.exceptionOrNull()
                val message = when {
                    exception is FirebaseAuthException && exception.errorCode == "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Ce numéro est déjà lié à un autre compte"
                    else -> exception?.message ?: "Échec de la liaison du numéro"
                }
                Log.e("PhoneAuthViewModel", "Échec liaison: $message")
                _phoneAuthState.value = PhoneAuthState.Error(message)
            }
        } catch (e: Exception) {
            Log.e("PhoneAuthViewModel", "Erreur liaison téléphone", e)
            _phoneAuthState.value = PhoneAuthState.Error(
                message = "Erreur: ${e.message}"
            )
        }
    }

    /**
     * Créer un nouveau compte avec téléphone
     */
    private suspend fun verifyPhoneForNewAccount(code: String, verificationId: String) {
        try {
            val result = firebaseAuthManager.verifyPhoneCode(verificationId, code)
            if (result.isSuccess) {
                Log.d("PhoneAuthViewModel", "Nouveau compte créé avec téléphone")
                _phoneAuthState.value = PhoneAuthState.Success(
                    user = result.getOrNull(),
                    isNewUser = true
                )
            } else {
                val exception = result.exceptionOrNull()
                val message = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Code incorrect"
                    else -> exception?.message ?: "Échec de la vérification"
                }
                Log.e("PhoneAuthViewModel", "Échec verification: $message")
                _phoneAuthState.value = PhoneAuthState.Error(message)
            }
        } catch (e: Exception) {
            Log.e("PhoneAuthViewModel", "Erreur verification", e)
            _phoneAuthState.value = PhoneAuthState.Error(
                message = e.message ?: "Erreur lors de la vérification"
            )
        }
    }

    /**
     * Renvoyer le code SMS
     */
    fun resendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ) {
        Log.d("PhoneAuthViewModel", "Renvoi du code SMS")
        sendVerificationCode(phoneNumber, activity)
    }

    /**
     * Gérer le succès de la vérification (auto-verification)
     */
    private fun handleVerificationSuccess(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                if (isLinkingExistingUser && auth.currentUser != null) {
                    // Lier le téléphone au compte existant
                    val result = firebaseAuthManager.linkPhoneCredential(credential)
                    if (result.isSuccess) {
                        _phoneAuthState.value = PhoneAuthState.Success(
                            user = result.getOrNull(),
                            isNewUser = false
                        )
                    } else {
                        _phoneAuthState.value = PhoneAuthState.Error(
                            message = "Échec de la liaison"
                        )
                    }
                } else {
                    // Créer nouveau compte
                    val result = auth.signInWithCredential(credential)
                    if (result.isSuccessful) {
                        _phoneAuthState.value = PhoneAuthState.Success(
                            user = result.result?.user,
                            isNewUser = true
                        )
                    } else {
                        _phoneAuthState.value = PhoneAuthState.Error(
                            message = "Échec de l'authentification automatique"
                        )
                    }
                }
            } catch (e: Exception) {
                _phoneAuthState.value = PhoneAuthState.Error(
                    message = e.message ?: "Erreur lors de l'authentification"
                )
            }
        }
    }

    /**
     * Réinitialiser l'état
     */
    fun resetState() {
        _phoneAuthState.value = PhoneAuthState.Idle
        isLinkingExistingUser = false
    }
}

/**
 * États de l'authentification par téléphone
 */
sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object SendingCode : PhoneAuthState()
    data class CodeSent(
        val verificationId: String
    ) : PhoneAuthState()
    object Verifying : PhoneAuthState()
    data class Success(
        val user: FirebaseUser?,
        val isNewUser: Boolean = false  // Indique si c'est un nouvel utilisateur
    ) : PhoneAuthState()
    data class Error(
        val message: String
    ) : PhoneAuthState()
}