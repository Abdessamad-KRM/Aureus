package com.example.aureus.ui.transfer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.TransferRepository
import com.example.aureus.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Transfer ViewModel - Gestion des transferts entre utilisateurs
 * ✅ PHASE 4: ViewModel complet pour SendMoney et RequestMoney
 */
@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val firebaseDataManager: FirebaseDataManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    // ==================== UI STATES ====================

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    // ==================== SEND MONEY ====================

    /**
     * Sélectionner un contact pour transfert
     */
    fun selectContact(contact: Contact) {
        _selectedContact.value = contact
        _uiState.value = _uiState.value.copy(contactValidationError = null)

        // Si le contact a un firebaseUserId, le valider
        contact.firebaseUserId?.let { userId ->
            validateContactUser(userId)
        }
    }

    /**
     * Sélectionner un contact par ID (pour réception depuis navigation)
     */
    fun selectContactById(contactId: String, contacts: List<Contact>) {
        val contact = contacts.find { it.id == contactId }
        if (contact != null) {
            selectContact(contact)
        }
    }

    /**
     * Définir le montant du transfert
     */
    fun setAmount(value: String) {
        // Valider: seulement chiffres et un point décimal
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d+$"))) {
            _amount.value = value
            _uiState.value = _uiState.value.copy(amountValidationError = null)

            // Vérifier si le montant dépasse les limites
            value.toDoubleOrNull()?.let { amt ->
                if (amt > TRANSFER_MAX_AMOUNT) {
                    _uiState.value = _uiState.value.copy(
                        amountValidationError = "Le montant maximum est de ${TRANSFER_MAX_AMOUNT} MAD"
                    )
                }
            }
        }
    }

    /**
     * Définir la description du transfert
     */
    fun setDescription(value: String) {
        _description.value = value
    }

    /**
     * Valider si l'utilisateur Firebase du contact existe
     */
    private fun validateContactUser(firebaseUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidatingContact = true)

            when (val result = transferRepository.validateUserId(firebaseUserId)) {
                is Resource.Success -> {
                    if (result.data.exists) {
                        _uiState.value = _uiState.value.copy(
                            isValidatingContact = false,
                            contactValidationError = null,
                            isContactAppUser = true,
                            contactUserInfo = result.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isValidatingContact = false,
                            contactValidationError = "Ce contact n'est pas un utilisateur de l'app",
                            isContactAppUser = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isValidatingContact = false,
                        contactValidationError = "Impossible de vérifier le contact"
                    )
                }
                else -> {
                    // Handle Idle and Loading states
                    _uiState.value = _uiState.value.copy(
                        isValidatingContact = false
                    )
                }
            }
        }
    }

    /**
     * Exécuter le transfert d'argent (après vérification PIN)
     */
    fun executeTransfer(): Resource<String> {
        var result: Resource<String> = Resource.Idle

        viewModelScope.launch {
            val contact = _selectedContact.value
            val amountValue = _amount.value.toDoubleOrNull()
            val desc = _description.value.ifBlank { _selectedContact.value?.getDisplayNameForTransfer() ?: "Transfer" }

            // Validation
            when {
                contact == null -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez sélectionner un contact")
                    result = Resource.Error("Veuillez sélectionner un contact")
                    return@launch
                }
                contact.firebaseUserId == null -> {
                    _uiState.value = _uiState.value.copy(error = "Ce contact ne peut pas recevoir d'argent")
                    result = Resource.Error("Ce contact ne peut pas recevoir d'argent")
                    return@launch
                }
                !_uiState.value.isContactAppUser -> {
                    _uiState.value = _uiState.value.copy(error = _uiState.value.contactValidationError)
                    result = Resource.Error(_uiState.value.contactValidationError ?: "Contact invalide")
                    return@launch
                }
                amountValue == null || amountValue <= 0 -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez entrer un montant valide")
                    result = Resource.Error("Veuillez entrer un montant valide")
                    return@launch
                }
                amountValue > TRANSFER_MAX_AMOUNT -> {
                    _uiState.value = _uiState.value.copy(error = "Le montant maximum est de ${TRANSFER_MAX_AMOUNT} MAD")
                    result = Resource.Error("Limite de transfert dépassée")
                    return@launch
                }
            }

            // Exécuter le transfert
            _uiState.value = _uiState.value.copy(isTransferring = true)

            when (val transferResult = transferRepository.transferMoney(
                recipientUserId = contact.firebaseUserId!!,
                amount = amountValue,
                description = desc
            )) {
                is Resource.Success -> {
                    // Track succès - Analytics
                    val senderId = firebaseDataManager.currentUserId()
                    if (senderId != null) {
                        analyticsManager.trackTransferSent(
                            userId = senderId,
                            amount = amountValue,
                            recipient = contact.name,
                            method = "wallet_to_wallet"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferSuccess = true,
                        transferResultData = transferResult.data,
                        error = null
                    )

                    result = Resource.Success("Transfert effectué avec succès!")

                    // Reset le formulaire
                    resetForm()
                }
                is Resource.Error -> {
                    // Track échec - Analytics
                    val senderId = firebaseDataManager.currentUserId()
                    if (senderId != null) {
                        analyticsManager.trackTransactionFailed(
                            userId = senderId,
                            error = transferResult.message ?: "Unknown error"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        error = transferResult.message ?: "Erreur lors du transfert"
                    )

                    result = Resource.Error(transferResult.message ?: "Erreur")
                }
                else -> {
                    // Handle Idle and Loading states
                    _uiState.value = _uiState.value.copy(
                        isTransferring = false
                    )
                    result = Resource.Error("Transfert en cours...")
                }
            }
        }

        return result
    }

    // ==================== REQUEST MONEY ====================

    /**
     * Créer une demande d'argent
     */
    fun createMoneyRequest(): Resource<String> {
        var result: Resource<String> = Resource.Idle

        viewModelScope.launch {
            val contact = _selectedContact.value
            val amountValue = _amount.value.toDoubleOrNull()
            val reason = _description.value.ifBlank { "Demande de paiement" }

            // Validation
            when {
                contact == null -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez sélectionner un contact")
                    result = Resource.Error("Veuillez sélectionner un contact")
                    return@launch
                }
                contact.firebaseUserId == null -> {
                    _uiState.value = _uiState.value.copy(error = "Ce contact ne peut pas être sollicité")
                    result = Resource.Error("Contact invalide")
                    return@launch
                }
                amountValue == null || amountValue <= 0 -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez entrer un montant valide")
                    result = Resource.Error("Montant invalide")
                    return@launch
                }
            }

            // Créer la demande
            _uiState.value = _uiState.value.copy(isCreatingRequest = true)

            when (val requestResult = transferRepository.createMoneyRequest(
                recipientUserId = contact.firebaseUserId!!,
                amount = amountValue,
                reason = reason
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        requestSuccess = true,
                        requestId = requestResult.data,
                        error = null
                    )

                    result = Resource.Success("Demande envoyée!")
                    resetForm()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        error = requestResult.message ?: "Erreur lors de la demande"
                    )
                    result = Resource.Error(requestResult.message ?: "Erreur")
                }
                else -> {
                    // Handle Idle and Loading states
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false
                    )
                    result = Resource.Error("Demande en cours...")
                }
            }
        }

        return result
    }

    /**
     * Charger les demandes d'argent reçues
     */
    fun loadIncomingMoneyRequests() {
        val userId = firebaseDataManager.currentUserId() ?: return

        transferRepository.getMoneyRequestsReceived(userId, 10)
            .onEach { requests ->
                _uiState.value = _uiState.value.copy(
                    incomingMoneyRequests = requests
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Erreur de chargement des demandes: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Rejeter une demande d'argent
     */
    fun rejectMoneyRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = transferRepository.rejectMoneyRequest(requestId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Demande refusée"
                    )
                    // Recharger les demandes
                    loadIncomingMoneyRequests()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Erreur lors du refus"
                    )
                }
                else -> {
                    // Handle Idle and Loading states
                }
            }
        }
    }

    // ==================== UTILITIES ====================

    /**
     * Vérifier les limites de transfert
     */
    fun checkTransferLimits() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            when (val result = transferRepository.checkTransferLimits(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        transferLimits = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Impossible de vérifier les limites"
                    )
                }
                else -> {
                    // Handle Idle and Loading states
                }
            }
        }
    }

    /**
     * Reset le formulaire
     */
    fun resetForm() {
        _selectedContact.value = null
        _amount.value = ""
        _description.value = ""
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            transferSuccess = false,
            requestSuccess = false,
            contactValidationError = null,
            amountValidationError = null
        )
    }

    /**
     * Clear les messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    companion object {
        const val TRANSFER_MAX_AMOUNT = 50000.0  // 50,000 MAD
        const val TRANSFER_DAILY_LIMIT = 20000.0  // 20,000 MAD
        const val TRANSFER_MONTHLY_LIMIT = 100000.0  // 100,000 MAD
    }
}

// ==================== DATA CLASSES ====================

data class TransferUiState(
    val isValidatingContact: Boolean = false,
    val isTransferring: Boolean = false,
    val isCreatingRequest: Boolean = false,
    val transferSuccess: Boolean = false,
    val requestSuccess: Boolean = false,
    val contactValidationError: String? = null,
    val amountValidationError: String? = null,
    val isContactAppUser: Boolean = false,
    val contactUserInfo: com.example.aureus.domain.repository.UserInfo? = null,
    val transferResultData: com.example.aureus.domain.repository.TransferResult? = null,
    val requestId: String? = null,
    val incomingMoneyRequests: List<Map<String, Any>> = emptyList(),
    val transferLimits: com.example.aureus.domain.repository.TransferLimits? = null,
    val error: String? = null,
    val successMessage: String? = null
)