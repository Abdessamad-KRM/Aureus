package com.example.aureus.domain.model

import java.util.Date

/**
 * Contact Data Model - Domain Layer
 * Represents a contact in the user's address book
 * ✅ PHASE 2: Ajout de firebaseUserId pour supporter les transferts entre utilisateurs
 *
 * Used for:
 * - Send Money functionality
 * - Request Money functionality
 * - Contact Management
 */
data class Contact(
    val id: String = "",
    val name: String,
    val phone: String,
    val email: String? = null,
    val avatar: String? = null,
    val accountNumber: String? = null,

    // ✅ NOUVEAU CHAMP: ID Firebase de l'utilisateur (si contact est aussi utilisateur de l'app)
    val firebaseUserId: String? = null,

    val isFavorite: Boolean = false,
    val isBankContact: Boolean = false,
    val category: ContactCategory? = null,
    val isVerifiedAppUser: Boolean = false,  // ✅ Marqueur: si l'utilisateur est vérifié comme utilisateur de l'app
    val lastUsed: Date? = null,              // ✅ Date de dernière utilisation pour "Recent contacts"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Get initials for avatar placeholder (ex: "Mohammed EL ALAMI" -> "MA")
     */
    fun getInitials(): String {
        return name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    /**
     * Get first name for display in lists
     */
    fun getFirstName(): String {
        return name.split(" ").firstOrNull() ?: name
    }

    /**
     * ✅ Check if this contact is also an app user
     */
    fun isAppUser(): Boolean {
        return firebaseUserId != null && isVerifiedAppUser
    }

    /**
     * ✅ Get display name for transfer
     */
    fun getDisplayNameForTransfer(): String {
        return if (isAppUser()) {
            "$name (App User)"
        } else {
            name
        }
    }
}

/**
 * Contact Categories for organization
 */
enum class ContactCategory(val displayName: String) {
    FAMILY("Family"),
    FRIENDS("Friends"),
    WORK("Work"),
    BUSINESS("Business"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): ContactCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}