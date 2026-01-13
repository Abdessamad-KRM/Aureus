package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.analytics.AnalyticsManager
import com.example.aureus.data.firestore.PinFirestoreManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel - Firebase Auth Integration
 * Handles authentication logic and creates user documents in Firestore
 * 100% Firebase - NO DEMO ACCOUNT LOGIC
 * Phase 1: Secure PIN hash with salt
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: FirebaseAuthManager,
    private val dataManager: FirebaseDataManager,
    private val pinFirestoreManager: PinFirestoreManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val loginState: StateFlow<Resource<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val registerState: StateFlow<Resource<User>> = _registerState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val logoutState: StateFlow<Resource<Unit>> = _logoutState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val resetPasswordState: StateFlow<Resource<Unit>> = _resetPasswordState.asStateFlow()

    private val _emailVerificationState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val emailVerificationState: StateFlow<Resource<Unit>> = _emailVerificationState.asStateFlow()

    private val _updatePasswordState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updatePasswordState: StateFlow<Resource<Unit>> = _updatePasswordState.asStateFlow()

    private val _googleSignInState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val googleSignInState: StateFlow<Resource<User>> = _googleSignInState.asStateFlow()

    val isLoggedIn: Boolean
        get() = authManager.isUserLoggedIn()

    init {
        // Initialize states based on Firebase Auth
        _loginState.value = if (authManager.isUserLoggedIn()) {
            Resource.Loading
        } else {
            Resource.Error("Not logged in")
        }
    }

    /**
     * Login with email and password
     * 100% Firebase Auth - no demo bypass
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading

            try {
                val result = authManager.loginWithEmail(email, password)

                if (result.isSuccess) {
                    val firebaseUser = result.getOrNull()!!
                    val now = com.google.firebase.Timestamp.now().toDate().toString()

                    // Track successful login - PHASE 11: Analytics
                    analyticsManager.trackLogin("email", firebaseUser.uid)
                    analyticsManager.setUserId(firebaseUser.uid)

                    // Create User object from FirebaseUser
                    _loginState.value = Resource.Success(
                        User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                            lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                            phone = firebaseUser.phoneNumber,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                } else {
                    val error = result.exceptionOrNull()
                    val message = when (error) {
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Email ou mot de passe incorrect"
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Utilisateur introuvable"
                        else -> error?.message ?: "Login failed"
                    }
                    // Track failed login attempt - PHASE 11: Analytics
                    analyticsManager.trackError("login_error", message, null)
                    _loginState.value = Resource.Error(message)
                }
            } catch (e: Exception) {
                // Track login exception - PHASE 11: Analytics
                analyticsManager.trackException(e, null, mapOf("error_type" to "login_exception"))
                _loginState.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Register with email and password
     * Creates Firebase Auth user and Firestore document
     */
    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        pin: String? = null
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading

            try {
                // 1. Create Firebase Auth user
                val authResult = authManager.registerWithEmail(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone ?: ""
                )

                if (authResult.isSuccess) {
                    val firebaseUser = authResult.getOrNull()!!

                    // ✅ PHASE 1 CORRECTION: CRÉER user SANS PIN hashé (sera configuré après)
                    val userResult = dataManager.createUser(
                        userId = firebaseUser.uid,
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone ?: "",
                        pin = ""
                    )

                    if (userResult.isSuccess) {
                        // ✅ PHASE 1 CORRECTION: Si PIN fourni, le sauvegarder hashé avec salt
                        if (!pin.isNullOrEmpty()) {
                            pinFirestoreManager.savePin(pin)
                        }

                        // 3. Create default data for new users
                        dataManager.createDefaultCards(firebaseUser.uid)
                        dataManager.createDefaultTransactions(firebaseUser.uid)

                        // Track successful registration - PHASE 11: Analytics
                        analyticsManager.trackSignUp("email", firebaseUser.uid)
                        analyticsManager.setUserId(firebaseUser.uid)
                        analyticsManager.setUserProperties(
                            userId = firebaseUser.uid,
                            accountType = "personal",
                            country = "MA",
                            preferredLanguage = "fr"
                        )

                        val now = com.google.firebase.Timestamp.now().toDate().toString()
                        _registerState.value = Resource.Success(
                            User(
                                id = firebaseUser.uid,
                                email = email,
                                firstName = firstName,
                                lastName = lastName,
                                phone = phone,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    } else {
                        // Rollback: delete Firebase Auth user if Firestore fails
                        try {
                            withContext(Dispatchers.IO) {
                                firebaseUser.delete().await()  // ✅ NON-BLOQUANT
                            }
                        } catch (e: Exception) {
                            // Log l'erreur mais ne pas bloquer l'UI
                            Log.e("AuthViewModel", "Failed to rollback Firebase user", e)
                        }
                        _registerState.value = Resource.Error(userResult.exceptionOrNull()?.message ?: "Failed to create user profile")
                    }
                } else {
                    _registerState.value = Resource.Error(authResult.exceptionOrNull()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerState.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Logout from Firebase Auth
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = Resource.Loading

            // Get user ID before signing out for tracking - PHASE 11: Analytics
            val currentUserId = authManager.currentUser?.uid
            currentUserId?.let { userId ->
                analyticsManager.trackLogout(userId)
                analyticsManager.clearUserData()
            }

            authManager.signOut()
            _logoutState.value = Resource.Success(Unit)
        }
    }

    /**
     * Reset password - sends email with reset link
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading

            try {
                val result = authManager.resetPassword(email)
                if (result.isSuccess) {
                    _resetPasswordState.value = Resource.Success(Unit)
                } else {
                    _resetPasswordState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
                }
            } catch (e: Exception) {
                _resetPasswordState.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Send email verification link
     */
    fun sendEmailVerification() {
        viewModelScope.launch {
            _emailVerificationState.value = Resource.Loading

            try {
                val result = authManager.sendEmailVerification()
                if (result.isSuccess) {
                    _emailVerificationState.value = Resource.Success(Unit)
                } else {
                    _emailVerificationState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to send verification email")
                }
            } catch (e: Exception) {
                _emailVerificationState.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Update password for the currently logged-in user
     */
    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _updatePasswordState.value = Resource.Loading

            try {
                val result = authManager.updatePassword(newPassword)
                if (result.isSuccess) {
                    _updatePasswordState.value = Resource.Success(Unit)
                } else {
                    val message = when (result.exceptionOrNull()) {
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Mot de passe trop faible (min 6 caractères)"
                        is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException -> "Vous devez vous reconnecter pour effectuer cette action"
                        else -> result.exceptionOrNull()?.message ?: "Failed to update password"
                    }
                    _updatePasswordState.value = Resource.Error(message)
                }
            } catch (e: Exception) {
                _updatePasswordState.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return authManager.currentUser?.isEmailVerified == true
    }

    fun resetLoginState() {
        _loginState.value = Resource.Loading
    }

    fun resetRegisterState() {
        _registerState.value = Resource.Loading
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Loading
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = Resource.Loading
    }

    fun resetEmailVerificationState() {
        _emailVerificationState.value = Resource.Loading
    }

    fun resetUpdatePasswordState() {
        _updatePasswordState.value = Resource.Loading
    }

    /**
     * Clear auth state - called when user is logged out elsewhere
     */
    fun clearAuthState() {
        _loginState.value = Resource.Error("User logged out")
        _logoutState.value = Resource.Success(Unit)
    }

    /**
     * Authentifier avec Google Credential
     */
    fun signInWithGoogleCredential(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            _googleSignInState.value = Resource.Loading

            try {
                // Sign in with Firebase using the Google credential
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val authResult = auth.signInWithCredential(credential).await()

                if (authResult.user != null) {
                    val firebaseUser: com.google.firebase.auth.FirebaseUser = authResult.user!!
                    val now = com.google.firebase.Timestamp.now().toDate().toString()

                    // Create or update user in Firestore
                    val userId = firebaseUser.uid
                    var userExists = false

                    try {
                        // ✅ Wrappe dans Dispatchers.IO pour éviter blocage
                        withContext(Dispatchers.IO) {
                            val userDoc = dataManager.firestore.document("users/$userId").get().await()
                            userExists = userDoc.exists()
                        }
                    } catch (e: Exception) {
                        // User doesn't exist yet
                        Log.d("AuthViewModel", "User doc not found (expected for new users)")
                    }

                    if (!userExists) {
                        // Create new user document
                        dataManager.createUser(
                            userId = userId,
                            email = firebaseUser.email ?: "",
                            firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                            lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                            phone = firebaseUser.phoneNumber ?: "",
                            pin = ""
                        )
                        // Create default data
                        dataManager.createDefaultCards(userId)
                        dataManager.createDefaultTransactions(userId)
                    }

                    // Track successful Google Sign-In - PHASE 11: Analytics
                    analyticsManager.trackSignUp("google", userId)
                    analyticsManager.trackLogin("google", userId)
                    analyticsManager.setUserId(userId)
                    analyticsManager.setUserProperties(
                        userId = userId,
                        accountType = "personal",
                        country = "MA",
                        preferredLanguage = "fr"
                    )

                    val user = User(
                        id = userId,
                        email = firebaseUser.email ?: "",
                        firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                        lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                        phone = firebaseUser.phoneNumber,
                        createdAt = now,
                        updatedAt = now
                    )

                    _googleSignInState.value = Resource.Success(user)
                } else {
                    _googleSignInState.value = Resource.Error("Google Sign-In Failed: No user returned")
                }
            } catch (e: Exception) {
                _googleSignInState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }
}