package com.example.aureus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.aureus.data.local.entity.StatisticsCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Statistics Cache
 * Phase 5: Offline Statistics Caching
 */
@Dao
interface StatisticsCacheDao {

    /**
     * Save or update a cache entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: StatisticsCacheEntity)

    /**
     * Get cache entry by key (as Flow)
     */
    @Query("SELECT * FROM statistics_cache WHERE cacheKey = :cacheKey")
    fun getCacheByKeyFlow(cacheKey: String): Flow<StatisticsCacheEntity?>

    /**
     * Get cache entry by key (suspend)
     */
    @Query("SELECT * FROM statistics_cache WHERE cacheKey = :cacheKey")
    suspend fun getCacheByKey(cacheKey: String): StatisticsCacheEntity?

    /**
     * Get all cached entries for a user
     */
    @Query("SELECT * FROM statistics_cache WHERE userId = :userId")
    fun getAllUserCachesFlow(userId: String): Flow<List<StatisticsCacheEntity>>

    /**
     * Get all cached entries by type
     */
    @Query("SELECT * FROM statistics_cache WHERE userId = :userId AND statType = :statType")
    fun getCachesByTypeFlow(userId: String, statType: String): Flow<List<StatisticsCacheEntity>>

    /**
     * Get non-expired cache entry
     */
    @Query("SELECT * FROM statistics_cache WHERE cacheKey = :cacheKey AND expiresAt > :currentTime")
    suspend fun getValidCache(cacheKey: String, currentTime: Long = System.currentTimeMillis()): StatisticsCacheEntity?

    /**
     * Delete a specific cache entry
     */
    @Query("DELETE FROM statistics_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCache(cacheKey: String)

    /**
     * Delete all caches for a user
     */
    @Query("DELETE FROM statistics_cache WHERE userId = :userId")
    suspend fun deleteAllUserCaches(userId: String)

    /**
     * Delete all caches of a specific type for a user
     */
    @Query("DELETE FROM statistics_cache WHERE userId = :userId AND statType = :statType")
    suspend fun deleteCachesByType(userId: String, statType: String)

    /**
     * Delete expired cache entries
     */
    @Query("DELETE FROM statistics_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredCaches(currentTime: Long = System.currentTimeMillis())

    /**
     * Delete all caches (clear all)
     */
    @Query("DELETE FROM statistics_cache")
    suspend fun clearAllCaches()

    /**
     * Count total cache entries for a user
     */
    @Query("SELECT COUNT(*) FROM statistics_cache WHERE userId = :userId")
    suspend fun countUserCaches(userId: String): Int

    /**
     * Get oldest cache entry
     */
    @Query("SELECT * FROM statistics_cache WHERE userId = :userId ORDER BY createdAt ASC LIMIT 1")
    suspend fun getOldestCache(userId: String): StatisticsCacheEntity?

    /**
     * Invalidate/refresh cache - update expiresAt to trigger refresh
     */
    @Query("UPDATE statistics_cache SET expiresAt = 0, isFresh = 0 WHERE cacheKey = :cacheKey")
    suspend fun invalidateCache(cacheKey: String)

    /**
     * Invalidate all caches of a type
     */
    @Query("UPDATE statistics_cache SET expiresAt = 0, isFresh = 0 WHERE userId = :userId AND statType = :statType")
    suspend fun invalidateCachesByType(userId: String, statType: String)

    /**
     * Mark cache as offline mode
     */
    @Query("UPDATE statistics_cache SET isOfflineMode = 1 WHERE userId = :userId")
    suspend fun markAsOffline(userId: String)

    /**
     * Mark cache as online mode
     */
    @Query("UPDATE statistics_cache SET isOfflineMode = 0, isFresh = 0 WHERE userId = :userId")
    suspend fun markAsOnline(userId: String)

    /**
     * Transaction: Insert new cache and cleanup old expired ones
     */
    @Transaction
    suspend fun upsertWithCleanup(cache: StatisticsCacheEntity) {
        upsert(cache)
        // Clean up expired caches
        deleteExpiredCaches()

        // Limit cache size - keep only latest 50 entries per user
        val count = countUserCaches(cache.userId)
        if (count > 50) {
            val oldest = getOldestCache(cache.userId)
            oldest?.let { deleteCache(it.cacheKey) }
        }
    }
}