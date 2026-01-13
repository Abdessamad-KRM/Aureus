package com.example.aureus.ui.home.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncResult
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Phase 14: Tests - HomeViewModel Unit Tests
 *
 * Tests complets pour HomeViewModel vérifiant:
 * - Chargement des données utilisateur
 * - Balance calculation
 * - Send money functionality
 * - Add card functionality
 * - Offline mode handling
 * - Analytics tracking
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var firebaseDataManager: FirebaseDataManager

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var offlineSyncManager: OfflineSyncManager

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: HomeViewModel

    private val testUserId = "test_user_id"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Setup default behavior for mocks
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        whenever(offlineSyncManager.getSyncStatus()).thenReturn(SyncStatus(isOnline = true, null, 0, false))
        whenever(offlineSyncManager.syncNow()).thenReturn(SyncResult.Success("Synced"))

        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== USER DATA TESTS ====================

    @Test
    fun `uiState should have default values initially`() {
        // Then
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertEquals(true, state.isLoading)
        assertEquals(0.0, state.totalBalance)
        assertEquals(emptyList<Map<String, Any>>(), state.cards)
        assertEquals(emptyList<Map<String, Any>>(), state.recentTransactions)
    }

    @Test
    fun `getCurrentUserName should return first name when available`() {
        // Given
        val userData = mapOf(
            "firstName" to "John",
            "lastName" to "Doe"
        )
        // Mock the user data flow
        setUserFlow(userData)

        // When
        val result = viewModel.getCurrentUserName()

        // Then
        assertEquals("John", result)
    }

    @Test
    fun `getCurrentUserName should return User when no first name`() {
        // Given
        val userData = mapOf(
            "firstName" to "",
            "lastName" to ""
        )
        setUserFlow(userData)

        // When
        val result = viewModel.getCurrentUserName()

        // Then
        assertEquals("User", result)
    }

    @Test
    fun `getCurrentUserName should handle null user`() {
        // Given - User is null
        whenever(firebaseDataManager.getUser(testUserId)).thenReturn(flowOf(null))

        // Re-create ViewModel to trigger init
        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )

        // When
        val result = viewModel.getCurrentUserName()

        // Then
        assertEquals("User", result)
    }

    // ==================== SEND MONEY TESTS ====================

    @Test
    fun `sendMoney should return success on valid transaction`() = runTest {
        // Given
        whenever(
            firebaseDataManager.createTransaction(any())
        ).thenReturn(Result.success("txn_123"))

        // When
        val results = mutableListOf<Result<String>>()
        viewModel.sendMoney(100.0, "recipient@test.com").collect { result ->
            results.add(result)
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals("Money sent to recipient@test.com!", results[0].getOrNull())

        // Verify analytics tracking
        verify(analyticsManager, times(1)).trackTransferSent(
            userId = testUserId,
            amount = 100.0,
            recipient = "recipient@test.com",
            method = "wallet_to_wallet"
        )
        verify(analyticsManager, times(1)).trackTransactionCreated(
            userId = testUserId,
            type = "EXPENSE",
            category = "Transfer",
            amount = 100.0,
            method = "wallet_to_wallet"
        )
    }

    @Test
    fun `sendMoney should return error when user not logged in`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // Re-create ViewModel
        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )

        // When
        val results = mutableListOf<Result<String>>()
        viewModel.sendMoney(100.0, "recipient@test.com").collect { result ->
            results.add(result)
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertEquals("User not logged in", results[0].exceptionOrNull()?.message)
    }

    @Test
    fun `sendMoney should track failed transaction`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        whenever(
            firebaseDataManager.createTransaction(any())
        ).thenReturn(Result.failure(Exception("Transaction failed")))

        // When
        val results = mutableListOf<Result<String>>()
        viewModel.sendMoney(100.0, "recipient@test.com").collect { result ->
            results.add(result)
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)

        // Verify error tracking
        verify(analyticsManager, times(1)).trackTransactionFailed(
            userId = testUserId,
            error = "Transaction failed"
        )
    }

    // ==================== ADD CARD TESTS ====================

    @Test
    fun `addCard should return success on valid card addition`() = runTest {
        // Given
        val userInfo = mapOf("firstName" to "John", "lastName" to "Doe")
        setUserFlow(userInfo)

        whenever(
            firebaseDataManager.addCard(any(), any(), any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(Result.success("card_456"))

        // When
        val results = mutableListOf<Result<String>>()
        viewModel.addCard("VISA").collect { result ->
            results.add(result)
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals("Card added successfully!", results[0].getOrNull())

        // Verify analytics tracking
        verify(analyticsManager, times(1)).trackCardAdded(userId = testUserId, cardType = "VISA")
    }

    @Test
    fun `addCard should return error when user not logged in`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // Re-create ViewModel
        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )

        // When
        val results = mutableListOf<Result<String>>()
        viewModel.addCard("VISA").collect { result ->
            results.add(result)
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertEquals("User not logged in", results[0].exceptionOrNull()?.message)
    }

    // ==================== REFRESH DATA TESTS ====================

    @Test
    fun `refreshData should reload all data when online`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        whenever(offlineSyncManager.getSyncStatus()).thenReturn(
            SyncStatus(isOnline = true, null, 0, false)
        )
        whenever(offlineSyncManager.syncNow()).thenReturn(SyncResult.Success("Synced"))

        // Mock Firebase data flows
        setupCardFlow()
        setupTransactionFlow()
        setupBalanceFlow()
        setUserFlow(mapOf("firstName" to "Test", "lastName" to "User"))

        // Create ViewModel with mocks
        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )

        // Wait for initial load to complete
        advanceUntilIdle()

        // When
        viewModel.refreshData()
        advanceUntilIdle()

        // Then
        verify(offlineSyncManager, atLeastOnce()).getSyncStatus()
    }

    @Test
    fun `refreshData should load from cache when offline`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        whenever(offlineSyncManager.getSyncStatus()).thenReturn(
            SyncStatus(isOnline = false, null, 0, false)
        )

        // Create ViewModel with mocks
        viewModel = HomeViewModel(
            firebaseDataManager,
            database,
            offlineSyncManager,
            analyticsManager
        )

        // Wait for initial load
        advanceUntilIdle()

        // When
        viewModel.refreshData()
        advanceUntilIdle()

        // Then - should try to load from cache (verify database calls)
        verify(database, atLeastOnce()).cardDao()
        verify(database, atLeastOnce()).transactionDao()
    }

    // ==================== ANALYTICS TESTS ====================

    @Test
    fun `trackScreenView should call analytics manager`() {
        // When
        viewModel.trackScreenView("HomeScreen")

        // Then
        verify(analyticsManager, times(1)).trackScreenView("HomeScreen")
    }

    @Test
    fun `trackBalanceCheck should call analytics manager when logged in`() {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)

        // When
        viewModel.trackBalanceCheck(1500.0, "home_dashboard")

        // Then
        verify(analyticsManager, times(1)).trackBalanceCheck(
            userId = testUserId,
            balance = 1500.0,
            source = "home_dashboard"
        )
    }

    @Test
    fun `trackBalanceCheck should not call analytics when not logged in`() {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        viewModel.trackBalanceCheck(1500.0, "home_dashboard")

        // Then
        verify(analyticsManager, never()).trackBalanceCheck(any(), any(), any())
    }

    // ==================== OFFLINE MODE TESTS ====================

    @Test
    fun `trackOfflineModeEnabled should call analytics manager`() {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)

        // When
        viewModel.trackOfflineModeEnabled()

        // Then
        verify(analyticsManager, times(1)).trackOfflineModeEnabled(testUserId)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun setUserFlow(userData: Map<String, Any>?) {
        whenever(firebaseDataManager.getUser(testUserId)).thenReturn(flowOf(userData))
    }

    private fun setupCardFlow() {
        val cardData = listOf(
            mapOf(
                "cardId" to "card_123",
                "userId" to testUserId,
                "cardNumber" to "4242",
                "cardHolder" to "John Doe",
                "expiryDate" to "12/28",
                "cardType" to "VISA",
                "isDefault" to true
            )
        )
        whenever(firebaseDataManager.getUserCards(testUserId)).thenReturn(flowOf(cardData))
    }

    private fun setupTransactionFlow() {
        val transactionData = listOf(
            mapOf(
                "transactionId" to "txn_123",
                "userId" to testUserId,
                "type" to "EXPENSE",
                "category" to "Food",
                "title" to "Coffee",
                "amount" to 25.0,
                "date" to Timestamp.now(),
                "description" to "Coffee shop"
            )
        )
        whenever(firebaseDataManager.getRecentTransactions(testUserId, 5)).thenReturn(
            flowOf(transactionData)
        )
    }

    private fun setupBalanceFlow() {
        whenever(firebaseDataManager.getUserTotalBalance(testUserId)).thenReturn(flowOf(1500.0))
    }
}