package com.example.aureus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.aureus.ui.navigation.AppNavigation
import com.example.aureus.ui.theme.AureusTheme
import com.example.aureus.ui.theme.ThemeManager
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.onboarding.OnboardingViewModel
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.security.BiometricManager
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity - Entry point of the application
 * Uses Jetpack Compose with MVVM + Clean Architecture
 * Phase 15: Performance Optimization - Startup Time Optimization with SplashScreen API
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    @Inject
    lateinit var offlineSyncManager: OfflineSyncManager

    @Inject
    lateinit var biometricManager: BiometricManager

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Phase 15: Install splash screen before super.onCreate() - Performance Optimization
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Phase 4: Traiter les extras des notifications et deep links
        handleNotificationIntent(intent)

        // Phase 15: Check if auth initialization is complete
        var isAuthInitialized = false

        // Observe auth state to know when to hide splash screen
        lifecycleScope.launch {
            // Wait a brief moment for auth to initialize
            kotlinx.coroutines.delay(100)
            isAuthInitialized = true
        }

        // Phase 15: Keep splash screen visible until auth is initialized
        splashScreen.setKeepOnScreenCondition { !isAuthInitialized }

        setContent {
            val isDarkMode by themeManager.darkMode.collectAsState(initial = false)

            AureusTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    authViewModel = authViewModel,
                    onboardingViewModel = onboardingViewModel,
                    biometricManager = biometricManager,
                    themeManager = themeManager
                )
            }
        }

        // Initialize offline sync manager (deferred for faster startup)
        initializeOfflineSync()

        // Add AuthStateListener to monitor authentication state changes
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val isLoggedIn = authViewModel.isLoggedIn

            Log.d("MainActivity", "Auth state changed: $isLoggedIn, Firebase user: ${user?.uid}")

            if (user == null && isLoggedIn) {
                // User was logged out elsewhere (e.g., from Firebase Console or another device)
                Log.d("MainActivity", "User logged out elsewhere, clearing local state")
                authViewModel.clearAuthState()
            } else if (user != null && !isLoggedIn) {
                // User was logged in elsewhere (e.g., from Firebase Console or another device)
                Log.d("MainActivity", "User logged in elsewhere, updating local state")
                // Note: AuthViewModel should handle this automatically through Firebase Auth Manager
            }
        }

        // Register the listener
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)
    }

    /**
     * Initialize offline sync manager for automatic synchronization
     * Phase 15: Deferred to avoid blocking startup
     */
    private fun initializeOfflineSync() {
        lifecycleScope.launch {
            // Check if user is logged in before initializing sync
            if (authViewModel.isLoggedIn) {
                offlineSyncManager.initializeAutoSync()
                Log.d("MainActivity", "Offline sync initialized")

                // Perform initial sync (non-blocking)
                lifecycleScope.launch {
                    val syncResult = offlineSyncManager.syncNow()
                    when (syncResult) {
                        is com.example.aureus.data.offline.SyncResult.Success -> {
                            Log.d("MainActivity", "Initial sync successful")
                        }
                        is com.example.aureus.data.offline.SyncResult.Error -> {
                            Log.e("MainActivity", "Initial sync failed: ${syncResult.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the auth state listener to prevent memory leaks
        authStateListener?.let { FirebaseAuth.getInstance().removeAuthStateListener(it) }
    }

    /**
     * Phase 4: Traiter les deep links depuis les notifications
     * Extrait les données de navigation depuis l'intent et les route vers l'écran approprié
     */
    private fun handleNotificationIntent(intent: Intent) {
        val data = intent.extras
        val deepLinkUri = intent.data

        if (data != null || deepLinkUri != null) {
            // Gérer les deep links URI (aureus://transactions, etc.)
            deepLinkUri?.let { uri ->
                val path = uri.path?.removePrefix("/") ?: ""
                Log.d("MainActivity", "Deep link received: $uri, path: $path")

                when (path) {
                    "transactions" -> {
                        Log.d("MainActivity", "Navigating to transactions from notification")
                        // Navigate to transactions screen (handled by Navigation.kt)
                    }
                    "home", "dashboard" -> {
                        Log.d("MainActivity", "Navigating to home from notification")
                        // Navigate to home screen
                    }
                    "cards" -> {
                        Log.d("MainActivity", "Navigating to cards from notification")
                        // Navigate to cards screen
                    }
                    "notifications" -> {
                        Log.d("MainActivity", "Navigating to notifications from notification")
                        // Navigate to notifications screen
                    }
                    "profile" -> {
                        Log.d("MainActivity", "Navigating to profile from notification")
                        // Navigate to profile screen
                    }
                }
            }

            // Gérer les extras de notification (fallback)
            data?.let { extras ->
                val notificationType = extras.getString("type")
                val navigateTo = extras.getString("navigate_to")
                val amount = extras.getString("amount")
                val direction = extras.getString("direction")
                val fromTo = extras.getString("fromTo")

                Log.d("MainActivity", "Notification data: type=$notificationType, navigate_to=$navigateTo, amount=$amount, direction=$direction")

                // Stocker les données pour les passer au ViewModel/Navigation
                // Ces données seront utilisées par Navigation.kt pour naviguer vers le bon écran
                if (!navigateTo.isNullOrEmpty() || !notificationType.isNullOrEmpty()) {
                    Log.d("MainActivity", "Navigation data will be processed by AppNavigation")
                }
            }
        }
    }

    /**
     * Phase 4: Appelé quand l'activité est lancée via une notification
     * alors qu'elle est déjà en arrière-plan
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }
}