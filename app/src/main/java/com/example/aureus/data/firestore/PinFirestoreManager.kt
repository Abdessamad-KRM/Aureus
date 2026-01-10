package com.example.aureus.data.firestore

import android.util.Log
import com.example.aureus.domain.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore Manager pour gérer le PIN utilisateur
 */
@Singleton
class PinFirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PIN_FIELD = "securityPin"
        private const val PIN_SALT = "aureus_bank_salt_2024"  // Pour le hachage
        private const val PIN_CREATED_AT = "pinCreatedAt"
    }

    /**
     * Sauvegarder le PIN dans Firestore (hashé pour la sécurité)
     */
    suspend fun savePin(pin: String): Resource<Unit> {
        val user = auth.currentUser
            ?: return Resource.Error("Utilisateur non connecté")

        return try {
            // Hasher le PIN avant stockage (sécurité)
            val hashedPin = hashPin(pin)
            val timestamp = System.currentTimeMillis()

            // Créer/mettre à jour le document utilisateur avec le PIN
            val userDoc = firestore.collection(USERS_COLLECTION).document(user.uid)

            // Vérifier si le document existe
            val snapshot = userDoc.get().await()

            if (snapshot.exists()) {
                // Mettre à jour le PIN
                userDoc.update(
                    mapOf(
                        PIN_FIELD to hashedPin,
                        PIN_CREATED_AT to timestamp
                    )
                ).await()
            } else {
                // Créer nouveau document utilisateur avec PIN
                userDoc.set(
                    mapOf(
                        "uid" to user.uid,
                        "email" to (user.email ?: ""),
                        PIN_FIELD to hashedPin,
                        PIN_CREATED_AT to timestamp,
                        "createdAt" to timestamp,
                        "updatedAt" to timestamp
                    )
                ).await()
            }

            Log.d("PinFirestoreManager", "PIN sauvegardé avec succès pour user: ${user.uid}")
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
     * Vérifier le PIN (pour authentification)
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
            val inputHashedPin = hashPin(pin)

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
     * Hasher le PIN (SHA-256 + salt)
     */
    private fun hashPin(pin: String): String {
        return try {
            val saltedPin = pin + PIN_SALT
            val bytes = saltedPin.toByteArray()
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("PinFirestoreManager", "Erreur hash PIN", e)
            // Fallback si SHA-256 n'est pas disponible
            pin.hashCode().toString()
        }
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