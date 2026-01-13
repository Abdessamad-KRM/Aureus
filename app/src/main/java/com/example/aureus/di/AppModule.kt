package com.example.aureus.di

import android.content.Context
import androidx.room.Room
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.CardDao
import com.example.aureus.data.local.dao.ContactDao
import com.example.aureus.data.local.dao.StatisticsCacheDao
import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.offline.NetworkMonitor
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatusPublisher
import com.example.aureus.data.remote.RetrofitClient
import com.example.aureus.data.remote.api.AccountApiService
import com.example.aureus.data.remote.api.AuthApiService
import com.example.aureus.data.remote.api.TransactionApiService
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.repository.AccountRepositoryImpl
import com.example.aureus.data.repository.AuthRepositoryImpl
import com.example.aureus.data.repository.CardRepositoryImpl
import com.example.aureus.data.repository.ContactRepositoryImpl
import com.example.aureus.data.repository.NotificationRepositoryImpl
import com.example.aureus.data.repository.StatisticRepositoryImpl
import com.example.aureus.data.repository.TransactionRepositoryFirebaseImpl
import com.example.aureus.data.repository.TransactionRepositoryImpl
import com.example.aureus.data.repository.TransferRepositoryImpl
import com.example.aureus.data.repository.UserRepositoryImpl
import com.example.aureus.domain.repository.AccountRepository
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.domain.repository.CardRepository
import com.example.aureus.domain.repository.ContactRepository
import com.example.aureus.domain.repository.NotificationRepository
import com.example.aureus.domain.repository.StatisticRepository
import com.example.aureus.domain.repository.TransferRepository
import com.example.aureus.domain.repository.TransactionRepository
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import com.example.aureus.domain.repository.UserRepository
import com.example.aureus.util.SharedPreferencesManager
import com.example.aureus.data.firestore.PinFirestoreManager
import com.example.aureus.notification.NotificationHelper
import com.example.aureus.security.BiometricManager
import com.example.aureus.security.PinSecurityManager
import com.example.aureus.security.EncryptionService
import com.example.aureus.security.PinAttemptTracker
import com.example.aureus.security.SecurityLogger
import com.example.aureus.security.SecureCredentialManager
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.ui.theme.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt Module for Dependency Injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mybank_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCardDao(database: AppDatabase): CardDao {
        return database.cardDao()
    }

    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }

    @Provides
    @Singleton
    fun provideStatisticsCacheDao(database: AppDatabase): StatisticsCacheDao {
        return database.statisticsCacheDao()
    }

    @Provides
    @Singleton
    fun provideSecureStorageManager(@ApplicationContext context: Context): com.example.aureus.security.SecureStorageManager {
        return com.example.aureus.security.SecureStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(
        @ApplicationContext context: Context,
        secureStorage: com.example.aureus.security.SecureStorageManager
    ): SharedPreferencesManager {
        return SharedPreferencesManager(context, secureStorage)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(): AuthApiService {
        return RetrofitClient.authApiService
    }

    @Provides
    @Singleton
    fun provideAccountApiService(): AccountApiService {
        return RetrofitClient.accountApiService
    }

    @Provides
    @Singleton
    fun provideTransactionApiService(): TransactionApiService {
        return RetrofitClient.transactionApiService
    }

    // ==================== FIREBASE MODULES ====================

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthManager(auth: FirebaseAuth): FirebaseAuthManager {
        return FirebaseAuthManager(auth)
    }

    @Provides
    @Singleton
    fun provideFirebaseDataManager(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirebaseDataManager {
        return FirebaseDataManager(auth, firestore, storage)
    }

    @Provides
    @Singleton
    fun providePinFirestoreManager(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        encryptionService: EncryptionService  // ✅ Sécurité Phase 3
    ): PinFirestoreManager {
        return PinFirestoreManager(firestore, auth, encryptionService)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NotificationHelper {
        return NotificationHelper(context, firestore, auth)
    }

    // ==================== REPOSITORIES ====================

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuthManager: FirebaseAuthManager,
        firebaseDataManager: FirebaseDataManager
    ): UserRepository {
        return UserRepositoryImpl(firebaseAuthManager, firebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthManager: FirebaseAuthManager,
        firebaseDataManager: FirebaseDataManager
    ): AuthRepository {
        // Firebase-only authentication repository
        return AuthRepositoryImpl(firebaseAuthManager, firebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        userDao: UserDao,
        preferencesManager: SharedPreferencesManager
    ): AccountRepository {
        return AccountRepositoryImpl(
            accountDao = accountDao,
            userDao = userDao,
            tokenGetter = { preferencesManager.getToken() }
        )
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        preferencesManager: SharedPreferencesManager
    ): TransactionRepository {
        return TransactionRepositoryImpl(
            transactionDao = transactionDao,
            tokenGetter = { preferencesManager.getToken() }
        )
    }

    @Provides
    @Singleton
    fun provideTransactionRepositoryFirebase(
        firebaseDataManager: FirebaseDataManager
    ): TransactionRepositoryFirebase {
        return TransactionRepositoryFirebaseImpl(firebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideContactRepository(
        firebaseDataManager: FirebaseDataManager
    ): ContactRepository {
        return ContactRepositoryImpl(firebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideCardRepository(
        firebaseDataManager: FirebaseDataManager,
        encryptionService: EncryptionService  // ✅ Sécurité Phase 3
    ): CardRepository {
        return CardRepositoryImpl(firebaseDataManager, encryptionService)
    }

    @Provides
    @Singleton
    fun provideStatisticRepository(
        transactionRepositoryFirebase: TransactionRepositoryFirebase,
        firebaseDataManager: FirebaseDataManager,
        statisticsCacheDao: StatisticsCacheDao
    ): StatisticRepository {
        return StatisticRepositoryImpl(transactionRepositoryFirebase, firebaseDataManager, statisticsCacheDao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firebaseDataManager: FirebaseDataManager
    ): NotificationRepository {
        return NotificationRepositoryImpl(firebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideTransferRepository(
        firebaseDataManager: FirebaseDataManager
    ): TransferRepository {
        return TransferRepositoryImpl(firebaseDataManager)
    }

    // ==================== OFFLINE-FIRST MODULES (PHASE 8) ====================

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideSyncStatusPublisher(): SyncStatusPublisher {
        return SyncStatusPublisher()
    }

    @Provides
    @Singleton
    fun provideOfflineSyncManager(
        @ApplicationContext context: Context,
        database: AppDatabase,
        firebaseDataManager: FirebaseDataManager,
        networkMonitor: NetworkMonitor,
        auth: FirebaseAuth,
        syncStatusPublisher: SyncStatusPublisher
    ): OfflineSyncManager {
        return OfflineSyncManager(context, database, firebaseDataManager, networkMonitor, auth, syncStatusPublisher)
    }

    // ==================== BIOMETRIC MODULES (PHASE 9) ====================

    @Provides
    @Singleton
    fun provideBiometricManager(
        @ApplicationContext context: Context
    ): BiometricManager {
        return BiometricManager(context)
    }

    // ==================== SECURITY MODULES (PHASE 1) ====================

    @Provides
    @Singleton
    fun providePinSecurityManager(): PinSecurityManager {
        return PinSecurityManager()
    }

    @Provides
    @Singleton
    fun providePinAttemptTracker(
        @ApplicationContext context: Context
    ): PinAttemptTracker {
        return PinAttemptTracker(context)
    }

    @Provides
    @Singleton
    fun provideEncryptionService(): EncryptionService {
        return EncryptionService()  // ✅ Sécurité Phase 3 - Encryption/Hashage
    }

    @Provides
    @Singleton
    fun provideSecurityLogger(@ApplicationContext context: Context): SecurityLogger {
        return SecurityLogger(context)  // ✅ Sécurité Phase 7 - Security Logger
    }

    @Provides
    @Singleton
    fun provideSecureCredentialManager(
        @ApplicationContext context: Context,
        secureStorage: com.example.aureus.security.SecureStorageManager
    ): SecureCredentialManager {
        return SecureCredentialManager(context, secureStorage)  // ✅ PHASE 1: EncryptedSharedPreferences
    }

    // ==================== ANALYTICS MODULES (PHASE 11) ====================

    @Provides
    @Singleton
    fun provideAnalyticsManager(): AnalyticsManager {
        return AnalyticsManager()
    }

    // ==================== THEME MANAGER MODULES (PHASE 12) ====================

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}