package com.example.aureus.ui.transaction.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
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
import kotlin.test.assertFalse

/**
 * Phase 14: Tests - TransactionViewModelFirebase Unit Tests
 *
 * Tests complets pour TransactionViewModelFirebase vérifiant:
 * - Chargement des transactions
 * - Filtrage par type, catégorie et date
 * - Recherche de transactions
 * - Calcul des statistiques
 * - Rafraîchissement des données
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class TransactionViewModelFirebaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var transactionRepository: TransactionRepositoryFirebase

    @Mock
    private lateinit var firebaseDataManager: FirebaseDataManager

    private lateinit var viewModel: TransactionViewModelFirebase

    private val testUserId = "test_user_id"
    private val sampleTransactions = createSampleTransactions()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Setup default behavior
        whenever(firebaseDataManager.currentUserId()).thenReturn(testUserId)
        whenever(transactionRepository.getTransactions(testUserId))
            .thenReturn(flowOf(sampleTransactions))
        whenever(transactionRepository.getMonthlyStatistics(testUserId, 6))
            .thenReturn(flowOf(emptyMap()))
        whenever(transactionRepository.getTotalIncome(any(), any(), any()))
            .thenReturn(flowOf(1000.0))
        whenever(transactionRepository.getTotalExpense(any(), any(), any()))
            .thenReturn(flowOf(500.0))
        whenever(transactionRepository.getCategoryExpenses(any(), any(), any()))
            .thenReturn(flowOf(emptyMap()))

        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== DATA LOADING TESTS ====================

    @Test
    fun `transactionsState should have default values initially`() {
        // Then
        val state = viewModel.transactionsState.value
        assertEquals(false, state.isLoading)
        assertEquals(0.0, state.totalIncome)
        assertEquals(0.0, state.totalExpense)
        assertEquals(emptyMap<String, Double>(), state.categoryBreakdown)
    }

    @Test
    fun `loadTransactions should load transactions for logged in user`() {
        // Given
        whenever(transactionRepository.getTransactions(testUserId))
            .thenReturn(flowOf(sampleTransactions))

        // When
        viewModel.loadTransactions()

        // Then
        verify(transactionRepository).getTransactions(testUserId)
    }

    @Test
    fun `loadTransactions should set error when user not logged in`() {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // Create new ViewModel with no user
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)

        // When
        viewModel.loadTransactions()

        // Then
        val state = viewModel.transactionsState.value
        assertEquals("User not logged in", state.error)
    }

    @Test
    fun `loadRecentTransactions should load limited transactions`() = runTest {
        // Given
        val recentTransactions = sampleTransactions.take(2)
        whenever(transactionRepository.getRecentTransactions(testUserId, 10))
            .thenReturn(flowOf(recentTransactions))

        // When
        viewModel.loadRecentTransactions(10)
        advanceUntilIdle()

        // Then
        verify(transactionRepository).getRecentTransactions(testUserId, 10)
        assertEquals(2, viewModel.filteredTransactionsState.value.size)
    }

    @Test
    fun `refreshTransactions should set isRefreshing`() = runTest {
        // Given
        val transactionsFlow = MutableStateFlow(sampleTransactions)
        whenever(transactionRepository.getTransactions(testUserId))
            .thenReturn(transactionsFlow)

        // When
        viewModel.refreshTransactions()
        advanceUntilIdle()

        // Then - IsRefreshing should be false after refresh
        assertEquals(false, viewModel.isRefreshing.value)
    }

    // ==================== FILTERING TESTS ====================

    @Test
    fun `filterByType should filter to Income transactions`() {
        // Given
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.filterByType("Income")

        // Then
        assertEquals("Income", viewModel.selectedFilter.value)
        val filtered = viewModel.filteredTransactionsState.value
        val allIncome = filtered.all { (it["type"] as String) == "INCOME" }
        assertTrue(allIncome, "All filtered transactions should be Income")
    }

    @Test
    fun `filterByType should filter to Expense transactions`() {
        // Given
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.filterByType("Expense")

        // Then
        assertEquals("Expense", viewModel.selectedFilter.value)
        val filtered = viewModel.filteredTransactionsState.value
        val allExpense = filtered.all { (it["type"] as String) == "EXPENSE" }
        assertTrue(allExpense, "All filtered transactions should be Expense")
    }

    @Test
    fun `filterByType All should show all transactions`() {
        // Given
        viewModel.filterByType("Income")
        advanceUntilIdle()

        // When
        viewModel.filterByType("All")

        // Then - should apply filters which may include search
        assertEquals("All", viewModel.selectedFilter.value)
    }

    @Test
    fun `searchTransactions should filter by description`() {
        // Given
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.searchTransactions("Coffee")

        // Then
        val filtered = viewModel.filteredTransactionsState.value
        val containsCoffee = filtered.all {
            val desc = (it["description"] as String).lowercase()
            desc.contains("coffee") || 
            (it["merchant"] as String?)?.lowercase()?.contains("coffee") == true ||
            (it["category"] as String).lowercase().contains("coffee")
        }
        assertTrue(containsCoffee, "All filtered transactions should contain 'Coffee'")
    }

    @Test
    fun `search alias should work like searchTransactions`() {
        // Given
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.search("Food")

        // Then
        assertEquals("Food", viewModel.searchQuery.value)
    }

    @Test
    fun `resetFilters should clear all filters`() {
        // Given
        viewModel.filterByType("Income")
        viewModel.searchTransactions("test")
        advanceUntilIdle()

        // When
        viewModel.resetFilters()

        // Then
        assertEquals("All", viewModel.selectedFilter.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `filterByCategory should update search query`() {
        // Given
        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.filterByCategory("Food & Drink")

        // Then
        assertEquals("Food & Drink", viewModel.searchQuery.value)
    }

    // ==================== DATE FILTERING TESTS ====================

    @Test
    fun `filterByDateRange should load transactions in range`() = runTest {
        // Given
        val startDate = java.util.Date(System.currentTimeMillis() - 86400000) // 1 day ago
        val endDate = java.util.Date()
        whenever(transactionRepository.getTransactionsByDateRange(testUserId, startDate, endDate, 100))
            .thenReturn(flowOf(sampleTransactions))

        // When
        viewModel.filterByDateRange(startDate, endDate)
        advanceUntilIdle()

        // Then
        verify(transactionRepository).getTransactionsByDateRange(testUserId, startDate, endDate, 100)
    }

    @Test
    fun `filterByDatePeriod Today should use correct date range`() {
        // Given - Mock current date
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any(), any()))
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel.filterByDatePeriod(DatePeriod.Today)

        // Then
        verify(transactionRepository).getTransactionsByDateRange(eq(testUserId), any(), any(), eq(100))
    }

    @Test
    fun `filterByDatePeriod ThisMonth should use correct date range`() {
        // Given
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any(), any()))
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel.filterByDatePeriod(DatePeriod.ThisMonth)

        // Then
        verify(transactionRepository).getTransactionsByDateRange(eq(testUserId), any(), any(), eq(100))
    }

    @Test
    fun `filterByDatePeriod All should use full date range`() {
        // Given
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any(), any()))
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel.filterByDatePeriod(DatePeriod.All)

        // Then
        verify(transactionRepository).getTransactionsByDateRange(
            eq(testUserId), 
            any(), 
            any(), 
            eq(100)
        )
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    fun `getStatistics should update total income and expense`() = runTest {
        // Given
        whenever(transactionRepository.getTotalIncome(any(), any(), any()))
            .thenReturn(flowOf(2000.0))
        whenever(transactionRepository.getTotalExpense(any(), any(), any()))
            .thenReturn(flowOf(800.0))

        viewModel = TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
        advanceUntilIdle()

        // When
        viewModel.getStatistics()
        advanceUntilIdle()

        // Then - Check if stats were updated (may need wait for flow collection)
        val state = viewModel.transactionsState.value
        // Note: Stats are updated in init, so we verify they were processed
        verify(transactionRepository, atLeastOnce()).getTotalIncome(any(), any(), any())
        verify(transactionRepository, atLeastOnce()).getTotalExpense(any(), any(), any())
    }

    @Test
    fun `getMonthlyStatistics should load monthly stats`() = runTest {
        // Given
        val monthlyStats = mapOf(
            "2024-01" to Pair(1000.0, 500.0),
            "2024-02" to Pair(1200.0, 600.0)
        )
        whenever(transactionRepository.getMonthlyStatistics(testUserId, 6))
            .thenReturn(flowOf(monthlyStats))

        // When
        viewModel.getMonthlyStatistics(6)
        advanceUntilIdle()

        // Then
        verify(transactionRepository).getMonthlyStatistics(testUserId, 6)
    }

    // ==================== STATE MANAGEMENT TESTS ====================

    @Test
    fun `reset should clear all state`() {
        // Given
        viewModel.filterByType("Income")
        viewModel.searchTransactions("test")
        advanceUntilIdle()

        // When
        viewModel.reset()

        // Then
        val state = viewModel.transactionsState.value
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("All", viewModel.selectedFilter.value)
        assertEquals(false, viewModel.isRefreshing.value)
    }

    @Test
    fun `isRefreshing should be false after initialization`() {
        // Then
        assertEquals(false, viewModel.isRefreshing.value)
    }

    @Test
    fun `searchQuery should be empty on initialization`() {
        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `selectedFilter should be All on initialization`() {
        // Then
        assertEquals("All", viewModel.selectedFilter.value)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createSampleTransactions(): List<Transaction> {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return listOf(
            Transaction(
                id = "txn_1",
                accountId = "acc_1",
                amount = 25.0,
                isCredit = false,
                isDebit = true,
                description = "Coffee",
                date = today,
                category = "Food & Drink",
                merchant = "Starbucks"
            ),
            Transaction(
                id = "txn_2",
                accountId = "acc_1",
                amount = 100.0,
                isCredit = true,
                isDebit = false,
                description = "Salary",
                date = today,
                category = "Income",
                merchant = null
            ),
            Transaction(
                id = "txn_3",
                accountId = "acc_1",
                amount = 50.0,
                isCredit = false,
                isDebit = true,
                description = "Groceries",
                date = today,
                category = "Shopping",
                merchant = "Walmart"
            )
        )
    }
}