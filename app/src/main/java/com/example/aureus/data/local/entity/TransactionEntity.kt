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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Double,
    val description: String,
    val category: String?,
    val merchant: String?,
    val date: String,
    val balanceAfter: Double,
    val lastSyncedAt: Long = System.currentTimeMillis()
)