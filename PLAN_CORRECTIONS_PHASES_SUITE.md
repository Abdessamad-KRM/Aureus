# 🚀 PLAN DE CORRECTIONS - SUITE (PHASE 7-10)
**Date**: 11 Janvier 2026
**Basé sur**: PLAN_CORRECTIONS_PHASES.md (Phases 1-6)
**Objectif**: Finaliser l'app avec features avancées et atteindre 10/10

---

## 📊 SITUATION ACTUELLE APRÈS PHASES 1-6

Après avoir complété les phases 1-6, l'app devrait être:
- ✅ **100% fonctionnelle** pour authentification
- ✅ **100% dynamique** avec Firebase
- ✅ **Score**: 9.5/10
- ⚠️ **Manque**: Offline-First, notifications, biometrics, charts pro

---

## 🌐 PHASE 7: OFFLINE-FIRST COMPLET (4-5 jours)

### Objectif
Implémenter une synchronisation bidirectionnelle Firestore ↔ Room pour que l'app fonctionne parfaitement sans internet

---

#### ÉTAPE 7.1: Améliorer AppDatabase

**Fichier**: `app/src/main/java/com/example/aureus/data/local/AppDatabase.kt`

**Ajouter méthodes de synchronisation**:

```kotlin
@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        BankCardEntity::class,
        ContactEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao
    abstract fun contactDao(): ContactDao
    
    // Méthodes de synchronisation
    suspend fun syncWithFirebase(firebaseDataManager: FirebaseDataManager, userId: String) {
        // Sync transactions
        syncTransactions(firebaseDataManager, userId)
        
        // Sync cards
        syncCards(firebaseDataManager, userId)
        
        // Sync contacts
        syncContacts(firebaseDataManager, userId)
    }
    
    private suspend fun syncTransactions(firebaseDataManager: FirebaseDataManager, userId: String) {
        // 1. Get transactions from Firestore
        val firebaseTransactions = firebaseDataManager.getUserTransactions(userId).first()
        
        // 2. Upsert to Room
        firebaseTransactions.forEach { transactionMap ->
            val transaction = TransactionEntity(
                transactionId = transactionMap["transactionId"] as? String ?: "",
                userId = transactionMap["userId"] as? String ?: "",
                type = transactionMap["type"] as? String ?: "",
                category = transactionMap["category"] as? String ?: "",
                title = transactionMap["title"] as? String ?: "",
                description = transactionMap["description"] as? String ?: "",
                amount = transactionMap["amount"] as? Double ?: 0.0,
                merchant = transactionMap["merchant"] as? String,
                recipientName = transactionMap["recipientName"] as? String,
                status = transactionMap["status"] as? String ?: "COMPLETED",
                createdAt = transactionDao().formatTimestamp(transactionMap["createdAt"]),
                updatedAt = transactionDao().formatTimestamp(transactionMap["updatedAt"])
            )
            transactionDao().upsert(transaction)
        }
    }
    
    private suspend fun syncCards(firebaseDataManager: FirebaseDataManager, userId: String) {
        val firebaseCards = firebaseDataManager.getUserCards(userId).first()
        
        firebaseCards.forEach { cardMap ->
            val card = BankCardEntity(
                id = cardMap["cardId"] as? String ?: "",
                userId = cardMap["userId"] as? String ?: "",
                accountId = cardMap["accountId"] as? String ?: "",
                cardNumber = cardMap["cardNumber"] as? String ?: "",
                cardHolder = cardMap["cardHolder"] as? String ?: "",
                expiryDate = cardMap["expiryDate"] as? String ?: "",
                cardType = cardMap["cardType"] as? String ?: "VISA",
                cardColor = cardMap["cardColor"] as? String ?: "navy",
                isDefault = cardMap["isDefault"] as? Boolean ?: false,
                isActive = cardMap["isActive"] as? Boolean ?: true,
                dailyLimit = cardMap["dailyLimit"] as? Double ?: 10000.0,
                monthlyLimit = cardMap["monthlyLimit"] as? Double ?: 50000.0,
                createdAt = cardMap["createdAt"].toString(),
                updatedAt = cardMap["updatedAt"].toString()
            )
            cardDao().upsert(card)
        }
    }
    
    private suspend fun syncContacts(firebaseDataManager: FirebaseDataManager, userId: String) {
        val firebaseContacts = firebaseDataManager.getUserContacts(userId).first()
        
        firebaseContacts.forEach { contactMap ->
            val contact = ContactEntity(
                id = contactMap["id"] as? String ?: "",
                userId = userId,
                name = contactMap["name"] as? String ?: "",
                phone = contactMap["phone"] as? String ?: "",
                email = contactMap["email"] as? String,
                accountNumber = contactMap["accountNumber"] as? String,
                isFavorite = contactMap["isFavorite"] as? Boolean ?: false,
                isBankContact = contactMap["isBankContact"] as? Boolean ?: false,
                category = contactMap["category"] as? String,
                createdAt = contactMap["createdAt"].toString(),
                updatedAt = contactMap["updatedAt"].toString()
            )
            contactDao().upsert(contact)
        }
    }
}
```

**Ajouter méthode à TransactionDao**:

```kotlin
// Upert (insert or update)
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsert(transaction: TransactionEntity)

// Get unsynced transactions
@Query("SELECT * FROM transactions WHERE synced = 0")
suspend fun getUnsyncedTransactions(): List<TransactionEntity>

// Mark as synced
@Query("UPDATE transactions SET synced = 1 WHERE transactionId = :transactionId")
suspend fun markAsSynced(transactionId: String)

// Helper pour formater Timestamp
fun formatTimestamp(timestamp: Any?): Long {
    return when (timestamp) {
        is com.google.firebase.Timestamp -> timestamp.toDate().time
        is java.util.Date -> timestamp.time
        else -> System.currentTimeMillis()
    }
}
```

---

#### ÉTAPE 7.2: Créer OfflineSyncManager Complet

**Fichier**: `app/src/main/java/com/example/aureus/data/offline/OfflineSyncManager.kt`

**Implémentation complète**:

```kotlin
package com.example.aureus.data.offline

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline Sync Manager
 * Gère la synchronisation automatique Firestore ↔ Room
 */
@Singleton
class OfflineSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val firebaseDataManager: FirebaseDataManager,
    private val networkMonitor: NetworkMonitor,
    private val auth: FirebaseAuth
) {

    private val syncScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val SYNC_WORK_NAME = "firebase_sync_work"
        private const val TAG = "OfflineSyncManager"
    }

    /**
     * Initialiser la synchronisation automatique
     */
    fun initializeAutoSync() {
        // Sync toutes les 15 minutes quand connecté
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder(
            FirebaseSyncWorker::class.java,
            15, // repeat interval (minutes)
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30_000, // 30 seconds
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        
        Log.d(TAG, "Auto sync initialized")
    }

    /**
     * Synchroniser immédiatement (manual sync)
     */
    suspend fun syncNow(): SyncResult = withContext(Dispatchers.IO) {
        try {
            if (!auth.currentUser) {
                return@withContext SyncResult.Error("User not logged in")
            }
            
            if (!networkMonitor.isConnected()) {
                return@withContext SyncResult.Error("No internet connection")
            }
            
            val userId = auth.currentUser?.uid ?: return@withContext SyncResult.Error("User ID null")
            
            Log.d(TAG, "Starting manual sync for user: $userId")
            
            // Sync transactions
            syncTransactions(userId)
            
            // Sync cards
            syncCards(userId)
            
            // Sync contacts
            syncContacts(userId)
            
            // Upload unsynced changes
            uploadPendingChanges(userId)
            
            Log.d(TAG, "Manual sync completed successfully")
            return@withContext SyncResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync failed", e)
            return@withContext SyncResult.Error(e.message ?: "Sync failed")
        }
    }

    /**
     * Synchroniser les transactions
     */
    private suspend fun syncTransactions(userId: String) {
        // Download from Firestore
        val firebaseTransactions = firebaseDataManager.getUserTransactions(userId, limit = 100).first()
        
        // Upsert to Room
        firebaseTransactions.forEach { transactionMap ->
            val timestamp = transactionMap["createdAt"]
            val createdAt = when (timestamp) {
                is com.google.firebase.Timestamp -> timestamp.toDate().time
                is java.util.Date -> timestamp.time
                else -> System.currentTimeMillis()
            }
            
            val transaction = com.example.aureus.data.local.entity.TransactionEntity(
                transactionId = transactionMap["transactionId"] as? String ?: "",
                userId = userId,
                type = transactionMap["type"] as? String ?: "EXPENSE",
                category = transactionMap["category"] as? String ?: "OTHER",
                title = transactionMap["title"] as? String ?: "Transaction",
                description = transactionMap["description"] as? String ?: "",
                amount = transactionMap["amount"] as? Double ?: 0.0,
                merchant = transactionMap["merchant"] as? String,
                recipientName = transactionMap["recipientName"] as? String,
                status = transactionMap["status"] as? String ?: "COMPLETED",
                cardId = transactionMap["cardId"] as? String,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis(),
                synced = true
            )
            database.transactionDao().upsert(transaction)
        }
        
        Log.d(TAG, "Synced ${firebaseTransactions.size} transactions")
    }

    /**
     * Synchroniser les cartes
     */
    private suspend fun syncCards(userId: String) {
        val firebaseCards = firebaseDataManager.getUserCards(userId).first()
        
        firebaseCards.forEach { cardMap ->
            val card = com.example.aureus.data.local.entity.BankCardEntity(
                id = cardMap["cardId"] as? String ?: "",
                userId = userId,
                accountId = cardMap["accountId"] as? String ?: "",
                cardNumber = cardMap["cardNumber"] as? String ?: "",
                cardHolder = cardMap["cardHolder"] as? String ?: "",
                expiryDate = cardMap["expiryDate"] as? String ?: "",
                cardType = cardMap["cardType"] as? String ?: "VISA",
                cardColor = cardMap["cardColor"] as? String ?: "navy",
                isDefault = cardMap["isDefault"] as? Boolean ?: false,
                isActive = cardMap["isActive"] as? Boolean ?: true,
                dailyLimit = cardMap["dailyLimit"] as? Double ?: 10000.0,
                monthlyLimit = cardMap["monthlyLimit"] as? Double ?: 50000.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            database.cardDao().upsert(card)
        }
        
        Log.d(TAG, "Synced ${firebaseCards.size} cards")
    }

    /**
     * Synchroniser les contacts
     */
    private suspend fun syncContacts(userId: String) {
        val firebaseContacts = firebaseDataManager.getUserContacts(userId).first()
        
        firebaseContacts.forEach { contactMap ->
            val contact = com.example.aureus.data.local.entity.ContactEntity(
                id = contactMap["id"] as? String ?: "",
                userId = userId,
                name = contactMap["name"] as? String ?: "",
                phone = contactMap["phone"] as? String ?: "",
                email = contactMap["email"] as? String,
                accountNumber = contactMap["accountNumber"] as? String,
                isFavorite = contactMap["isFavorite"] as? Boolean ?: false,
                isBankContact = contactMap["isBankContact"] as? Boolean ?: false,
                category = contactMap["category"] as? String,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            database.contactDao().upsert(contact)
        }
        
        Log.d(TAG, "Synced ${firebaseContacts.size} contacts")
    }

    /**
     * Uploader les changements en attente (conflit resolution)
     */
    private suspend fun uploadPendingChanges(userId: String) {
        // Récupérer les transactions non synchronisées
        // (à implémenter avec mécanisme de conflit)
        Log.d(TAG, "Uploading pending changes")
    }
    
    /**
     * Obtenir le statut de synchronisation
     */
    suspend fun getSyncStatus(): SyncStatus {
        val isOnline = networkMonitor.isConnected()
        val lastSync = getLastSyncTimestamp()
        val pendingChanges = getPendingChangesCount()
        
        return SyncStatus(
            isOnline = isOnline,
            lastSync = lastSync,
            pendingChanges = pendingChanges,
            isSyncing = false
        )
    }
    
    private suspend fun getLastSyncTimestamp(): Long? {
        // Implémenter avec SharedPreferences
        return null
    }
    
    private suspend fun getPendingChangesCount(): Int {
        // Compter les modifications non synchronisées
        return 0
    }
}

/**
 * Résultat de synchronisation
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}

/**
 * Statut de synchronisation
 */
data class SyncStatus(
    val isOnline: Boolean,
    val lastSync: Long?,
    val pendingChanges: Int,
    val isSyncing: Boolean
)
```

---

#### ÉTAPE 7.3: Créer FirebaseSyncWorker

**Fichier**: `app/src/main/java/com/example/aureus/data/offline/FirebaseSyncWorker.kt`

```kotlin
package com.example.aureus.data.offline

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Worker pour la synchronisation automatique Firebase
 */
@HiltWorker
class FirebaseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlineSyncManager: OfflineSyncManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "FirebaseSyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting background sync")
            
            val syncResult = offlineSyncManager.syncNow()
            
            when (syncResult) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Background sync successful")
                    Result.success()
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Background sync failed: ${syncResult.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker crashed", e)
            Result.failure()
        }
    }
}
```

---

#### ÉTAPE 7.4: Modifier ViewModels pour Offline-First

**Exemple: HomeViewModel**

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val offlineSyncManager: OfflineSyncManager
) : ViewModel() {
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Monitor sync status
            while (true) {
                _syncStatus.value = offlineSyncManager.getSyncStatus()
                kotlinx.coroutines.delay(5000)
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            // Try Firebase first, fallback to Room
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val userId = firebaseDataManager.currentUserId()
                if (userId != null) {
                    loadUserData(userId)
                }
            } else {
                loadFromOfflineCache()
            }
        }
    }
    
    private fun loadFromOfflineCache() {
        // Load data from Room database
        viewModelScope.launch {
            val transactions = database.transactionDao().getAllTransactions()
            val cards = database.cardDao().getAllCards()
            
            // Update UI state with offline data
        }
    }
}
```

---

#### ÉTAPE 7.5: Test Offline-First

**Test Scenarios**:

1. **Mode Online → Offline**:
   - Ouvrir app avec internet
   - Charger transactions, cartes, contacts
   - Couper internet
   - Naviguer dans l'app
   - Vérifier: Données toujours accessibles (depuis Room)

2. **Mode Offline → Online**:
   - Ouvrir app sans internet
   - Voir les données en cache
   - Connecter internet
   - Vérifier: Synchronisation automatique déclenchée

3. **Mode Offline Modifications**:
   - Sans internet, ajouter une transaction
   - Se reconnecter
   - Vérifier: Transaction uploadée sur Firebase

4. **Conflit Resolution**:
   - Modifier même donnée offline et online
   - Se reconnecter
   - Vérifier: Conflit géré correctement

---

## 🔔 PHASE 8: NOTIFICATIONS PUSH (3-4 jours)

### Objectif
Implémenter Firebase Cloud Messaging pour les notifications de transaction et alertes

---

#### ÉTAPE 8.1: Configuration FCM

**Ajouter dans build.gradle.kts (app)**:

```kotlin
dependencies {
    // Existing dependencies...
    implementation("com.google.firebase:firebase-messaging:23.4.0")
}
```

**Configurer AndroidManifest.xml**:

```xml
<manifest>
    <application>
        <!-- FCM Service -->
        <service
            android:name=".service.FirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

---

#### ÉTAPE 8.2: Créer FirebaseMessagingService

**Fichier**: `app/src/main/java/com/example/aureus/service/FirebaseMessagingService.kt`

```kotlin
package com.example.aureus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aureus.MainActivity
import com.example.aureus.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Firebase Messaging Service
 * Gère les notifications push reçues
 */
@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val CHANNEL_ID = "aureus_notifications"
        private const val CHANNEL_NAME = "Aureus Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for Aureus Banking"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("FCM Message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "Aureus Banking",
                body = it.body ?: "New notification",
                data = remoteMessage.data
            )
        }

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("New FCM Token: $token")
        
        // Envoyer le token au backend (Firebase Firestore)
        sendTokenToServer(token)
    }

    /**
     * Envoyer la notification
     */
    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_data", data as HashMap<String, String>)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createNotificationChannel()

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    /**
     * Gérer message de type data
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "transaction" -> {
                // Nouvelle transaction
                notificationHelper.showTransactionNotification(
                    title = data["title"] ?: "New Transaction",
                    amount = data["amount"] ?: "0",
                    type = data["transaction_type"] ?: "EXPENSE"
                )
            }
            "balance_alert" -> {
                // Alert solde bas
                notificationHelper.showBalanceAlert(
                    currentBalance = data["balance"] ?: "0",
                    threshold = data["threshold"] ?: "1000"
                )
            }
            "transfer" -> {
                // Argent reçu/envoyé
                notificationHelper.showTransferNotification(
                    amount = data["amount"] ?: "0",
                    direction = data["direction"] ?: "received",
                    fromTo = data["from_to"] ?: "Someone"
                )
            }
        }
    }

    /**
     * Envoyer le token au serveur
     */
    private fun sendTokenToServer(token: String) {
        // Implémenter envoi du token vers Firebase Firestore
        Timber.d("Sending FCM token to server: $token")
        
        // TODO: Sauvegarder dans users/{userId}/fcmTokens/{token}
    }

    /**
     * Créer le canal de notification (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

---

#### ÉTAPE 8.3: Créer NotificationHelper

**Fichier**: `app/src/main/java/com/example/aureus/service/NotificationHelper.kt`

```kotlin
package com.example.aureus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aureus.MainActivity
import com.example.aureus.R
import com.example.aureus.ui.theme.SemanticGreen
import com.example.aureus.ui.theme.SemanticRed
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper pour les notifications
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_TRANSACTION_ID = "transactions"
        private const val CHANNEL_ALERT_ID = "alerts"
        private const val CHANNEL_TRANSFER_ID = "transfers"
        
        private const val NOTIFICATION_TRANSACTION = 1001
        private const val NOTIFICATION_BALANCE_ALERT = 1002
        private const val NOTIFICATION_TRANSFER = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Créer les canaux de notification
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal transactions
            val transactionChannel = NotificationChannel(
                CHANNEL_TRANSACTION_ID,
                "Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les nouvelles transactions"
            }

            // Canal alerts
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes importantes (solde bas, sécurité)"
                enableVibration(true)
                enableLights(true)
            }

            // Canal transferts
            val transferChannel = NotificationChannel(
                CHANNEL_TRANSFER_ID,
                "Transfers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les transferts reçus/envoyés"
            }

            notificationManager.createNotificationChannel(transactionChannel)
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(transferChannel)
        }
    }

    /**
     * Notification de transaction
     */
    fun showTransactionNotification(title: String, amount: String, type: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_TRANSACTION,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRANSACTION_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Amount: $amount MAD")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val color = if (type == "INCOME") SemanticGreen else SemanticRed
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_TRANSACTION, builder.build())
        }
    }

    /**
     * Alert solde bas
     */
    fun showBalanceAlert(currentBalance: String, threshold: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "dashboard")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_BALANCE_ALERT,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("⚠️ Low Balance Alert")
            .setContentText("Your balance ($currentBalance MAD) is below threshold ($threshold MAD)")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your balance is getting low. Consider adding funds to avoid transaction failures.")
            )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_BALANCE_ALERT, builder.build())
        }
    }

    /**
     * Notification de transfert
     */
    fun showTransferNotification(amount: String, direction: String, fromTo: String) {
        val title = if (direction == "received") {
            "💰 Money Received!"
        } else {
            "💸 Money Sent"
        }

        val body = if (direction == "received") {
            "You received $amount MAD from $fromTo"
        } else {
            "You sent $amount MAD to $fromTo"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "transactions")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_TRANSFER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRANSFER_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_TRANSFER, builder.build())
        }
    }
}
```

---

#### ÉTAPE 8.4: Ajouter NotificationManager au backend Firebase

**Créer Cloud Function pour Envoyer Notifications**:

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Notification sur nouvelle transaction
exports.sendTransactionNotification = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();
        
        if (!transaction) return null;

        const userId = transaction.userId;
        
        // Récupérer les tokens FCM de l'utilisateur
        const tokensSnapshot = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('fcmTokens')
            .get();
        
        const tokens = [];
        tokensSnapshot.forEach(doc => {
            tokens.push(doc.id);
        });
        
        if (tokens.length === 0) return null;

        // Préparer notification
        const message = {
            notification: {
                title: 'New Transaction',
                body: `${transaction.title}: ${transaction.amount} MAD`
            },
            data: {
                type: 'transaction',
                title: transaction.title,
                amount: transaction.amount.toString(),
                transaction_type: transaction.type
            },
            tokens: tokens
        };

        // Envoyer notification
        const response = await admin.messaging().sendMulticast(message);
        
        console.log(`${response.successCount} messages were sent successfully`);
        
        return null;
    });

// Alert solde bas
exports.checkBalanceAndSendAlert = functions.firestore
    .document('accounts/{accountId}')
    .onWrite(async (change, context) => {
        const newBalance = change.after.data().balance;
        
        // Si solde < 1000 MAD, envoyer alert
        if (newBalance < 1000 && newBalance > 0) {
            const userId = change.after.data().userId;
            
            // Récupérer tokens
            const tokensSnapshot = await admin.firestore()
                .collection('users')
                .doc(userId)
                .collection('fcmTokens')
                .get();
            
            const tokens = [];
            tokensSnapshot.forEach(doc => {
                tokens.push(doc.id);
            });
            
            if (tokens.length === 0) return null;
            
            const message = {
                notification: {
                    title: '⚠️ Low Balance Alert',
                    body: `Your balance is ${newBalance} MAD`
                },
                data: {
                    type: 'balance_alert',
                    balance: newBalance.toString(),
                    threshold: '1000'
                },
                tokens: tokens
            };
            
            await admin.messaging().sendMulticast(message);
        }
        
        return null;
    });
```

---

#### ÉTAPE 8.5: Test Notifications

**Test Scenarios**:

1. **FCM Token Registration**:
   - Installer app sur device
   - Vérifier logs: "New FCM Token: xxx"
   - Vérifier token sauvegardé dans Firestore

2. **Notification Transaction**:
   - Créer transaction via app
   - Cloud Function déclenchée
   - Notification reçue sur device

3. **Alert Solde Bas**:
   - Réduire solde sous 1000 MAD
   - Notification alert reçue

4. **Notification Directe**:
   - Envoyer depuis Firebase Console
   - Notification reçue

---

## 👆 PHASE 9: BIOMETRIC AUTHENTICATION (2-3 jours)

### Objectif
Ajouter FingerPrint et FaceID pour un accès rapide et sécurisé

---

#### ÉTAPE 9.1: Configuration Biometric

**Ajouter à build.gradle.kts (app)**:

```kotlin
dependencies {
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
}
```

---

#### ÉTAPE 9.2: Créer BiometricManager

**Fichier**: `app/src/main/java/com/example/aureus/security/BiometricManager.kt`

```kotlin
package com.example.aureus.security

import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.aureus.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère l'authentification biométrique (Fingerprint, FaceID)
 */
@Singleton
class BiometricManager @Inject constructor(
    private val context: Context
) {

    private val _authResult = MutableStateFlow<BiometricResult>(BiometricResult.Idle)
    val authResult: StateFlow<BiometricResult> = _authResult.asStateFlow()

    /**
     * Vérifier si l'appareil supporte la biométrie
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoCredentials
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.Unsupported
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.Unknown
            else -> BiometricAvailability.Unknown
        }
    }

    /**
     * Lancer l'authentification biométrique
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Aureus Banking",
        subtitle: String = "Use your fingerprint to continue",
        description: String = "Touch the sensor to verify your identity",
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit = {},
        onError: (error: String) -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        // Vérifier disponibilité
        val availability = isBiometricAvailable()
        if (availability != BiometricAvailability.Available) {
            _authResult.value = BiometricResult.Error("Biometric not available: ${availability.name}")
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(context)
            
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        _authResult.value = BiometricResult.Success
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        _authResult.value = BiometricResult.Failed
                        onFailure()
                    }

                    override fun onAuthenticationError errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        _authResult.value = BiometricResult.Error(errString.toString())
                        onError(errString.toString())
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            _authResult.value = BiometricResult.Error("Biometric error: ${e.message}")
        }
    }

    /**
     * Demander à l'utilisateur d'activer la biométrie
     */
    fun promptEnableBiometric(activity: FragmentActivity) {
        val intent = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }
}

sealed class BiometricResult {
    object Idle : BiometricResult()
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

enum class BiometricAvailability {
    Available,
    NoHardware,
    HardwareUnavailable,
    NoCredentials,
    SecurityUpdateRequired,
    Unsupported,
    Unknown
}
```

---

#### ÉTAPE 9.3: Créer BiometricLockScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/auth/screen/BiometricLockScreen.kt`

```kotlin
package com.example.aureus.ui.auth.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.security.BiometricManager
import com.example.aureus.security.BiometricResult
import com.example.aureus.ui.theme.*
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * Écran de verrouillage biométrique
 */
@Composable
fun BiometricLockScreen(
    biometricManager: BiometricManager,
    onUnlockSuccess: () -> Unit = {},
    onUsePin: () -> Unit = {},
    onEnableBiometric: () -> Unit = {}
) {
    val activity = LocalContext.current as? FragmentActivity
    val authResult by biometricManager.authResult.collectAsState()
    
    var showEnablePrompt by remember { mutableStateOf(false) }
    var autoAuthenticateTriggered by remember { mutableStateOf(false) }
    
    // Auto-authentifier quand écran ouvre
    LaunchedEffect(Unit) {
        if (!autoAuthenticateTriggered && activity != null) {
            autoAuthenticateTriggered = true
            kotlinx.coroutines.delay(500)
            
            biometricManager.authenticate(
                activity = activity,
                title = "Unlock Aureus Banking",
                subtitle = "Use your fingerprint to continue",
                description = "Touch the sensor to verify your identity",
                negativeButtonText = "Use PIN",
                onSuccess = { onUnlockSuccess() },
                onError = { error ->
                    if (error.contains("NoCredentials")) {
                        showEnablePrompt = true
                    }
                }
            )
        }
    }
    
    // Handle result
    LaunchedEffect(authResult) {
        when (authResult) {
            is BiometricResult.Success -> {
                // Success handled in onSuccess callback
            }
            is BiometricResult.Failed -> {
                // User cancelled or biometric failed, stay on screen
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        PrimaryNavyBlue,
                        PrimaryMediumBlue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Animated Fingerprint Icon
            AnimatedFingerprintIcon(
                isScanning = authResult == BiometricResult.Idle || authResult is BiometricResult.Failed
            )
            
            // Title
            Text(
                text = "Unlock to Continue",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = NeutralWhite
            )
            
            // Subtitle
            Text(
                text = when (authResult) {
                    is BiometricResult.Success -> "Identity Verified!"
                    is BiometricResult.Failed -> "Authentication Failed"
                    is BiometricResult.Error -> "Authentication Error"
                    BiometricResult.Idle -> "Touch the fingerprint sensor"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = NeutralWhite.copy(alpha = 0.7f)
            )
            
            // Use PIN Button
            Button(
                onClick = {
                    onUsePin()
                    autoAuthenticateTriggered = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeutralWhite.copy(alpha = 0.1f),
                    contentColor = NeutralWhite
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Lock, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Use PIN Instead", fontWeight = FontWeight.Medium)
            }
        }
    }
    
    // Enable Biometric Prompt Dialog
    if (showEnablePrompt) {
        AlertDialog(
            onDismissRequest = { showEnablePrompt = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Enable Biometric Authentication",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            },
            text = {
                Text(
                    "No fingerprint registered on this device. Would you like to set up biometric authentication for faster access?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEnablePrompt = false
                        activity?.let { biometricManager.promptEnableBiometric(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                ) {
                    Text("Setup Biometric")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnablePrompt = false }) {
                    Text("Skip")
                }
            }
        )
    }
}

@Composable
private fun AnimatedFingerprintIcon(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint")
    
    val scale by animateFloatAsState(
        targetValue = if (isScanning) 1.0f else 1.1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isScanning) alpha else 1f),
        ) {
            drawCircle(
                color = SecondaryGold,
                radius = size.minDimension / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 10f),
                        phase = 0f
                    )
                )
            )
        }
        
        // Fingerprint icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SecondaryGold.copy(alpha = 0.2f))
                .border(
                    width = 3.dp,
                    color = if (isScanning) SecondaryGold.copy(alpha = alpha) else SecondaryGold,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = SecondaryGold,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
```

---

#### ÉTAPE 9.4: Intégrer Biometric dans AppNavigation

**Modifier `SplashScreenAdvanced`**:

```kotlin
@Composable
fun SplashScreenAdvanced(
    biometricManager: BiometricManager,
    onSplashFinished: () -> Unit = {},
    onRequireBiometric: () -> Unit = {}
) {
    // ... existing code ...
    
    val authViewModel: AuthViewModel = hiltViewModel()
    
    LaunchedEffect(Unit) {
        delay(2000)
        
        val nextRoute = when {
            !onboardingViewModel.isOnboardingCompleted() -> Screen.Onboarding.route
            !authViewModel.isLoggedIn -> Screen.Login.route
            
            // Utilisateur loggé, vérifier biometric
            biometricManager.isBiometricAvailable() == BiometricAvailability.Available -> {
                onRequireBiometric()
            }
            
            else -> Screen.Dashboard.route
        }
        
        if (nextRoute != "biometric_lock") {
            onSplashFinished(nextRoute)
        }
    }
}
```

---

#### ÉTAPE 9.5: Test Biometric

**Test Scenarios**:

1. **Device with Fingerprint**:
   - Setup fingerprint in device settings
   - Lock app
   - Tap fingerprint sensor
   - App unlocks

2. **Device without Fingerprint**:
   - Open app
   - Prompt: "Enable Biometric Authentication"
   - Tap "Setup Biometric" → Device settings
   - Setup fingerprint
   - Back to app → Try again

3. **Cancel Fingerprint**:
   - Tap "Use PIN"
   - Navigate to PIN screen
   - Enter PIN → Unlock

4. **Failed Authentication**:
   - Wrong fingerprint/face
   - Error message displayed
   - Retry or use PIN

---

## 📈 PHASE 10: CHARTS PROFESSIONNELS (2-3 jours)

### Objectif
Remplacer les charts simplifiés par VICO Chart library pour des visualisations professionnelles

---

#### ÉTAPE 10.1: Configuration VICO Chart

**Ajouter à build.gradle.kts (app)**:

```kotlin
dependencies {
    implementation("com.patrykandpatrick.vico:compose:1.14.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
    implementation("com.patrykandpatrick.vico:core:1.14.0")
}
```

---

#### ÉTAPE 10.2: Créer LineChartComponent

**Fichier**: `app/src/main/java/com/example/aureus/ui/components/charts/LineChartComponent.kt`

```kotlin
package com.example.aureus.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.VerticalGradientShader
import com.patrykandpatrick.vico.core.chart.decoration.LineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.data.DataPoint
import com.patrykandpatrick.vico.core.data.chunk
import com.patrykandpatrick.vico.core.data.series.series
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerComponent
import com.patrykandpatrick.vico.core.model.Line
import com.patrykandpatrick.vico.core.model.Color as VicoColor

/**
 * Line Chart professionnel pour les statistiques
 */
@Composable
fun LineChartComponent(
    incomeData: List<Double>,
    expenseData: List<Double>,
    labels: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
    modifier: Modifier = Modifier
        .fillMaxSize()
        .height(200.dp)
        .background(Color.White)
        .padding(16.dp)
) {
    val incomePoints = incomeData.mapIndexed { index, value ->
        DataPoint(x = index.toFloat(), y = value)
    }
    
    val expensePoints = expenseData.mapIndexed { index, value ->
        DataPoint(x = index.toFloat(), y = value)
    }

    Chart(
        chart = lineChart(
            lines = listOf(
                Line(
                    lineChartData = series(
                        incomePoints,
                        LineCartesianLayer.LineFill.single(
                            VicoColor(0xFF4CAF50), // Green for income
                            VerticalGradientShader(
                                VicoColor(0xFF4CAF50),
                                VicoColor(0x004CAF50)
                            )
                        )
                    ),
                    lineSpec = LineCartesianLayer.LineSpec(
                        color = VicoColor(0xFF4CAF50),
                        thickness = 4.dp,
                        shape = Shapes.roundedCornerShape(cornerSize = 4.dp)
                    ),
                    lineBackgroundShader = null
                ),
                Line(
                    lineChartData = series(
                        expensePoints,
                        LineCartesianLayer.LineFill.single(
                            VicoColor(0xFFFF5252), // Red for expense
                            VerticalGradientShader(
                                VicoColor(0xFFFF5252),
                                VicoColor(0x00FF5252)
                            )
                        )
                    ),
                    lineSpec = LineCartesianLayer.LineSpec(
                        color = VicoColor(0xFFFF5252),
                        thickness = 4.dp,
                        shape = Shapes.roundedCornerShape(cornerSize = 4.dp)
                    ),
                    lineBackgroundShader = null
                )
            )
        ),
        modifier = modifier,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(labels),
        marker = marker(),
        panZoom = rememberPanZoomHandler(
            initialZoom = Zoom.Content
        )
    )
}

private fun startAxis() = startAxis(
    // TODO: Configure start axis
)

private fun bottomAxis(labels: List<String>) = bottomAxis(
    // TODO: Configure bottom axis with labels
)

private fun marker(): Marker {
    // TODO: Implement marker for tap event
}
```

---

#### ÉTAPE 10.3: Créer PieChartComponent

**Fichier**: `app/src/main/java/com/example/aureus/ui/components/charts/PieChartComponent.kt`

```kotlin
package com.example.aureus.ui.components.charts

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.pie.pieChart
import com.patrykandpatrick.vico.core.chart.pie.PieChart

/**
 * Pie Chart pour les catégories de dépenses
 */
@Composable
fun PieChartComponent(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
        .width(300.dp)
        .height(300.dp)
        .padding(16.dp)
) {
    val pieSlices = data.map { (category, amount) ->
        PieChartData.Slice(
            label = category,
            value = amount,
            color = getCategoryColor(category)
        )
    }

    Chart(
        chart = pieChart(
            slices = pieSlices
        ),
        modifier = modifier
    )
}

private fun getCategoryColor(category: String): com.patrykandpatrick.vico.core.model.Color {
    return when (category.lowercase()) {
        "shopping" -> com.patrykandpatrick.vico.core.model.Color(0xFF2196F3)
        "food & drink" -> com.patrykandpatrick.vico.core.model.Color(0xFFFF9800)
        "transport" -> com.patrykandpatrick.vico.core.model.Color(0xFF4CAF50)
        "entertainment" -> com.patrykandpatrick.vico.core.model.Color(0xFF9C27B0)
        "bills" -> com.patrykandpatrick.vico.core.model.Color(0xFFF44336)
        else -> com.patrykandpatrick.vico.core.model.Color(0xFF607D8B)
    }
}
```

---

#### ÉTAPE 10.4: Modifier StatisticsScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/statistics/StatisticsScreen.kt`

**Remplacer SimplifiedChart() par VICO charts**:

```kotlin
@Composable
private fun MonthlyTrendChart(
    incomeData: List<Double>,
    expenseData: List<Double>,
    months: List<String>
) {
    LineChartComponent(
        incomeData = incomeData,
        expenseData = expenseData,
        labels = months,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun CategoryBreakdownChart(
    categoryData: Map<String, Double>
) {
    PieChartComponent(
        data = categoryData,
        modifier = Modifier
            .size(300.dp)
    )
}
```

---

#### ÉTAPE 10.5: Test Charts

**Test Scenarios**:

1. **Line Chart**:
   - Charger données de 6 months
   - Vérifier: Income (green) and Expense (red) lines
   - Touch chart → Marker displays value

2. **Pie Chart**:
   - Charger catégories de dépenses
   - Vérifier: Every category with correct color
   - Touch slice → Shows percentage

3. **Empty State**:
   - Aucune donnée
   - Vérifier: Empty state affiché correctement

---

## 📊 RÉSUMÉ DES PHASES 7-10

| Phase | Durée | Complexité | Impact |
|-------|-------|------------|--------|
| Phase 7: Offline-First | 4-5 jours | Haute | ★★★★★ |
| Phase 8: Notifications | 3-4 jours | Moyenne | ★★★★☆ |
| Phase 9: Biometric Auth | 2-3 jours | Faible | ★★★☆☆ |
| Phase 10: Charts Pro | 2-3 jours | Moyenne | ★★★☆☆ |

**TOTAL**: 11-15 jours (2-3 semaines)

---

## 🎯 SCORE FINAL ATTENDU

Après Phase 10:

| Métrique | Après Phase 6 | Après Phase 10 |
|----------|---------------|----------------|
| Authentification | 100% | 100% |
| Offline Support | 0% | **100%** |
| Notifications | 0% | **100%** |
| Biometric Auth | 0% | **100%** |
| Charts Pro | 20% | **100%** |
| **SCORE GLOBAL** | 9.5/10 | **10/10** 🎉 |

---

## 📝 CHECKLIST PHASES 7-10

### Phase 7: Offline-First
- [ ] Améliorer AppDatabase avec sync
- [ ] Créer OfflineSyncManager complet
- [ ] Créer FirebaseSyncWorker
- [ ] Modifier ViewModels pour offline
- [ ] Test Online → Offline
- [ ] Test Offline → Online
- [ ] Test conflit resolution

### Phase 8: Notifications Push
- [ ] Configurer FCM dans build.gradle
- [ ] Créer FirebaseMessagingService
- [ ] Créer NotificationHelper
- [ ] Déployer Cloud Functions
- [ ] Test notifications transactions
- [ ] Test alert solde bas
- [ ] Test notifications directes

### Phase 9: Biometric Auth
- [ ] Configurer Biometric library
- [ ] Créer BiometricManager
- [ ] Créer BiometricLockScreen
- [ ] Intégrer dans navigation
- [ ] Test fingerprint unlock
- [ ] Test fallback PIN
- [ ] Test device sans biometric

### Phase 10: Charts Pro
- [ ] Configurer VICO Chart
- [ ] Créer LineChartComponent
- [ ] Créer PieChartComponent
- [ ] Modifier StatisticsScreen
- [ ] Test line chart
- [ ] Test pie chart
- [ ] Test empty states

---

## 🚀 PROCHAINES ÉTAPES (PHASE 11+)

Une fois les phases 7-10 complétées:

1. **Phase 11**: Analytics et Monitoring
2. **Phase 12**: Dark Mode complet
3. **Phase 13**: Internationalization (i18n) complète
4. **Phase 14**: Unit Tests + UI Tests
5. **Phase 15**: Performance Optimization

---

**PLAN SUITE CRÉÉ LE**: 11 Janvier 2026
**ESTIMATION**: 11-15 jours (2-3 semaines)
**AUTEUR**: Firebender AI Assistant
**PROJET**: Aureus Banking Application