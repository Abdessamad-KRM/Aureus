package com.example.aureus.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.CardDetail
import com.example.aureus.domain.model.mapToCardDetail
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// UI State for CardDetail
sealed class CardDetailUiState {
    object Loading : CardDetailUiState()
    data class Success(val card: CardDetail) : CardDetailUiState()
    data class Error(val message: String) : CardDetailUiState()
}

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _cardDetailState = MutableStateFlow<CardDetailUiState>(CardDetailUiState.Loading)
    val cardDetailState: StateFlow<CardDetailUiState> = _cardDetailState.asStateFlow()

    private val _cardTransactions = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val cardTransactions: StateFlow<List<Map<String, Any>>> = _cardTransactions.asStateFlow()

    fun loadCardDetail(cardId: String) {
        viewModelScope.launch {
            _cardDetailState.value = CardDetailUiState.Loading

            try {
                val result = firebaseDataManager.getCardById(cardId)

                if (result.isSuccess) {
                    val cardData = result.getOrNull()!!
                    val cardDetail = mapToCardDetail(cardData)

                    _cardDetailState.value = CardDetailUiState.Success(cardDetail)

                    // Load transactions for this card
                    loadCardTransactions(cardId)
                } else {
                    _cardDetailState.value = CardDetailUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load card"
                    )
                }
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun loadCardTransactions(cardId: String) {
        viewModelScope.launch {
            val userId = authManager.currentUser?.uid
            if (userId != null) {
                firebaseDataManager.getTransactionsByCard(cardId, userId)
                    .collect { transactions ->
                        _cardTransactions.value = transactions
                    }
            }
        }
    }

    fun freezeCard(cardId: String) {
        viewModelScope.launch {
            try {
                firebaseDataManager.updateCard(cardId, mapOf(
                    "status" to "FROZEN",
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
                // Reload card detail
                loadCardDetail(cardId)
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to freeze card")
            }
        }
    }

    fun blockCard(cardId: String) {
        viewModelScope.launch {
            try {
                firebaseDataManager.updateCard(cardId, mapOf(
                    "status" to "BLOCKED",
                    "isActive" to false,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
                loadCardDetail(cardId)
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to block card")
            }
        }
    }

    fun setAsDefault(cardId: String) {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUser?.uid
                if (userId != null) {
                    firebaseDataManager.setDefaultCard(userId, cardId)
                    loadCardDetail(cardId)
                }
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to set as default")
            }
        }
    }
}