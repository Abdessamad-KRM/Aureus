package com.example.aureus.di

import android.content.Context
import androidx.room.Room
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.remote.RetrofitClient
import com.example.aureus.data.remote.api.AccountApiService
import com.example.aureus.data.remote.api.AuthApiService
import com.example.aureus.data.remote.api.TransactionApiService
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.repository.AccountRepositoryImpl
import com.example.aureus.data.repository.AuthRepositoryImpl
import com.example.aureus.data.repository.TransactionRepositoryImpl
import com.example.aureus.domain.repository.AccountRepository
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.domain.repository.TransactionRepository
import com.example.aureus.util.SharedPreferencesManager
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
    fun provideSharedPreferencesManager(@ApplicationContext context: Context): SharedPreferencesManager {
        return SharedPreferencesManager(context)
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

    // ==================== REPOSITORIES ====================

    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        preferencesManager: SharedPreferencesManager
    ): AuthRepository {
        // Utiliser l'implémentation avec UserDao et PreferencesManager
        return AuthRepositoryImpl(userDao, preferencesManager)
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
}