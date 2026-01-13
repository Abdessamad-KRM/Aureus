package com.example.aureus.data.offline

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.ContactEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.util.TimeoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
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
    private val auth: FirebaseAuth,
    val syncStatusPublisher: SyncStatusPublisher
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

        val periodicWorkRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(
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
     * Synchroniser immédiatement (manual sync) avec timeout (Phase 3)
     */
    suspend fun syncNow(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Validation avant timeout
            if (auth.currentUser == null) {
                return@withContext SyncResult.Error("User not logged in")
            }

            if (!networkMonitor.isConnected()) {
                return@withContext SyncResult.Error("No internet connection")
            }

            val userId = auth.currentUser?.uid ?: return@withContext SyncResult.Error("User ID null")

            Log.d(TAG, "Starting manual sync for user: $userId")

            // Wrapper timeout pour l'opération de sync
            withTimeout(TimeoutManager.SYNC_TIMEOUT) {
                // Sync transactions
                syncTransactions(userId)

                // Sync cards
                syncCards(userId)

                // Sync contacts
                syncContacts(userId)

                // Upload unsynced changes
                uploadPendingChanges(userId)
            }

            saveLastSyncTimestamp()

            Log.d(TAG, "Manual sync completed successfully")

            // Publier le statut de sync after successful sync
            val currentStatus = getSyncStatus()
            syncStatusPublisher.publishSyncStatus(currentStatus)

            return@withContext SyncResult.Success

        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Sync timed out", e)
            // Publier statut d'erreur
            val errorStatus = SyncStatus(isOnline = false, lastSync = null, pendingChanges = 0, isSyncing = false)
            syncStatusPublisher.publishSyncStatus(errorStatus)
            return@withContext SyncResult.Error("Sync timed out: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync failed", e)
            // Publier statut d'erreur
            val errorStatus = SyncStatus(isOnline = false, lastSync = null, pendingChanges = 0, isSyncing = false)
            syncStatusPublisher.publishSyncStatus(errorStatus)
            return@withContext SyncResult.Error(e.message ?: "Sync failed")
        }
    }

    /**
     * Synchroniser les transactions
     */
    private suspend fun syncTransactions(userId: String) {
        try {
            // Download from Firestore
            val firebaseTransactions = firebaseDataManager.getUserTransactions(userId, limit = 100).first()
            
            // Upsert to Room
            firebaseTransactions.forEach { transactionMap ->
                val timestamp = transactionMap["createdAt"]
                val createdAt = when (timestamp) {
                    is com.google.firebase.Timestamp -> timestamp.toDate().time
                    is java.util.Date -> timestamp.time
                    is String -> {
                        // Try to parse ISO string
                        try {
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp as String)?.time 
                                ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    }
                    else -> System.currentTimeMillis()
                }
                
                val transaction = TransactionEntity(
                    id = transactionMap["transactionId"] as? String ?: transactionMap["id"] as? String ?: "",
                    accountId = transactionMap["accountId"] as? String ?: "",
                    userId = userId,
                    type = transactionMap["type"] as? String ?: "EXPENSE",
                    amount = transactionMap["amount"] as? Double ?: 0.0,
                    description = transactionMap["description"] as? String ?: transactionMap["title"] as? String ?: "Transaction",
                    category = transactionMap["category"] as? String ?: "OTHER",
                    merchant = transactionMap["merchant"] as? String,
                    date = createdAt.toString(),
                    balanceAfter = transactionMap["balanceAfter"] as? Double ?: 0.0,
                    lastSyncedAt = System.currentTimeMillis(),
                    isSynced = true,
                    isPendingUpload = false
                )
                database.transactionDao().upsert(transaction)
            }
            
            Log.d(TAG, "Synced ${firebaseTransactions.size} transactions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync transactions", e)
        }
    }

    /**
     * Synchroniser les cartes
     */
    private suspend fun syncCards(userId: String) {
        try {
            val firebaseCards = firebaseDataManager.getUserCards(userId).first()
            
            firebaseCards.forEach { cardMap ->
                val card = BankCardEntity(
                    id = cardMap["cardId"] as? String ?: "",
                    userId = userId,
                    accountId = cardMap["accountId"] as? String ?: "",
                    cardNumber = cardMap["cardNumber"] as? String ?: "",
                    cardHolder = cardMap["cardHolder"] as? String ?: "",
                    expiryDate = cardMap["expiryDate"] as? String ?: "",
                    balance = 0.0, // Cards may not have balance in Firebase
                    cardType = cardMap["cardType"] as? String ?: "VISA",
                    cardColor = cardMap["cardColor"] as? String ?: "navy",
                    isDefault = cardMap["isDefault"] as? Boolean ?: false,
                    isActive = cardMap["isActive"] as? Boolean ?: true,
                    status = "ACTIVE",
                    dailyLimit = cardMap["dailyLimit"] as? Double ?: 10000.0,
                    monthlyLimit = cardMap["monthlyLimit"] as? Double ?: 50000.0,
                    createdAt = cardMap["createdAt"].toString(),
                    updatedAt = cardMap["updatedAt"].toString(),
                    lastSyncedAt = System.currentTimeMillis()
                )
                database.cardDao().upsert(card)
            }
            
            Log.d(TAG, "Synced ${firebaseCards.size} cards")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync cards", e)
        }
    }

    /**
     * Synchroniser les contacts
     */
    private suspend fun syncContacts(userId: String) {
        try {
            val firebaseContacts = firebaseDataManager.getUserContacts(userId).first()
            
            firebaseContacts.forEach { contactMap ->
                val contact = ContactEntity(
                    id = contactMap["id"] as? String ?: "",
                    userId = userId,
                    name = contactMap["name"] as? String ?: "",
                    phone = contactMap["phone"] as? String ?: "",
                    email = contactMap["email"] as? String,
                    avatar = contactMap["avatar"] as? String,
                    accountNumber = contactMap["accountNumber"] as? String,
                    isFavorite = contactMap["isFavorite"] as? Boolean ?: false,
                    isBankContact = contactMap["isBankContact"] as? Boolean ?: false,
                    category = contactMap["category"] as? String,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastSyncedAt = System.currentTimeMillis()
                )
                database.contactDao().upsert(contact)
            }
            
            Log.d(TAG, "Synced ${firebaseContacts.size} contacts")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync contacts", e)
        }
    }

    /**
     * Uploader les changements en attente (conflit resolution)
     */
    private suspend fun uploadPendingChanges(userId: String) {
        try {
            // Get unsynced transactions
            val unsyncedTransactions = database.transactionDao().getUnsyncedTransactions()
            
            if (unsyncedTransactions.isNotEmpty()) {
                Log.d(TAG, "Uploading ${unsyncedTransactions.size} pending transactions")
                
                unsyncedTransactions.forEach { transaction ->
                    try {
                        val transactionData: Map<String, Any> = mapOf(
                            "accountId" to transaction.accountId,
                            "userId" to (transaction.userId ?: userId),
                            "type" to transaction.type,
                            "amount" to transaction.amount,
                            "category" to (transaction.category ?: "OTHER"),
                            "title" to transaction.description,
                            "description" to transaction.description,
                            "merchant" to (transaction.merchant ?: ""),
                            "balanceAfter" to transaction.balanceAfter
                        )
                        
                        val result = firebaseDataManager.createTransaction(transactionData)
                        
                        if (result.isSuccess) {
                            database.transactionDao().markAsSynced(transaction.id)
                            Log.d(TAG, "Uploaded transaction: ${transaction.id}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to upload transaction: ${transaction.id}", e)
                    }
                }
            }
            
            Log.d(TAG, "Uploading pending changes completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload pending changes", e)
        }
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
        val prefs = context.getSharedPreferences("aureus_sync", Context.MODE_PRIVATE)
        return prefs.getLong("last_sync_timestamp", 0).takeIf { it > 0 }
    }
    
    private fun saveLastSyncTimestamp() {
        val prefs = context.getSharedPreferences("aureus_sync", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
    }
    
    private suspend fun getPendingChangesCount(): Int {
        return database.transactionDao().getUnsyncedTransactions().size
    }
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