package com.example.aureus.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.NotificationRepository
import com.example.aureus.domain.repository.TransferRepository
import com.example.aureus.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject

/**
 * HomeViewModel - Offline-First ViewModel with Firebase + Room
 * Supports offline mode with automatic cache fallback
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val database: AppDatabase,
    private val offlineSyncManager: OfflineSyncManager,
    private val analyticsManager: AnalyticsManager,
    private val authManager: FirebaseAuthManager,
    private val notificationRepository: NotificationRepository,
    private val transferRepository: TransferRepository
) : ViewModel() {

    // UI States
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Account State
    private val _totalBalanceState = MutableStateFlow<Resource<Double>>(Resource.Idle)
    val totalBalanceState: StateFlow<Resource<Double>> = _totalBalanceState.asStateFlow()

    // Sync Status
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Deferred properties for lazy loading
    private var userDataDeferred: Deferred<Unit>? = null
    private var cardsDataDeferred: Deferred<Unit>? = null
    private var transactionsDataDeferred: Deferred<Unit>? = null

    // Unread notification count
    val unreadCount: StateFlow<Int> = authManager.currentUser?.uid?.let { userId ->
        notificationRepository.getUnreadCount(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )
    } ?: MutableStateFlow(0)

    init {
        // Observer le statut de sync via Flow (sans boucle!)
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
            }
        }

        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadUserData(userId)
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }

    private fun loadUserData(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Try Firebase first, fallback to Room if offline
            val isOnline = offlineSyncManager.getSyncStatus().isOnline
            
            if (isOnline) {
                loadFromFirebase(userId)
            } else {
                loadFromOfflineCache(userId)
            }
        }
    }
    
    /**
     * Load data from Firebase (online mode)
     */
    private fun loadFromFirebase(userId: String) {
        _uiState.update { it.copy(isLoading = true, isOfflineMode = false) }

        viewModelScope.launch {
            // Tracker si tous les chargements sont terminés ✅
            var userLoaded = false
            var cardsLoaded = false
            var balanceLoaded = false

            // Charger l'utilisateur avec async (lazy loading)
            userDataDeferred = async {
                firebaseDataManager.getUser(userId).collect { userData ->
                    userData?.let {
                        _uiState.update { state ->
                            state.copy(user = it)
                        }
                        userLoaded = true
                        checkAllDataLoaded(userLoaded, cardsLoaded, balanceLoaded)
                    }
                }
            }

            // Charger les cartes avec async (lazy loading)
            cardsDataDeferred = async {
                firebaseDataManager.getUserCards(userId).collect { cards ->
                    _uiState.update { state ->
                        val defaultCard = cards.firstOrNull {
                            (it["isDefault"] as? Boolean) == true
                        } ?: cards.firstOrNull()
                        state.copy(
                            cards = cards,
                            defaultCard = defaultCard
                        )
                    }
                    cardsLoaded = true
                    checkAllDataLoaded(userLoaded, cardsLoaded, balanceLoaded)
                }
            }

            // Charger le solde total avec async (lazy loading)
            transactionsDataDeferred = async {
                firebaseDataManager.getUserTotalBalance(userId).collect { balance ->
                    _totalBalanceState.value = Resource.Success(balance)
                    _uiState.update { it.copy(totalBalance = balance) }
                    balanceLoaded = true
                    checkAllDataLoaded(userLoaded, cardsLoaded, balanceLoaded)
                }
            }

            // Charger les transactions récentes de manière lazy
            lazyLoadRecentTransactions(userId)
        }
    }

    /**
     * Vérifie si toutes les données sont chargées et met à jour isLoading ✅
     */
    private fun checkAllDataLoaded(userLoaded: Boolean, cardsLoaded: Boolean, balanceLoaded: Boolean) {
        if (userLoaded && cardsLoaded && balanceLoaded) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Lazy load recent transactions separately
     */
    private fun lazyLoadRecentTransactions(userId: String) {
        viewModelScope.launch {
            firebaseDataManager.getRecentTransactions(userId, 5).collect { transactions ->
                _uiState.update { it.copy(recentTransactions = transactions) }
            }
        }
    }
    
    /**
     * Load data from Room database (offline mode)
     */
    private fun loadFromOfflineCache(userId: String) {
        _uiState.update { it.copy(isLoading = true, isOfflineMode = true) }

        viewModelScope.launch {
            // Load user data from cache (if available)
            // Note: User is not stored in Room, so we'll use cached data

            // Tracker si tous les chargements sont terminés ✅
            var cardsLoaded = false
            var transactionsLoaded = false

            // Charger les cartes depuis Room avec async (lazy loading)
            cardsDataDeferred = async {
                database.cardDao().getActiveCards(userId).collect { cardEntities ->
                    val cards = cardEntities.map { entity ->
                        mapOf(
                            "cardId" to entity.id,
                            "userId" to entity.userId,
                            "accountId" to entity.accountId,
                            "cardNumber" to entity.cardNumber,
                            "cardHolder" to entity.cardHolder,
                            "expiryDate" to entity.expiryDate,
                            "cardType" to entity.cardType,
                            "cardColor" to entity.cardColor,
                            "isDefault" to entity.isDefault,
                            "isActive" to entity.isActive,
                            "dailyLimit" to entity.dailyLimit,
                            "monthlyLimit" to entity.monthlyLimit
                        )
                    }
                    val defaultCard = cards.firstOrNull {
                        (it["isDefault"] as? Boolean) == true
                    } ?: cards.firstOrNull()
                    _uiState.update { state ->
                        state.copy(
                            cards = cards,
                            defaultCard = defaultCard
                        )
                    }
                    cardsLoaded = true
                    checkOfflineDataLoaded(cardsLoaded, transactionsLoaded)
                }
            }

            // Charger les transactionsDepuis Room avec lazy loading
            transactionsDataDeferred = async {
                database.transactionDao().getTransactionsById(userId).collect { transactionEntities ->
                    val transactions: List<Map<String, Any>> = transactionEntities.map { entity ->
                        mapOf<String, Any>(
                            "transactionId" to (entity.id as Any),
                            "userId" to (entity.userId as Any),
                            "accountId" to (entity.accountId as Any),
                            "type" to (entity.type as Any),
                            "category" to (entity.category as Any),
                            "title" to (entity.description as Any),
                            "description" to (entity.description as Any),
                            "amount" to (entity.amount as Any),
                            "merchant" to ((entity.merchant ?: "") as Any),
                            "date" to ((entity.date ?: System.currentTimeMillis()) as Any),
                            "balanceAfter" to ((entity.balanceAfter ?: 0.0) as Any)
                        )
                    }
                    _uiState.update { it.copy(recentTransactions = transactions.take(5)) }

                    // Calculate total balance from transactions
                    val income = transactionEntities.filter { it.type == "CREDIT" || it.type == "INCOME" }
                        .sumOf { it.amount }
                    val expense = transactionEntities.filter { it.type == "DEBIT" || it.type == "EXPENSE" }
                        .sumOf { it.amount }
                    val balance = income - expense
                    _totalBalanceState.value = Resource.Success(balance)
                    _uiState.update { it.copy(totalBalance = balance) }

                    transactionsLoaded = true
                    checkOfflineDataLoaded(cardsLoaded, transactionsLoaded)
                }
            }
        }
    }

    /**
     * Vérifie si toutes les données offline sont chargées ✅
     */
    private fun checkOfflineDataLoaded(cardsLoaded: Boolean, transactionsLoaded: Boolean) {
        if (cardsLoaded && transactionsLoaded) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup if needed
    }

    fun refreshData() {
        val userId = firebaseDataManager.currentUserId() ?: return
        
        viewModelScope.launch {
            // Try to sync first if online
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val syncResult = offlineSyncManager.syncNow()
                when (syncResult) {
                    is com.example.aureus.data.offline.SyncResult.Success -> {
                        loadFromFirebase(userId)
                    }
                    is com.example.aureus.data.offline.SyncResult.Error -> {
                        // Sync failed, load from cache anyway
                        loadFromOfflineCache(userId)
                    }
                }
            } else {
                loadFromOfflineCache(userId)
            }
        }
    }

    fun getCurrentUserName(): String {
        val user = _uiState.value.user
        val firstName = user?.get("firstName") as? String ?: ""
        val lastName = user?.get("lastName") as? String ?: ""
        return if (firstName.isNotEmpty()) firstName else "User"
    }

    /**
     * ✅ PHASE 4: sendMoneyToContact délégué à TransferRepository
     * Utilise Cloud Function pour transaction atomique
     */
    fun sendMoneyToContact(contact: Contact, amount: Double): Flow<Result<String>> = flow {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) {
            emit(Result.failure(Exception("User not logged in")))
            return@flow
        }

        // Vérifier si le contact a un firebaseUserId
        if (contact.firebaseUserId == null) {
            emit(Result.failure(Exception("Ce contact ne peut pas recevoir d'argent")))
            return@flow
        }

        // Exécuter le transfert via TransferRepository
        val result = transferRepository.transferMoney(
            recipientUserId = contact.firebaseUserId,
            amount = amount,
            description = "Transfer to ${contact.name}"
        )

        when (result) {
            is Resource.Success -> {
                // Track successful transfer - PHASE 11: Analytics
                analyticsManager.trackTransferSent(
                    userId = userId,
                    amount = amount,
                    recipient = contact.name,
                    method = "wallet_to_wallet"
                )
                analyticsManager.trackTransactionCreated(
                    userId = userId,
                    type = "EXPENSE",
                    category = "Transfer",
                    amount = amount,
                    method = "wallet_to_wallet"
                )

                emit(Result.success("Transfert réussi vers ${contact.name}!"))
            }
            is Resource.Error -> {
                // Track failed transfer - PHASE 11: Analytics
                analyticsManager.trackTransactionFailed(
                    userId = userId,
                    error = result.message ?: "Transfer failed"
                )

                emit(Result.failure(
                    Exception(result.message ?: "Impossible d'effectuer le transfert")
                ))
            }
            else -> {
                // Handle Idle and Loading states
                emit(Result.failure(Exception("Transfert en cours...")))
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use sendMoneyToContact(Contact, Double) instead
     */
    fun sendMoney(amount: Double, recipient: String): Flow<Result<String>> = flow {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) {
            emit(Result.failure(Exception("User not logged in")))
            return@flow
        }

        val transactionData = mutableMapOf(
            "userId" to userId,
            "type" to "EXPENSE",
            "category" to "Transfer",
            "title" to "Money Transfer",
            "description" to "Transfer to $recipient",
            "amount" to amount,
            "recipientName" to recipient
        )

        val result = firebaseDataManager.createTransaction(transactionData)
        if (result.isSuccess) {
            // Track successful transfer - PHASE 11: Analytics
            analyticsManager.trackTransferSent(
                userId = userId,
                amount = amount,
                recipient = recipient,
                method = "wallet_to_wallet"
            )
            analyticsManager.trackTransactionCreated(
                userId = userId,
                type = "EXPENSE",
                category = "Transfer",
                amount = amount,
                method = "wallet_to_wallet"
            )
            emit(Result.success("Money sent to $recipient!"))
        } else {
            // Track failed transfer - PHASE 11: Analytics
            analyticsManager.trackTransactionFailed(
                userId = userId,
                error = result.exceptionOrNull()?.message ?: "Transaction failed"
            )
            emit(Result.failure(result.exceptionOrNull() ?: Exception("Transaction failed")))
        }
    }

    fun addCard(cardType: String): Flow<Result<String>> = flow {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) {
            emit(Result.failure(Exception("User not logged in")))
            return@flow
        }

        val user = _uiState.value.user
        val cardHolder = "${user?.get("firstName") ?: ""} ${user?.get("lastName") ?: ""}".trim()

        val result = firebaseDataManager.addCard(
            userId = userId,
            accountId = "", // Will be determined by FirebaseDataManager
            cardNumber = "4242",
            cardHolder = cardHolder,
            expiryDate = "12/28",
            cvv = "***",
            cardType = cardType,
            cardColor = "navy",
            isDefault = false
        )

        if (result.isSuccess) {
            // Track card added - PHASE 11: Analytics
            analyticsManager.trackCardAdded(userId, cardType)
            emit(Result.success("Card added successfully!"))
        } else {
            emit(Result.failure(result.exceptionOrNull() ?: Exception("Failed to add card")))
        }
    }

    /**
     * Track screen view - PHASE 11: Analytics
     */
    fun trackScreenView(screenName: String) {
        analyticsManager.trackScreenView(screenName)
    }

    /**
     * Track balance check - PHASE 11: Analytics
     */
    fun trackBalanceCheck(balance: Double, source: String) {
        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            analyticsManager.trackBalanceCheck(userId, balance, source)
        }
    }

    /**
     * Track offline mode enabled - PHASE 11: Analytics
     */
    fun trackOfflineModeEnabled() {
        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            analyticsManager.trackOfflineModeEnabled(userId)
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: Map<String, Any>? = null,
    val cards: List<Map<String, Any>> = emptyList(),
    val defaultCard: Map<String, Any>? = null,
    val totalBalance: Double = 0.0,
    val recentTransactions: List<Map<String, Any>> = emptyList(),
    val error: String? = null,
    val isOfflineMode: Boolean = false // Phase 8: Offline-First indicator
)