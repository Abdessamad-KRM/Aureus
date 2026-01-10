package com.example.aureus.data.remote.firebase

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication Manager
 * Gère l'authentification email/phone/Google
 */
@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {

    val currentUser = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Connexion avec email & mot de passe
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Inscription avec email & mot de passe
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Envoyer lien de vérification email
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Déconnexion
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Réinitialiser mot de passe
     */
    suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Mettre à jour le mot de passe
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== GOOGLE AUTH ====================

    /**
     * Configurer Google Sign In Client
     * Note: Google Play Services needs to be configured for this
     */
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.example.aureus.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Authentifier avec Google
     */
    suspend fun signInWithGoogleCredential(account: GoogleSignInAccount): Result<FirebaseUser> = try {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = auth.signInWithCredential(credential).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Vérifier si l'utilisateur est nouveau
     */
    fun isNewUser(): Boolean {
        return auth.currentUser?.metadata?.creationTimestamp == auth.currentUser?.metadata?.lastSignInTimestamp
    }

    // ==================== PHONE AUTH ====================

    /**
     * Vérifier le numéro de téléphone
     */
    fun verifyPhoneNumber(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (Exception) -> Unit
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60L,
            TimeUnit.SECONDS,
            activity,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId)
                }
            }
        )
    }

    /**
     * Vérifier le code SMS
     */
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<FirebaseUser> = try {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val result = auth.signInWithCredential(credential).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Lier le numéro de téléphone à l'utilisateur actuel
    suspend fun linkPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> = try {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val result = user.linkWithCredential(credential).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Flow d'auth state change
     */
    fun getAuthStateFlow(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // ==================== QUANTITAU AUTH (Frequent Logins) ====================

    /**
     * Enregistrer un compte Quantil (email/password fréquent)
     * Stocké localement via SharedPreferences
     */
    private val _storedAccounts = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val storedAccounts: StateFlow<List<Map<String, String>>> = _storedAccounts

    fun saveAccount(email: String, password: String, label: String? = null) {
        val accounts = _storedAccounts.value.toMutableList()
        val accountMap = mapOf(
            "email" to email,
            "password" to password,
            "label" to (label ?: email.split("@")[0])
        )

        // Éviter les doublons
        if (!accounts.any { it["email"] == email }) {
            accounts.add(accountMap)
            _storedAccounts.value = accounts
        }
    }

    fun removeAccount(email: String) {
        val accounts = _storedAccounts.value.toMutableList()
        accounts.removeAll { it["email"] == email }
        _storedAccounts.value = accounts
    }

    fun getAllAccounts(): List<Map<String, String>> {
        return _storedAccounts.value
    }
}