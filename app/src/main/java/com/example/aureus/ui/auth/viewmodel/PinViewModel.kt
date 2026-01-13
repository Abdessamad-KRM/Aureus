package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.firestore.PinFirestoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ✅ PHASE 1 CORRECTION: PinViewModel pour vérification PIN sécurisée
 * Utilise PinFirestoreManager pour récupérer et vérifier le PIN hashé depuis Firestore
 */
@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinFirestoreManager: PinFirestoreManager
) : ViewModel() {

    private val _verificationResult = MutableStateFlow<Boolean?>(null)
    val verificationResult: StateFlow<Boolean?> = _verificationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * ✅ Vérifier le PIN - fonction suspend pure
     * Récupère le PIN hashé depuis Firestore avec salt
     */
    suspend fun verifyPin(pin: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val isValid = pinFirestoreManager.verifyPin(pin)
            _verificationResult.value = isValid

            if (!isValid) {
                _errorMessage.value = "PIN incorrect"
            }
            isValid
        } catch (e: Exception) {
            _errorMessage.value = "Erreur lors de la vérification"
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * ✅ Vérifier le PIN et exécuter une action si correct
     * (Backward compatibility wrapper)
     */
    suspend fun verifyPinAndExecute(pin: String, onComplete: (Boolean) -> Unit) {
        val isValid = verifyPin(pin)
        onComplete(isValid)
    }

    /**
     * Réinitialiser l'état
     */
    fun reset() {
        _verificationResult.value = null
        _errorMessage.value = null
    }
}