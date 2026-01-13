package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.ContactRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contact Repository Implementation - Firebase-based
 * Stores contacts in Firestore at: users/{userId}/contacts/{contactId}
 * Provides real-time synchronization and offline support
 */
@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager
) : ContactRepository {

    companion object {
        private const val TAG = "ContactRepositoryImpl"
    }

    // ==================== READ OPERATIONS ====================

    override fun getContacts(userId: String): Flow<List<Contact>> {
        return firebaseDataManager.getUserContacts(userId).map { contactsList ->
            contactsList.mapNotNull { mapToContact(it) }.sortedBy { it.name.lowercase() }
        }
    }

    override fun getFavoriteContacts(userId: String): Flow<List<Contact>> {
        return getContacts(userId).map { contacts ->
            contacts.filter { it.isFavorite }
        }
    }

    override fun getContactsByCategory(userId: String, category: String): Flow<List<Contact>> {
        return getContacts(userId).map { contacts ->
            contacts.filter { it.category?.name?.equals(category, ignoreCase = true) == true }
        }
    }

    override fun getBankContacts(userId: String): Flow<List<Contact>> {
        return getContacts(userId).map { contacts ->
            contacts.filter { !it.accountNumber.isNullOrBlank() }
        }
    }

    override fun getRecentContacts(userId: String, limit: Int): Flow<List<Contact>> {
        return getContacts(userId).map { contacts ->
            contacts.sortedByDescending { it.createdAt }.take(limit)
        }
    }

    override suspend fun getContactById(contactId: String): Resource<Contact> {
        // Implementation would query specific document by ID
        // For now, return error since we need userId to query
        return Resource.Error("Contact ID lookup requires user context")
    }

    // ==================== WRITE OPERATIONS ====================

    override suspend fun addContact(userId: String, contact: Contact): Resource<String> {
        return try {
            val contactData = contactToMap(contact)
            val result = firebaseDataManager.addContact(userId, contactData)
            if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: contact.id)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to add contact")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun updateContact(contactId: String, updatedContact: Contact): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val contactData = contactToMap(updatedContact)
            val result = firebaseDataManager.updateContact(userId, contactId, contactData)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to update contact")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun deleteContact(contactId: String): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val result = firebaseDataManager.deleteContact(userId, contactId)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to delete contact")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun toggleFavorite(contactId: String, isFavorite: Boolean): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val updates = mapOf("isFavorite" to isFavorite)
            val result = firebaseDataManager.updateContact(userId, contactId, updates)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to toggle favorite")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    // ==================== SEARCH OPERATIONS ====================

    override fun searchContacts(userId: String, query: String): Flow<List<Contact>> {
        return getContacts(userId).map { contacts ->
            val lowerQuery = query.lowercase()
            contacts.filter { contact ->
                contact.name.lowercase().contains(lowerQuery) ||
                contact.phone.lowercase().contains(lowerQuery) ||
                contact.email?.lowercase()?.contains(lowerQuery) == true ||
                contact.accountNumber?.lowercase()?.contains(lowerQuery) == true
            }
        }
    }

    override suspend fun findContactByPhone(userId: String, phone: String): Resource<Contact> {
        return try {
            val contacts = getContacts(userId)
            var resultContact: Contact? = null

            contacts.collect { contactList ->
                resultContact = contactList.find {
                    it.phone.replace(" ", "") == phone.replace(" ", "")
                }
            }

            resultContact?.let { Resource.Success(it) }
                ?: Resource.Error("Contact not found")
        } catch (e: Exception) {
            Resource.Error("Failed to find contact: ${e.message}", e)
        }
    }

    override suspend fun findContactByAccountNumber(
        userId: String,
        accountNumber: String
    ): Resource<Contact> {
        return try {
            val contacts = getContacts(userId)
            var resultContact: Contact? = null

            contacts.collect { contactList ->
                resultContact = contactList.find {
                    it.accountNumber?.replace(" ", "") == accountNumber.replace(" ", "")
                }
            }

            resultContact?.let { Resource.Success(it) }
                ?: Resource.Error("Contact not found")
        } catch (e: Exception) {
            Resource.Error("Failed to find contact: ${e.message}", e)
        }
    }

    // ==================== BATCH OPERATIONS ====================

    override suspend fun importContacts(
        userId: String,
        contacts: List<Contact>
    ): Resource<Unit> {
        return try {
            contacts.forEach { contact ->
                addContact(userId, contact)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to import contacts: ${e.message}", e)
        }
    }

    override suspend fun deleteAllContacts(userId: String): Resource<Unit> {
        return try {
            val firestore = firebaseDataManager.firestore
            // Get all contacts and delete them using a batch
            val contactsQuerySnapshot = firestore
                .collection("users")
                .document(userId)
                .collection("contacts")
                .get()
                .await()

            // Delete all contacts in a single batch operation
            val batch = firestore.batch()
            for (document in contactsQuerySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete all contacts: ${e.message}", e)
        }
    }

    // ==================== SYNC OPERATIONS ====================

    override suspend fun refreshContacts(userId: String): Resource<Unit> {
        // Flow automatically refreshes, this is a placeholder
        return Resource.Success(Unit)
    }

    // ==================== MAPPING FUNCTIONS ====================

    /**
     * Convert Contact domain model to Firestore Map
     * ✅ PHASE 2: Includes firebaseUserId, isVerifiedAppUser, lastUsed fields
     */
    private fun contactToMap(contact: Contact): Map<String, Any> {
        return mapOf(
            "id" to contact.id,
            "name" to contact.name,
            "phone" to contact.phone,
            "email" to (contact.email ?: ""),
            "avatar" to (contact.avatar ?: ""),
            "accountNumber" to (contact.accountNumber ?: ""),
            "firebaseUserId" to (contact.firebaseUserId ?: ""),
            "isFavorite" to contact.isFavorite,
            "isBankContact" to contact.isBankContact,
            "category" to (contact.category?.name ?: ContactCategory.OTHER.name),
            "isVerifiedAppUser" to contact.isVerifiedAppUser,
            "lastUsed" to (contact.lastUsed?.let { Timestamp(it) } ?: Timestamp(Date())),
            "createdAt" to Timestamp(contact.createdAt),
            "updatedAt" to Timestamp(Date())
        )
    }

    /**
     * Convert Firestore Map to Contact domain model
     * ✅ PHASE 2: Handles firebaseUserId, isVerifiedAppUser, lastUsed fields
     */
    private fun mapToContact(data: Map<String, Any>): Contact? {
        return try {
            val id = data["id"] as? String ?: return null
            val name = data["name"] as? String ?: return null
            val phone = data["phone"] as? String ?: return null
            val email = (data["email"] as? String)?.takeIf { it.isNotBlank() }
            val avatar = (data["avatar"] as? String)?.takeIf { it.isNotBlank() }
            val accountNumber = (data["accountNumber"] as? String)?.takeIf { it.isNotBlank() }
            val firebaseUserId = (data["firebaseUserId"] as? String)?.takeIf { it.isNotBlank() }
            val isFavorite = data["isFavorite"] as? Boolean ?: false
            val isBankContact = data["isBankContact"] as? Boolean ?: false
            val categoryStr = data["category"] as? String ?: ContactCategory.OTHER.name
            val isVerifiedAppUser = data["isVerifiedAppUser"] as? Boolean ?: false

            val createdAt = when (val timestamp = data["createdAt"]) {
                is Timestamp -> timestamp.toDate()
                is Date -> timestamp
                else -> Date()
            }

            val updatedAt = when (val timestamp = data["updatedAt"]) {
                is Timestamp -> timestamp.toDate()
                is Date -> timestamp
                else -> Date()
            }

            val lastUsed = when (val timestamp = data["lastUsed"]) {
                is Timestamp -> timestamp.toDate()
                is Date -> timestamp
                else -> null
            }

            Contact(
                id = id,
                name = name,
                phone = phone,
                email = email,
                avatar = avatar,
                accountNumber = accountNumber,
                firebaseUserId = firebaseUserId,
                isFavorite = isFavorite,
                isBankContact = isBankContact,
                category = ContactCategory.fromString(categoryStr),
                isVerifiedAppUser = isVerifiedAppUser,
                lastUsed = lastUsed,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}