package com.example.aureus.ui.contact.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.MainDispatcherRule
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.domain.repository.ContactRepository
import com.example.aureus.domain.model.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import java.util.Date

/**
 * ContactViewModel Test
 * Tests for contact management, search, filters, and transfer operations
 */
@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class ContactViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var firebaseDataManager: FirebaseDataManager

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var offlineSyncManager: com.example.aureus.data.offline.OfflineSyncManager

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: ContactViewModel

    private val testUserId = "test_user_id"
    private val testContacts = listOf(
        Contact(
            id = "contact_1",
            name = "John Doe",
            phone = "+1234567890",
            email = "john@example.com",
            accountNumber = "ACC123",
            category = ContactCategory.FAMILY,
            isBankContact = true,
            isFavorite = true,
            createdAt = Date(),
            updatedAt = Date()
        ),
        Contact(
            id = "contact_2",
            name = "Jane Smith",
            phone = "+0987654321",
            email = "jane@example.com",
            accountNumber = null,
            category = ContactCategory.FRIENDS,
            isBankContact = false,
            isFavorite = false,
            createdAt = Date(),
            updatedAt = Date()
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup default mock behaviors
        `when`(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        `when`(contactRepository.getContacts(testUserId)).thenReturn(emptyFlow())
    }

    @After
    fun tearDown() {
        // MainDispatcherRule handles cleanup
    }

    // ==================== INITIAL STATE TESTS ====================

    @Test
    fun `initial state should have empty contacts`() = runTest {
        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.contacts.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== LOAD CONTACTS TESTS ====================

    @Test
    fun `loadContacts should populate uiState with contacts`() = runTest {
        // Given
        `when`(contactRepository.getContacts(testUserId)).thenReturn(flowOf(testContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.uiState.value.contacts.size)
        assertEquals("John Doe", viewModel.uiState.value.contacts[0].name)
        verify(contactRepository).getContacts(testUserId)
    }

    @Test
    fun `loadContacts should set error without user`() = runTest {
        // Given
        `when`(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        advanceUntilIdle()

        // Then
        assertEquals("User not logged in", viewModel.uiState.value.error)
        verify(contactRepository, never()).getContacts(any())
    }

    // ==================== LOAD FAVORITE CONTACTS TESTS ====================

    @Test
    fun `loadFavoriteContacts should populate favorite contacts`() = runTest {
        // Given
        val favorites = testContacts.filter { it.isFavorite }
        `when`(contactRepository.getFavoriteContacts(testUserId)).thenReturn(flowOf(favorites))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.loadFavoriteContacts()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.favoriteContacts.size)
        assertTrue(viewModel.uiState.value.favoriteContacts[0].isFavorite)
    }

    // ==================== LOAD BANK CONTACTS TESTS ====================

    @Test
    fun `loadBankContacts should populate bank contacts`() = runTest {
        // Given
        val bankContacts = testContacts.filter { it.isBankContact }
        `when`(contactRepository.getBankContacts(testUserId)).thenReturn(flowOf(bankContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.loadBankContacts()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.bankContacts.size)
        assertTrue(viewModel.uiState.value.bankContacts[0].isBankContact)
    }

    // ==================== LOAD RECENT CONTACTS TESTS ====================

    @Test
    fun `loadRecentContacts should load limited recent contacts`() = runTest {
        // Given
        `when`(contactRepository.getRecentContacts(testUserId, 5)).thenReturn(flowOf(testContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.loadRecentContacts(5)
        advanceUntilIdle()

        // Then
        verify(contactRepository).getRecentContacts(testUserId, 5)
    }

    // ==================== ADD CONTACT TESTS ====================

    @Test
    fun `addContact should call repository and track analytics`() = runTest {
        // Given
        `when`(contactRepository.addContact(any(), any()))
            .thenReturn(Resource.Success("new_contact_id"))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.addContact(
            name = "New Contact",
            phone = "+1112223333",
            email = "new@example.com",
            accountNumber = "ACC999",
            category = ContactCategory.OTHER
        )
        advanceUntilIdle()

        // Then
        verify(contactRepository).addContact(eq(testUserId), any())
        verify(analyticsManager).trackContactAdded(testUserId)
        assertEquals("Contact added successfully", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `addContact should set error on failure`() = runTest {
        // Given
        `when`(contactRepository.addContact(any(), any()))
            .thenReturn(Resource.Error("Failed to add"))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.addContact(
            name = "New Contact",
            phone = "+1112223333",
            email = "new@example.com",
            accountNumber = "ACC999",
            category = ContactCategory.OTHER
        )
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.error?.contains("Failed") == true)
    }

    @Test
    fun `addContact should fail without user`() = runTest {
        // Given
        `when`(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.addContact(
            name = "New Contact",
            phone = "+1112223333"
        )
        advanceUntilIdle()

        // Then
        assertEquals("User not logged in", viewModel.uiState.value.error)
    }

    // ==================== UPDATE CONTACT TESTS ====================

    @Test
    fun `updateContact should call repository`() = runTest {
        // Given
        val updatedContact = testContacts[0].copy(name = "Updated Name")
        `when`(contactRepository.updateContact(any(), any()))
            .thenReturn(Resource.Success(Unit))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.updateContact(updatedContact)
        advanceUntilIdle()

        // Then
        verify(contactRepository).updateContact(eq("contact_1"), any())
        assertEquals("Contact updated successfully", viewModel.uiState.value.successMessage)
    }

    // ==================== DELETE CONTACT TESTS ====================

    @Test
    fun `deleteContact should call repository and track analytics`() = runTest {
        // Given
        `when`(contactRepository.deleteContact("contact_1"))
            .thenReturn(Resource.Success(Unit))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.deleteContact("contact_1")
        advanceUntilIdle()

        // Then
        verify(contactRepository).deleteContact("contact_1")
        verify(analyticsManager).trackContactRemoved(testUserId)
        assertEquals("Contact deleted successfully", viewModel.uiState.value.successMessage)
    }

    // ==================== TOGGLE FAVORITE TESTS ====================

    @Test
    fun `toggleFavorite should toggle favorite status`() = runTest {
        // Given
        `when`(contactRepository.toggleFavorite("contact_1", false))
            .thenReturn(Resource.Success(Unit))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(contacts = testContacts)
        viewModel.toggleFavorite("contact_1")
        advanceUntilIdle()

        // Then
        verify(contactRepository).toggleFavorite("contact_1", false)
        assertEquals("Favorite status updated", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `toggleFavorite should work with contact object`() = runTest {
        // Given
        `when`(contactRepository.toggleFavorite("contact_1", false))
            .thenReturn(Resource.Success(Unit))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(contacts = testContacts)
        viewModel.toggleFavorite(testContacts[0])
        advanceUntilIdle()

        // Then
        verify(contactRepository).toggleFavorite("contact_1", false)
    }

    // ==================== SEARCH TESTS (TRANSFER FUNCTIONALITY) ====================

    @Test
    fun `searchContacts should filter by name`() = runTest {
        // Given
        val searchResults = listOf(testContacts[0])
        `when`(contactRepository.searchContacts(testUserId, "John"))
            .thenReturn(flowOf(searchResults))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.searchContacts("John")
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("John Doe", viewModel.uiState.value.filteredContacts[0].name)
        assertEquals("John", viewModel.searchQuery.value)
    }

    @Test
    fun `searchContacts should filter by phone number`() = runTest {
        // Given
        val searchResults = listOf(testContacts[1])
        `when`(contactRepository.searchContacts(testUserId, "+0987"))
            .thenReturn(flowOf(searchResults))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.searchContacts("+0987")
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Jane Smith", viewModel.uiState.value.filteredContacts[0].name)
    }

    @Test
    fun `searchContacts should filter by email`() = runTest {
        // Given
        val searchResults = listOf(testContacts[1])
        `when`(contactRepository.searchContacts(testUserId, "jane@example"))
            .thenReturn(flowOf(searchResults))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.searchContacts("jane@example")
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
    }

    // ==================== FILTER BY CATEGORY TESTS ====================

    @Test
    fun `filterByCategory should filter contacts`() = runTest {
        // Given
        val familyContacts = testContacts.filter { it.category == ContactCategory.FAMILY }
        `when`(contactRepository.getContactsByCategory(testUserId, "FAMILY"))
            .thenReturn(flowOf(familyContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.filterByCategory(ContactCategory.FAMILY)
        advanceUntilIdle()

        // Then
        assertEquals(ContactCategory.FAMILY, viewModel.selectedCategory.value)
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
    }

    @Test
    fun `filterByCategory with null should reset filter`() = runTest {
        // Given
        `when`(contactRepository.getContacts(testUserId)).thenReturn(flowOf(testContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(contacts = testContacts)
        viewModel.filterByCategory(null)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedCategory.value)
        assertEquals(2, viewModel.uiState.value.filteredContacts.size)
    }

    // ==================== TOGGLE FAVORITES ONLY TESTS ====================

    @Test
    fun `toggleFavoritesOnly should show only favorites`() = runTest {
        // Given
        `when`(contactRepository.getContacts(testUserId)).thenReturn(flowOf(testContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(contacts = testContacts)
        viewModel.toggleFavoritesOnly()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.showFavoritesOnly.value)
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertTrue(viewModel.uiState.value.filteredContacts[0].isFavorite)
    }

    // ==================== RESET FILTERS TESTS ====================

    @Test
    fun `resetFilters should reset all filters`() = runTest {
        // Given
        `when`(contactRepository.getContacts(testUserId)).thenReturn(flowOf(testContacts))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(
            contacts = testContacts,
            filteredContacts = listOf(testContacts[0])
        )
        viewModel.resetFilters()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedCategory.value)
        assertFalse(viewModel.showFavoritesOnly.value)
        assertEquals(2, viewModel.uiState.value.filteredContacts.size)
    }

    // ==================== CLEAR MESSAGES TESTS ====================

    @Test
    fun `clearMessages should reset success and error messages`() = runTest {
        // Setup
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(
            successMessage = "Success",
            error = "Error"
        )

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== REFRESH TESTS (PHASE 7 OFFLINE-FIRST) ====================

    @Test
    fun `refresh should call repository refresh`() = runTest {
        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        verify(contactRepository).refreshContacts(testUserId)
    }

    @Test
    fun `refresh should fail without user`() = runTest {
        // Given
        `when`(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        verify(contactRepository, never()).refreshContacts(any())
    }

    // ==================== FIND CONTACT BY PHONE TESTS ====================

    @Test
    fun `findContactByPhone should return contact`() = runTest {
        // Given
        `when`(contactRepository.findContactByPhone(testUserId, "+1234567890"))
            .thenReturn(Resource.Success(testContacts[0]))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        val result = viewModel.findContactByPhone("+1234567890")

        // Then
        assertNotNull(result)
        assertEquals("John Doe", result?.name)
    }

    @Test
    fun `findContactByPhone should return null on error`() = runTest {
        // Given
        `when`(contactRepository.findContactByPhone(testUserId, "+0000000000"))
            .thenReturn(Resource.Error("Not found"))

        // When
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        val result = viewModel.findContactByPhone("+0000000000")

        // Then
        assertNull(result)
    }

    // ==================== RESET STATE TESTS ====================

    @Test
    fun `reset should clear all state`() = runTest {
        // Setup
        viewModel = ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
        viewModel.uiState.value = viewModel.uiState.value.copy(
            contacts = testContacts,
            filteredContacts = testContacts,
            successMessage = "Success",
            error = "Error"
        )

        // When
        viewModel.reset()

        // Then
        assertTrue(viewModel.uiState.value.contacts.isEmpty())
        assertTrue(viewModel.uiState.value.filteredContacts.isEmpty())
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedCategory.value)
        assertFalse(viewModel.showFavoritesOnly.value)
    }
}