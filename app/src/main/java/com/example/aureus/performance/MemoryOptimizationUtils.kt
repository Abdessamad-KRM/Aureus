package com.example.aureus.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Memory Optimization Utilities - Phase 15: Performance Optimization
 *
 * Provides utilities for:
 * - Remembered computations with proper cleanup
 * - Cached expensive objects
 * - Lifecycle-aware resource management
 */

/**
 * Remember expensive computation with proper caching
 * Phase 15: Avoid recomputing expensive operations on every recomposition
 */
@Composable
fun <T> rememberExpensive(
    key: Any,
    computation: () -> T
): T {
    var cachedValue by remember(key) { mutableStateOf<T?>(null) }

    LaunchedEffect(key) {
        cachedValue = computation()
    }

    return cachedValue ?: computation()
}

/**
 * Remember a formatter instance with caching
 * Phase 15: Currency formatters are expensive to create
 */
@Composable
fun rememberCurrencyFormatter(locale: Locale = Locale("fr", "MA")): NumberFormat {
    return remember(locale) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance("MAD")
        }
    }
}

/**
 * Remember a date formatter instance with caching
 * Phase 15: Date formatters are expensive to create and not thread-safe in some versions
 */
@Composable
fun rememberDateFormatter(pattern: String): java.text.SimpleDateFormat {
    return remember(pattern) {
        java.text.SimpleDateFormat(pattern, Locale.getDefault())
    }
}

/**
 * Lifecycle-aware memory cleanup
 * Phase 15: Properly dispose resources when not needed
 */
@Composable
fun rememberLifecycleAwareResource(
    resource: () -> Any,
    onCleanup: (Any) -> Unit
): Any {
    val lifecycleOwner = LocalLifecycleOwner.current
    var resourceInstance by remember { mutableStateOf(resource()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE ||
                event == Lifecycle.Event.ON_STOP ||
                event == Lifecycle.Event.ON_DESTROY) {
                onCleanup(resourceInstance)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onCleanup(resourceInstance)
        }
    }

    return resourceInstance
}

import androidx.compose.runtime.DisposableEffect

/**
 * Prevent unnecessary recomposition by using derived state
 * Phase 15: Only recompute when dependencies actually change
 */
@Composable
fun <T, R> rememberDerivedStateOf(
    state: T,
    transform: (T) -> R
): R {
    return remember(state) {
        transform(state)
    }
}

/**
 * Lazy computation that only executes when actually needed
 * Phase 15: Defer expensive operations
 */
class LazyValue<T>(
    private val computation: () -> T
) {
    private var _value: T? = null
    private var _computed = false

    fun get(): T {
        if (!_computed) {
            _value = computation()
            _computed = true
        }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    fun reset() {
        _value = null
        _computed = false
    }

    fun isComputed(): Boolean = _computed
}

/**
 * Remember a lazy value for expensive computations
 */
@Composable
fun <T> rememberLazyValue(computation: () -> T): LazyValue<T> {
    return remember { LazyValue(computation) }
}

/**
 * Memory-aware cached list with size limit
 * Phase 15: Prevent memory leaks by limiting cache size
 */
class MemoryAwareCache<K, V>(
    private val maxSize: Int = 100
) {
    private val cache = mutableMapOf<K, V>()
    private val accessOrder = mutableListOf<K>()

    fun get(key: K): V? {
        cache[key]?.let { value ->
            // Remove and re-add to track most recently used
            accessOrder.remove(key)
            accessOrder.add(key)
            return value
        }
        return null
    }

    fun put(key: K, value: V) {
        // Evict oldest if cache is full
        if (cache.size >= maxSize && !cache.containsKey(key)) {
            val oldest = accessOrder.removeFirst()
            cache.remove(oldest)
        }

        // Update or add
        if (!cache.containsKey(key)) {
            accessOrder.add(key)
        }
        cache[key] = value
    }

    fun remove(key: K) {
        cache.remove(key)
        accessOrder.remove(key)
    }

    fun clear() {
        cache.clear()
        accessOrder.clear()
    }

    fun size(): Int = cache.size
}

/**
 * Remember a memory-aware cache instance
 */
@Composable
fun <K, V> rememberMemoryAwareCache(maxSize: Int = 100): MemoryAwareCache<K, V> {
    return remember(maxSize) { MemoryAwareCache<K, V>(maxSize) }
}

/**
 * Image cache manager for tracking loaded images
 * Phase 15: Better memory management for images
 */
class ImageResourceManager {
    private val loadedImageUrls = mutableSetOf<String>()

    fun trackImageUrl(url: String) {
        loadedImageUrls.add(url)
    }

    fun untrackImageUrl(url: String) {
        loadedImageUrls.remove(url)
    }

    fun clearOldImages() {
        // In a real implementation, this would clear Coil caches
        loadedImageUrls.clear()
    }

    fun getLoadedImageCount(): Int = loadedImageUrls.size
}

/**
 * Remember an image resource manager
 */
@Composable
fun rememberImageResourceManager(): ImageResourceManager {
    return remember { ImageResourceManager() }
}