package com.example.aureus.data.firestore

import android.util.Log
import com.example.aureus.domain.model.Resource
import com.example.aureus.security.EncryptionService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore Manager pour gérer le PIN utilisateur
 */
@Singleton
class PinFirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val encryptionService: EncryptionService
) {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PIN_FIELD = "securityPin"
        private const val PIN_SALT_FIELD = "securityPinSalt"
        private const val PIN_HASHED_FIELD = "pinHashed"
        private const val PIN_UPDATED_AT = "pinUpdatedAt"
    }

    /**
     * ✅ PHASE 1 CORRECTION: Sauvegarder le PIN avec SALT unique par utilisateur
     */
    suspend fun savePin(pin: String): Resource<Unit> {
        val user = auth.currentUser
            ?: return Resource.Error("Utilisateur non connecté")

        return try {
            // Générer salt unique pour cet utilisateur
            val pinSalt = java.util.UUID.randomUUID().toString()

            // Hasher PIN avec SALT
            val hashedPin = encryptionService.hashPin(pin + pinSalt)
            val timestamp = Timestamp.now()

            val userDoc = firestore.collection(USERS_COLLECTION).document(user.uid)
            val snapshot = userDoc.get().await()

            if (snapshot.exists()) {
                userDoc.update(
                    mapOf(
                        PIN_FIELD to hashedPin,
                        PIN_SALT_FIELD to pinSalt,  // ✅ NOUVEAU!
                        PIN_HASHED_FIELD to true,
                        PIN_UPDATED_AT to timestamp,
                        "pinConfigured" to true
                    )
                ).await()
            } else {
                userDoc.set(
                    mapOf(
                        "uid" to user.uid,
                        "email" to (user.email ?: ""),
                        PIN_FIELD to hashedPin,
                        PIN_SALT_FIELD to pinSalt,  // ✅ NOUVEAU!
                        PIN_HASHED_FIELD to true,
                        PIN_UPDATED_AT to timestamp,
                        "pinConfigured" to true,
                        "createdAt" to timestamp,
                        "updatedAt" to timestamp
                    )
                ).await()
            }

            Log.d("PinFirestoreManager", "PIN hashé avec SALT sauvegardé pour user: ${user.uid}")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("PinFirestoreManager", "Erreur sauvegarde PIN", e)
            Resource.Error(e.message ?: "Erreur lors de la sauvegarde du PIN")
        }
    }

    /**
     * Vérifier si l'utilisateur a un PIN configuré
     */
    suspend fun hasPinConfigured(): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .await()

            snapshot.exists() && snapshot.contains(PIN_FIELD)
        } catch (e: Exception) {
            Log.e("PinFirestoreManager", "Erreur vérification PIN", e)
            false
        }
    }

    /**
     * ✅ PHASE 1 CORRECTION: Vérifier le PIN avec SALT
     */
    suspend fun verifyPin(pin: String): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                return false
            }

            val storedHashedPin = snapshot.getString(PIN_FIELD)
            val pinSalt = snapshot.getString(PIN_SALT_FIELD)  // ✅ NOUVEAU!

            if (pinSalt == null) {
                // PIN ancien sans salt - migration automatique
                val inputHashedPin = encryptionService.hashPin(pin)
                val match = storedHashedPin == inputHashedPin
                if (match) {
                    // Migrer automatiquement avec salt
                    savePin(pin)
                }
                return match
            }

            // ✅ Utiliser SALT pour hashage
            val inputHashedPin = encryptionService.hashPin(pin + pinSalt)
            storedHashedPin == inputHashedPin
        } catch (e: Exception) {
            Log.e("PinFirestoreManager", "Erreur vérification PIN", e)
            false
        }
    }

    /**
     * Mettre à jour le PIN existant
     */
    suspend fun updatePin(newPin: String): Resource<Unit> {
        return savePin(newPin)  // Réutiliser la même logique de sauvegarde
    }

    /**
     * Obtenir les informations utilisateur depuis Firestore
     */
    suspend fun getUserData(): Resource<Map<String, Any?>> {
        val user = auth.currentUser
            ?: return Resource.Error("Utilisateur non connecté")

        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .await()

            if (snapshot.exists()) {
                Resource.Success(snapshot.data ?: emptyMap())
            } else {
                Resource.Error("Utilisateur non trouvé dans la base de données")
            }
        } catch (e: Exception) {
            Log.e("PinFirestoreManager", "Erreur récupération user", e)
            Resource.Error(e.message ?: "Erreur lors de la récupération des données")
        }
    }
}