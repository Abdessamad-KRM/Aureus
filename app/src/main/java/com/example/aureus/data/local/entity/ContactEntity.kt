package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Contact Entity for Room Database
 * ✅ PHASE 2: Ajout de firebaseUserId et isVerifiedAppUser
 * Enables offline-first functionality for contacts
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index("userId"),
        Index("name"),
        Index("isFavorite"),
        Index("firebaseUserId")
    ]
)
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val email: String?,
    val avatar: String?,
    val accountNumber: String?,

    // ✅ NOUVEAUX CHAMPS
    val firebaseUserId: String? = null,
    val isVerifiedAppUser: Boolean = false,
    val lastUsed: Long? = null,

    val isFavorite: Boolean = false,
    val isBankContact: Boolean = false,
    val category: String?, // "FAMILY", "FRIENDS", "WORK", "BUSINESS", "OTHER"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)