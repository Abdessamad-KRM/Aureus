# PLAN DE CORRECTION COMPLET - Système de Notifications Aureus

**Date de création:** 12 Janvier 2026
**Statut:** Plan détaillé pour corriger tous les problèmes identifiés
**Priorité:** HAUTE - Système de notifications non fonctionnel pour l'historique

---

## 📊 DIAGNOSTIC GLOBAL

### Problèmes identifiés: 8
- 🔴 Critiques: 4
- 🟡 Modéré: 3
- 🟢 Faible: 1

### Impact: HAUT
- Les notifications push fonctionnent MAIS pas d'UI pour consulter l'historique
- Conflit de configuration entre services
- Mauvaise expérience utilisateur

---

## 🔴 PHASE 1: CORRECTIONS CRITIQUES (Urgent - 1-2 heures)

### 1.1 Supprimer le service en double `MyFirebaseMessagingService.kt`

**Problème:** Deux services FCM existent, seul `FirebaseMessagingService.kt` est utilisé dans le manifeste.

**Action:**
```
Fichier à supprimer:
  app/src/main/java/com/example/aureus/notification/MyFirebaseMessagingService.kt

Raison: Code mort - non déclaré dans AndroidManifest.xml
```

**Vérification:**
- [ ] Recherche d'imports de `MyFirebaseMessagingService` dans tout le projet
- [ ] Suppression du fichier
- [ ] Vérification que l'app compile toujours

---

### 1.2 Unifier les IDs de canaux de notification

**Problème:** 3 fichiers utilisent des IDs différents pour les mêmes canaux:
- `Constants.kt` → `channel_transactions`, `channel_alerts`, `channel_info`
- `FirebaseMessagingService.kt` → `aureus_notifications`, `transactions`, `alerts`, `transfers`
- `NotificationHelper.kt` → `transactions`, `alerts`, `transfers`

**Solution:** Unifier sur une seule convention (choisir celle de `NotificationHelper.kt`)

**Fichiers à modifier:**

#### 1.2.1 Modifier `app/src/main/java/com/example/aureus/util/Constants.kt`

```kotlin
// ANCIEN CODE (à remplacer):
const val CHANNEL_TRANSACTION = "channel_transactions"
const val CHANNEL_ALERTS = "channel_alerts"
const val CHANNEL_INFO = "channel_info"

// NOUVEAU CODE:
const val CHANNEL_TRANSACTION = "transactions"
const val CHANNEL_ALERTS = "alerts"
const val CHANNEL_TRANSFER = "transfers"
const val CHANNEL_INFO = "aureus_notifications"

// Notification IDs (non changés)
const val NOTIFICATION_ID_TRANSACTION = 1001
const val NOTIFICATION_ID_LOW_BALANCE = 1002
const val NOTIFICATION_ID_INFO = 1003
const val NOTIFICATION_ID_TRANSFER = 1004
```

#### 1.2.2 Modifier `app/src/main/java/com/example/aureus/notification/FirebaseMessagingService.kt`

Ligne 26-28:
```kotlin
// ANCIEN CODE:
private const val CHANNEL_ID = "aureus_notifications"
private const val CHANNEL_NAME = "Aureus Notifications"
private const val CHANNEL_DESCRIPTION = "Notifications for Aureus Banking"

// NOUVEAU CODE:
private const val CHANNEL_ID = "aureus_notifications"
private const val CHANNEL_NAME = "Aureus Notifications"
private const val CHANNEL_DESCRIPTION = "Notifications for Aureus Banking" inchangé
```

**Aucun changement pour CHANNEL_ID** - déjà correct.
Les IDs dans les méthodes `showTransactionNotification()`, `showBalanceAlert()`, `showTransferNotification()` sont déjà corrects.

**Vérification:**
- [ ] Toutes les notifications s'affichent correctement sur Android 8+
- [ ] Les canaux apparaissent une seule fois dans les settings système

---

## 🟡 PHASE 2: CREATION DE L'UI DES NOTIFICATIONS (Important - 4-6 heures)

### 2.1 Créer le modèle de données `Notification.kt`

**Problème:** Aucun modèle pour stocker les notifications reçues.

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/domain/model/Notification.kt
```

**Code:**
```kotlin
package com.example.aureus.domain.model

import java.util.Date

/**
 * Modèle de notification pour l'historique
 */
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val timestamp: Date = Date(),
    val imageUrl: String? = null
)

/**
 * Types de notifications supportés
 */
enum class NotificationType {
    TRANSACTION,
    TRANSFER_RECEIVED,
    TRANSFER_SENT,
    BALANCE_ALERT,
    SECURITY_ALERT,
    PROMOTION,
    INFO,
    SYSTEM
}

/**
 * Modèle de préférences de notification
 */
data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val transactionAlerts: Boolean = true,
    val lowBalanceAlerts: Boolean = true,
    val transferAlerts: Boolean = true,
    val promotionalNotifications: Boolean = false,
    val quietHours: QuietHours? = null
)

/**
 * Config des heures silencieuses
 */
data class QuietHours(
    val enabled: Boolean = false,
    val startTime: String = "22:00", // 10 PM
    val endTime: String = "08:00"   // 8 AM
)
```

---

### 2.2 Créer le Repository des notifications

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/domain/repository/NotificationRepository.kt
```

**Code:**
```kotlin
package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Notification
import com.example.aureus.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Interface pour gérer les notifications
 */
interface NotificationRepository {

    // Obtenir toutes les notifications d'un utilisateur
    fun getUserNotifications(userId: String): Flow<List<Notification>>

    // Obtenir les notifications non lues
    fun getUnreadNotifications(userId: String): Flow<List<Notification>>

    // Marquer comme lu
    suspend fun markAsRead(notificationId: String): Result<Unit>

    // Marquer toutes comme lues
    suspend fun markAllAsRead(userId: String): Result<Unit>

    // Supprimer une notification
    suspend fun deleteNotification(notificationId: String): Result<Unit>

    // Obtenir les préférences
    fun getNotificationPreferences(userId: String): Flow<NotificationPreferences>

    // Mettre à jour les préférences
    suspend fun updateNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Result<Unit>

    // Sauvegarder une notification reçue (pour l'historique)
    suspend fun saveNotification(notification: Notification): Result<Unit>

    // Obtenir le compteur de notifications non lues
    fun getUnreadCount(userId: String): Flow<Int>
}
```

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/data/repository/NotificationRepositoryImpl.kt
```

**Code:**
```kotlin
package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Notification
import com.example.aureus.domain.model.NotificationPreferences
import com.example.aureus.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager
) : NotificationRepository {

    // Collection Firestore pour les notifications
    private val notificationsCollection = firebaseDataManager.firestore.collection("notifications")

    override fun getUserNotifications(userId: String): Flow<List<Notification>> {
        return firebaseDataManager.firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50) // Limiter à 50 notifications
            .get()
            .asFlow()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    mapToNotification(doc.data.orEmpty())
                }
            }
    }

    override fun getUnreadNotifications(userId: String): Flow<List<Notification>> {
        return firebaseDataManager.firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .asFlow()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    mapToNotification(doc.data.orEmpty())
                }
            }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true, "readAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val batch = firebaseDataManager.firestore.batch()
            val snapshots = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            snapshots.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "isRead" to true,
                    "readAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotificationPreferences(userId: String): Flow<NotificationPreferences> {
        return firebaseDataManager.firestore.collection("users")
            .document(userId)
            .get()
            .asFlow()
            .map { document ->
                val prefsMap = document.get("notificationPreferences") as? Map<*, *> ?: emptyMap<String, Any>()
                mapToNotificationPreferences(prefsMap)
            }
    }

    override suspend fun updateNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Result<Unit> {
        return try {
            firebaseDataManager.firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "notificationPreferences" to mapOf(
                            "pushEnabled" to preferences.pushEnabled,
                            "emailEnabled" to preferences.emailEnabled,
                            "transactionAlerts" to preferences.transactionAlerts,
                            "lowBalanceAlerts" to preferences.lowBalanceAlerts,
                            "transferAlerts" to preferences.transferAlerts,
                            "promotionalNotifications" to preferences.promotionalNotifications,
                            "quietHours" to preferences.quietHours?.let {
                                mapOf(
                                    "enabled" to it.enabled,
                                    "startTime" to it.startTime,
                                    "endTime" to it.endTime
                                )
                            }
                        ),
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveNotification(notification: Notification): Result<Unit> {
        return try {
            notificationsCollection.document(notification.id)
                .set(
                    mapOf(
                        "id" to notification.id,
                        "userId" to notification.userId,
                        "title" to notification.title,
                        "body" to notification.body,
                        "type" to notification.type.name,
                        "data" to notification.data,
                        "isRead" to notification.isRead,
                        "timestamp" to notification.timestamp,
                        "imageUrl" to notification.imageUrl,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return firebaseDataManager.firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.size ?: 0
            }
            .asFlow()
            .map { it as Int }
    }

    private fun mapToNotification(data: Map<String, Any>): Notification {
        val typeStr = data["type"] as? String ?: "INFO"
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (e: Exception) {
            NotificationType.INFO
        }

        return Notification(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            body = data["body"] as? String ?: "",
            type = type,
            data = data["data"] as? Map<String, String> ?: emptyMap(),
            isRead = data["isRead"] as? Boolean ?: false,
            timestamp = data["timestamp"] as? Date ?: Date(),
            imageUrl = data["imageUrl"] as? String
        )
    }

    private fun mapToNotificationPreferences(data: Map<*, *>): NotificationPreferences {
        val quietHoursMap = data["quietHours"] as? Map<*, *>

        return NotificationPreferences(
            pushEnabled = data["pushEnabled"] as? Boolean ?: true,
            emailEnabled = data["emailEnabled"] as? Boolean ?: true,
            transactionAlerts = data["transactionAlerts"] as? Boolean ?: true,
            lowBalanceAlerts = data["lowBalanceAlerts"] as? Boolean ?: true,
            transferAlerts = data["transferAlerts"] as? Boolean ?: true,
            promotionalNotifications = data["promotionalNotifications"] as? Boolean ?: false,
            quietHours = quietHoursMap?.let {
                NotificationPreferences.QuietHours(
                    enabled = it["enabled"] as? Boolean ?: false,
                    startTime = it["startTime"] as? String ?: "22:00",
                    endTime = it["endTime"] as? String ?: "08:00"
                )
            }
        )
    }
}

// Extension function to convert Task to Flow (simplified)
private fun <T> com.google.android.gms.tasks.Task<T>.asFlow(): Flow<T> {
    // Implementation using kotlinx-coroutines-play-services
    // This is a placeholder - actual implementation would use Tasks.await()
    TODO("Implement Task to Flow conversion")
}
```

---

### 2.3 Créer le ViewModel des notifications

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/ui/notifications/viewmodel/NotificationViewModel.kt
```

**Code:**
```kotlin
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
    } ?: MutableStateFlow(emptyList())

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
        // La liste est déjà réactive via Flow, mais peut forcer un rechargement si nécessaire
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
```

---

### 2.4 Créer l'écran des notifications

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/ui/notifications/NotificationScreen.kt
```

**Code:**
```kotlin
package com.example.aureus.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.aureus.domain.model.Notification as NotificationModel
import com.example.aureus.domain.model.NotificationType
import com.example.aureus.ui.notifications.viewmodel.NotificationViewModel
import com.example.aureus.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                unreadCount = unreadCount,
                onNavigateBack = onNavigateBack,
                onMarkAllAsRead = {
                    if (unreadCount > 0) {
                        viewModel.markAllAsRead()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
        ) {
            when (uiState) {
                is NotificationViewModel.NotificationUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SecondaryGold
                    )
                }
                is NotificationViewModel.NotificationUiState.Error -> {
                    ErrorState((uiState as NotificationViewModel.NotificationUiState.Error).message) {
                        viewModel.refreshNotifications()
                    }
                }
                else -> {
                    if (notifications.isEmpty()) {
                        EmptyState()
                    } else {
                        NotificationList(
                            notifications = notifications,
                            onNotificationClick = { notification ->
                                viewModel.markAsRead(notification.id)
                            },
                            onNotificationDelete = { notificationId ->
                                viewModel.deleteNotification(notificationId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onNavigateBack: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (unreadCount > 0) {
                    Badge(
                        containerColor = SemanticRed,
                        modifier = Modifier.offset(y = (-4).dp)
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            if (unreadCount > 0) {
                TextButton(onClick = onMarkAllAsRead) {
                    Text(
                        text = "Tout marquer comme lu",
                        fontSize = 14.sp,
                        color = SecondaryGold
                    )
                }
            }
            IconButton(onClick = { /* Filtres */ }) {
                Icon(Icons.Default.FilterList, "Filter")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
    )
}

@Composable
private fun NotificationList(
    notifications: List<NotificationModel>,
    onNotificationClick: (NotificationModel) -> Unit,
    onNotificationDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onDelete = { onNotificationDelete(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItem(
    notification: NotificationModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val backgroundColor = if (!notification.isRead) {
        SecondaryGold.copy(alpha = 0.1f)
    } else {
        NeutralWhite
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title + Timestamp + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Notification Icon
                    NotificationTypeIcon(
                        type = notification.type,
                        isRead = notification.isRead
                    )

                    // Title and Body
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = PrimaryNavyBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notification.body,
                            fontSize = 14.sp,
                            color = if (notification.isRead) {
                                NeutralMediumGray
                            } else {
                                PrimaryNavyBlue.copy(alpha = 0.8f)
                            },
                            lineHeight = 18.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = NeutralMediumGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Timestamp and Expand button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(notification.timestamp),
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )

                if (notification.body.length > 100) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Voir moins" else "Voir plus",
                            fontSize = 12.sp,
                            color = SecondaryGold
                        )
                    }
                }
            }

            // Image if available
            notification.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Notification image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la notification") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette notification ?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Supprimer", color = SemanticRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    isRead: Boolean
) {
    val (icon, color) = when (type) {
        NotificationType.TRANSACTION -> Icons.Default.ShoppingCart to SecondaryGold
        NotificationType.TRANSFER_RECEIVED -> Icons.Default.ArrowDownward to android.graphics.Color.parseColor("#4CAF50")
        NotificationType.TRANSFER_SENT -> Icons.Default.ArrowUpward to SemanticRed
        NotificationType.BALANCE_ALERT -> Icons.Default.AccountBalance to Android.graphics.Color.parseColor("#FF9800")
        NotificationType.SECURITY_ALERT -> Icons.Default.Security to android.graphics.Color.parseColor("#9C27B0")
        NotificationType.PROMOTION -> Icons.Default.LocalOffer to android.graphics.Color.parseColor("#2196F3")
        NotificationType.INFO -> Icons.Default.Info to SecondaryGold
        NotificationType.SYSTEM -> Icons.Default.Settings to NeutralMediumGray
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isRead) color.copy(alpha = 0.2f) else color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = NeutralMediumGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucune notification",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralMediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vous serez notifié ici des nouvelles transactions et alertes",
            fontSize = 14.sp,
            color = NeutralMediumGray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SemanticRed.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Erreur de chargement",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralMediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = NeutralMediumGray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
        ) {
            Text("Réessayer")
        }
    }
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "À l'instant"
        minutes < 60 -> "Il y a $minutes min"
        hours < 24 -> "Il y a $hours h"
        days < 2 -> "Hier"
        days < 7 -> "Il y a $days jours"
        else -> SimpleDateFormat("dd MMM", Locale.FRENCH).format(timestamp)
    }
}
```

---

### 2.5 Ajouter la route et la navigation

**Fichier à modifier:**
```
app/src/main/java/com/example/aureus/ui/navigation/Navigation.kt
```

**Ajouter dans la classe Screen (après ligne 42):**
```kotlin
object Notifications : Screen("notifications")
```

**Ajouter dans la fonction NotificationScreen (dans MainScreen.kt - après onNavigateToProfile):**
```kotlin
onNavigateToNotifications: () -> Unit = {},
```

**Modifier HomeScreen - Ajouter badge de notification dans le header:**
```kotlin
// Dans HomeHeaderDemo, ajouter un bouton notifications
val unreadCount by viewModel.unreadCount.collectAsState(initial = 0)

Box(modifier = Modifier.size(40.dp)) {
    IconButton(onClick = onNavigateToNotifications) {
        Icon(Icons.Default.Notifications, "Notifications")
    }
    if (unreadCount > 0) {
        Badge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp),
            containerColor = SemanticRed
        ) {
            Text(
                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
```

**Ajouter la route dans AppNavigation:**
```kotlin
// Notifications Screen
composable(Screen.Notifications.route) {
    NotificationScreen(
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}
```

---

## 🟡 PHASE 3: PERMISSION POST_NOTIFICATIONS (Android 13+)

### 3.1 Ajouter l'écran de demande de permission

**Fichier à créer:**
```
app/src/main/java/com/example/aureus/ui/components/NotificationPermissionRequest.kt
```

**Code:**
```kotlin
package com.example.aureus.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.aureus.ui.theme.SemanticRed
import com.example.aureus.ui.theme.SecondaryGold

@Composable
fun NotificationPermissionRequest(
    onPermissionGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Permission launcher for Android 13+
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onPermissionGranted()
        }
    }

    // Auto-check on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onPermissionGranted()
            }
        } else {
            onPermissionGranted()
        }
    }

    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = SemanticRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Activer les notifications",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recevez des alertes en temps réel pour vos:",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionItem(text = "✓ Nouvelles transactions")
                    PermissionItem(text = "✓ Alertes de solde")
                    PermissionItem(text = "✓ Transferts reçus/envoyés")
                    PermissionItem(text = "✓ Alertes de sécurité")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                ) {
                    Text("Activer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pas maintenant", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PermissionItem(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
fun checkNotificationPermission(): Boolean {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
```

**Modifier `SplashScreenAdvanced.kt` pour demander la permission:**
```kotlin
// Dans la SplashScreenAdvanced, ajouter:
var showPermissionRequest by remember { mutableStateOf(true) }

if (showPermissionRequest) {
    NotificationPermissionRequest(
        onPermissionGranted = {
            showPermissionRequest = false
        },
        onDismiss = {
            showPermissionRequest = false
            // Continuer sans permission
        }
    )
}
```

---

### 3.2 Modifier FirebaseMessagingService pour sauvegarder les notifications

**Fichier à modifier:**
```
app/src/main/java/com/example/aureus/notification/FirebaseMessagingService.kt
```

**Dans la méthode `onMessageReceived`, ajouter la sauvegarde dans Firestore:**
```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

    // ✅ SAUVEGARDER la notification dans Firestore pour l'historique
    saveNotificationToFirestore(remoteMessage)

    // Check if message contains a notification payload
    remoteMessage.notification?.let {
        Log.d(TAG, "Message Notification Body: ${it.body}")
        sendNotification(
            title = it.title ?: "Aureus Banking",
            body = it.body ?: "New notification",
            data = remoteMessage.data
        )
    }

    // Check if message contains data payload
    if (remoteMessage.data.isNotEmpty()) {
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        handleDataMessage(remoteMessage.data)
    }
}

/**
 * ✅ NOUVEAU: Sauvegarder notification dans Firestore
 */
private fun saveNotificationToFirestore(remoteMessage: RemoteMessage) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val notificationId = "notif_${System.currentTimeMillis()}"

    val notificationType = when {
        remoteMessage.data["type"] == "transaction" -> "TRANSACTION"
        remoteMessage.data["type"] == "transfer" -> {
            if (remoteMessage.data["direction"] == "received") {
                "TRANSFER_RECEIVED"
            } else {
                "TRANSFER_SENT"
            }
        }
        remoteMessage.data["type"] == "balance_alert" -> "BALANCE_ALERT"
        else -> "INFO"
    }

    val notificationData = mapOf(
        "id" to notificationId,
        "userId" to userId,
        "title" to (remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Aureus"),
        "body" to (remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""),
        "type" to notificationType,
        "data" to remoteMessage.data,
        "isRead" to false,
        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "imageUrl" to remoteMessage.data["imageUrl"],
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )

    firebase.firestore.collection("notifications")
        .document(notificationId)
        .set(notificationData)
        .addOnSuccessListener {
            Log.d(TAG, "Notification saved to Firestore: $notificationId")
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Failed to save notification to Firestore", e)
        }
}

// Ajouter firebase comme dépendance:
private val firebase = com.google.firebase.Firebase.getInstance()
```

---

## 🟡 PHASE 4: AMÉLIORATIONS NAVIGATION ET ICONES

### 4.1 Créer une icône de notification appropriée

**Fichier à créer:**
```
app/src/main/res/drawable/ic_notification_small.xml
```

**Code:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <!-- Icône blanche avec transparence (canal alpha uniquement) -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/>
</vector>
```

**Modifier FirebaseMessagingService pour utiliser la nouvelle icône:**
```kotlin
.setSmallIcon(R.drawable.ic_notification_small)  // ✅ Nouvelle icône
```

---

### 4.2 Implémenter Deep Links depuis les notifications

**Modifier FirebaseMessagingService:**
```kotlin
private fun sendNotification(title: String, body: String, data: Map<String, String>) {
    // ✅ Extraire le type pour le deep link
    val destination = data["destination"] ?: "home"

    // Deep link pour navigation
    val deepLink = when (destination) {
        "transactions" -> "aureus://transactions"
        "dashboard" -> "aureus://home"
        "cards" -> "aureus://cards"
        else -> "aureus://home"
    }

    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(deepLink)).apply {
        setPackage(packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(Bundle().apply {
            data.forEach { (key, value) ->
                putString(key, value)
            }
        })
    }

    val pendingIntent = PendingIntent.getActivity(
        this,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ... reste du code inchangé
}
```

**Modifier MainActivity pour traiter les deep links:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ... code existant

    // ✅ Traiter les extras des notifications
    handleNotificationIntent(intent)
}

private fun handleNotificationIntent(intent: Intent) {
    val data = intent.extras
    if (data != null) {
        val notificationType = data.getString("type")
        val navigationData = data.getString("navigate_to")

        Log.d("MainActivity", "Notification data: $notificationType, navigate_to: $navigationData")

        // Émettre un événement pour que Navigation.kt puisse traiter
        viewModel.handleNotificationData(navigationData)
    }
}
```

---

## 🟡 PHASE 5: INJECTION DE DÉPENDANCES

### 5.1 Ajouter le repository dans `AppModule.kt`

**Fichier à modifier:**
```
app/src/main/java/com/example/aureus/di/AppModule.kt
```

**Ajouter les imports:**
```kotlin
import com.example.aureus.data.repository.NotificationRepositoryImpl
import com.example.aureus.domain.repository.NotificationRepository
```

**Ajouter la méthode provider (après les autres providers):**
```kotlin
@Provides
@Singleton
fun provideNotificationRepository(
    firebaseDataManager: FirebaseDataManager
): NotificationRepository {
    return NotificationRepositoryImpl(firebaseDataManager)
}
```

**Ajouter dans ViewModelModule.kt:**
```kotlin
import com.example.aureus.ui.notifications.viewmodel.NotificationViewModel

@Provides
fun provideNotificationViewModel(
    notificationRepository: NotificationRepository,
    authManager: FirebaseAuthManager
): NotificationViewModel {
    return NotificationViewModel(notificationRepository, authManager)
}
```

---

### 5.2 Ajouter NotificationViewModel dans HomeViewModel

**Fichier à modifier:**
```
app/src/main/java/com/example/aureus/ui/home/viewmodel/HomeViewModel.kt
```

**Ajouter la dépendance et le Flow:**
```kotlin
@Inject
private lateinit var notificationRepository: NotificationRepository

val unreadCount: StateFlow<Int> = authManager.currentUser?.uid?.let { userId ->
    notificationRepository.getUnreadCount(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
} ?: MutableStateFlow(0)
```

---

## 🟢 PHASE 6: AMÉLIORATIONS OPTIONNELLES (Futur)

### 6.1 Actions sur les notifications

Ajouter des boutons d'action inline (marquer comme lu, supprimer) sans ouvrir l'app.

### 6.2 Groupement de notifications

Implémenter le groupement des notifications similaires (ex: "3 nouvelles transactions").

### 6.3 Notifications locales

Ajouter des notifications programmées:
- Rappels de factures
- Alertes de budget
- Notifications in-app

### 6.4 Notifications riches

- Notifications avec grandes images
- Notifications avec boutons d'action (Ouvrer, Ignorer)
- Notifications avec média (audio, vidéo)

### 6.5 Analytics

- Suivre les taux d'ouverture des notifications
- A/B testing des messages
- Personnalisation des notifications

### 6.6 Heures silencieuses

- Respecter les préférences de "Quiet Hours"
- Ne pas envoyer de notifications entre 22h et 8h (configurable)

---

## ✅ CHECKLIST DE VALIDATION

### Phase 1 - Corrections Critiques
- [ ] Supprimer `MyFirebaseMessagingService.kt`
- [ ] Unifier les IDs de canaux de notification
- [ ] Vérifier que les notifications s'affichent correctement

### Phase 2 - UI des Notifications
- [ ] Créer `Notification.kt` (domain model)
- [ ] Créer `NotificationRepository.kt` et `NotificationRepositoryImpl.kt`
- [ ] Créer `NotificationViewModel.kt`
- [ ] Créer `NotificationScreen.kt`
- [ ] Ajouter la route dans `Navigation.kt`
- [ ] Ajouter le badge dans `HomeScreen.kt`
- [ ] Créer la collection Firestore "notifications"

### Phase 3 - Permission Android 13+
- [ ] Créer `NotificationPermissionRequest.kt`
- [ ] Modifier `SplashScreenAdvanced.kt`
- [ ] Modifier `FirebaseMessagingService.kt` pour sauvegarder les notifs

### Phase 4 - Deep Links et Icônes
- [ ] Créer `ic_notification_small.xml`
- [ ] Modifier `FirebaseMessagingService.kt` pour utiliser la nouvelle icône
- [ ] Implémenter les deep links
- [ ] Modifier `MainActivity.kt` pour traiter les extras

### Phase 5 - Injection de Dépendances
- [ ] Modifier `AppModule.kt`
- [ ] Modifier `ViewModelModule.kt`
- [ ] Modifier `HomeViewModel.kt`

### Phase 6 - Tests
- [ ] Tests unitaires du repository
- [ ] Tests d'intégration avec Firestore
- [ ] Tests UI avec Jetpack Compose Testing
- [ ] Tests FCM (envoi réel de notifications)

---

## 📝 NOTES IMPORTANTES

### Firestore Rules
Ajouter ces règles dans `firestore.rules`:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Notifications rules
    match /notifications/{notificationId} {
      allow read, write: if request.auth != null
      && request.auth.uid == resource.data.userId;
    }

    // Users can read their notification preferences
    match /users/{userId} {
      allow read: if request.auth != null
      && request.auth.uid == userId;
      allow write: if request.auth != null
      && request.auth.uid == userId;
    }
  }
}
```

### Firebase Cloud Functions
Créer une Cloud Function pour filtrer les notifications selon les préférences:

```javascript
// functions/index.js - Notification Preferences Filter
exports.sendFilteredNotification = functions.firestore
  .document('users/{userId}')
  .onWrite(async (change, context) => {
    const user = change.after.data();
    const preferences = user.notificationPreferences || {};

    // Envoyer notification seulement si pushEnabled = true
    if (preferences.pushEnabled === false) {
      return null;
    }

    // Filtrer selon le type
    // ... implémentation complète

    // Envoyer via FCM
    const message = {
      token: user.fcmToken,
      notification: {
        title: 'Titre',
        body: 'Corps'
      }
    };

    return admin.messaging().send(message);
  });
```

### Indexes Firestore
Créer des indexes pour les notifications:
```json
{
  "indexes": [
    {
      "collectionGroup": "notifications",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "userId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "timestamp",
          "order": "DESCENDING"
        }
      ]
    }
  ]
}
```

---

## 🎯 ESTIMATION DU TEMPS

| Phase | Temps estimé | Priorité |
|-------|-------------|----------|
| Phase 1 - Corrections Critiques | 1-2 heures | 🔴 URGENT |
| Phase 2 - UI des Notifications | 4-6 heures | 🔴 URGENT |
| Phase 3 - Permission Android 13+ | 2-3 heures | 🟡 IMPORTANT |
| Phase 4 - Deep Links et Icônes | 2-3 heures | 🟡 IMPORTANT |
| Phase 5 - Injection de Dépendances | 1-2 heures | 🟡 IMPORTANT |
| Phase 6 - Tests | 3-4 heures | 🟢 RECOMMANDÉ |
| **TOTAL** | **13-20 heures** | |

---

## 📚 RESSOURCES UTILES

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Android Notifications Guide](https://developer.android.com/develop/ui/views/notifications)
- [Jetpack Compose Notifications](https://developer.android.com/jetpack/compose/layouts/material/components)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/rules)

---

**Fin du Plan de Correction**
**Date de création:** 12 Janvier 2026
**Prochaine étape:** Commencer par la Phase 1 - Correction des IDs de canaux et suppression du service en double