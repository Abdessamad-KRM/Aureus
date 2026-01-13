package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.aureus.data.local.converter.DateConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Entity for caching statistics in Room database
 * Phase 5: Offline Statistics Caching
 */
@Entity(tableName = "statistics_cache")
data class StatisticsCacheEntity(
    @PrimaryKey
    val cacheKey: String, // Format: "{userId}_{statType}_{period}" (e.g., "user123_monthlyStats_MONTHLY")
    val userId: String,
    val statType: String, // "monthlyStats", "categoryBreakdown", "totalBalance", etc.
    val period: String, // "MONTHLY", "WEEKLY", "YEARLY", "ALL_TIME"
    val jsonData: String, // Serialized JSON data
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000), // 15 minutes cache TTL
    val isFresh: Boolean = true,
    val isOfflineMode: Boolean = false
) {
    companion object {
        fun generateCacheKey(userId: String, statType: String, period: String = "default"): String {
            return "${userId}_${statType}_$period"
        }

        val CACHE_TTL_MS = 15 * 60 * 1000 // 15 minutes
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }

    /**
     * Helper to parse JSON data to specific type
     */
    inline fun <reified T> parseData(): T? {
        return try {
            val gson = Gson()
            val type = object : TypeToken<T>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper to create cache entity from data
     */
    companion object {
        fun <T> fromData(
            userId: String,
            statType: String,
            data: T,
            period: String = "default",
            ttlMs: Long = CACHE_TTL_MS
        ): StatisticsCacheEntity {
            return try {
                val gson = Gson()
                val jsonData = gson.toJson(data)
                StatisticsCacheEntity(
                    cacheKey = generateCacheKey(userId, statType, period),
                    userId = userId,
                    statType = statType,
                    period = period,
                    jsonData = jsonData,
                    expiresAt = System.currentTimeMillis() + ttlMs
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to serialize data to JSON", e)
            }
        }
    }
}