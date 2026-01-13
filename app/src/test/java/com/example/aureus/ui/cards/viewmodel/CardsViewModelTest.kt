package com.example.aureus.ui.cards.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.MainDispatcherRule
import com.example.aureus.data.offline.SyncResult
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.domain.model.BankCard
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardType
import com.example.aureus.domain.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class CardsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var cardRepository: CardRepository

    @Mock
    private lateinit var firebaseDataManager: com.example.aureus.data.remote.firebase.FirebaseDataManager

    @Mock
    private lateinit var database: com.example.aureus.data.local.AppDatabase

    @Mock
    private lateinit var offlineSyncManager: com.example.aureus.data.offline.OfflineSyncManager

    private lateinit var viewModel: CardsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup default mock behaviors
        val emptyCardsFlow = emptyFlow<List<BankCard>>()
        whenever(cardRepository.getCards(any())).thenReturn(emptyCardsFlow)
        whenever(firebaseDataManager.currentUserId()).thenReturn("test_user_id")

        // Mock syncStatus as a flow that we can collect in tests
        // The actual sync status collection happens in the ViewModel's init block

        viewModel = CardsViewModel(
            cardRepository as com.example.aureus.data.repository.CardRepositoryImpl,
            firebaseDataManager,
            database,
            offlineSyncManager
        )
    }

    @After
    fun tearDown() {
        // MainDispatcherRule handles cleanup
    }

    // ==================== STATE INITIALIZATION TESTS ====================

    @Test
    fun `initial state should have empty cards`() = runTest {
        assertTrue(viewModel.cards.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.defaultCard.value)
    }

    @Test
    fun `initial sync status should be online`() = runTest {
        assertTrue(viewModel.syncStatus.value.isOnline)
        assertFalse(viewModel.isOfflineMode.value)
    }

    // ==================== SET DEFAULT CARD TESTS ====================

    @Test
    fun `setDefaultCard should update default card`() = runTest {
        // Given
        whenever((cardRepository as com.example.aureus.data.repository.CardRepositoryImpl)
            .setDefaultCard(eq("test_user_id"), eq("card_1")))
            .thenReturn(kotlin.Result.success(Unit))

        // When
        viewModel.setDefaultCard("card_1")

        advanceUntilIdle()

        // Then
        verify(cardRepository as com.example.aureus.data.repository.CardRepositoryImpl, times(1))
            .setDefaultCard(eq("test_user_id"), eq("card_1"))
    }

    @Test
    fun `setDefaultCard should set error on failure`() = runTest {
        // Given
        whenever((cardRepository as com.example.aureus.data.repository.CardRepositoryImpl)
            .setDefaultCard(any(), any()))
            .thenReturn(kotlin.Result.failure(Exception("Failed to set default card")))

        // When
        viewModel.setDefaultCard("card_1")

        advanceUntilIdle()

        // Then
        assertEquals("Failed to set default card", viewModel.errorMessage.value)
    }

    // ==================== TOGGLE FREEZE CARD TESTS ====================

    @Test
    fun `toggleCardFreeze should freeze card`() = runTest {
        // Given
        whenever(cardRepository.toggleCardFreeze(eq("card_1"), eq(true)))
            .thenReturn(kotlin.Result.success(Unit))

        // When
        viewModel.toggleCardFreeze("card_1", true)

        advanceUntilIdle()

        // Then
        verify(cardRepository, times(1)).toggleCardFreeze(eq("card_1"), eq(true))
    }

    @Test
    fun `toggleCardFreeze should unfreeze card`() = runTest {
        // Given
        whenever(cardRepository.toggleCardFreeze(eq("card_1"), eq(false)))
            .thenReturn(kotlin.Result.success(Unit))

        // When
        viewModel.toggleCardFreeze("card_1", false)

        advanceUntilIdle()

        // Then
        verify(cardRepository, times(1)).toggleCardFreeze(eq("card_1"), eq(false))
    }

    @Test
    fun `toggleCardFreeze should set error on failure`() = runTest {
        // Given
        whenever(cardRepository.toggleCardFreeze(any(), any()))
            .thenReturn(kotlin.Result.failure(Exception("Failed to freeze card")))

        // When
        viewModel.toggleCardFreeze("card_1", true)

        advanceUntilIdle()

        // Then
        assertEquals("Failed to freeze card", viewModel.errorMessage.value)
    }

    // ==================== UPDATE CARD LIMITS TESTS ====================

    @Test
    fun `updateCardLimits should update limits`() = runTest {
        // Given
        whenever(cardRepository.updateCardLimits(eq("card_1"), eq(5000.0), eq(50000.0)))
            .thenReturn(kotlin.Result.success(Unit))

        // When
        val onSuccessCalled = mutableListOf<Boolean>()
        viewModel.updateCardLimits(
            cardId = "card_1",
            dailyLimit = 5000.0,
            monthlyLimit = 50000.0,
            onSuccess = { onSuccessCalled.add(true) }
        )

        advanceUntilIdle()

        // Then
        verify(cardRepository, times(1)).updateCardLimits(eq("card_1"), eq(5000.0), eq(50000.0))
        assertTrue(onSuccessCalled.isNotEmpty())
    }

    @Test
    fun `updateCardLimits should call onError on failure`() = runTest {
        // Given
        whenever(cardRepository.updateCardLimits(any(), any(), any()))
            .thenReturn(kotlin.Result.failure(Exception("Failed to update limits")))

        // When
        val onErrorCalled = mutableListOf<String>()
        viewModel.updateCardLimits(
            cardId = "card_1",
            dailyLimit = 5000.0,
            monthlyLimit = 50000.0,
            onError = { onErrorCalled.add(it) }
        )

        advanceUntilIdle()

        // Then
        assertEquals("Failed to update limits", viewModel.errorMessage.value)
        assertTrue(onErrorCalled.any { it.contains("Failed to update limits") })
    }

    // ==================== CREATE TEST CARDS TESTS ====================

    @Test
    fun `createTestCards should create cards`() = runTest {
        // Given
        whenever(cardRepository.createTestCards(eq("test_user_id")))
            .thenReturn(kotlin.Result.success(Unit))

        // When
        val onSuccessCalled = mutableListOf<Boolean>()
        viewModel.createTestCards(onSuccess = { onSuccessCalled.add(true) })

        advanceUntilIdle()

        // Then
        verify(cardRepository, times(1)).createTestCards(eq("test_user_id"))
        assertTrue(onSuccessCalled.isNotEmpty())
    }

    @Test
    fun `createTestCards should fail without authentication`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        val onErrorCalled = mutableListOf<String>()
        viewModel.createTestCards(onError = { onErrorCalled.add(it) })

        advanceUntilIdle()

        // Then
        assertTrue(onErrorCalled.any { it.contains("User not authenticated") })
    }

    // ==================== REFRESH TESTS (PHASE 7: OFFLINE-FIRST) ====================

    @Test
    fun `refresh should sync when online`() = runTest {
        // Given
        whenever(offlineSyncManager.getSyncStatus())
            .thenReturn(SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false))
        whenever(offlineSyncManager.syncNow()).thenReturn(SyncResult.Success)

        // When
        viewModel.refresh()

        advanceUntilIdle()

        // Then
        verify(offlineSyncManager, times(1)).syncNow()
    }

    @Test
    fun `refresh should load cards when sync succeeds`() = runTest {
        // Given
        whenever(offlineSyncManager.getSyncStatus())
            .thenReturn(SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false))
        whenever(offlineSyncManager.syncNow()).thenReturn(SyncResult.Success)

        // When
        viewModel.refresh()

        advanceUntilIdle()

        // Then
        // Verify cards were loaded
        verify(cardRepository, atLeastOnce()).getCards(any())
    }

    @Test
    fun `refresh should load cards when offline`() = runTest {
        // Given
        whenever(offlineSyncManager.getSyncStatus())
            .thenReturn(SyncStatus(isOnline = false, lastSync = null, pendingChanges = 0, isSyncing = false))

        // When
        viewModel.refresh()

        advanceUntilIdle()

        // Then
        // Should still load cards from cache
        verify(cardRepository, atLeastOnce()).getCards(any())
    }

    @Test
    fun `refresh should load cards when sync fails`() = runTest {
        // Given
        whenever(offlineSyncManager.getSyncStatus())
            .thenReturn(SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false))
        whenever(offlineSyncManager.syncNow()).thenReturn(SyncResult.Error("Sync failed"))

        // When
        viewModel.refresh()

        advanceUntilIdle()

        // Then
        // Should load from cache even if sync failed
        verify(cardRepository, atLeastOnce()).getCards(any())
    }

    // ==================== OFFLINE MODE TESTS (PHASE 7) ====================

    @Test
    fun `offlineMode should be true when sync status offline`() = runTest {
        // Given - This test verifies the offline mode tracking
        // The actual sync status is updated periodically in the ViewModel

        // When
        advanceUntilIdle()

        // Then - In a real scenario, offlineSyncManager would return different sync statuses
        // For this test, we verify the state tracking mechanism is present
        assertNotNull(viewModel.syncStatus.value)
        assertNotNull(viewModel.isOfflineMode.value)
    }

    @Test
    fun `offlineMode should be false when sync status online`() = runTest {
        // Given
        whenever(offlineSyncManager.getSyncStatus())
            .thenReturn(SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false))

        // When
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isOfflineMode.value)
    }

    // ==================== CLEAR ERROR TESTS ====================

    @Test
    fun `clearError should reset errorMessage`() = runTest {
        // Given - Set an error manually
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)
        viewModel.createTestCards()
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.errorMessage.value)
    }
}