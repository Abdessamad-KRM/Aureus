package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.firestore.PinFirestoreManager
import com.example.aureus.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pin ViewModel
 * Gère l'état et les opérations liées au PIN
 */
@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinFirestoreManager: PinFirestoreManager
) : ViewModel() {

    private val _pinState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val pinState: StateFlow<Resource<Unit>> = _pinState.asStateFlow()

    /**
     * Sauvegarder le PIN dans Firestore
     */
    fun savePin(pin: String) {
        viewModelScope.launch {
            _pinState.value = Resource.Loading
            val result = pinFirestoreManager.savePin(pin)
            _pinState.value = result
        }
    }

    /**
     * Vérifier si l'utilisateur a un PIN configuré
     */
    suspend fun hasPinConfigured(): Boolean {
        return pinFirestoreManager.hasPinConfigured()
    }

    /**
     * Vérifier le PIN
     */
    suspend fun verifyPin(pin: String): Boolean {
        return pinFirestoreManager.verifyPin(pin)
    }

    /**
     * Mettre à jour le PIN
     */
    fun updatePin(newPin: String) {
        viewModelScope.launch {
            _pinState.value = Resource.Loading
            val result = pinFirestoreManager.updatePin(newPin)
            _pinState.value = result
        }
    }

    fun resetState() {
        _pinState.value = Resource.Idle
    }
}