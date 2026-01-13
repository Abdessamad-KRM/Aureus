package com.example.aureus.di

import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.repository.AccountRepository
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.domain.repository.ContactRepository
import com.example.aureus.domain.repository.StatisticRepository
import com.example.aureus.domain.repository.TransactionRepository
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import com.example.aureus.domain.repository.TransferRepository
import com.example.aureus.data.firestore.PinFirestoreManager
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.auth.viewmodel.PinViewModel
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.onboarding.OnboardingViewModel
import com.example.aureus.ui.dashboard.viewmodel.DashboardViewModel
import com.example.aureus.ui.home.viewmodel.HomeViewModel
import com.example.aureus.ui.statistics.viewmodel.StatisticsViewModel
import com.example.aureus.ui.transaction.viewmodel.TransactionViewModel
import com.example.aureus.ui.transaction.viewmodel.TransactionViewModelFirebase
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.util.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Dagger Hilt Module for ViewModels
 * Firebase integration enabled
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    // ==================== AUTH VIEWMODEL (Firebase) ====================

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        authRepository: AuthRepository,
        authManager: FirebaseAuthManager,
        dataManager: FirebaseDataManager,
        pinFirestoreManager: PinFirestoreManager,
        analyticsManager: AnalyticsManager
    ): AuthViewModel {
        return AuthViewModel(authRepository, authManager, dataManager, pinFirestoreManager, analyticsManager)
    }

    @Provides
    @ViewModelScoped
    fun providePinViewModel(
        pinFirestoreManager: PinFirestoreManager
    ): PinViewModel {
        return PinViewModel(pinFirestoreManager)
    }

    // ==================== HOME VIEWMODEL (Firebase) ====================

    @Provides
    @ViewModelScoped
    fun provideHomeViewModel(
        dataManager: FirebaseDataManager,
        database: AppDatabase,
        offlineSyncManager: OfflineSyncManager,
        analyticsManager: AnalyticsManager,
        authManager: FirebaseAuthManager,
        notificationRepository: com.example.aureus.domain.repository.NotificationRepository,
        transferRepository: TransferRepository
    ): HomeViewModel {
        return HomeViewModel(dataManager, database, offlineSyncManager, analyticsManager, authManager, notificationRepository, transferRepository)
    }

    // ==================== STATISTICS VIEWMODEL (Firebase) ====================

    @Provides
    @ViewModelScoped
    fun provideStatisticsViewModel(
        dataManager: FirebaseDataManager,
        statisticRepository: StatisticRepository,
        database: AppDatabase,
        offlineSyncManager: OfflineSyncManager
    ): StatisticsViewModel {
        return StatisticsViewModel(dataManager, statisticRepository, database, offlineSyncManager)
    }

    // ==================== LEGACY VIEWMODELS (Retrofit-based) ====================

    @Provides
    @ViewModelScoped
    fun provideDashboardViewModel(
        accountRepository: AccountRepository
    ): DashboardViewModel {
        return DashboardViewModel(accountRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideTransactionViewModel(
        transactionRepository: TransactionRepository
    ): TransactionViewModel {
        return TransactionViewModel(transactionRepository)
    }

    // ==================== TRANSACTION VIEWMODEL (Firebase-based) ====================

    @Provides
    @ViewModelScoped
    fun provideTransactionViewModelFirebase(
        transactionRepository: TransactionRepositoryFirebase,
        firebaseDataManager: FirebaseDataManager
    ): TransactionViewModelFirebase {
        return TransactionViewModelFirebase(transactionRepository, firebaseDataManager)
    }

    // ==================== CONTACT VIEWMODEL (Firebase-based) ====================

    @Provides
    @ViewModelScoped
    fun provideContactViewModel(
        contactRepository: ContactRepository,
        firebaseDataManager: FirebaseDataManager,
        database: AppDatabase,
        offlineSyncManager: OfflineSyncManager,
        analyticsManager: AnalyticsManager
    ): ContactViewModel {
        return ContactViewModel(contactRepository, firebaseDataManager, database, offlineSyncManager, analyticsManager)
    }

    // ==================== ONBOARDING VIEWMODEL ====================

    @Provides
    @ViewModelScoped
    fun provideOnboardingViewModel(
        preferencesManager: SharedPreferencesManager
    ): OnboardingViewModel {
        return OnboardingViewModel(preferencesManager)
    }

    // ==================== NOTIFICATION VIEWMODEL (PHASE 5) ====================

    @Provides
    @ViewModelScoped
    fun provideNotificationViewModel(
        notificationRepository: com.example.aureus.domain.repository.NotificationRepository,
        authManager: FirebaseAuthManager
    ): com.example.aureus.ui.notifications.viewmodel.NotificationViewModel {
        return com.example.aureus.ui.notifications.viewmodel.NotificationViewModel(notificationRepository, authManager)
    }
}