package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Contact Repository Interface
 * Defines CRUD operations for user contacts
 * All contacts are stored per-user in Firestore sub-collection
 */
interface ContactRepository {

    // ==================== READ OPERATIONS ====================

    /**
     * Get all contacts for a user in real-time
     * @param userId The user's Firebase UID
     * @return Flow of contacts list
     */
    fun getContacts(userId: String): Flow<List<Contact>>

    /**
     * Get favorite contacts
     * @param userId The user's Firebase UID
     * @return Flow of favorite contacts list
     */
    fun getFavoriteContacts(userId: String): Flow<List<Contact>>

    /**
     * Get contacts by category
     * @param userId The user's Firebase UID
     * @param category The category to filter by
     * @return Flow of contacts in the category
     */
    fun getContactsByCategory(userId: String, category: String): Flow<List<Contact>>

    /**
     * Get bank contacts (contacts with account numbers)
     * @param userId The user's Firebase UID
     * @return Flow of bank contacts list
     */
    fun getBankContacts(userId: String): Flow<List<Contact>>

    /**
     * Get contacts recently used in transactions
     * @param userId The user's Firebase UID
     * @param limit Maximum number of contacts to return
     * @return Flow of recently used contacts
     */
    fun getRecentContacts(userId: String, limit: Int = 5): Flow<List<Contact>>

    /**
     * Get a specific contact by ID
     * @param contactId The contact's ID
     * @return Resource with Contact or error
     */
    suspend fun getContactById(contactId: String): Resource<Contact>

    // ==================== WRITE OPERATIONS ====================

    /**
     * Add a new contact
     * @param userId The user's Firebase UID
     * @param contact The contact to add
     * @return Resource with the created contact ID or error
     */
    suspend fun addContact(userId: String, contact: Contact): Resource<String>

    /**
     * Update an existing contact
     * @param contactId The contact's ID
     * @param updatedContact The updated contact data
     * @return Resource with success or error
     */
    suspend fun updateContact(contactId: String, updatedContact: Contact): Resource<Unit>

    /**
     * Delete a contact
     * @param contactId The contact's ID
     * @return Resource with success or error
     */
    suspend fun deleteContact(contactId: String): Resource<Unit>

    /**
     * Toggle favorite status
     * @param contactId The contact's ID
     * @param isFavorite New favorite status
     * @return Resource with success or error
     */
    suspend fun toggleFavorite(contactId: String, isFavorite: Boolean): Resource<Unit>

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Search contacts by name, phone, or email
     * @param userId The user's Firebase UID
     * @param query The search query
     * @return Flow of matching contacts
     */
    fun searchContacts(userId: String, query: String): Flow<List<Contact>>

    /**
     * Find a contact by phone number
     * @param userId The user's Firebase UID
     * @param phone The phone number to search for
     * @return Resource with Contact or error
     */
    suspend fun findContactByPhone(userId: String, phone: String): Resource<Contact>

    /**
     * Find a contact by account number
     * @param userId The user's Firebase UID
     * @param accountNumber The account number to search for
     * @return Resource with Contact or error
     */
    suspend fun findContactByAccountNumber(userId: String, accountNumber: String): Resource<Contact>

    // ==================== BATCH OPERATIONS ====================

    /**
     * Import contacts from a list
     * @param userId The user's Firebase UID
     * @param contacts List of contacts to import
     * @return Resource with success or error
     */
    suspend fun importContacts(userId: String, contacts: List<Contact>): Resource<Unit>

    /**
     * Delete all contacts for a user (use with caution)
     * @param userId The user's Firebase UID
     * @return Resource with success or error
     */
    suspend fun deleteAllContacts(userId: String): Resource<Unit>

    // ==================== SYNC OPERATIONS ====================

    /**
     * Refresh contacts from Firestore
     * @param userId The user's Firebase UID
     * @return Resource with success or error
     */
    suspend fun refreshContacts(userId: String): Resource<Unit>
}