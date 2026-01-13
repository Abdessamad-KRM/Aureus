package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Bank Card Entity for Room Database
 * Enables offline-first functionality for cards
 */
@Entity(
    tableName = "bank_cards",
    indices = [Index("userId"), Index("accountId")]
)
data class BankCardEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val balance: Double,
    val cardType: String, // "VISA", "MASTERCARD", "AMEX", "DISCOVER"
    val cardColor: String, // "NAVY", "GOLD", "BLACK", "BLUE", "PURPLE", "GREEN"
    val isDefault: Boolean = false,
    val accountId: String = "",
    val isActive: Boolean = true,
    val status: String = "ACTIVE", // "ACTIVE", "FROZEN", "EXPIRED", "BLOCKED", "PENDING"
    val dailyLimit: Double = 10000.0,
    val monthlyLimit: Double = 50000.0,
    val spendingToday: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis()
)