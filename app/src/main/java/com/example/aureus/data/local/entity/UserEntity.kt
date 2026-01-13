package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Entity for Room Database
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val createdAt: String,
    val updatedAt: String,
    val profileImage: String? = null,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)