package com.example.aureus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.aureus.ui.navigation.AppNavigation
import com.example.aureus.ui.theme.AureusTheme
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.onboarding.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point of the application
 * Uses Jetpack Compose with MVVM + Clean Architecture
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AureusTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    onboardingViewModel = onboardingViewModel
                )
            }
        }
    }
}