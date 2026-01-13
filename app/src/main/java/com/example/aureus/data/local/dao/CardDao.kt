package com.example.aureus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.aureus.data.local.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Bank Cards
 * Provides offline-first CRUD operations
 */
@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: BankCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<BankCardEntity>)

    // Upsert (insert or update) for sync operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: BankCardEntity)

    @Query("SELECT * FROM bank_cards WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getCardsByUserId(userId: String): Flow<List<BankCardEntity>>

    @Query("SELECT * FROM bank_cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): BankCardEntity?

    @Query("SELECT * FROM bank_cards WHERE userId = :userId AND cardNumber = :cardNumber")
    suspend fun getCardByNumber(userId: String, cardNumber: String): BankCardEntity?

    @Query("SELECT * FROM bank_cards WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultCard(userId: String): BankCardEntity?

    @Query("SELECT * FROM bank_cards WHERE userId = :userId AND isActive = 1 ORDER BY isDefault DESC, createdAt DESC")
    fun getActiveCards(userId: String): Flow<List<BankCardEntity>>

    @Update
    suspend fun updateCard(card: BankCardEntity)

    @Query("UPDATE bank_cards SET isDefault = 0 WHERE userId = :userId AND id != :cardId")
    suspend fun unsetOtherDefaultCards(userId: String, cardId: String)

    @Query("UPDATE bank_cards SET isDefault = 1 WHERE id = :cardId")
    suspend fun setDefaultCard(cardId: String)

    @Query("UPDATE bank_cards SET isActive = :isActive, lastSyncedAt = :lastSyncedAt WHERE id = :cardId")
    suspend fun updateCardActiveStatus(cardId: String, isActive: Boolean, lastSyncedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bank_cards SET status = :status, lastSyncedAt = :lastSyncedAt WHERE id = :cardId")
    suspend fun updateCardStatus(cardId: String, status: String, lastSyncedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bank_cards SET balance = :balance, lastSyncedAt = :lastSyncedAt WHERE id = :cardId")
    suspend fun updateCardBalance(cardId: String, balance: Double, lastSyncedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM bank_cards WHERE id = :cardId")
    suspend fun deleteCard(cardId: String)

    @Query("DELETE FROM bank_cards WHERE userId = :userId")
    suspend fun deleteAllCardsByUserId(userId: String)

    @Query("SELECT SUM(balance) FROM bank_cards WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalBalance(userId: String): Double?

    @Query("SELECT COUNT(*) FROM bank_cards WHERE userId = :userId")
    suspend fun getCardCount(userId: String): Int

    @Query("SELECT * FROM bank_cards WHERE userId = :userId AND lastSyncedAt < :timestamp")
    suspend fun getStaleCards(userId: String, timestamp: Long): List<BankCardEntity>

    @Query("UPDATE bank_cards SET lastSyncedAt = :timestamp WHERE userId = :userId AND lastSyncedAt > 0")
    suspend fun invalidateCache(userId: String, timestamp: Long)
}