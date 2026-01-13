package com.example.aureus.data.offline

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.ContactEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException

/**
 * Worker pour la synchronisation automatique Firebase
 */
@HiltWorker
class FirebaseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: AppDatabase,
    private val firebaseDataManager: FirebaseDataManager,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "FirebaseSyncWorker"
        private const val SYNC_TIMEOUT = 30000L // 30 seconds
    }

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            log(TAG, "Starting background sync")

            if (auth.currentUser == null) {
                log(TAG, "User not logged in")
                return ListenableWorker.Result.failure()
            }

            val userId = auth.currentUser?.uid ?: return run {
                log(TAG, "User ID null")
                ListenableWorker.Result.failure()
            }

            // Perform sync with timeout
            syncWithTimeout(userId)

            log(TAG, "Background sync successful")
            ListenableWorker.Result.success()
        } catch (e: TimeoutCancellationException) {
            log(TAG, "Sync timed out: ${e.message}")
            ListenableWorker.Result.retry()
        } catch (e: Exception) {
            log(TAG, "Sync worker crashed", e)
            ListenableWorker.Result.failure()
        }
    }

    private suspend fun syncWithTimeout(userId: String) {
        withTimeout(SYNC_TIMEOUT) {
            syncTransactions(userId)
            syncCards(userId)
            syncContacts(userId)
        }
    }

    private suspend fun syncTransactions(userId: String) {
        try {
            val firebaseTransactions = firebaseDataManager.getUserTransactions(userId, limit = 100).first()

            firebaseTransactions.forEach { transactionMap ->
                val timestamp = transactionMap["createdAt"]
                val createdAt = when (timestamp) {
                    is com.google.firebase.Timestamp -> timestamp.toDate().time
                    is java.util.Date -> timestamp.time
                    is String -> {
                        try {
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp)?.time
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

            log(TAG, "Synced ${firebaseTransactions.size} transactions")
        } catch (e: Exception) {
            log(TAG, "Failed to sync transactions", e)
        }
    }

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
                    balance = 0.0,
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

            log(TAG, "Synced ${firebaseCards.size} cards")
        } catch (e: Exception) {
            log(TAG, "Failed to sync cards", e)
        }
    }

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

            log(TAG, "Synced ${firebaseContacts.size} contacts")
        } catch (e: Exception) {
            log(TAG, "Failed to sync contacts", e)
        }
    }

    private fun log(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.d(tag, message)
        }
    }
}