package com.example.aureus.data.offline

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 6: Tests et Validation
 * Unit tests pour SyncStatusPublisher
 *
 * Tests requis:
 * - Publication des changements de statut
 * - Pas de publication de doublons
 * - Flow correctement configuré
 */
@ExperimentalCoroutinesApi
class SyncStatusPublisherTest {

    @Test
    fun `should publish sync status changes`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)
        val status2 = SyncStatus(isOnline = false, lastSync = null, pendingChanges = 5, isSyncing = false)

        var collectedStatus: SyncStatus? = null
        val job = launch {
            publisher.syncStatusFlow.collect {
                collectedStatus = it
            }
        }

        publisher.publishSyncStatus(status1)
        assertEquals(status1, collectedStatus)

        publisher.publishSyncStatus(status2)
        assertEquals(status2, collectedStatus)

        job.cancel()
    }

    @Test
    fun `should not publish duplicate status`() = runTest {
        val publisher = SyncStatusPublisher()

        val status = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)
        var collectedCount = 0

        val job = launch {
            publisher.syncStatusFlow.collect {
                collectedCount++
            }
        }

        publisher.publishSyncStatus(status)
        publisher.publishSyncStatus(status) // Doublon
        publisher.publishSyncStatus(status) // Doublon again

        // Devrait recevoir le statut une seule fois (car les doublons sont ignorés)
        assertEquals(1, collectedCount)

        job.cancel()
    }

    @Test
    fun `should only publish when status actually changes`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)
        val status2 = SyncStatus(isOnline = false, lastSync = null, pendingChanges = 0, isSyncing = false)
        val status3 = SyncStatus(isOnline = false, lastSync = null, pendingChanges = 5, isSyncing = false)

        val collectedStatuses = mutableListOf<SyncStatus>()

        val job = launch {
            publisher.syncStatusFlow.collect {
                collectedStatuses.add(it)
            }
        }

        publisher.publishSyncStatus(status1)
        publisher.publishSyncStatus(status1) // Doublon - ignoré
        publisher.publishSyncStatus(status2) // Différent - publié
        publisher.publishSyncStatus(status2) // Doublon - ignoré
        publisher.publishSyncStatus(status3) // Différent - publié

        // Devrait avoir 3 statuts collectés (status1, status2, status3)
        assertEquals(3, collectedStatuses.size)
        assertEquals(status1, collectedStatuses[0])
        assertEquals(status2, collectedStatuses[1])
        assertEquals(status3, collectedStatuses[2])

        job.cancel()
    }

    @Test
    fun `should store last status`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = 12345L, pendingChanges = 0, isSyncing = false)

        assertNull("Initial last status should be null", publisher.getLastStatus())

        publisher.publishSyncStatus(status1)

        assertEquals("Last status should be stored", status1, publisher.getLastStatus())
    }

    @Test
    fun `should update last status on each publish`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)
        val status2 = SyncStatus(isOnline = false, lastSync = System.currentTimeMillis(), pendingChanges = 10, isSyncing = true)

        publisher.publishSyncStatus(status1)
        assertEquals(status1, publisher.getLastStatus())

        publisher.publishSyncStatus(status2)
        assertEquals(status2, publisher.getLastStatus())
    }

    @Test
    fun `should not update last status for duplicates`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)
        val status2 = SyncStatus(isOnline = false, lastSync = System.currentTimeMillis(), pendingChanges = 10, isSyncing = true)

        publisher.publishSyncStatus(status1)
        assertEquals(status1, publisher.getLastStatus())

        publisher.publishSyncStatus(status1) // Doublon

        // Last status ne devrait pas changer
        assertEquals(status1, publisher.getLastStatus())
        assertNotEquals(status2, publisher.getLastStatus())
    }

    @Test
    fun `should flow replay last status to new collectors`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = 12345L, pendingChanges = 0, isSyncing = false)
        publisher.publishSyncStatus(status1)

        // Nouveau collector devrait recevoir le dernier statut
        var collectedStatus: SyncStatus? = null
        val job = launch {
            publisher.syncStatusFlow.collect {
                collectedStatus = it
            }
        }

        // Flow a replay=1, donc le collector devrait recevoir le dernier statut immédiatement
        assertEquals(status1, collectedStatus)

        job.cancel()
    }

    @Test
    fun `should handle multiple concurrent collectors`() = runTest {
        val publisher = SyncStatusPublisher()

        val status1 = SyncStatus(isOnline = true, lastSync = null, pendingChanges = 0, isSyncing = false)

        val collector1Statuses = mutableListOf<SyncStatus>()
        val collector2Statuses = mutableListOf<SyncStatus>()
        val collector3Statuses = mutableListOf<SyncStatus>()

        val job1 = launch {
            publisher.syncStatusFlow.collect {
                collector1Statuses.add(it)
            }
        }

        val job2 = launch {
            publisher.syncStatusFlow.collect {
                collector2Statuses.add(it)
            }
        }

        val job3 = launch {
            publisher.syncStatusFlow.collect {
                collector3Statuses.add(it)
            }
        }

        publisher.publishSyncStatus(status1)

        // Tous les collectors devraient recevoir le statut
        assertEquals(1, collector1Statuses.size)
        assertEquals(1, collector2Statuses.size)
        assertEquals(1, collector3Statuses.size)

        job1.cancel()
        job2.cancel()
        job3.cancel()
    }
}