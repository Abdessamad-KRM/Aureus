package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.TestAccount
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel - Firebase Auth Integration
 * Handles authentication logic and creates user documents in Firestore
 * 
 * MODE DÉMO: demo@aureus.ma / Demo1234! bypass SMS et PIN verification
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: FirebaseAuthManager,
    private val dataManager: FirebaseDataManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val loginState: StateFlow<Resource<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val registerState: StateFlow<Resource<User>> = _registerState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val logoutState: StateFlow<Resource<Unit>> = _logoutState.asStateFlow()

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
     * Login avec support du compte démo
     * Si credentials démo match → bypass Firebase et retourner directement
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading

            // Vérifier si c'est le compte démo (MODE BYPASS)
            if (isDemoAccount(email, password)) {
                // Bypass complet - retourner directement le User démo
                _loginState.value = Resource.Success(
                    User(
                        id = TestAccount.USER_ID,
                        email = TestAccount.EMAIL,
                        firstName = TestAccount.FIRST_NAME,
                        lastName = TestAccount.LAST_NAME,
                        phone = TestAccount.PHONE,
                        createdAt = "2026-01-10",
                        updatedAt = "2026-01-10"
                    )
                )
            } else {
                // Sinon, procéder avec Firebase Auth normal
                val result = authManager.loginWithEmail(email, password)

                if (result.isSuccess) {
                    val firebaseUser = result.getOrNull()!!
                    val now = com.google.firebase.Timestamp.now().toDate().toString()
                    // Créer un User object à partir de FirebaseUser
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
                    _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Login failed")
                }
            }
        }
    }

    /**
     * Vérifie si les credentials correspondent au compte démo
     */
    private fun isDemoAccount(email: String, password: String): Boolean {
        return email.trim().lowercase() == "demo@aureus.ma" && password == "Demo1234!"
    }

    /**
     * Vérifie si un email correspond au compte démo
     * Utile pour bypass SMS verification après register démo
     */
    fun isDemoEmail(email: String): Boolean {
        return email.trim().lowercase() == "demo@aureus.ma"
    }

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

            // Mode démo bypass pour register aussi
            if (isDemoEmail(email)) {
                val now = com.google.firebase.Timestamp.now().toDate().toString()
                _registerState.value = Resource.Success(
                    User(
                        id = TestAccount.USER_ID,
                        email = TestAccount.EMAIL,
                        firstName = TestAccount.FIRST_NAME,
                        lastName = TestAccount.LAST_NAME,
                        phone = TestAccount.PHONE,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                // Flux normal Firebase
                try {
                    // 1. Créer l'utilisateur dans Firebase Auth
                    val authResult = authManager.registerWithEmail(
                        email = email,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone ?: ""
                    )

                    if (authResult.isSuccess) {
                        val firebaseUser = authResult.getOrNull()!!

                        // 2. Créer le document utilisateur dans Firestore
                        val userResult = dataManager.createUser(
                            userId = firebaseUser.uid,
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone ?: "",
                            pin = pin ?: ""
                        )

                        if (userResult.isSuccess) {
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
                            // Si la création Firestore échoue, supprimer l'user Auth (rollback)
                            firebaseUser.delete()
                            _registerState.value = Resource.Error(userResult.exceptionOrNull()?.message ?: "Failed to create user profile")
                        }
                    } else {
                        _registerState.value = Resource.Error(authResult.exceptionOrNull()?.message ?: "Registration failed")
                    }
                } catch (e: Exception) {
                    _registerState.value = Resource.Error(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = Resource.Loading
            authManager.signOut()
            _logoutState.value = Resource.Success(Unit)
        }
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
}