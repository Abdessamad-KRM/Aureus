package com.example.aureus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aureus.ui.auth.screen.LoginScreen
import com.example.aureus.ui.auth.screen.PinSetupScreen
import com.example.aureus.ui.auth.screen.RegisterScreen
import com.example.aureus.ui.auth.screen.SmsVerificationScreen
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.home.HomeScreen
import com.example.aureus.ui.main.MainScreen
import com.example.aureus.ui.onboarding.OnboardingScreen
import com.example.aureus.ui.onboarding.OnboardingViewModel
import com.example.aureus.ui.splash.SplashScreenAdvanced

/**
 * Navigation Routes
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object SmsVerification : Screen("sms_verification")
    object PinSetup : Screen("pin_setup")
    object Dashboard : Screen("dashboard")
}

/**
 * App Navigation
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    onboardingViewModel: OnboardingViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreenAdvanced(
                onSplashFinished = {
                    val nextRoute = when {
                        !onboardingViewModel.isOnboardingCompleted() -> Screen.Onboarding.route
                        authViewModel.isLoggedIn -> Screen.Dashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.SmsVerification.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // SMS Verification Screen
        composable(Screen.SmsVerification.route) {
            SmsVerificationScreen(
                phoneNumber = "+212 6XX XXX XXX", // TODO: Get from registration
                onVerificationSuccess = {
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(Screen.SmsVerification.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // PIN Setup Screen
        composable(Screen.PinSetup.route) {
            PinSetupScreen(
                onPinSetupComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PinSetup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard Screen - Using MainScreen with bottom navigation
        composable(Screen.Dashboard.route) {
            MainScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}