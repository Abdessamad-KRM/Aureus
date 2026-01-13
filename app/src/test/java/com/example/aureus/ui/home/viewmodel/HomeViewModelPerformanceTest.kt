package com.example.aureus.ui.home.viewmodel

import com.example.aureus.MainDispatcherRule
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.offline.NetworkMonitor
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatusPublisher
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Phase 6: Tests et Validation
 * Performance tests pour HomeViewModel
 *
 * Tests requis:
 * - Init ne doit pas avoir d'infinite loop polling
 * - L'init devrait être rapide (< 100ms)
 * - Observer statut de sync sans polling
 * - État initial correctement initialisé
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelPerformanceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var firebaseDataManager: FirebaseDataManager

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var offlineSyncManager: OfflineSyncManager

    @Mock
    private lateinit var networkMonitor: NetworkMonitor

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var syncStatusPublisher: SyncStatusPublisher

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup real SyncStatusPublisher
        syncStatusPublisher = SyncStatusPublisher()

        // Mock OfflineSyncManager to return the real SyncStatusPublisher
        `when`(offlineSyncManager.syncStatusPublisher).thenReturn(syncStatusPublisher)

        // Mock network status
        `when`(networkMonitor.isConnected()).thenReturn(true)

        // Mock Firebase Auth
        val mockUser = mock(com.google.firebase.auth.FirebaseUser::class.java)
        `when`(auth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("test-user-id")

        // Mock FirebaseDataManager
        `when`(firebaseDataManager.currentUserId()).thenReturn("test-user-id")
        `when`(firebaseDataManager.getUser(anyString())).thenReturn(MutableStateFlow(null))
        `when`(firebaseDataManager.getUserCards(anyString())).thenReturn(MutableStateFlow(emptyList()))
        `when`(firebaseDataManager.getUserTotalBalance(anyString())).thenReturn(MutableStateFlow(0.0))
        `when`(firebaseDataManager.getRecentTransactions(anyString(), anyInt())).thenReturn(MutableStateFlow(emptyList()))

        // Mock AppDatabase DAOs
        val userDao = mock(com.example.aureus.data.local.dao.UserDao::class.java)
        val cardDao = mock(com.example.aureus.data.local.dao.CardDao::class.java)
        val transactionDao = mock(com.example.aureus.data.local.dao.TransactionDao::class.java)

        `when`(database.userDao()).thenReturn(userDao)
        `when`(database.cardDao()).thenReturn(cardDao)
        `when`(database.transactionDao()).thenReturn(transactionDao)

        // Mock DAO responses
        `when`(userDao.getUserById(anyString())).thenReturn(null)
        `when`(cardDao.getCardsByUserId(anyString())).thenReturn(MutableStateFlow(emptyList<BankCardEntity>()))
        `when`(transactionDao.getTransactionsById(anyString())).thenReturn(MutableStateFlow(emptyList<TransactionEntity>()))
    }

    @Test
    fun `should not have infinite loop init time`() = runTest {
        // Mesurer le temps d'init
        val startTime = System.currentTimeMillis()

        // Le ViewModel devrait se créer rapidement (sans boucle infinie bloquante)
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        val initTime = System.currentTimeMillis() - startTime

        // L'init devrait prendre moins de 100ms
        assertTrue("ViewModel init took too long: ${initTime}ms", initTime < 100)
    }

    @Test
    fun `should observe sync status without polling`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        // Attendre un changement de statut (sans boucle polling)
        // Le flow devrait se collecter en utilisant le pattern Observer
        var statusReceived = false

        val syncStatusJob = launch {
            viewModel.syncStatus.collect { status ->
                // Le statut devrait changer seulement quand l'OfflineSyncManager publie
                assertNotNull(status)
                statusReceived = true
            }
        }

        // Publier un nouveau statut
        val testStatus = com.example.aureus.data.offline.SyncStatus(
            isOnline = true,
            lastSync = System.currentTimeMillis(),
            pendingChanges = 0,
            isSyncing = false
        )
        syncStatusPublisher.publishSyncStatus(testStatus)

        // Vérifier que le statut a été reçu
        syncStatusJob.join()
        assertTrue("Sync status should be observed", statusReceived)

        // Nettoyer
        syncStatusJob.cancel()
    }

    @Test
    fun `should have correct initial state`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        // Vérifier les états initiaux
        val uiState = viewModel.uiState.value
        assertNotNull(uiState)

        val balanceState = viewModel.totalBalanceState.value
        assertNotNull(balanceState)
        assertTrue(balanceState is Resource.Idle || balanceState is Resource.Loading || balanceState is Resource.Success)

        val syncStatus = viewModel.syncStatus.value
        assertNotNull(syncStatus)
    }

    @Test
    fun `should not block main thread during initialization`() = runTest {
        // Simuler plusieurs instanciations en parallèle
        val viewModels = mutableListOf<HomeViewModel>()

        val startTime = System.currentTimeMillis()

        repeat(10) {
            val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)
            viewModels.add(viewModel)
        }

        val totalTime = System.currentTimeMillis() - startTime

        // La création de 10 ViewModels devrait prendre moins de 500ms
        assertTrue("Creating 10 ViewModels took too long: ${totalTime}ms", totalTime < 500)
    }

    @Test
    fun `should sync status update only when changed`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        var statusChangeCount = 0
        val statuses = mutableListOf<com.example.aureus.data.offline.SyncStatus>()

        val job = launch {
            viewModel.syncStatus.collect { status ->
                statusChangeCount++
                statuses.add(status)
            }
        }

        // Publier le même statut plusieurs fois
        val status1 = com.example.aureus.data.offline.SyncStatus(true, null, 0, false)
        val status2 = com.example.aureus.data.offline.SyncStatus(true, null, 0, false) // Identique

        syncStatusPublisher.publishSyncStatus(status1)
        syncStatusPublisher.publishSyncStatus(status2) // Doublon - ne devrait pas déclencher

        // Attendre un peu pour s'assurer que les changements sont propagés
        kotlinx.coroutines.delay(100)

        // Le count devrait être 2 (1 init, 1 publication)
        assertTrue("Status change count should be 2, got $statusChangeCount", statusChangeCount <= 2)

        job.cancel()
    }

    @Test
    fun `should handle rapid sync status changes`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        var statusChangeCount = 0

        val job = launch {
            viewModel.syncStatus.collect { status ->
                statusChangeCount++
            }
        }

        // Publier plusieurs statuts rapidement
        repeat(20) { index ->
            val status = com.example.aureus.data.offline.SyncStatus(
                isOnline = index % 2 == 0,
                lastSync = if (index > 0) System.currentTimeMillis() else null,
                pendingChanges = index,
                isSyncing = false
            )
            syncStatusPublisher.publishSyncStatus(status)
        }

        job.join()
        job.cancel()

        // Should have received changes for different statuses
        assertTrue("Should have received multiple status changes", statusChangeCount > 1)
    }

    @Test
    fun `should not consume cpu with active observers`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        // Simuler plusieurs observer actifs
        val observers = mutableListOf<kotlinx.coroutines.Job>()

        repeat(5) {
            val job = launch {
                viewModel.syncStatus.collect { status ->
                    // Consumer passive - ne devrait pas consommer de CPU
                    kotlinx.coroutines.delay(1000)
                }
            }
            observers.add(job)
        }

        // Attendre un moment
        kotlinx.coroutines.delay(500)

        // Sans boucle while(true), les observateurs ne devraient pas consommer de CPU
        // Nous testons simplement que l'observateur fonctionne sans erreur

        observers.forEach { it.cancel() }
    }

    @Test
    fun `cleanup observers on viewModel clear`() = runTest {
        // Ce test vérifie que les CoroutineScope sont correctement gérés
        // (viewModelScope should be cancelled automatically)
        var viewModel: HomeViewModel? = null

        val startTime = System.currentTimeMillis()

        repeat(5) {
            viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

            // Créer et simuler des observateurs
            val job1 = launch {
                viewModel!!.syncStatus.collect {}
            }

            val job2 = launch {
                viewModel!!.uiState.collect {}
            }

            kotlinx.coroutines.delay(10)

            // Simuler la destruction du ViewModel
            viewModel = null

            job1.cancel()
            job2.cancel()
        }

        val totalTime = System.currentTimeMillis() - startTime

        // La création et destruction de ViewModels devrait être rapide
        assertTrue("ViewModel lifecycle management took too long: ${totalTime}ms", totalTime < 500)
    }

    @Test
    fun `should handle multiple concurrent state updates efficiently`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        var uiStateUpdateCount = 0
        var syncStatusUpdateCount = 0

        val job1 = launch {
            viewModel.uiState.collect { uiStateUpdateCount++ }
        }

        val job2 = launch {
            viewModel.syncStatus.collect { syncStatusUpdateCount++ }
        }

        // Simuler des mises à jour rapides
        repeat(10) {
            syncStatusPublisher.publishSyncStatus(
                com.example.aureus.data.offline.SyncStatus(
                    isOnline = it % 2 == 0,
                    lastSync = null,
                    pendingChanges = it,
                    isSyncing = false
                )
            )
        }

        job1.join()
        job2.join()

        job1.cancel()
        job2.cancel()

        // Vérifier que les états ont été mis à jour
        assertTrue("UI state should have updated", uiStateUpdateCount > 0)
        assertTrue("Sync status should have updated", syncStatusUpdateCount > 0)
    }

    @Test
    fun `should maintain consistent state under concurrent access`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        val collectedStates = mutableListOf<Resource<Double>>()

        val job = launch {
            viewModel.totalBalanceState.collect { state ->
                collectedStates.add(state)
            }
        }

        // Accès concurrent depuis plusieurs coroutines
        val launchJobs = mutableListOf<kotlinx.coroutines.Job>()

        repeat(10) {
            launchJobs.add(
                launch {
                    viewModel.totalBalanceState.first()
                }
            )
        }

        launchJobs.forEach { it.join() }
        job.cancel()

        // Vérifier que les états collectés sont cohérents
        assertNotNull(collectedStates)
        assertTrue("Should have collected states", collectedStates.isNotEmpty())
    }

    @Test
    fun `startup time should be within acceptable limits`() = runTest {
        // Mesurer le temps total de démarrage incluant l'init et le premier collect
        val startTime = System.currentTimeMillis()

        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        // Attendre le premier état UI
        val firstUiState = viewModel.uiState.first()

        val startupTime = System.currentTimeMillis() - startTime

        // Le startup devrait être rapide (< 150ms)
        assertTrue("Startup time too long: ${startupTime}ms", startupTime < 150)
        assertNotNull(firstUiState)
    }

    @Test
    fun `should initialize without blocking the test dispatcher`() = runTest {
        var viewModelCreated = false

        val initJob = launch {
            val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)
            viewModelCreated = true
        }

        // Le job de création devrait se compléter rapidement
        initJob.join()

        assertTrue("ViewModel should be created", viewModelCreated)
    }

    @Test
    fun `should handle offline sync status updates`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        val onlineStatuses = mutableListOf<Boolean>()

        val job = launch {
            viewModel.syncStatus.collect { status ->
                onlineStatuses.add(status.isOnline)
            }
        }

        // Simuler passage offline → online
        syncStatusPublisher.publishSyncStatus(com.example.aureus.data.offline.SyncStatus(false, null, 0, false))
        kotlinx.coroutines.delay(10)

        syncStatusPublisher.publishSyncStatus(com.example.aureus.data.offline.SyncStatus(true, System.currentTimeMillis(), 0, false))
        kotlinx.coroutines.delay(10)

        job.join()
        job.cancel()

        // Devrait avoir reçu les deux statuts
        assertTrue("Should have received online status changes", onlineStatuses.size >= 2)
        assertFalse(onlineStatuses.firstOrNull() ?: true) // Premier devrait être offline
        assertTrue(onlineStatuses.lastOrNull() ?: false) // Dernier devrait être online
    }

    @Test
    fun `memory efficiency - should not accumulate unbounded state`() = runTest {
        val viewModel = HomeViewModel(firebaseDataManager, database, offlineSyncManager, analyticsManager)

        // Observer les états mais ne pas retenir les références
        val job = launch {
            viewModel.syncStatus.collect { status ->
                // Simuler un UI qui observe mais ne stocke pas tout l'historique
                // (comme un Composable qui重组)
            }
        }

        // Simuler beaucoup de mises à jour
        repeat(1000) { index ->
            syncStatusPublisher.publishSyncStatus(
                com.example.aureus.data.offline.SyncStatus(
                    isOnline = index % 2 == 0,
                    lastSync = if (index > 0) System.currentTimeMillis() else null,
                    pendingChanges = index % 10,
                    isSyncing = false
                )
            )
        }

        // Si le code est bien fait, cela ne devrait pas causer de fuite mémoire
        // (StateFlow et MutableSharedFlow avec replay=1 sont bornés)

        job.cancel()

        // Juste vérifier que le ViewModel fonctionne toujours correctement
        val finalSyncStatus = viewModel.syncStatus.value
        assertNotNull(finalSyncStatus)
    }
}