package com.example.aureus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.auth.screen.*
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.auth.viewmodel.PhoneAuthState
import com.example.aureus.ui.auth.viewmodel.PhoneAuthViewModel
import com.example.aureus.ui.main.MainScreen
import com.example.aureus.ui.onboarding.OnboardingScreen
import com.example.aureus.ui.onboarding.OnboardingViewModel
import com.example.aureus.ui.splash.SplashScreenAdvanced
import com.example.aureus.ui.transfer.SendMoneyScreen
import com.example.aureus.ui.transfer.RequestMoneyScreen
import com.example.aureus.ui.transactions.TransactionsFullScreen
import com.example.aureus.ui.cards.AddCardScreen

/**
 * Navigation Routes - Version démo statique
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object PhoneNumberInput : Screen("phone_input/{phoneNumber}")
    object SmsVerification : Screen("sms_verification/{phoneNumber}")
    object PinSetup : Screen("pin_setup")
    object Dashboard : Screen("dashboard")
    object SendMoney : Screen("send_money")
    object RequestMoney : Screen("request_money")
    object Transactions : Screen("transactions")
    object AddCard : Screen("add_card")
}

object ScreenUtils {
    fun phoneNumberInputScreen(phoneNumber: String = "") =
        "phone_input/${phoneUrlEncode(phoneNumber)}"

    fun smsVerificationScreen(phoneNumber: String) =
        "sms_verification/${phoneUrlEncode(phoneNumber)}"

    private fun phoneUrlEncode(phone: String): String =
        phone.replace("/", "%2F").replace(" ", "%20")
}

/**
 * App Navigation - Version démo statique complète
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
            val phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Login email/password -> utilisateur déjà a compte + téléphone + PIN
                    // MODE DÉMO: Pas d'étapes supplémentaires, direct au Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleSignInSuccess = {
                    // Google Sign-In -> compte créé, maintenant lier téléphone
                    phoneAuthViewModel.setLinkingExistingUser(true)
                    navController.navigate(ScreenUtils.phoneNumberInputScreen()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            val phoneViewModel: PhoneAuthViewModel = hiltViewModel()

            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { phoneNumber ->
                    phoneViewModel.setLinkingExistingUser(true)
                    phoneViewModel.resetState()
                    navController.navigate(ScreenUtils.smsVerificationScreen(phoneNumber)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Phone Number Input Screen
        composable(
            route = Screen.PhoneNumberInput.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()

            val isUserLoggedIn = phoneAuthViewModel.isUserAlreadyLoggedIn()
            if (isUserLoggedIn) {
                phoneAuthViewModel.setLinkingExistingUser(true)
            }

            PhoneNumberInputScreen(
                userEmail = userEmail,
                onNavigateToSmsVerification = { phoneNumber ->
                    navController.navigate(ScreenUtils.smsVerificationScreen(phoneNumber)) {
                        popUpTo(Screen.PhoneNumberInput.route) { saveState = true }
                    }
                },
                onPhoneVerified = {
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // SMS Verification Screen
        composable(
            route = Screen.SmsVerification.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType; nullable = false }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: "+212 6XX XXX XXX"
            val phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()

            phoneAuthViewModel.setLinkingExistingUser(true)

            SmsVerificationScreen(
                phoneNumber = phoneNumber,
                shouldSendCode = true,
                onVerificationSuccess = {
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(Screen.SmsVerification.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResendCode = {
                    phoneAuthViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SmsVerification.route) { inclusive = true }
                    }
                }
            )
        }

        // PIN Setup Screen
        composable(Screen.PinSetup.route) {
            PinSetupScreen(
                onPinSetupComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard Screen - Main app with sidebar navigation
        composable(Screen.Dashboard.route) {
            MainScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToSendMoney = {
                    navController.navigate(Screen.SendMoney.route)
                },
                onNavigateToRequestMoney = {
                    navController.navigate(Screen.RequestMoney.route)
                },
                onNavigateToAddCard = {
                    navController.navigate(Screen.AddCard.route)
                },
                onNavigateToProfile = {
                    // Profile is in Settings tab
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Send Money Screen
        composable(Screen.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSendClick = { _, _ ->
                    // Simulation succès
                    navController.popBackStack()
                }
            )
        }

        // Request Money Screen
        composable(Screen.RequestMoney.route) {
            RequestMoneyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRequestClick = { _, _, _ ->
                    // Simulation succès
                }
            )
        }

        // Transactions Screen
        composable(Screen.Transactions.route) {
            TransactionsFullScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSearch = {
                    // Search functionality
                }
            )
        }

        // Add Card Screen
        composable(Screen.AddCard.route) {
            AddCardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}