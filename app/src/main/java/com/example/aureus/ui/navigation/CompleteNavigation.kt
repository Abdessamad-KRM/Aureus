package com.example.aureus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aureus.ui.auth.ChangePasswordScreen
import com.example.aureus.ui.auth.screen.*
import com.example.aureus.ui.cards.AddCardScreen
import com.example.aureus.ui.cards.AllCardsScreen
import com.example.aureus.ui.cards.MyCardsScreen
import com.example.aureus.ui.home.HomeScreen
import com.example.aureus.ui.onboarding.OnboardingScreen
import com.example.aureus.ui.profile.*
import com.example.aureus.ui.settings.LanguageSelectionScreen
import com.example.aureus.ui.settings.TermsAndConditionsScreen
import com.example.aureus.ui.splash.SplashScreenAdvanced
import com.example.aureus.ui.statistics.StatisticsScreen
import com.example.aureus.ui.transactions.TransactionDetailScreen
import com.example.aureus.ui.transactions.TransactionsFullScreen
import com.example.aureus.ui.transfer.RequestMoneyScreen
import com.example.aureus.ui.transfer.SendMoneyScreen

/**
 * Complete Navigation Routes for all screens
 */
sealed class AppScreen(val route: String) {
    // Auth Flow
    object Splash : AppScreen("splash")
    object Onboarding : AppScreen("onboarding")
    object Login : AppScreen("login")
    object Register : AppScreen("register")
    object SmsVerification : AppScreen("sms_verification")
    object PinSetup : AppScreen("pin_setup")
    
    // Main App
    object Home : AppScreen("home")
    object Statistics : AppScreen("statistics")
    object MyCards : AppScreen("my_cards")
    object AllCards : AppScreen("all_cards")
    object Transactions : AppScreen("transactions")
    object TransactionDetail : AppScreen("transaction_detail")
    object SendMoney : AppScreen("send_money")
    object RequestMoney : AppScreen("request_money")
    object Search : AppScreen("search")
    
    // Profile & Settings
    object Profile : AppScreen("profile")
    object EditProfile : AppScreen("edit_profile")
    object Settings : AppScreen("settings")
    object ChangePassword : AppScreen("change_password")
    object Language : AppScreen("language")
    object Terms : AppScreen("terms")
    
    // Cards Management
    object AddCard : AppScreen("add_card")
}

/**
 * Complete App Navigation with all screens connected
 */
@Composable
fun CompleteAppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreen.Splash.route
) {
    var isOnboardingCompleted = false // Should come from preferences
    var isLoggedIn = false // Should come from auth state

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== AUTH FLOW ====================
        
        // Splash Screen
        composable(AppScreen.Splash.route) {
            SplashScreenAdvanced(
                onSplashFinished = {
                    val nextRoute = when {
                        !isOnboardingCompleted -> AppScreen.Onboarding.route
                        isLoggedIn -> AppScreen.Home.route
                        else -> AppScreen.Login.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(AppScreen.Onboarding.route) {
            // Note: For static demo, OnboardingScreen should handle null viewModel
            // OnboardingScreen will use internal state management
        }

        // Login
        composable(AppScreen.Login.route) {
            // Note: For static demo, use AuthRepositoryStaticImpl
            // LoginScreen will validate against test@aureus.com / Test123456
        }

        // Register
        composable(AppScreen.Register.route) {
            // Note: Register will create new users but use static validation
        }

        // SMS Verification
        composable(AppScreen.SmsVerification.route) {
            SmsVerificationScreen(
                phoneNumber = "+212 6 12 34 56 78",
                onVerificationSuccess = {
                    navController.navigate(AppScreen.PinSetup.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // PIN Setup
        composable(AppScreen.PinSetup.route) {
            PinSetupScreen(
                onPinSetupComplete = {
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(AppScreen.PinSetup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== MAIN APP ====================

        // Home/Dashboard
        composable(AppScreen.Home.route) {
            HomeScreen(
                onNavigateToStatistics = {
                    navController.navigate(AppScreen.Statistics.route)
                },
                onNavigateToCards = {
                    navController.navigate(AppScreen.MyCards.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(AppScreen.Transactions.route)
                },
                onNavigateToProfile = {
                    navController.navigate(AppScreen.Profile.route)
                }
            )
        }

        // Statistics
        composable(AppScreen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // My Cards
        composable(AppScreen.MyCards.route) {
            MyCardsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddCard = {
                    navController.navigate(AppScreen.AddCard.route)
                }
            )
        }

        // All Cards
        composable(AppScreen.AllCards.route) {
            AllCardsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddCard = {
                    navController.navigate(AppScreen.AddCard.route)
                }
            )
        }

        // Add Card
        composable(AppScreen.AddCard.route) {
            AddCardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Transactions
        composable(AppScreen.Transactions.route) {
            TransactionsFullScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSearch = {
                    navController.navigate(AppScreen.Search.route)
                }
            )
        }

        // Transaction Detail
        composable(AppScreen.TransactionDetail.route) {
            TransactionDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Send Money
        composable(AppScreen.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSendClick = { contact, amount ->
                    // Show PIN verification then navigate back
                    navController.popBackStack()
                }
            )
        }

        // Request Money
        composable(AppScreen.RequestMoney.route) {
            RequestMoneyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRequestClick = { contact, amount, reason ->
                    navController.popBackStack()
                }
            )
        }

        // Search
        composable(AppScreen.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== PROFILE & SETTINGS ====================

        // Profile
        composable(AppScreen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditProfile = {
                    navController.navigate(AppScreen.EditProfile.route)
                },
                onLogout = {
                    isLoggedIn = false
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Edit Profile
        composable(AppScreen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }

        // Settings
        composable(AppScreen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChangePassword = {
                    navController.navigate(AppScreen.ChangePassword.route)
                },
                onLanguage = {
                    navController.navigate(AppScreen.Language.route)
                },
                onTerms = {
                    navController.navigate(AppScreen.Terms.route)
                }
            )
        }

        // Change Password
        composable(AppScreen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordChanged = {
                    navController.popBackStack()
                }
            )
        }

        // Language Selection
        composable(AppScreen.Language.route) {
            LanguageSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLanguageSelected = { language ->
                    // Save language preference
                }
            )
        }

        // Terms & Conditions
        composable(AppScreen.Terms.route) {
            TermsAndConditionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
