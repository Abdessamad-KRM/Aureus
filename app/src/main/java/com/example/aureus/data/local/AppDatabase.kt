package com.example.aureus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.CardDao
import com.example.aureus.data.local.dao.ContactDao
import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.local.entity.AccountEntity
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.ContactEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.local.entity.UserEntity
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import kotlinx.coroutines.flow.first

/**
 * Room Database Configuration
 * Enhanced for Offline-First Phase 7:
 * - Added BankCard and Contact entities
 * - Added sync tracking fields (lastSyncedAt, isSynced, isPendingUpload)
 * - Increased version to 2 for schema migration
 * - Added bidirectional sync methods (Firestore ↔ Room)
 */
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
    
    /**
     * Synchroniser toutes les données avec Firebase
     * Phase 7 - Offline-First Complete
     */
    suspend fun syncWithFirebase(firebaseDataManager: FirebaseDataManager, userId: String) {
        // Sync transactions
        syncTransactions(firebaseDataManager, userId)
        
        // Sync cards
        syncCards(firebaseDataManager, userId)
        
        // Sync contacts
        syncContacts(firebaseDataManager, userId)
    }
    
    /**
     * Synchroniser les transactions depuis Firestore vers Room
     */
    private suspend fun syncTransactions(firebaseDataManager: FirebaseDataManager, userId: String) {
        try {
            // 1. Get transactions from Firestore
            val firebaseTransactions = firebaseDataManager.getUserTransactions(userId, limit = 100).first()
            
            // 2. Upsert to Room
            firebaseTransactions.forEach { transactionMap ->
                val timestamp = transactionMap["createdAt"]
                val createdAt = when (timestamp) {
                    is com.google.firebase.Timestamp -> timestamp.toDate().time.toString()
                    is java.util.Date -> timestamp.time.toString()
                    else -> System.currentTimeMillis().toString()
                }
                
                val transaction = TransactionEntity(
                    id = transactionMap["transactionId"] as? String ?: "",
                    accountId = transactionMap["accountId"] as? String ?: "",
                    userId = userId,
                    type = transactionMap["type"] as? String ?: "EXPENSE",
                    amount = transactionMap["amount"] as? Double ?: 0.0,
                    description = transactionMap["description"] as? String 
                        ?: transactionMap["title"] as? String ?: "Transaction",
                    category = transactionMap["category"] as? String,
                    merchant = transactionMap["merchant"] as? String,
                    date = createdAt,
                    balanceAfter = transactionMap["balanceAfter"] as? Double ?: 0.0,
                    lastSyncedAt = System.currentTimeMillis(),
                    isSynced = true,
                    isPendingUpload = false
                )
                transactionDao().upsert(transaction)
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDatabase", "Failed to sync transactions", e)
        }
    }
    
    /**
     * Synchroniser les cartes depuis Firestore vers Room
     */
    private suspend fun syncCards(firebaseDataManager: FirebaseDataManager, userId: String) {
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
                    dailyLimit = cardMap["dailyLimit"] as? Double ?: 10000.0,
                    monthlyLimit = cardMap["monthlyLimit"] as? Double ?: 50000.0,
                    createdAt = cardMap["createdAt"].toString(),
                    updatedAt = cardMap["updatedAt"].toString(),
                    lastSyncedAt = System.currentTimeMillis()
                )
                cardDao().upsert(card)
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDatabase", "Failed to sync cards", e)
        }
    }
    
    /**
     * Synchroniser les contacts depuis Firestore vers Room
     */
    private suspend fun syncContacts(firebaseDataManager: FirebaseDataManager, userId: String) {
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
                    firebaseUserId = contactMap["firebaseUserId"] as? String,
                    isFavorite = contactMap["isFavorite"] as? Boolean ?: false,
                    isBankContact = contactMap["isBankContact"] as? Boolean ?: false,
                    category = contactMap["category"] as? String,
                    isVerifiedAppUser = contactMap["isVerifiedAppUser"] as? Boolean ?: false,
                    lastUsed = (contactMap["lastUsed"] as? com.google.firebase.Timestamp)?.toDate()?.time,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastSyncedAt = System.currentTimeMillis()
                )
                contactDao().upsert(contact)
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDatabase", "Failed to sync contacts", e)
        }
    }
}