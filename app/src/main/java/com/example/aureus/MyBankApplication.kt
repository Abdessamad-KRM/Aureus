package com.example.aureus

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aureus.notification.NotificationHelper
import com.example.aureus.data.offline.FirebaseSyncWorker
import com.example.aureus.util.SharedPreferencesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class for MyBank
 * Annotated with @HiltAndroidApp for Hilt dependency injection
 * Phase 7: Configured WorkManager with Hilt worker factory
 * Phase 1: Secure Storage Migration - EncryptedSharedPreferences
 */
@HiltAndroidApp
class MyBankApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }

    private fun initializeApp() {
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            android.util.Log.d("MyBankApplication", "Firebase initialized successfully")

            // ✅ PHASE 1: Firebase App Check - Protection supplémentaire
            setupFirebaseAppCheck()
        } catch (e: Exception) {
            android.util.Log.e("MyBankApplication", "Failed to initialize Firebase", e)
        }

        // ✅ PHASE 1: Migrer les données vers EncryptedSharedPreferences
        runCatching {
            sharedPreferencesManager.migrateFromLegacy(this)
            android.util.Log.d("MyBankApplication", "Security migration completed successfully")
        }.onFailure {
            android.util.Log.w("MyBankApplication", "Security migration failed, using fallback", it)
        }

        // Register FCM token
        registerFcmToken()

        // Phase 7: Initialize WorkManager for offline sync
        initializeWorkManager()
    }

    /**
     * ✅ PHASE 1 CORRECTION: Firebase App Check - Protection API Key
     */
    private fun setupFirebaseAppCheck() {
        val appCheck = FirebaseAppCheck.getInstance()

        // Check if we're in debug mode using ApplicationInfo flags
        val isDebuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebuggable) {
            // Debug: Use debug app check factory (allows debug requests)
            try {
                val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val getInstanceMethod = debugFactoryClass.getMethod("getInstance")
                val factory = getInstanceMethod.invoke(null)
                val installMethod = FirebaseAppCheck::class.java.getMethod("installAppCheckProviderFactory", Class.forName("com.google.firebase.appcheck.FirebaseAppCheckProviderFactory"))
                installMethod.invoke(appCheck, factory)
                android.util.Log.d("MyBankApplication", "Firebase App Check initialized in DEBUG mode")
            } catch (e: Exception) {
                android.util.Log.w("MyBankApplication", "Failed to initialize debug App Check, using Play Integrity", e)
                // Fallback to Play Integrity
                appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                android.util.Log.d("MyBankApplication", "Firebase App Check initialized in RELEASE mode (fallback)")
            }
        } else {
            // Release: Play Integrity provider
            appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            android.util.Log.d("MyBankApplication", "Firebase App Check initialized in RELEASE mode")
        }
    }

    private fun registerFcmToken() {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("MyBankApplication", "FCM Token: $token")
                notificationHelper.registerFcmToken(token)
            } else {
                android.util.Log.e("MyBankApplication", "Failed to get FCM token", task.exception)
            }
        }
    }

    /**
     * Phase 7: Initialize WorkManager for background sync
     * Schedule periodic sync when network is available
     */
    private fun initializeWorkManager() {
        try {
            // Create constraints: run only when network is available
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()

            // Create periodic work request (sync every 15 minutes)
            val syncWorkRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("offline_sync")
                .build()

            // Enqueue work with Keep policy to avoid unnecessary duplicate work
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "OfflineSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )

            android.util.Log.d("MyBankApplication", "WorkManager initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MyBankApplication", "Failed to initialize WorkManager", e)
        }
    }
}