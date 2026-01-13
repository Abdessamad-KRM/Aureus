package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.TransferRepository
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transfer Repository Implementation
 * ✅ PHASE 3: Implémentation avec Cloud Functions
 */
@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager
) : TransferRepository {

    private val functions = FirebaseFunctions.getInstance()

    // ==================== TRANSFERS ====================

    override suspend fun transferMoney(
        recipientUserId: String,
        amount: Double,
        description: String
    ): Resource<com.example.aureus.domain.repository.TransferResult> {
        return try {
            val callable = functions.getHttpsCallable("executeWalletTransfer")

            val data = mapOf(
                "recipientUserId" to recipientUserId,
                "amount" to amount,
                "description" to description
            )

            val result: HttpsCallableResult = callable.call(data).await()
            // Access data using reflection for compatibility
            val resultMap = try {
                val field = result.javaClass.getDeclaredField("data")
                field.isAccessible = true
                field.get(result) as? Map<String, Any>
            } catch (e: Exception) {
                null
            }

            if (resultMap?.get("success") == true) {
                Resource.Success(
                    com.example.aureus.domain.repository.TransferResult(
                        success = true,
                        transactionId = resultMap["transactionId"] as? String ?: "",
                        recipientTransactionId = resultMap["recipientTransactionId"] as? String ?: "",
                        senderBalance = (resultMap["senderBalance"] as? Double) ?: 0.0,
                        recipientBalance = (resultMap["recipientBalance"] as? Double) ?: 0.0,
                        amount = (resultMap["amount"] as? Double) ?: 0.0,
                        timestamp = resultMap["timestamp"]?.toString() ?: ""
                    )
                )
            } else {
                Resource.Error(resultMap?.get("message") as? String ?: "Transfer failed")
            }
        } catch (e: Exception) {
            // Extraire message d'erreur from Firebase Functions
            val errorMessage = when {
                e.message?.contains("Insufficient balance") == true ->
                    "Solde insuffisant"
                e.message?.contains("Daily transfer limit") == true ->
                    "Limite journalière dépassée"
                e.message?.contains("Recipient account not found") == true ->
                    "Compte destinataire introuvable"
                e.message?.contains("Cannot transfer money to yourself") == true ->
                    "Impossible de transférer à votre propre compte"
                e.message?.contains("User not found") == true ->
                    "Utilisateur introuvable"
                else ->
                    e.message ?: "Erreur lors du transfert"
            }
            Resource.Error(errorMessage, e)
        }
    }

    override suspend fun createMoneyRequest(
        recipientUserId: String,
        amount: Double,
        reason: String
    ): Resource<String> {
        return try {
            val callable = functions.getHttpsCallable("createMoneyRequest")

            val data = mapOf(
                "recipientUserId" to recipientUserId,
                "amount" to amount,
                "reason" to reason
            )

            val result: HttpsCallableResult = callable.call(data).await()
            // Access data using reflection for compatibility
            val resultMap = try {
                val field = result.javaClass.getDeclaredField("data")
                field.isAccessible = true
                field.get(result) as? Map<String, Any>
            } catch (e: Exception) {
                null
            }

            if (resultMap?.get("success") == true) {
                Resource.Success(resultMap["requestId"] as? String ?: "")
            } else {
                Resource.Error(resultMap?.get("message") as? String ?: "Request failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la demande", e)
        }
    }

    override suspend fun validateUserId(userId: String): Resource<com.example.aureus.domain.repository.UserInfo> {
        return try {
            val callable = functions.getHttpsCallable("validateUserId")

            val data = mapOf("userId" to userId)

            val result: HttpsCallableResult = callable.call(data).await()
            // Access data using reflection for compatibility
            val resultMap = try {
                val field = result.javaClass.getDeclaredField("data")
                field.isAccessible = true
                field.get(result) as? Map<String, Any>
            } catch (e: Exception) {
                null
            }

            if (resultMap != null) {
                Resource.Success(
                    com.example.aureus.domain.repository.UserInfo(
                        exists = resultMap["exists"] as? Boolean ?: false,
                        userId = resultMap["userId"] as? String,
                        firstName = resultMap["firstName"] as? String,
                        lastName = resultMap["lastName"] as? String,
                        email = resultMap["email"] as? String,
                        phone = resultMap["phone"] as? String
                    )
                )
            } else {
                Resource.Error("Validation failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error validating user", e)
        }
    }

    // ==================== TRANSACTIONS DE TRANSFERT ====================

    override fun getOutgoingTransfers(userId: String, limit: Int): Flow<List<Map<String, Any>>> {
        // Filtrer les transactions de type Transfer sortantes
        return firebaseDataManager.getUserTransactions(userId, limit)
            .map { transactions ->
                transactions.filter { tx ->
                    tx["type"] == "EXPENSE" &&
                    tx["category"] == "Transfer" &&
                    tx["senderUserId"] == userId
                }
            }
    }

    override fun getIncomingTransfers(userId: String, limit: Int): Flow<List<Map<String, Any>>> {
        // Filtrer les transactions de type Transfer entrantes
        return firebaseDataManager.getUserTransactions(userId, limit)
            .map { transactions ->
                transactions.filter { tx ->
                    tx["type"] == "INCOME" &&
                    tx["category"] == "Transfer" &&
                    tx["recipientUserId"] == userId
                }
            }
    }

    override fun getMoneyRequestsReceived(userId: String, limit: Int): Flow<List<Map<String, Any>>> {
        return callbackFlow {
            val listener = firebaseDataManager.firestore
                .collection("moneyRequests")
                .whereEqualTo("targetUserId", userId)
                .whereEqualTo("status", "PENDING")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val requests = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                    trySend(requests)
                }

            awaitClose { listener.remove() }
        }
    }

    override fun getMoneyRequestsSent(userId: String, limit: Int): Flow<List<Map<String, Any>>> {
        return callbackFlow {
            val listener = firebaseDataManager.firestore
                .collection("moneyRequests")
                .whereEqualTo("requesterUserId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val requests = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                    trySend(requests)
                }

            awaitClose { listener.remove() }
        }
    }

    override suspend fun acceptMoneyRequest(requestId: String): Resource<String> {
        return try {
            val callable = functions.getHttpsCallable("acceptMoneyRequest")
            val result: HttpsCallableResult = callable.call(mapOf("requestId" to requestId)).await()
            val resultMap = result.getData() as? Map<String, Any?>

            if (resultMap?.get("success") == true) {
                Resource.Success(resultMap["message"] as? String ?: "Demande acceptée")
            } else {
                Resource.Error(resultMap?.get("message") as? String ?: "Acceptation échouée")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de l'acceptation", e)
        }
    }

    override suspend fun rejectMoneyRequest(requestId: String): Resource<String> {
        return try {
            firebaseDataManager.firestore
                .collection("moneyRequests")
                .document(requestId)
                .update(
                    mapOf(
                        "status" to "REJECTED",
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()

            Resource.Success("Request rejected")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to reject request", e)
        }
    }

    // ==================== LIMITES ET QUOTAS ====================

    override suspend fun checkTransferLimits(userId: String): Resource<com.example.aureus.domain.repository.TransferLimits> {
        return try {
            // Récupérer les transactions du jour
            val today = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time

            val transactions = firebaseDataManager.getUserTransactions(userId, 1000)
                .map { txList ->
                    txList.filter { tx ->
                        tx["category"] == "Transfer" &&
                        tx["type"] == "EXPENSE"
                    }
                }

            // Calculer les totaux
            var dailyTotal = 0.0
            var monthlyTotal = 0.0
            val now = System.currentTimeMillis()
            val dailyLimit = 20000.0  // 20,000 MAD
            val monthlyLimit = 100000.0  // 100,000 MAD
            val maxTransfer = 50000.0  // 50,000 MAD

            // Simplifié - en prod, faire des queries Firestore filtering
            Resource.Success(
                com.example.aureus.domain.repository.TransferLimits(
                    dailyLimit = dailyLimit,
                    dailyUsed = dailyTotal,
                    dailyRemaining = dailyLimit - dailyTotal,
                    monthlyLimit = monthlyLimit,
                    monthlyUsed = monthlyTotal,
                    monthlyRemaining = monthlyLimit - monthlyTotal,
                    maxTransferAmount = maxTransfer
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check limits", e)
        }
    }
}