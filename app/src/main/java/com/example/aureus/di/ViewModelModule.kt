package com.example.aureus.di

import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.repository.AccountRepository
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.domain.repository.TransactionRepository
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.dashboard.viewmodel.DashboardViewModel
import com.example.aureus.ui.home.viewmodel.HomeViewModel
import com.example.aureus.ui.statistics.viewmodel.StatisticsViewModel
import com.example.aureus.ui.transaction.viewmodel.TransactionViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Singleton

/**
 * Dagger Hilt Module for ViewModels
 * Version démo statique - ViewModels sans dépendances Firebase
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    // ==================== AUTH VIEWMODEL (Updated for Firebase) ====================

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        authRepository: AuthRepository,
        authManager: FirebaseAuthManager,
        dataManager: FirebaseDataManager
    ): AuthViewModel {
        return AuthViewModel(authRepository, authManager, dataManager)
    }

    // ==================== STATIC DEMO VIEWMODELS (Sans Firebase) ====================

    @Provides
    @ViewModelScoped
    fun provideHomeViewModel(): HomeViewModel {
        return HomeViewModel()
    }

    @Provides
    @ViewModelScoped
    fun provideStatisticsViewModel(): StatisticsViewModel {
        return StatisticsViewModel()
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
}