package com.example.aureus.ui.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.Notification
import com.example.aureus.domain.model.NotificationPreferences
import com.example.aureus.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    // État des notifications
    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Liste des notifications
    val notifications: StateFlow<List<Notification>> = authManager.currentUser?.uid?.let { userId ->
        notificationRepository.getUserNotifications(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow<List<Notification>>(emptyList()).also { _uiState.value = NotificationUiState.Success }

    // Notifications non lues
    val unreadNotifications: StateFlow<List<Notification>> = authManager.currentUser?.uid?.let { userId ->
        notificationRepository.getUnreadNotifications(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow(emptyList())

    // Compteur de notifications non lues
    val unreadCount: StateFlow<Int> = authManager.currentUser?.uid?.let { userId ->
        notificationRepository.getUnreadCount(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )
    } ?: MutableStateFlow(0)

    // Préférences de notification
    val preferences: StateFlow<NotificationPreferences?> = authManager.currentUser?.uid?.let { userId ->
        notificationRepository.getNotificationPreferences(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } ?: MutableStateFlow(null)

    init {
        refreshNotifications()
    }

    /**
     * Rafraîchir les notifications
     */
    fun refreshNotifications() {
        _uiState.value = NotificationUiState.Success
    }

    /**
     * Marquer une notification comme lue
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = authManager.currentUser?.uid ?: return@launch
            notificationRepository.markAllAsRead(userId)
        }
    }

    /**
     * Supprimer une notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    /**
     * Mettre à jour les préférences de notification
     */
    fun updatePreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            val userId = authManager.currentUser?.uid ?: return@launch
            notificationRepository.updateNotificationPreferences(userId, preferences)
        }
    }

    /**
     * Toggle push notifications
     */
    fun togglePushNotifications(enabled: Boolean) {
        preferences.value?.let { currentPrefs ->
            updatePreferences(currentPrefs.copy(pushEnabled = enabled))
        }
    }

    /**
     * Toggle transaction alerts
     */
    fun toggleTransactionAlerts(enabled: Boolean) {
        preferences.value?.let { currentPrefs ->
            updatePreferences(currentPrefs.copy(transactionAlerts = enabled))
        }
    }

    /**
     * Toggle low balance alerts
     */
    fun toggleLowBalanceAlerts(enabled: Boolean) {
        preferences.value?.let { currentPrefs ->
            updatePreferences(currentPrefs.copy(lowBalanceAlerts = enabled))
        }
    }

    /**
     * Toggle transfer alerts
     */
    fun toggleTransferAlerts(enabled: Boolean) {
        preferences.value?.let { currentPrefs ->
            updatePreferences(currentPrefs.copy(transferAlerts = enabled))
        }
    }

    /**
     * Toggle promotional notifications
     */
    fun togglePromotionalNotifications(enabled: Boolean) {
        preferences.value?.let { currentPrefs ->
            updatePreferences(currentPrefs.copy(promotionalNotifications = enabled))
        }
    }

    /**
     * États de l'UI
     */
    sealed class NotificationUiState {
        object Loading : NotificationUiState()
        object Success : NotificationUiState()
        data class Error(val message: String) : NotificationUiState()
    }
}