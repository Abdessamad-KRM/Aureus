package com.example.aureus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.EntryPointAccessors
import com.example.aureus.di.PinSecurityManagerEntryPoint
import com.example.aureus.di.PinAttemptTrackerEntryPoint
import com.example.aureus.di.FirebaseDataManagerEntryPoint
import com.example.aureus.security.BiometricManager
import com.example.aureus.security.BiometricAvailability
import android.util.Log
import com.example.aureus.ui.auth.screen.*
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.auth.viewmodel.PhoneAuthState
import com.example.aureus.ui.auth.viewmodel.PhoneAuthViewModel
import com.example.aureus.ui.main.MainScreen
import com.example.aureus.ui.onboarding.OnboardingScreen
import com.example.aureus.ui.onboarding.OnboardingViewModel
import com.example.aureus.ui.splash.SplashScreenAdvanced
import com.example.aureus.ui.transfer.SendMoneyScreenFirebase
import com.example.aureus.ui.transfer.RequestMoneyScreenFirebase
import com.example.aureus.ui.transfer.viewmodel.TransferViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.example.aureus.ui.transactions.TransactionsFullScreenFirebase
import com.example.aureus.ui.cards.AddCardScreen
import com.example.aureus.ui.contact.screen.ContactManagementScreen
import com.example.aureus.ui.contact.screen.ContactAddEditScreen
import com.example.aureus.ui.transactions.TransactionDetailScreenFirebase
import com.example.aureus.ui.theme.ThemeManager
import com.example.aureus.ui.notifications.NotificationScreen

/**
 * Navigation Routes - Version démo statique
 */
sealed class Screen(val route: String, val deepLinkUriPattern: String? = null) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object PhoneNumberInput : Screen("phone_input/{phoneNumber}")
    object SmsVerification : Screen("sms_verification/{phoneNumber}")
    object PinCheckpoint : Screen("pin_checkpoint")  // Vérifie si PIN configuré
    object UnlockWithPin : Screen("unlock_with_pin")  // ✅ Nouveau: Entrer PIN pour accéder au Dashboard
    object PinSetup : Screen("pin_setup")
    object BiometricLock : Screen("biometric_lock")
    // Phase 4: Deep Links pour navigation depuis notifications
    object Dashboard : Screen("dashboard", deepLinkUriPattern = "aureus://home")
    object SendMoney : Screen("send_money", deepLinkUriPattern = "aureus://send_money")
    object RequestMoney : Screen("request_money", deepLinkUriPattern = "aureus://request_money")
    object Transactions : Screen("transactions", deepLinkUriPattern = "aureus://transactions")
    object TransactionDetail : Screen("transaction_detail/{transactionId}", deepLinkUriPattern = "aureus://transaction_detail/{transactionId}")
    object AddCard : Screen("add_card", deepLinkUriPattern = "aureus://cards")
    object ContactManagement : Screen("contact_management")
    object ContactAddEdit : Screen("contact_add_edit/{contactId}")
    object PinVerification : Screen("pin_verification/{action}")
    object PinLockout : Screen("pin_lockout")
    object Notifications : Screen("notifications", deepLinkUriPattern = "aureus://notifications")
    object CardDetail : Screen("card_detail/{cardId}", deepLinkUriPattern = "aureus://card_detail/{cardId}")
}

object ScreenUtils {
    fun phoneNumberInputScreen(phoneNumber: String = "") =
        "phone_input/${phoneUrlEncode(phoneNumber)}"

    fun smsVerificationScreen(phoneNumber: String) =
        "sms_verification/${phoneUrlEncode(phoneNumber)}"

    fun contactAddEditScreen(contactId: String = "") =
        "contact_add_edit/$contactId"

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
    biometricManager: BiometricManager,
    themeManager: ThemeManager,
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
                biometricManager = biometricManager,
                onSplashFinished = {
                    val nextRoute = when {
                        !onboardingViewModel.isOnboardingCompleted() -> Screen.Onboarding.route
                        !authViewModel.isLoggedIn -> Screen.Login.route
                        biometricManager.isBiometricAvailable() == BiometricAvailability.Available -> Screen.BiometricLock.route
                        else -> Screen.PinCheckpoint.route  // ✅ Vérifier PIN avant Dashboard
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Biometric Lock Screen (Phase 9)
        composable(Screen.BiometricLock.route) {
            BiometricLockScreen(
                biometricManager = biometricManager,
                onUnlockSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                },
                onUsePin = {
                    // Navigate to PIN unlock screen
                    navController.navigate(Screen.UnlockWithPin.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                },
                onEnableBiometric = {
                    // Prompt user to enable biometric in device settings
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
                    // Login email/password -> vérifier si PIN configuré
                    navController.navigate(Screen.PinCheckpoint.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleSignInSuccess = {
                    // Google Sign-In -> compte créé, maintenant lier téléphone
                    phoneAuthViewModel.setLinkingExistingUser(true)

                    // Pour l'instant, toujours demander le numéro de téléphone pour Google Auth
                    // (peut être optimisé plus tard pour vérifier si le numéro existe déjà)
                    navController.navigate(ScreenUtils.phoneNumberInputScreen()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // PIN Checkpoint Screen - Vérifie si PIN configuré
        composable(Screen.PinCheckpoint.route) {
            val context = LocalContext.current
            val pinFirestoreManager = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    FirebaseDataManagerEntryPoint::class.java
                ).pinFirestoreManager()
            }

            PinCheckpointScreen(
                pinFirestoreManager = pinFirestoreManager,
                onPinSetupRequired = {
                    // Pas de PIN - aller à PinSetup
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(Screen.PinCheckpoint.route) { inclusive = true }
                    }
                },
                onPinAlreadyConfigured = {
                    // PIN déjà configuré - aller à UnlockWithPin
                    navController.navigate(Screen.UnlockWithPin.route) {
                        popUpTo(Screen.PinCheckpoint.route) { inclusive = true }
                    }
                }
            )
        }

        // Unlock With PIN Screen - Entrer PIN pour accéder au Dashboard
        composable(Screen.UnlockWithPin.route) {
            val context = LocalContext.current
            val pinSecurityManager = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    PinSecurityManagerEntryPoint::class.java
                ).pinSecurityManager()
            }
            val pinAttemptTracker = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    PinAttemptTrackerEntryPoint::class.java
                ).pinAttemptTracker()
            }

            UnlockWithPinScreen(
                pinSecurityManager = pinSecurityManager,
                pinAttemptTracker = pinAttemptTracker,
                onUnlockSuccess = {
                    // PIN correct - naviguer vers Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.UnlockWithPin.route) { inclusive = true }
                    }
                },
                onLogout = {
                    // Logout - retour au Login
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
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
        // Phase 4: Deep link support for notifications
        composable(
            route = Screen.Dashboard.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://home"
                },
                navDeepLink {
                    uriPattern = "aureus://dashboard"
                }
            )
        ) {
            MainScreen(
                navController = navController,
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
                onNavigateToContacts = {
                    navController.navigate(Screen.ContactManagement.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
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
        composable(
            route = Screen.SendMoney.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://send_money"
                }
            )
        ) {
            SendMoneyScreenFirebase(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSendClick = { _, _ ->
                    // After PIN verified and send completed, navigate to Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SendMoney.route) { inclusive = true }
                    }
                },
                onAddContactClick = {
                    navController.navigate(Screen.ContactManagement.route)
                }
            )
        }

        // Request Money Screen
        composable(
            route = Screen.RequestMoney.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://request_money"
                }
            )
        ) {
            RequestMoneyScreenFirebase(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRequestClick = { _, _, _ ->
                    // After PIN verified and request sent, navigate to Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.RequestMoney.route) { inclusive = true }
                    }
                },
                onAddContactClick = {
                    navController.navigate(Screen.ContactManagement.route)
                }
            )
        }

        // Transactions Screen - Firebase version (100% dynamique)
        // Phase 4: Deep link support for notifications
        composable(
            route = Screen.Transactions.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://transactions"
                }
            )
        ) {
            TransactionsFullScreenFirebase(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSearchClick = {
                    // Search functionality
                },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }

        // Add Card Screen
        composable(
            route = Screen.AddCard.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://cards"
                }
            )
        ) {
            AddCardScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddSuccess = {
                    // After PIN verified and card added, navigate back to Dashboard (Cards tab)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.AddCard.route) { inclusive = true }
                    }
                }
            )
        }

        // Card Detail Screen
        composable(
            route = Screen.CardDetail.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://card_detail/{cardId}"
                }
            ),
            arguments = listOf(
                navArgument("cardId") { type = NavType.StringType; nullable = false }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""

            com.example.aureus.ui.cards.CardDetailScreen(
                cardId = cardId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Contact Management Screen
        composable(Screen.ContactManagement.route) {
            ContactManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactSelected = { /* Handle contact selection */ },
                onEditContact = { contactId ->
                    navController.navigate("${Screen.ContactAddEdit.route}/$contactId")
                },
                onAddContact = {
                    navController.navigate("${Screen.ContactAddEdit.route}")
                }
            )
        }

        // Pin Verification Screen (Phase 1 - Security)
        composable(
            route = Screen.PinVerification.route,
            arguments = listOf(navArgument("action") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val action = backStackEntry.arguments?.getString("action") ?: ""
            val pinSecurityManager = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    PinSecurityManagerEntryPoint::class.java
                ).pinSecurityManager()
            }
            val pinAttemptTracker = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    PinAttemptTrackerEntryPoint::class.java
                ).pinAttemptTracker()
            }
            val transferViewModel: TransferViewModel = hiltViewModel()

            // ✅ PHASE 7: PIN validé → Exécuter l'action!

            PinVerificationScreen(
                title = when (action) {
                    "send_money" -> "Confirmer le transfert"
                    "add_card" -> "Confirmer l'ajout de carte"
                    "edit_profile" -> "Confirmer les modifications"
                    "request_money" -> "Confirmer la demande"
                    else -> "Confirmer l'action"
                },
                message = "Entrez votre code PIN pour continuer",
                pinSecurityManager = pinSecurityManager,
                pinAttemptTracker = pinAttemptTracker,
                onSuccess = {
                    // ✅ PHASE 7: PIN validé → Exécuter l'action!

                    when (action) {
                        "send_money" -> {
                            // ✅ EXÉCUTER LE TRANSFERT RÉEL!

                            // Exécuter le transfert via ViewModel et observer le résultat
                            val scope = CoroutineScope(Dispatchers.Main)
                            var job: Job? = null

                            job = scope.launch {
                                // Exécuter le transfert
                                transferViewModel.executeTransfer()

                                // Attendre un petit délai pour uiState update
                                delay(300)

                                // Vérifier le résultat avant de naviguer
                                when {
                                    transferViewModel.uiState.value.transferSuccess -> {
                                        // ✅ Succès - naviguer vers Dashboard
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.SendMoney.route) { inclusive = true }
                                        }
                                    }
                                    transferViewModel.uiState.value.error != null -> {
                                        // ❌ Erreur - rester sur l'écran SendMoney pour afficher l'erreur
                                        job?.cancel()
                                    }
                                    else -> {
                                        // Statut inconnu - naviguer quand même
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.SendMoney.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }
                        "request_money" -> {
                            // ✅ CRÉER LA DEMANDE RÉELLE!

                            // Exécuter la création de demande et observer le résultat
                            val scope = CoroutineScope(Dispatchers.Main)
                            var job: Job? = null

                            job = scope.launch {
                                // Exécuter la création de demande
                                transferViewModel.createMoneyRequest()

                                // Attendre un petit délai pour uiState update
                                delay(300)

                                // Vérifier le résultat avant de naviguer
                                when {
                                    transferViewModel.uiState.value.requestSuccess -> {
                                        // ✅ Succès - naviguer vers Dashboard
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.RequestMoney.route) { inclusive = true }
                                        }
                                    }
                                    transferViewModel.uiState.value.error != null -> {
                                        // ❌ Erreur - rester sur l'écran RequestMoney pour afficher l'erreur
                                        job?.cancel()
                                    }
                                    else -> {
                                        // Statut inconnu - naviguer quand même
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.RequestMoney.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }
                        "add_card" -> {
                            // Pour add_card, naviguer simplement
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.AddCard.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.popBackStack()
                        }
                    }
                },
                onCancel = {
                    // Annuler → Reset le formulaire si c'est un transfer/request
                    if (action == "send_money" || action == "request_money") {
                        transferViewModel.resetForm()
                    }
                    navController.popBackStack()
                }
            )
        }

        // Pin Lockout Screen (Phase 4 - Security)
        composable(Screen.PinLockout.route) {
            val context = LocalContext.current
            val pinAttemptTracker = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    PinAttemptTrackerEntryPoint::class.java
                ).pinAttemptTracker()
            }
            val authViewModel: AuthViewModel = hiltViewModel()

            PinLockoutScreen(
                onLockoutExpired = {
                    // Lockout expired, navigate back or to dashboard
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Contact Add/Edit Screen
        composable(
            route = Screen.ContactAddEdit.route,
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.takeIf { it.isNotBlank() } ?: ""

            ContactAddEditScreen(
                contactId = contactId.takeIf { it.isNotBlank() },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Transaction Detail Screen - Firebase version
        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType; nullable = false }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val firebaseDataManager = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    FirebaseDataManagerEntryPoint::class.java
                ).firebaseDataManager()
            }

            TransactionDetailScreenFirebase(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                firebaseDataManager = firebaseDataManager
            )
        }

        // Notifications Screen
        // Phase 4: Deep link support for notifications
        composable(
            route = Screen.Notifications.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "aureus://notifications"
                }
            )
        ) {
            NotificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}