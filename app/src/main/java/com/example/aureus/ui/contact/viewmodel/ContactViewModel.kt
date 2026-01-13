package com.example.aureus.ui.contact.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.ContactRepository
import com.example.aureus.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Contact ViewModel - Offline-First avec support Room cache
 * Phase 7: Offline-First Complete
 */
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val firebaseDataManager: FirebaseDataManager,
    private val database: AppDatabase,
    private val offlineSyncManager: OfflineSyncManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    // ==================== UI STATES ====================

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ContactCategory?>(null)
    val selectedCategory: StateFlow<ContactCategory?> = _selectedCategory.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    // Sync Status (Phase 7)
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // ==================== DATA LOADING ====================

    init {
        // Observer le statut de sync via Flow
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
                _isOfflineMode.value = !status.isOnline
            }
        }

        loadContacts()
    }

    /**
     * Load all contacts for the current user in real-time
     */
    fun loadContacts() {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            contactRepository.getContacts(userId).collect { contacts ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contacts = contacts,
                    error = null
                )
                applyFilters()
                _isLoading.value = false
            }
        }
    }

    /**
     * Load favorite contacts
     */
    fun loadFavoriteContacts() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            contactRepository.getFavoriteContacts(userId).collect { favorites ->
                _uiState.value = _uiState.value.copy(favoriteContacts = favorites)
            }
        }
    }

    /**
     * Load bank contacts (those with account numbers)
     */
    fun loadBankContacts() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            contactRepository.getBankContacts(userId).collect { bankContacts ->
                _uiState.value = _uiState.value.copy(bankContacts = bankContacts)
            }
        }
    }

    /**
     * Load recently used contacts
     */
    fun loadRecentContacts(limit: Int = 5) {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            contactRepository.getRecentContacts(userId, limit).collect { recentContacts ->
                _uiState.value = _uiState.value.copy(recentContacts = recentContacts)
            }
        }
    }

    // ==================== CRUD OPERATIONS ====================

    /**
     * Add a new contact
     */
    fun addContact(
        name: String,
        phone: String,
        email: String? = null,
        accountNumber: String? = null,
        category: ContactCategory = ContactCategory.OTHER
    ) {
        val userId = firebaseDataManager.currentUserId() ?: run {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val contactId = "contact_${System.currentTimeMillis()}"
            val contact = Contact(
                id = contactId,
                name = name,
                phone = phone,
                email = email,
                accountNumber = accountNumber,
                category = category,
                isBankContact = !accountNumber.isNullOrBlank(),
                createdAt = Date(),
                updatedAt = Date()
            )

            val result = contactRepository.addContact(userId, contact)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    // Track contact added - PHASE 11: Analytics
                    analyticsManager.trackContactAdded(userId)
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Contact added successfully",
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to add contact",
                        successMessage = null
                    )
                }
                is Resource.Loading, is Resource.Idle -> {
                    // Loading/Idle state handled by _isLoading
                }
            }
        }
    }

    /**
     * Update an existing contact
     */
    fun updateContact(contact: Contact) {
        val userId = firebaseDataManager.currentUserId() ?: run {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val updatedContact = contact.copy(updatedAt = Date())
            val result = contactRepository.updateContact(contact.id, updatedContact)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Contact updated successfully",
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update contact"
                    )
                }
                is Resource.Loading, is Resource.Idle -> {
                    // Loading/Idle state handled by _isLoading
                }
            }
        }
    }

    /**
     * Delete a contact
     */
    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = contactRepository.deleteContact(contactId)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    // Track contact removed - PHASE 11: Analytics
                    val userId = firebaseDataManager.currentUserId()
                    if (userId != null) {
                        analyticsManager.trackContactRemoved(userId)
                    }
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Contact deleted successfully",
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete contact"
                    )
                }
                is Resource.Loading, is Resource.Idle -> {
                    // Loading/Idle state handled by _isLoading
                }
            }
        }
    }

    /**
     * Toggle favorite status for a contact by ID
     */
    fun toggleFavorite(contactId: String) {
        val contact = _uiState.value.contacts.find { it.id == contactId } ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val result = contactRepository.toggleFavorite(contactId, !contact.isFavorite)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Favorite status updated",
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update favorite"
                    )
                }
                is Resource.Loading, is Resource.Idle -> {
                    // Loading/Idle state handled by _isLoading
                }
            }
        }
    }

    /**
     * Toggle favorite status for a contact object
     */
    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = contactRepository.toggleFavorite(contact.id, !contact.isFavorite)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Favorite status updated",
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update favorite"
                    )
                }
                is Resource.Loading, is Resource.Idle -> {
                    // Loading/Idle state handled by _isLoading
                }
            }
        }
    }

    // ==================== SEARCH & FILTER ====================

    /**
     * Search contacts by name, phone, or email
     */
    fun searchContacts(query: String) {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            _searchQuery.value = query
            contactRepository.searchContacts(userId, query).collect { results ->
                _uiState.value = _uiState.value.copy(filteredContacts = results)
            }
        }
    }

    /**
     * Filter contacts by category
     */
    fun filterByCategory(category: ContactCategory?) {
        val userId = firebaseDataManager.currentUserId() ?: return

        _selectedCategory.value = category

        viewModelScope.launch {
            if (category != null) {
                contactRepository.getContactsByCategory(userId, category.name)
                    .collect { results ->
                        _uiState.value = _uiState.value.copy(filteredContacts = results)
                    }
            } else {
                applyFilters()
            }
        }
    }

    /**
     * Toggle showing only favorites
     */
    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
        applyFilters()
    }

    /**
     * Apply all active filters
     */
    private fun applyFilters() {
        var filtered = _uiState.value.contacts

        // Filter by favorites
        if (_showFavoritesOnly.value) {
            filtered = filtered.filter { it.isFavorite }
        }

        // Filter by category
        _selectedCategory.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filter by search query
        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter { contact ->
                contact.name.lowercase().contains(query) ||
                contact.phone.lowercase().contains(query) ||
                contact.email?.lowercase()?.contains(query) == true
            }
        }

        _uiState.value = _uiState.value.copy(filteredContacts = filtered)
    }

    // ==================== UTILITY ====================

    /**
     * Reset all filters
     */
    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _showFavoritesOnly.value = false
        applyFilters()
    }

    /**
     * Clear success and error messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null
        )
    }

    /**
     * Refresh contacts from Firestore
     */
    fun refresh() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            _isLoading.value = true
            contactRepository.refreshContacts(userId)
            _isLoading.value = false
        }
    }

    /**
     * Get contact by phone number (send money screen helper)
     */
    suspend fun findContactByPhone(phone: String): Contact? {
        val userId = firebaseDataManager.currentUserId() ?: return null

        return when (val result = contactRepository.findContactByPhone(userId, phone)) {
            is Resource.Success -> result.data
            is Resource.Error -> null
            is Resource.Loading, is Resource.Idle -> null
        }
    }

    /**
     * Reset view model state
     */
    fun reset() {
        _uiState.value = ContactUiState()
        _isLoading.value = false
        _searchQuery.value = ""
        _selectedCategory.value = null
        _showFavoritesOnly.value = false
    }
}

// ==================== DATA CLASSES ====================

/**
 * UI State for Contact Management
 */
data class ContactUiState(
    val isLoading: Boolean = true,
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val favoriteContacts: List<Contact> = emptyList(),
    val bankContacts: List<Contact> = emptyList(),
    val recentContacts: List<Contact> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null
)