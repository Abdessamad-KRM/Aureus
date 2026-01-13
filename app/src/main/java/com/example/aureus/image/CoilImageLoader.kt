package com.example.aureus.image

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.decode.GifDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.util.DebugLogger
import java.util.concurrent.TimeUnit

/**
 * Coil Image Loader Configuration - Phase 15: Performance Optimization
 *
 * Provides optimized image loading with:
 * - Memory caching
 * - Disk caching
 * - Proper configuration for image sizes
 * - SVG and GIF support
 */
object CoilImageLoader {

    /**
     * Create optimized ImageLoader for the application
     */
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    . maxSizePercent(
                        maxPercent = 0.25f, // Use 25% of available memory
                        minSizeBytes = 5 * 1024 * 1024 // At least 5MB
                    )
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB disk cache
                    .build()
            }
            .respectCacheHeaders(false) // Disable cache headers to use our cache
            .crossfade(true) // Enable smooth crossfade animations
            .crossfadeDuration(300) // 300ms crossfade duration

            // Network optimizations
            .networkCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)

            // Image size optimization
            .imageLoaderInterceptor {
                // Automatically resize images based on target view
                it.request.size(it.request.sizeResolver.size())
            }

            // Decoder support
            .components {
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
            }

            // HTTP client optimization
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            }

            // Logger for debug builds
            .apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    logger(DebugLogger())
                }
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
            .placeholder(coil.compose.R.drawable.ic_image_placeholder)
            .error(coil.compose.R.drawable.ic_error_placeholder)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
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
            .placeholder(coil.compose.R.drawable.ic_image_placeholder)
            .error(coil.compose.R.drawable.ic_error_placeholder)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
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
            .placeholder(coil.compose.R.drawable.ic_image_placeholder)
            .error(coil.compose.R.drawable.ic_error_placeholder)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .size(width, height)
            .build()
    }
}