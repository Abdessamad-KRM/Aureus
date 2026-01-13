package com.example.aureus.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Phase 6: Tests et Validation
 * Unit tests pour TimeoutManager
 *
 * Tests requis:
 * - Timeout sur opérations lentes
 * - Complétion sur opérations rapides
 * - Timeout personnalisés
 * - Différents types d'opérations (read, write, query, auth, sync)
 */
@ExperimentalCoroutinesApi
class TimeoutTest {

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow read operation`() = runTest {
        TimeoutManager.withReadTimeout(timeoutMs = 100) {
            delay(200) // Simule opération lente (> 100ms)
            "result"
        }
    }

    @Test
    fun `should complete on fast read operation`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(100)
            "success"
        }
        assertEquals("success", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow write operation`() = runTest {
        TimeoutManager.withWriteTimeout(timeoutMs = 150) {
            delay(250) // Simule opération d'écriture lente
            "written"
        }
    }

    @Test
    fun `should complete on fast write operation`() = runTest {
        val result = TimeoutManager.withWriteTimeout(timeoutMs = 1000) {
            delay(50)
            "data"
        }
        assertEquals("data", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow query operation`() = runTest {
        TimeoutManager.withQueryTimeout(timeoutMs = 100) {
            delay(200)
            "query result"
        }
    }

    @Test
    fun `should complete on fast query operation`() = runTest {
        val result = TimeoutManager.withQueryTimeout(timeoutMs = 500) {
            delay(10)
            "query data"
        }
        assertEquals("query data", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow auth operation`() = runTest {
        TimeoutManager.withAuthTimeout(timeoutMs = 100) {
            delay(200)
            "authenticated"
        }
    }

    @Test
    fun `should complete on fast auth operation`() = runTest {
        val result = TimeoutManager.withAuthTimeout(timeoutMs = 1000) {
            delay(50)
            "auth token"
        }
        assertEquals("auth token", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow sync operation`() = runTest {
        TimeoutManager.withSyncTimeout(timeoutMs = 3000) {
            delay(4000)
            "synced"
        }
    }

    @Test
    fun `should complete on fast sync operation`() = runTest {
        val result = TimeoutManager.withSyncTimeout(timeoutMs = 5000) {
            delay(100)
            "sync data"
        }
        assertEquals("sync data", result)
    }

    @Test
    fun `should use default read timeout`() = runTest {
        // Test avec le timeout par défaut (10 secondes)
        val result = TimeoutManager.withReadTimeout {
            delay(10)
            "fast read"
        }
        assertEquals("fast read", result)
    }

    @Test
    fun `should use default write timeout`() = runTest {
        // Test avec le timeout par défaut (15 secondes)
        val result = TimeoutManager.withWriteTimeout {
            delay(10)
            "fast write"
        }
        assertEquals("fast write", result)
    }

    @Test
    fun `should use default query timeout`() = runTest {
        // Test avec le timeout par défaut (10 secondes)
        val result = TimeoutManager.withQueryTimeout {
            delay(10)
            "fast query"
        }
        assertEquals("fast query", result)
    }

    @Test
    fun `should use default auth timeout`() = runTest {
        // Test avec le timeout par défaut (15 secondes)
        val result = TimeoutManager.withAuthTimeout {
            delay(10)
            "fast auth"
        }
        assertEquals("fast auth", result)
    }

    @Test
    fun `should use default sync timeout`() = runTest {
        // Test avec le timeout par défaut (30 secondes)
        val result = TimeoutManager.withSyncTimeout {
            delay(10)
            "fast sync"
        }
        assertEquals("fast sync", result)
    }

    @Test
    fun `should respect custom read timeout`() = runTest {
        // Test avec un timeout personnalisé
        val startTime = System.currentTimeMillis()
        var exceptionThrown = false

        try {
            TimeoutManager.withReadTimeout(timeoutMs = 200) {
                delay(400) // Plus long que le timeout
                "result"
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            exceptionThrown = true
        }

        val elapsedTime = System.currentTimeMillis() - startTime

        assertTrue("Exception should be thrown", exceptionThrown)
        assertTrue("Timeout should be close to 200ms (got ${elapsedTime}ms)", elapsedTime >= 190 && elapsedTime < 400)
    }

    @Test
    fun `should handle operations at exact timeout boundary`() = runTest {
        // Opération qui prend exactement la même durée que le timeout
        var result: String? = null
        var exceptionThrown = false

        try {
            result = TimeoutManager.withReadTimeout(timeoutMs = 200) {
                delay(200)
                "boundary result"
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            exceptionThrown = true
        }

        // Avec withTimeout, le timeout est strict, donc cela devrait soit réussir soit timeout
        // selon la précision du timing
        if (result != null) {
            assertEquals("boundary result", result)
        } else {
            assertTrue(exceptionThrown)
        }
    }

    @Test
    fun `should return complex data types`() = runTest {
        data class TestData(val id: String, val name: String, val value: Double)

        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(10)
            TestData("123", "Test", 99.99)
        }

        assertEquals("123", result.id)
        assertEquals("Test", result.name)
        assertEquals(99.99, result.value, 0.001)
    }

    @Test
    fun `should return lists`() = runTest {
        val result = TimeoutManager.withQueryTimeout(timeoutMs = 500) {
            delay(10)
            listOf(1, 2, 3, 4, 5)
        }

        assertEquals(5, result.size)
        assertTrue(result.contains(3))
    }

    @Test
    fun `should return maps`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(10)
            mapOf(
                "key1" to "value1",
                "key2" to "value2"
            )
        }

        assertEquals("value1", result["key1"])
        assertEquals("value2", result["key2"])
        assertEquals(2, result.size)
    }

    @Test
    fun `should handle null returns`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(10)
            null
        }

        assertNull(result)
    }

    @Test
    fun `should handle boolean returns`() = runTest {
        val result = TimeoutManager.withWriteTimeout(timeoutMs = 500) {
            delay(10)
            true
        }

        assertTrue(result)
    }

    @Test
    fun `should handle numeric returns`() = runTest {
        val intResult = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(10)
            42
        }
        assertEquals(42, intResult)

        val doubleResult = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(10)
            3.14159
        }
        assertEquals(3.14159, doubleResult, 0.00001)
    }

    @Test
    fun `should throw TimeoutException with proper message`() = runTest {
        var timeoutMessage: String? = null

        try {
            TimeoutManager.withReadTimeout(timeoutMs = 100) {
                delay(200)
                "result"
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            timeoutMessage = e.message
        }

        assertNotNull(timeoutMessage)
        assertTrue("Message should mention timeout", timeoutMessage?.contains("timed out") == true)
        assertTrue("Message should mention timeout duration", timeoutMessage?.contains("100") == true)
    }

    @Test
    fun `should preserve exceptions from operation`() = runTest {
        var thrownException: Exception? = null

        try {
            TimeoutManager.withReadTimeout(timeoutMs = 500) {
                delay(10)
                throw IllegalArgumentException("Custom error from operation")
            }
        } catch (e: IllegalArgumentException) {
            thrownException = e
        }

        assertNotNull(thrownException)
        assertEquals("Custom error from operation", thrownException?.message)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout and not preserve operation exception`() = runTest {
        // Si timeout, l'exception de timeout devrait avoir la priorité
        try {
            TimeoutManager.withReadTimeout(timeoutMs = 100) {
                delay(200)
                throw IllegalArgumentException("Operation exception")
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            // Timeout exception devrait être levée
            throw e
        }
    }

    @Test
    fun `should handle very short timeout`() = runTest {
        var exceptionThrown = false

        try {
            TimeoutManager.withReadTimeout(timeoutMs = 1) {
                delay(10)
                "result"
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    @Test
    fun `should handle very long operation`() = runTest {
        var exceptionThrown = false

        try {
            TimeoutManager.withReadTimeout(timeoutMs = 500) {
                delay(1000) // 1 seconde
                "result"
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    @Test
    fun `should handle multiple sequential timeouts`() = runTest {
        var successCount = 0
        var timeoutCount = 0

        // Test 1: Opération rapide - devrait réussir
        try {
            TimeoutManager.withReadTimeout(timeoutMs = 500) {
                delay(10)
                "fast"
            }
            successCount++
        } catch (e: Exception) {
            timeoutCount++
        }

        // Test 2: Opération lente - devrait timeout
        try {
            TimeoutManager.withReadTimeout(timeoutMs = 100) {
                delay(200)
                "slow"
            }
            successCount++
        } catch (e: Exception) {
            timeoutCount++
        }

        // Test 3: Opération moyenne - devrait réussir
        try {
            TimeoutManager.withReadTimeout(timeoutMs = 500) {
                delay(100)
                "medium"
            }
            successCount++
        } catch (e: Exception) {
            timeoutCount++
        }

        assertEquals(2, successCount)
        assertEquals(1, timeoutCount)
    }
}