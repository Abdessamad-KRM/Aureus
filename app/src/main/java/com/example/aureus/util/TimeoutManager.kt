package com.example.aureus.util

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException

/**
 * Manager pour les timeouts des opérations Firebase
 * Empêche les blocages indéfinis lors des opérations réseau
 */
object TimeoutManager {

    // Timeouts en millisecondes
    const val FIREBASE_READ_TIMEOUT = 10000L      // 10 secondes
    const val FIREBASE_WRITE_TIMEOUT = 15000L     // 15 secondes
    const val FIREBASE_QUERY_TIMEOUT = 10000L     // 10 secondes
    const val SYNC_TIMEOUT = 30000L               // 30 secondes
    const val AUTH_TIMEOUT = 15000L               // 15 secondes

    /**
     * Exécuter une opération de lecture avec timeout
     */
    suspend inline fun <T> withReadTimeout(
        timeoutMs: Long = FIREBASE_READ_TIMEOUT,
        crossinline operation: suspend () -> T
    ): T = try {
        withTimeout(timeoutMs) { operation() }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException("Read operation timed out after $timeoutMs ms")
    }

    /**
     * Exécuter une opération d'écriture avec timeout
     */
    suspend inline fun <T> withWriteTimeout(
        timeoutMs: Long = FIREBASE_WRITE_TIMEOUT,
        crossinline operation: suspend () -> T
    ): T = try {
        withTimeout(timeoutMs) { operation() }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException("Write operation timed out after $timeoutMs ms")
    }

    /**
     * Exécuter une requête avec timeout
     */
    suspend inline fun <T> withQueryTimeout(
        timeoutMs: Long = FIREBASE_QUERY_TIMEOUT,
        crossinline operation: suspend () -> T
    ): T = try {
        withTimeout(timeoutMs) { operation() }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException("Query operation timed out after $timeoutMs ms")
    }

    /**
     * Exécuter une opération d'authentification avec timeout
     */
    suspend inline fun <T> withAuthTimeout(
        timeoutMs: Long = AUTH_TIMEOUT,
        crossinline operation: suspend () -> T
    ): T = try {
        withTimeout(timeoutMs) { operation() }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException("Auth operation timed out after $timeoutMs ms")
    }

    /**
     * Exécuter une opération de sync avec timeout
     */
    suspend inline fun <T> withSyncTimeout(
        timeoutMs: Long = SYNC_TIMEOUT,
        crossinline operation: suspend () -> T
    ): T = try {
        withTimeout(timeoutMs) { operation() }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException("Sync operation timed out after $timeoutMs ms")
    }
}