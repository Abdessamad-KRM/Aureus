package com.example.aureus.di

import com.example.aureus.security.SecureCredentialManager
import com.example.aureus.security.PinSecurityManager
import com.example.aureus.security.PinAttemptTracker
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.firestore.PinFirestoreManager
import com.example.aureus.ui.theme.ThemeManager
import com.example.aureus.i18n.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Shared Hilt Entry Points for accessing singleton dependencies in Composables
 *
 * These entry points allow non-@Inject annotated composables to access
 * application-scoped dependencies provided by Hilt dependency injection.
 */

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SecureCredentialManagerEntryPoint {
    fun secureCredentialManager(): SecureCredentialManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PinSecurityManagerEntryPoint {
    fun pinSecurityManager(): PinSecurityManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PinAttemptTrackerEntryPoint {
    fun pinAttemptTracker(): PinAttemptTracker
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseDataManagerEntryPoint {
    fun firebaseDataManager(): FirebaseDataManager
    fun pinFirestoreManager(): PinFirestoreManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseAuthEntryPoint {
    fun firebaseAuth(): FirebaseAuth
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeManagerEntryPoint {
    fun themeManager(): ThemeManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LanguageManagerEntryPoint {
    fun languageManager(): LanguageManager
}