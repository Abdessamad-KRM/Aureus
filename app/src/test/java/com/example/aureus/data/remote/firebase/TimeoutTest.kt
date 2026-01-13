package com.example.aureus.data.remote.firebase

import com.example.aureus.util.TimeoutManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitaires pour TimeoutManager (Phase 3)
 * Vérifie que les timeouts fonctionnent correctement
 */
@ExperimentalCoroutinesApi
class TimeoutTest {

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow operation`() = runTest {
        TimeoutManager.withReadTimeout(timeoutMs = 100) {
            delay(200) // Simule opération lente
            "result"
        }
    }

    @Test
    fun `should complete on fast operation`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            delay(100)
            "success"
        }
        assertEquals("success", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow write operation`() = runTest {
        TimeoutManager.withWriteTimeout(timeoutMs = 100) {
            delay(200)
            "write result"
        }
    }

    @Test
    fun `should complete on fast write operation`() = runTest {
        val result = TimeoutManager.withWriteTimeout(timeoutMs = 500) {
            delay(100)
            "write success"
        }
        assertEquals("write success", result)
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
            delay(100)
            "query success"
        }
        assertEquals("query success", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow auth operation`() = runTest {
        TimeoutManager.withAuthTimeout(timeoutMs = 100) {
            delay(200)
            "auth result"
        }
    }

    @Test
    fun `should complete on fast auth operation`() = runTest {
        val result = TimeoutManager.withAuthTimeout(timeoutMs = 500) {
            delay(100)
            "auth success"
        }
        assertEquals("auth success", result)
    }

    @Test(expected = java.util.concurrent.TimeoutException::class)
    fun `should timeout on slow sync operation`() = runTest {
        TimeoutManager.withSyncTimeout(timeoutMs = 100) {
            delay(200)
            "sync result"
        }
    }

    @Test
    fun `should complete on fast sync operation`() = runTest {
        val result = TimeoutManager.withSyncTimeout(timeoutMs = 500) {
            delay(100)
            "sync success"
        }
        assertEquals("sync success", result)
    }

    @Test
    fun `should use default read timeout when not specified`() = runTest {
        val result = TimeoutManager.withReadTimeout {
            delay(100)
            "default read timeout"
        }
        assertEquals("default read timeout", result)
    }

    @Test
    fun `should use default write timeout when not specified`() = runTest {
        val result = TimeoutManager.withWriteTimeout {
            delay(100)
            "default write timeout"
        }
        assertEquals("default write timeout", result)
    }

    @Test
    fun `should use default query timeout when not specified`() = runTest {
        val result = TimeoutManager.withQueryTimeout {
            delay(100)
            "default query timeout"
        }
        assertEquals("default query timeout", result)
    }

    @Test
    fun `should use default auth timeout when not specified`() = runTest {
        val result = TimeoutManager.withAuthTimeout {
            delay(100)
            "default auth timeout"
        }
        assertEquals("default auth timeout", result)
    }

    @Test
    fun `should use default sync timeout when not specified`() = runTest {
        val result = TimeoutManager.withSyncTimeout {
            delay(100)
            "default sync timeout"
        }
        assertEquals("default sync timeout", result)
    }

    @Test
    fun `should throw timeout exception with correct message`() = runTest {
        try {
            TimeoutManager.withReadTimeout(timeoutMs = 100) {
                delay(200)
                "result"
            }
            fail("Expected TimeoutException")
        } catch (e: java.util.concurrent.TimeoutException) {
            assertTrue(e.message!!.contains("timed out"))
            assertTrue(e.message!!.contains("100"))
        }
    }

    @Test
    fun `should handle complex operations within timeout`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 1000) {
            var sum = 0
            repeat(100) {
                sum += it
                delay(5)
            }
            sum
        }
        assertEquals(4950, result) // 0 + 1 + 2 + ... + 99 = 4950
    }

    @Test
    fun `should work with zero delay`() = runTest {
        val result = TimeoutManager.withReadTimeout(timeoutMs = 500) {
            "immediate result"
        }
        assertEquals("immediate result", result)
    }
}