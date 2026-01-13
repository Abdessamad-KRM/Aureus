package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase-only Authentication Repository Implementation
 * Completely removes static/demo account logic
 */
class AuthRepositoryImpl @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val dataManager: FirebaseDataManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            // Firebase email/password authentication
            val result = authManager.loginWithEmail(email, password)

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()!!
                val now = com.google.firebase.Timestamp.now().toDate().toString()

                // Get user data from Firestore or return minimal user object
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                    lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                    phone = firebaseUser.phoneNumber,
                    createdAt = now,
                    updatedAt = now
                )

                Resource.Success(user)
            } else {
                val error = result.exceptionOrNull()
                val message = when (error) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Email ou mot de passe incorrect"
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Utilisateur introuvable"
                    else -> error?.message ?: "Erreur de connexion"
                }
                Resource.Error(message, error)
            }
        } catch (e: Exception) {
            Resource.Error("Une erreur est survenue: ${e.message}", e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Resource<User> {
        return try {
            // 1. Create Firebase Auth user
            val authResult = authManager.registerWithEmail(email, password, firstName, lastName, phone ?: "")

            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()!!

                // 2. Create Firestore user document
                val firestoreResult = dataManager.createUser(
                    userId = firebaseUser.uid,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone ?: "",
                    pin = ""
                )

                if (firestoreResult.isSuccess) {
                    val now = com.google.firebase.Timestamp.now().toDate().toString()

                    // 3. Create default data (cards, transactions) for new users
                    dataManager.createDefaultCards(firebaseUser.uid)
                    dataManager.createDefaultTransactions(firebaseUser.uid)

                    val user = User(
                        id = firebaseUser.uid,
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        createdAt = now,
                        updatedAt = now
                    )

                    Resource.Success(user)
                } else {
                    // Rollback: delete Firebase Auth user if Firestore fails
                    firebaseUser.delete().await()
                    Resource.Error(firestoreResult.exceptionOrNull()?.message ?: "Erreur lors de la création du profil")
                }
            } else {
                val error = authResult.exceptionOrNull()
                val message = when {
                    error?.message?.contains("email") == true -> "Email déjà utilisé ou invalide"
                    error?.message?.contains("password") == true -> "Le mot de passe doit contenir au moins 6 caractères"
                    else -> error?.message ?: "L'inscription a échoué"
                }
                Resource.Error(message, error)
            }
        } catch (e: Exception) {
            Resource.Error("Une erreur est survenue: ${e.message}", e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            // Sign out from Firebase Auth
            authManager.signOut()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erreur lors de la déconnexion: ${e.message}", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val firebaseUser = authManager.currentUser
                ?: return Resource.Error("Aucun utilisateur connecté")

            val now = com.google.firebase.Timestamp.now().toDate().toString()

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                phone = firebaseUser.phoneNumber,
                createdAt = now,
                updatedAt = now
            )

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("Erreur lors de la récupération de l'utilisateur: ${e.message}", e)
        }
    }

    override fun isLoggedIn(): Boolean = authManager.isUserLoggedIn()

    override fun getToken(): String? {
        // Firebase tokens are managed automatically
        // Return UID as a token identifier
        return authManager.currentUser?.uid
    }

    override fun getUserId(): String? = authManager.currentUser?.uid

    // ==================== ADDITIONAL METHODS ====================

    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            val result = authManager.resetPassword(email)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi du mot de passe")
            }
        } catch (e: Exception) {
            Resource.Error("Une erreur est survenue: ${e.message}", e)
        }
    }

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            val result = authManager.sendEmailVerification()
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi de la vérification")
            }
        } catch (e: Exception) {
            Resource.Error("Une erreur est survenue: ${e.message}", e)
        }
    }

    /**
     * Update user password
     */
    suspend fun updatePassword(newPassword: String): Resource<Unit> {
        return try {
            val result = authManager.updatePassword(newPassword)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Erreur lors de la mise à jour du mot de passe")
            }
        } catch (e: Exception) {
            Resource.Error("Une erreur est survenue: ${e.message}", e)
        }
    }

    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return authManager.currentUser?.isEmailVerified == true
    }
}