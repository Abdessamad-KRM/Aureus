package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Account Entity for Room Database
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val accountNumber: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val userId: String,
    val lastSyncedAt: Long = System.currentTimeMillis()
)