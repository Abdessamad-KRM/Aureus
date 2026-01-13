package com.example.aureus.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.repository.CardRepositoryImpl
import com.example.aureus.domain.model.BankCard
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * CardsViewModel - Offline-First avec support Room cache
 * Phase 7: Offline-First Complete
 */
@HiltViewModel
class CardsViewModel @Inject constructor(
    private val cardRepository: CardRepositoryImpl,
    private val firebaseDataManager: FirebaseDataManager,
    private val database: AppDatabase,
    private val offlineSyncManager: OfflineSyncManager
) : ViewModel() {

    // State pour liste des cartes (BankCard domain model)
    private val _cards = MutableStateFlow<List<BankCard>>(emptyList())
    val cards: StateFlow<List<BankCard>> = _cards.asStateFlow()

    // State pour l'état de chargement/erreur
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Carte par défaut
    private val _defaultCard = MutableStateFlow<BankCard?>(null)
    val defaultCard: StateFlow<BankCard?> = _defaultCard.asStateFlow()

    // Sync Status (Phase 7)
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    init {
        // Observer le statut de sync via Flow
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
                _isOfflineMode.value = !status.isOnline
            }
        }

        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadCards(userId)
        }
    }

    private fun loadCards(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            cardRepository.getCards(userId).collect { cardList ->
                _cards.value = cardList
                _defaultCard.value = cardList.find { it.isDefault }
                _isLoading.value = false
            }
        }
    }

    /**
     * Ajouter une nouvelle carte
     */
    fun addCard(
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String,
        cardType: CardType = CardType.VISA,
        cardColor: CardColor = CardColor.NAVY,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isLoading.value = true
            }

            val userId = firebaseDataManager.currentUserId() ?: run {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "User not authenticated"
                    _isLoading.value = false
                    onError("User not authenticated")
                }
                return@launch
            }

            // Avec withContext explicite pour la requête Firestore
            val snapshot = withContext(Dispatchers.IO) {
                firebaseDataManager.firestore
                    .collection("accounts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }

            val accounts = snapshot?.documents ?: emptyList()

            if (accounts.isEmpty()) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "No account found"
                    _isLoading.value = false
                    onError("No account found")
                }
                return@launch
            }

            val accountId = accounts[0].id
            val isFirstCard = _cards.value.isEmpty()

            cardRepository.addCard(
                userId = userId,
                accountId = accountId,
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cvv = cvv,
                cardType = cardType,
                cardColor = cardColor.name.lowercase(),
                isDefault = isFirstCard
            ).onSuccess {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = null
                    onSuccess()
                }
            }.onFailure { e ->
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Failed to add card"
                    onError(e.message ?: "Failed to add card")
                }
            }
        }
    }

    /**
     * Définir une carte comme par défaut
     */
    fun setDefaultCard(cardId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = firebaseDataManager.currentUserId() ?: run {
                _isLoading.value = false
                return@launch
            }

            cardRepository.setDefaultCard(userId, cardId).onSuccess {
                _isLoading.value = false
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Geler/Dégeler une carte
     */
    fun toggleCardFreeze(cardId: String, isFrozen: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            cardRepository.toggleCardFreeze(cardId, isFrozen).onSuccess {
                _isLoading.value = false
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Mettre à jour les limites de dépenses
     */
    fun updateCardLimits(
        cardId: String,
        dailyLimit: Double,
        monthlyLimit: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            cardRepository.updateCardLimits(cardId, dailyLimit, monthlyLimit).onSuccess {
                _isLoading.value = false
                onSuccess()
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
                onError(e.message ?: "Failed to update limits")
            }
        }
    }

    /**
     * Créer des cartes de test pour un nouvel utilisateur
     */
    fun createTestCards(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = firebaseDataManager.currentUserId() ?: run {
                _errorMessage.value = "User not authenticated"
                _isLoading.value = false
                onError("User not authenticated")
                return@launch
            }

            cardRepository.createTestCards(userId).onSuccess {
                _isLoading.value = false
                onSuccess()
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
                onError(e.message ?: "Failed to create test cards")
            }
        }
    }

    /**
     * Rafraîchir les données (avec sync Phase 7)
     */
    fun refresh() {
        val userId = firebaseDataManager.currentUserId() ?: return
        
        // Try to sync first if online (Phase 7)
        viewModelScope.launch {
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val syncResult = offlineSyncManager.syncNow()
                when (syncResult) {
                    is com.example.aureus.data.offline.SyncResult.Success -> {
                        loadCards(userId)
                    }
                    is com.example.aureus.data.offline.SyncResult.Error -> {
                        // Sync failed, load anyway
                        loadCards(userId)
                    }
                }
            } else {
                loadCards(userId)
            }
        }
    }

    /**
     * Effacer l'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }
}