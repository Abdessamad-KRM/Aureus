package com.example.aureus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.aureus.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Contacts
 * Provides offline-first CRUD operations
 */
@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    // Upsert (insert or update) for sync operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY isFavorite DESC, name ASC")
    fun getContactsByUserId(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isBankContact = 1 ORDER BY name ASC")
    fun getBankContacts(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchContacts(userId: String, query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND category = :category ORDER BY name ASC")
    fun getContactsByCategory(userId: String, category: String): Flow<List<ContactEntity>>

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isFavorite = :isFavorite, lastSyncedAt = :lastSyncedAt WHERE id = :contactId")
    suspend fun updateFavoriteStatus(contactId: String, isFavorite: Boolean, lastSyncedAt: Long = System.currentTimeMillis())

    @Query("UPDATE contacts SET category = :category, lastSyncedAt = :lastSyncedAt WHERE id = :contactId")
    suspend fun updateContactCategory(contactId: String, category: String?, lastSyncedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: String)

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteAllContactsByUserId(userId: String)

    @Query("SELECT COUNT(*) FROM contacts WHERE userId = :userId")
    suspend fun getContactCount(userId: String): Int

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentContacts(userId: String, limit: Int = 5): Flow<List<ContactEntity>>

    // ✅ PHASE 2: New query for contacts by lastUsed (transactions)
    @Query("SELECT * FROM contacts WHERE userId = :userId AND lastUsed IS NOT NULL ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecentlyUsedContacts(userId: String, limit: Int = 5): Flow<List<ContactEntity>>

    // ✅ PHASE 2: Find contact by Firebase userId (for transfers)
    @Query("SELECT * FROM contacts WHERE userId = :userId AND firebaseUserId = :firebaseUserId")
    suspend fun findContactByFirebaseUserId(userId: String, firebaseUserId: String): ContactEntity?

    // ✅ PHASE 2: Find app user contacts (verified users)
    @Query("SELECT * FROM contacts WHERE userId = :userId AND isVerifiedAppUser = 1 ORDER BY name ASC")
    fun getAppUserContacts(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND lastSyncedAt < :timestamp")
    suspend fun getStaleContacts(userId: String, timestamp: Long): List<ContactEntity>

    @Query("UPDATE contacts SET lastSyncedAt = :timestamp WHERE lastSyncedAt > 0")
    suspend fun invalidateCache(userId: String, timestamp: Long)

    // ✅ PHASE 2: Update lastUsed timestamp after transaction
    @Query("UPDATE contacts SET lastUsed = :lastUsed, updatedAt = :lastSyncedAt WHERE id = :contactId")
    suspend fun updateLastUsed(contactId: String, lastUsed: Long = System.currentTimeMillis(), lastSyncedAt: Long = System.currentTimeMillis())

    // ✅ PHASE 2: Update contact app user verification status
    @Query("UPDATE contacts SET firebaseUserId = :firebaseUserId, isVerifiedAppUser = :isVerified, updatedAt = :lastSyncedAt WHERE id = :contactId")
    suspend fun updateAppUserStatus(contactId: String, firebaseUserId: String?, isVerified: Boolean, lastSyncedAt: Long = System.currentTimeMillis())
}