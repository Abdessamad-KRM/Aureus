package com.example.aureus.image

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.util.concurrent.TimeUnit

/**
 * Coil Image Loader Configuration - Phase 15: Performance Optimization
 *
 * Provides optimized image loading with:
 * - Memory caching
 * - Disk caching
 * - Proper configuration for image sizes
 */
object CoilImageLoader {

    /**
     * Create optimized ImageLoader for the application
     */
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB disk cache
                    .build()
            }
            .respectCacheHeaders(false) // Disable cache headers to use our cache
            .crossfade(true) // Enable smooth crossfade animations

            // Network optimizations
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)

            // HTTP client optimization
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            }
            .build()
    }

    /**
     * Build optimized ImageRequest for profile images
     */
    fun buildProfileImageRequest(context: Context, imageUrl: String?): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(256, 256) // Profile images at 256px
            .build()
    }

    /**
     * Build optimized ImageRequest for card images
     */
    fun buildCardImageRequest(context: Context, imageUrl: String?): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(512, 320) // Card images at 512x320px
            .build()
    }

    /**
     * Build optimized ImageRequest for general images
     */
    fun buildGeneralImageRequest(
        context: Context,
        imageUrl: String?,
        width: Int = 1024,
        height: Int = 1024
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(width, height)
            .build()
    }
}