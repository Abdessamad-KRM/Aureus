package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Transaction Entity for Room Database
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.NO_ACTION,  // Changed from CASCADE to prevent cascading deletions
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("accountId"), Index("date"), Index("type"), Index("userId")]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,  // Made non-nullable but FK constraint relaxed
    val userId: String? = null, // Added userId for better sync management
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Double,
    val description: String,
    val category: String?,
    val merchant: String?,
    val date: String,
    val balanceAfter: Double,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @androidx.room.ColumnInfo(name = "is_pending_upload")
    val isPendingUpload: Boolean = false
)