package com.example.aureus.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel - Version statique avec données fictives pour la démo
 * Utilise StaticData au lieu de Firebase pour une présentation client
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // Plus de dépendance Firebase - utilisé pour demo statique
) : ViewModel() {

    // UI States
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStaticData()
    }

    private fun loadStaticData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Simuler un petit délai de chargement
            kotlinx.coroutines.delay(500)

            // Charger les données statiques
            val user = TestAccount.user
            val cards = StaticCards.cards
            val defaultCard = cards.firstOrNull { it.isDefault } ?: cards.first()
            val totalBalance = cards.sumOf { it.balance }
            val recentTransactions = StaticTransactions.transactions.take(5)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = mapOf(
                        "firstName" to user.firstName,
                        "lastName" to user.lastName,
                        "email" to user.email,
                        "phone" to user.phone,
                        "address" to (user.address ?: ""),
                        "city" to (user.city ?: "")
                    ),
                    cards = cards.map { card ->
                        mapOf(
                            "id" to card.id,
                            "cardNumber" to card.cardNumber.takeLast(4),
                            "cardHolder" to card.cardHolder,
                            "cardType" to card.cardType.name,
                            "expiryDate" to card.expiryDate,
                            "balance" to card.balance
                        )
                    },
                    defaultCard = mapOf(
                        "id" to defaultCard.id,
                        "cardNumber" to defaultCard.cardNumber.takeLast(4),
                        "cardHolder" to defaultCard.cardHolder,
                        "cardType" to defaultCard.cardType.name,
                        "expiryDate" to defaultCard.expiryDate,
                        "balance" to defaultCard.balance
                    ),
                    totalBalance = totalBalance,
                    recentTransactions = recentTransactions.map { trx ->
                        mapOf(
                            "id" to trx.id,
                            "title" to trx.title,
                            "description" to trx.description,
                            "amount" to trx.amount,
                            "type" to trx.type.name,
                            "category" to trx.category.name,
                            "date" to com.google.firebase.Timestamp(trx.date),
                            "recipientName" to (trx.recipientName ?: ""),
                            "status" to trx.status.name
                        )
                    }
                )
            }
        }
    }

    fun refreshData() {
        loadStaticData()
    }

    fun getCurrentUserName(): String {
        val user = _uiState.value.user
        return (user?.get("firstName") as? String) ?: "User"
    }

    fun sendMoney(amount: Double, recipient: String): Flow<Result<String>> = flow {
        emit(Result.success("Money sent to $recipient!"))
    }

    fun addCard(cardType: String): Flow<Result<String>> = flow {
        emit(Result.success("Card added successfully!"))
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: Map<String, Any>? = null,
    val cards: List<Map<String, Any>> = emptyList(),
    val defaultCard: Map<String, Any>? = null,
    val totalBalance: Double = 0.0,
    val recentTransactions: List<Map<String, Any>> = emptyList(),
    val error: String? = null
)