# 🔧 PLAN COMPLET: CORRECTION TRANSACTION RÉELLE & DYNAMIQUE

**Projet:** Aureus Banking Application
**Date:** 12 janvier 2026
**Objectif:** Rendre les transactions entre utilisateurs fonctionnelles, atomiques et sécurisées

---

## 📋 RÉSUMÉ DU PLAN

| Phase | Description | Fichiers impactés | Priorité |
|-------|-------------|-------------------|----------|
| **Phase 1** | Backend: Cloud Functions | `functions/index.js` | 🔴 CRITIQUE |
| **Phase 2** | Data Models | `Contact.kt` | 🔴 CRITIQUE |
| **Phase 3** | Repository Layer | Multiple repositories | 🔴 CRITIQUE |
| **Phase 4** | ViewModel Layer | `HomeViewModel.kt` | 🔴 CRITIQUE |
| **Phase 5** | UI Layer - Transfer | `SendMoneyScreen*.kt` | 🔴 CRITIQUE |
| **Phase 6** | UI Layer - Request | `RequestMoneyScreen*.kt` | 🔴 CRITIQUE |
| **Phase 7** | Navigation Logic | `Navigation.kt` | 🔴 CRITIQUE |
| **Phase 8** | Validation & Security | Multiple | 🟠 ÉLEVÉE |
| **Phase 9** | Tests | Test files | 🟠 ÉLEVÉE |
| **Phase 10** | Monitoring & Logs | Firebase Console | 🟢 MOYENNE |

---

## 🚀 PHASE 1: BACKEND - CLOUD FUNCTIONS

### Objectif
Créer 3 Cloud Functions pour gérer les transferts atomiquement avec Firebase Firestore.

### 1.1 Créer `executeWalletTransfer` - Fonction principale de transfert

**Fichier à modifier:** `functions/index.js`

```javascript
/**
 * Phase 1.1: Execute Wallet Transfer - Fonction atomique de transfert
 * Débiteur envoyeur, crédite receveur, crée 2 transactions
 */
exports.executeWalletTransfer = functions.https.onCall(async (data, context) => {
    // ==================== VALIDATION AUTH ====================
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated to transfer money'
        );
    }

    const senderUserId = context.auth.uid;

    // ==================== VALIDATION INPUTS ====================
    const { recipientUserId, amount, description } = data;

    // Validation des champs requis
    if (!recipientUserId || !amount || amount <= 0) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Recipient user ID and valid amount are required'
        );
    }

    // Validation: ne pas transférer à soi-même
    if (senderUserId === recipientUserId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Cannot transfer money to yourself'
        );
    }

    // Validation du montant maximum
    const MAX_TRANSFER_AMOUNT = 50000; // 50,000 MAD
    if (amount > MAX_TRANSFER_AMOUNT) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            `Transfer amount exceeds maximum limit of ${MAX_TRANSFER_AMOUNT} MAD`
        );
    }

    const db = admin.firestore();

    try {
        // ==================== TRANSACTION ATOMIQUE ====================
        const result = await db.runTransaction(async (transaction) => {
            // 1. Récupérer le compte envoyeur
            const senderAccountsRef = db.collection('accounts')
                .where('userId', '==', senderUserId)
                .where('isActive', '==', true)
                .limit(1);

            const senderAccountsDoc = await transaction.get(senderAccountsRef);

            if (senderAccountsDoc.empty) {
                throw new functions.https.HttpsError(
                    'not-found',
                    'Sender account not found'
                );
            }

            const senderAccountId = senderAccountsDoc.docs[0].id;
            const senderAccount = senderAccountsDoc.docs[0].data();

            // 2. Récupérer le compte receveur
            const recipientAccountsRef = db.collection('accounts')
                .where('userId', '==', recipientUserId)
                .where('isActive', '==', true)
                .limit(1);

            const recipientAccountsDoc = await transaction.get(recipientAccountsRef);

            if (recipientAccountsDoc.empty) {
                throw new functions.https.HttpsError(
                    'not-found',
                    'Recipient account not found'
                );
            }

            const recipientAccountId = recipientAccountsDoc.docs[0].id;
            const recipientAccount = recipientAccountsDoc.docs[0].data();

            // 3. Vérifier le solde envoyeur
            const senderBalance = senderAccount.balance || 0;

            if (senderBalance < amount) {
                throw new functions.https.HttpsError(
                    'failed-precondition',
                    'Insufficient balance'
                );
            }

            // 4. Vérifier les quotas journaliers
            const today = admin.firestore.Timestamp.now().toDate();
            today.setHours(0, 0, 0, 0);

            const dailyTransfersQuery = db.collection('transactions')
                .where('senderUserId', '==', senderUserId)
                .where('type', '==', 'EXPENSE')
                .where('category', '==', 'Transfer')
                .where('createdAt', '>=', today);

            const dailyTransfersDoc = await transaction.get(dailyTransfersQuery);
            const dailyTotal = dailyTransfersDoc.docs.reduce((sum, doc) => {
                return sum + (doc.data().amount || 0);
            }, 0);

            const DAILY_TRANSFER_LIMIT = 20000; // 20,000 MAD par jour

            if (dailyTotal + amount > DAILY_TRANSFER_LIMIT) {
                throw new functions.https.HttpsError(
                    'failed-precondition',
                    `Daily transfer limit exceeded. Available: ${DAILY_TRANSFER_LIMIT - dailyTotal} MAD`
                );
            }

            // ==================== EXÉCUTION DU TRANSFERT ====================

            // 5. Débiter le compte envoyeur
            const newSenderBalance = senderBalance - amount;
            transaction.update(db.collection('accounts').doc(senderAccountId), {
                balance: newSenderBalance,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // 6. Créditer le compte receveur
            const newRecipientBalance = (recipientAccount.balance || 0) + amount;
            transaction.update(db.collection('accounts').doc(recipientAccountId), {
                balance: newRecipientBalance,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // 7. Créer transaction pour l'envoyeur (EXPENSE)
            const senderTransactionId = `trx_${Date.now()}_sender_${senderUserId}`;
            const senderTransaction = {
                transactionId: senderTransactionId,
                userId: senderUserId,
                accountId: senderAccountId,
                senderUserId: senderUserId,
                recipientUserId: recipientUserId,
                recipientName: description || 'Transfer',
                type: 'EXPENSE',
                category: 'Transfer',
                title: 'Money Sent',
                description: `Transfer to ${recipientUserId}`,
                amount: amount,
                merchant: 'Wallet Transfer',
                status: 'COMPLETED',
                balanceAfter: newSenderBalance,
                paymentMethod: 'wallet',
                direction: 'outgoing',
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            };

            transaction.set(
                db.collection('transactions').doc(senderTransactionId),
                senderTransaction
            );

            // 8. Créer transaction pour le receveur (INCOME)
            const recipientTransactionId = `trx_${Date.now()}_recipient_${recipientUserId}`;
            const recipientTransaction = {
                transactionId: recipientTransactionId,
                userId: recipientUserId,
                accountId: recipientAccountId,
                senderUserId: senderUserId,
                recipientUserId: recipientUserId,
                recipientName: description || '',
                type: 'INCOME',
                category: 'Transfer',
                title: 'Money Received',
                description: `Received from ${senderUserId}`,
                amount: amount,
                merchant: 'Wallet Transfer',
                status: 'COMPLETED',
                balanceAfter: newRecipientBalance,
                paymentMethod: 'wallet',
                direction: 'incoming',
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            };

            transaction.set(
                db.collection('transactions').doc(recipientTransactionId),
                recipientTransaction
            );

            // ==================== RÉPONSE ====================
            return {
                success: true,
                transactionId: senderTransactionId,
                recipientTransactionId: recipientTransactionId,
                senderBalance: newSenderBalance,
                recipientBalance: newRecipientBalance,
                amount: amount,
                timestamp: admin.firestore.FieldValue.serverTimestamp()
            };
        });

        // ==================== NOTIFICATION RECEVEUR ====================
        // Envoyer notification push au receveur après succès
        const recipientTokensSnapshot = await db.collection('users')
            .doc(recipientUserId)
            .collection('fcmTokens')
            .get();

        const fcmTokens = [];
        recipientTokensSnapshot.forEach(doc => {
            fcmTokens.push(doc.id);
        });

        if (fcmTokens.length > 0) {
            // Récupérer infos envoyeur
            const senderDoc = await db.collection('users').doc(senderUserId).get();
            const senderData = senderDoc.data();
            const senderName = `${senderData?.firstName || ''} ${senderData?.lastName || ''}`.trim() || 'Someone';

            const message = {
                notification: {
                    title: '💰 Money Received!',
                    body: `${senderName} sent you ${amount} MAD`
                },
                data: {
                    type: 'transfer_received',
                    amount: amount.toString(),
                    senderUserId: senderUserId,
                    senderName: senderName,
                    transactionId: result.transactionId
                },
                tokens: fcmTokens
            };

            try {
                await admin.messaging().sendMulticast(message);
                console.log(`Transfer notification sent to ${fcmTokens.length} devices`);
            } catch (notifyError) {
                console.error('Failed to send transfer notification:', notifyError);
                // Notification error doesn't fail the transfer
            }
        }

        // ==================== LOG D'AUDIT ====================
        await db.collection('transferAudit').add({
            senderUserId: senderUserId,
            recipientUserId: recipientUserId,
            amount: amount,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: 'completed',
            transactionId: result.transactionId
        });

        return result;

    } catch (error) {
        console.error('Wallet transfer failed:', error);

        // Log d'erreur d'audit
        await db.collection('transferAudit').add({
            senderUserId: senderUserId,
            recipientUserId: recipientUserId,
            amount: amount,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: 'failed',
            error: error.message
        }).catch(e => console.error('Failed to log audit:', e));

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Transfer failed: ' + error.message
        );
    }
});

/**
 * Phase 1.2: createMoneyRequest - Créer une demande d'argent
 */
exports.createMoneyRequest = functions.https.onCall(async (data, context) => {
    // Validation auth
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated'
        );
    }

    const requesterUserId = context.auth.uid;
    const { recipientUserId, amount, reason } = data;

    // Validation inputs
    if (!recipientUserId || !amount || amount <= 0) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Recipient user ID and valid amount are required'
        );
    }

    // Validation: ne pas demander à soi-même
    if (requesterUserId === recipientUserId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Cannot request money from yourself'
        );
    }

    const db = admin.firestore();

    try {
        // Créer la demande
        const requestId = `req_${Date.now()}_${requesterUserId}_${recipientUserId}`;

        const requestData = {
            requestId: requestId,
            requesterUserId: requesterUserId,
            targetUserId: recipientUserId,
            amount: amount,
            reason: reason || 'Money Request',
            status: 'PENDING',
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 jours
        };

        await db.collection('moneyRequests').doc(requestId).set(requestData);

        // Notifier le destinataire
        const recipientTokensSnapshot = await db.collection('users')
            .doc(recipientUserId)
            .collection('fcmTokens')
            .get();

        const fcmTokens = [];
        recipientTokensSnapshot.forEach(doc => {
            fcmTokens.push(doc.id);
        });

        if (fcmTokens.length > 0) {
            // Récupérer infos demandeur
            const requesterDoc = await db.collection('users').doc(requesterUserId).get();
            const requesterData = requesterDoc.data();
            const requesterName = `${requesterData?.firstName || ''} ${requesterData?.lastName || ''}`.trim() || 'Someone';

            const message = {
                notification: {
                    title: '💸 Money Request',
                    body: `${requesterName} requested ${amount} MAD`
                },
                data: {
                    type: 'money_request',
                    amount: amount.toString(),
                    requesterUserId: requesterUserId,
                    requesterName: requesterName,
                    requestId: requestId,
                    reason: reason || ''
                },
                tokens: fcmTokens
            };

            await admin.messaging().sendMulticast(message);
            console.log('Money request notification sent');
        }

        return {
            success: true,
            requestId: requestId,
            amount: amount,
            status: 'PENDING'
        };

    } catch (error) {
        console.error('Money request failed:', error);

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Request failed: ' + error.message
        );
    }
});

/**
 * Phase 1.3: validateUserId - Vérifier si un userId existe
 * Utilisé dans UI pour valider contacts avant transfert
 */
exports.validateUserId = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated'
        );
    }

    const { userId } = data;

    if (!userId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'User ID is required'
        );
    }

    const db = admin.firestore();

    try {
        const userDoc = await db.collection('users').doc(userId).get();

        if (!userDoc.exists) {
            return {
                exists: false,
                userId: null
            };
        }

        const userData = userDoc.data();

        return {
            exists: true,
            userId: userId,
            firstName: userData?.firstName || '',
            lastName: userData?.lastName || '',
            email: userData?.email || '',
            phone: userData?.phone || ''
        };

    } catch (error) {
        console.error('User validation failed:', error);

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Validation failed: ' + error.message
        );
    }
});
```

### 1.2 Déployer les nouvelles Cloud Functions

```bash
# Dans le dossier functions/
npm install firebase-functions@latest firebase-admin@latest

# Déployer
firebase deploy --only functions

# Vérifier le déploiement
firebase functions:list
```

---

## 🚀 PHASE 2: DATA MODELS

### 2.1 Modifier `Contact.kt` - Ajouter Firebase userId

**Fichier:** `app/src/main/java/com/example/aureus/domain/model/Contact.kt`

```kotlin
package com.example.aureus.domain.model

import java.util.Date

/**
 * Contact Data Model - Domain Layer
 * ✅ PHASE 2: Ajout de firebaseUserId pour supporter les transferts entre utilisateurs
 */
data class Contact(
    val id: String = "",
    val name: String,
    val phone: String,
    val email: String? = null,
    val avatar: String? = null,
    val accountNumber: String? = null,

    // ✅ NOUVEAU CHAMP: ID Firebase de l'utilisateur (si contact est aussi utilisateur de l'app)
    val firebaseUserId: String? = null,

    val isFavorite: Boolean = false,
    val isBankContact: Boolean = false,
    val category: ContactCategory? = null,
    val isVerifiedAppUser: Boolean = false,  // ✅ Marqueur: si l'utilisateur est vérifié comme utilisateur de l'app
    val lastUsed: Date? = null,              // ✅ Date de dernière utilisation pour "Recent contacts"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Get initials for avatar placeholder (ex: "Mohammed EL ALAMI" -> "MA")
     */
    fun getInitials(): String {
        return name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    /**
     * Get first name for display in lists
     */
    fun getFirstName(): String {
        return name.split(" ").firstOrNull() ?: name
    }

    /**
     * ✅ Check if this contact is also an app user
     */
    fun isAppUser(): Boolean {
        return firebaseUserId != null && isVerifiedAppUser
    }

    /**
     * ✅ Get display name for transfer
     */
    fun getDisplayNameForTransfer(): String {
        return if (isAppUser()) {
            "$name (App User)"
        } else {
            name
        }
    }
}

/**
 * Contact Categories for organization
 */
enum class ContactCategory(val displayName: String) {
    FAMILY("Family"),
    FRIENDS("Friends"),
    WORK("Work"),
    BUSINESS("Business"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): ContactCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
```

### 2.2 Modifier `ContactEntity.kt` - Synchroniser avec le modèle

**Fichier:** `app/src/main/java/com/example/aureus/data/local/entity/ContactEntity.kt`

```kotlin
package com.example.aureus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Contact Entity for Room Database
 * ✅ PHASE 2: Ajout de firebaseUserId et isVerifiedAppUser
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index("userId"),
        Index("name"),
        Index("isFavorite"),
        Index("firebaseUserId")
    ]
)
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val email: String?,
    val avatar: String?,
    val accountNumber: String?,

    // ✅ NOUVEAUX CHAMPS
    val firebaseUserId: String? = null,
    val isVerifiedAppUser: Boolean = false,
    val lastUsed: Long? = null,

    val isFavorite: Boolean = false,
    val isBankContact: Boolean = false,
    val category: String?, // "FAMILY", "FRIENDS", "WORK", "BUSINESS", "OTHER"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)
```

---

## 🚀 PHASE 3: REPOSITORY LAYER

### 3.1 Ajouter `TransferRepository` - Gestion des transferts

**Nouveau fichier:** `app/src/main/java/com/example/aureus/domain/repository/TransferRepository.kt`

```kotlin
package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Transfer Repository Interface - Gestion des transferts entre utilisateurs
 * ✅ PHASE 3: Repository pour transferts atomiques via Cloud Functions
 */
interface TransferRepository {

    // ==================== TRANSFERS ====================

    /**
     * Transférer de l'argent à un autre utilisateur
     * Utilise Cloud Function pour transaction atomique
     */
    suspend fun transferMoney(
        recipientUserId: String,
        amount: Double,
        description: String = ""
    ): Resource<TransferResult>

    /**
     * Créer une demande d'argent
     */
    suspend fun createMoneyRequest(
        recipientUserId: String,
        amount: Double,
        reason: String = ""
    ): Resource<String>

    /**
     * Valider si un userId existe et récupérer ses infos
     */
    suspend fun validateUserId(userId: String): Resource<UserInfo>

    // ==================== TRANSACTIONS DE TRANSFERT ====================

    /**
     * Obtenir les transferts sortants
     */
    fun getOutgoingTransfers(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les transferts entrants
     */
    fun getIncomingTransfers(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les demandes d'argent reçues
     */
    fun getMoneyRequestsReceived(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les demandes d'argent envoyées
     */
    fun getMoneyRequestsSent(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Accepter une demande d'argent
     */
    suspend fun acceptMoneyRequest(requestId: String): Resource<String>

    /**
     * Refuser une demande d'argent
     */
    suspend fun rejectMoneyRequest(requestId: String): Resource<String>

    // ==================== LIMITES ET QUOTAS ====================

    /**
     * Vérifier les limites de transfert
     */
    suspend fun checkTransferLimits(userId: String): Resource<TransferLimits>
}

/**
 * Résultats de transfert
 */
data class TransferResult(
    val success: Boolean,
    val transactionId: String,
    val recipientTransactionId: String,
    val senderBalance: Double,
    val recipientBalance: Double,
    val amount: Double,
    val timestamp: String
)

/**
 * Infos utilisateur validé
 */
data class UserInfo(
    val exists: Boolean,
    val userId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)

/**
 * Limites de transfert
 */
data class TransferLimits(
    val dailyLimit: Double,
    val dailyUsed: Double,
    val dailyRemaining: Double,
    val monthlyLimit: Double,
    val monthlyUsed: Double,
    val monthlyRemaining: Double,
    val maxTransferAmount: Double
)
```

### 3.2 Implémenter `TransferRepositoryImpl`

**Nouveau fichier:** `app/src/main/java/com/example/aureus/data/repository/TransferRepositoryImpl.kt`

```kotlin
package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.TransferRepository
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
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

    private val functions = Firebase.functions

    // ==================== TRANSFERS ====================

    override suspend fun transferMoney(
        recipientUserId: String,
        amount: Double,
        description: String
    ): Resource<TransferResult> {
        return try {
            val callable = functions.getHttpsCallable("executeWalletTransfer")

            val data = mapOf(
                "recipientUserId" to recipientUserId,
                "amount" to amount,
                "description" to description
            )

            val result: HttpsCallableResult = callable.call(data).await()
            resultMap = result.data as? Map<String, Any>

            if (resultMap?.get("success") == true) {
                Resource.Success(
                    TransferResult(
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
            val resultMap = result.data as? Map<String, Any>

            if (resultMap?.get("success") == true) {
                Resource.Success(resultMap["requestId"] as? String ?: "")
            } else {
                Resource.Error(resultMap?.get("message") as? String ?: "Request failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la demande", e)
        }
    }

    override suspend fun validateUserId(userId: String): Resource<UserInfo> {
        return try {
            val callable = functions.getHttpsCallable("validateUserId")

            val data = mapOf("userId" to userId)

            val result: HttpsCallableResult = callable.call(data).await()
            val resultMap = result.data as? Map<String, Any>

            if (resultMap != null) {
                Resource.Success(
                    UserInfo(
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
        // TODO: Implémenter la logique d'acceptation
        // Ceci devrait créer un transfert automatique
        return Resource.Error("Not implemented yet")
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

    override suspend fun checkTransferLimits(userId: String): Resource<TransferLimits> {
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
                TransferLimits(
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
```

### 3.3 Mettre à jour les DI Modules

**Fichier:** `app/src/main/java/com/example/aureus/di/ViewModelModule.kt`

```kotlin
// Ajouter ce binding dans ViewModelModule
@Provides
@Singleton
fun provideTransferRepository(
    firebaseDataManager: FirebaseDataManager
): TransferRepository {
    return TransferRepositoryImpl(firebaseDataManager)
}
```

---

## 🚀 PHASE 4: VIEWMODEL LAYER

### 4.1 Créer `TransferViewModel` - Gestion des transferts UI

**Nouveau fichier:** `app/src/main/java/com/example/aureus/ui/transfer/viewmodel/TransferViewModel.kt`

```kotlin
package com.example.aureus.ui.transfer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.TransferRepository
import com.example.aureus.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Transfer ViewModel - Gestion des transferts entre utilisateurs
 * ✅ PHASE 4: ViewModel complet pour SendMoney et RequestMoney
 */
@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val firebaseDataManager: FirebaseDataManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    // ==================== UI STATES ====================

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    // ==================== SEND MONEY ====================

    /**
     * Sélectionner un contact pour transfert
     */
    fun selectContact(contact: Contact) {
        _selectedContact.value = contact
        _uiState.value = _uiState.value.copy(contactValidationError = null)

        // Si le contact a un firebaseUserId, le valider
        contact.firebaseUserId?.let { userId ->
            validateContactUser(userId)
        }
    }

    /**
     * Sélectionner un contact par ID (pour réception depuis navigation)
     */
    fun selectContactById(contactId: String, contacts: List<Contact>) {
        val contact = contacts.find { it.id == contactId }
        if (contact != null) {
            selectContact(contact)
        }
    }

    /**
     * Définir le montant du transfert
     */
    fun setAmount(value: String) {
        // Valider: seulement chiffres et un point décimal
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d+$"))) {
            _amount.value = value
            _uiState.value = _uiState.value.copy(amountValidationError = null)

            // Vérifier si le montant dépasse les limites
            value.toDoubleOrNull()?.let { amt ->
                if (amt > TRANSFER_MAX_AMOUNT) {
                    _uiState.value = _uiState.value.copy(
                        amountValidationError = "Le montant maximum est de ${TRANSFER_MAX_AMOUNT} MAD"
                    )
                }
            }
        }
    }

    /**
     * Définir la description du transfert
     */
    fun setDescription(value: String) {
        _description.value = value
    }

    /**
     * Valider si l'utilisateur Firebase du contact existe
     */
    private fun validateContactUser(firebaseUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidatingContact = true)

            when (val result = transferRepository.validateUserId(firebaseUserId)) {
                is Resource.Success -> {
                    if (result.data.exists) {
                        _uiState.value = _uiState.value.copy(
                            isValidatingContact = false,
                            contactValidationError = null,
                            isContactAppUser = true,
                            contactUserInfo = result.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isValidatingContact = false,
                            contactValidationError = "Ce contact n'est pas un utilisateur de l'app",
                            isContactAppUser = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isValidatingContact = false,
                        contactValidationError = "Impossible de vérifier le contact"
                    )
                }
            }
        }
    }

    /**
     * Exécuter le transfert d'argent (après vérification PIN)
     */
    fun executeTransfer(): Resource<String> {
        var result: Resource<String> = Resource.Idle

        viewModelScope.launch {
            val contact = _selectedContact.value
            val amountValue = _amount.value.toDoubleOrNull()
            val desc = _description.value.ifBlank { _selectedContact.value?.getDisplayNameForTransfer() ?: "Transfer" }

            // Validation
            when {
                contact == null -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez sélectionner un contact")
                    result = Resource.Error("Veuillez sélectionner un contact")
                    return@launch
                }
                contact.firebaseUserId == null -> {
                    _uiState.value = _uiState.value.copy(error = "Ce contact ne peut pas recevoir d'argent")
                    result = Resource.Error("Ce contact ne peut pas recevoir d'argent")
                    return@launch
                }
                !_uiState.value.isContactAppUser -> {
                    _uiState.value = _uiState.value.copy(error = _uiState.value.contactValidationError)
                    result = Resource.Error(_uiState.value.contactValidationError ?: "Contact invalide")
                    return@launch
                }
                amountValue == null || amountValue <= 0 -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez entrer un montant valide")
                    result = Resource.Error("Veuillez entrer un montant valide")
                    return@launch
                }
                amountValue > TRANSFER_MAX_AMOUNT -> {
                    _uiState.value = _uiState.value.copy(error = "Le montant maximum est de ${TRANSFER_MAX_AMOUNT} MAD")
                    result = Resource.Error("Limite de transfert dépassée")
                    return@launch
                }
            }

            // Exécuter le transfert
            _uiState.value = _uiState.value.copy(isTransferring = true)

            when (val transferResult = transferRepository.transferMoney(
                recipientUserId = contact.firebaseUserId!!,
                amount = amountValue,
                description = desc
            )) {
                is Resource.Success -> {
                    // Track succès - Analytics
                    val senderId = firebaseDataManager.currentUserId()
                    if (senderId != null) {
                        analyticsManager.trackTransferSent(
                            userId = senderId,
                            amount = amountValue,
                            recipient = contact.name,
                            method = "wallet_to_wallet"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferSuccess = true,
                        transferResultData = transferResult.data,
                        error = null
                    )

                    result = Resource.Success("Transfert effectué avec succès!")

                    // Reset le formulaire
                    resetForm()
                }
                is Resource.Error -> {
                    // Track échec - Analytics
                    val senderId = firebaseDataManager.currentUserId()
                    if (senderId != null) {
                        analyticsManager.trackTransactionFailed(
                            userId = senderId,
                            error = transferResult.message ?: "Unknown error"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        error = transferResult.message ?: "Erreur lors du transfert"
                    )

                    result = Resource.Error(transferResult.message ?: "Erreur")
                }
            }
        }

        return result
    }

    // ==================== REQUEST MONEY ====================

    /**
     * Créer une demande d'argent
     */
    fun createMoneyRequest(): Resource<String> {
        var result: Resource<String> = Resource.Idle

        viewModelScope.launch {
            val contact = _selectedContact.value
            val amountValue = _amount.value.toDoubleOrNull()
            val reason = _description.value.ifBlank { "Demande de paiement" }

            // Validation
            when {
                contact == null -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez sélectionner un contact")
                    result = Resource.Error("Veuillez sélectionner un contact")
                    return@launch
                }
                contact.firebaseUserId == null -> {
                    _uiState.value = _uiState.value.copy(error = "Ce contact ne peut pas être sollicité")
                    result = Resource.Error("Contact invalide")
                    return@launch
                }
                amountValue == null || amountValue <= 0 -> {
                    _uiState.value = _uiState.value.copy(error = "Veuillez entrer un montant valide")
                    result = Resource.Error("Montant invalide")
                    return@launch
                }
            }

            // Créer la demande
            _uiState.value = _uiState.value.copy(isCreatingRequest = true)

            when (val requestResult = transferRepository.createMoneyRequest(
                recipientUserId = contact.firebaseUserId!!,
                amount = amountValue,
                reason = reason
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        requestSuccess = true,
                        requestId = requestResult.data,
                        error = null
                    )

                    result = Resource.Success("Demande envoyée!")
                    resetForm()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        error = requestResult.message ?: "Erreur lors de la demande"
                    )
                    result = Resource.Error(requestResult.message ?: "Erreur")
                }
            }
        }

        return result
    }

    /**
     * Charger les demandes d'argent reçues
     */
    fun loadIncomingMoneyRequests() {
        val userId = firebaseDataManager.currentUserId() ?: return

        transferRepository.getMoneyRequestsReceived(userId, 10)
            .onEach { requests ->
                _uiState.value = _uiState.value.copy(
                    incomingMoneyRequests = requests
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Erreur de chargement des demandes: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Rejeter une demande d'argent
     */
    fun rejectMoneyRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = transferRepository.rejectMoneyRequest(requestId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Demande refusée"
                    )
                    // Recharger les demandes
                    loadIncomingMoneyRequests()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Erreur lors du refus"
                    )
                }
            }
        }
    }

    // ==================== UTILITIES ====================

    /**
     * Vérifier les limites de transfert
     */
    fun checkTransferLimits() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            when (val result = transferRepository.checkTransferLimits(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        transferLimits = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Impossible de vérifier les limites"
                    )
                }
            }
        }
    }

    /**
     * Reset le formulaire
     */
    fun resetForm() {
        _selectedContact.value = null
        _amount.value = ""
        _description.value = ""
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            transferSuccess = false,
            requestSuccess = false,
            contactValidationError = null,
            amountValidationError = null
        )
    }

    /**
     * Clear les messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    companion object {
        const val TRANSFER_MAX_AMOUNT = 50000.0  // 50,000 MAD
        const val TRANSFER_DAILY_LIMIT = 20000.0  // 20,000 MAD
        const val TRANSFER_MONTHLY_LIMIT = 100000.0  // 100,000 MAD
    }
}

// ==================== DATA CLASSES ====================

data class TransferUiState(
    val isValidatingContact: Boolean = false,
    val isTransferring: Boolean = false,
    val isCreatingRequest: Boolean = false,
    val transferSuccess: Boolean = false,
    val requestSuccess: Boolean = false,
    val contactValidationError: String? = null,
    val amountValidationError: String? = null,
    val isContactAppUser: Boolean = false,
    val contactUserInfo: com.example.aureus.domain.repository.UserInfo? = null,
    val transferResultData: com.example.aureus.domain.repository.TransferResult? = null,
    val requestId: String? = null,
    val incomingMoneyRequests: List<Map<String, Any>> = emptyList(),
    val transferLimits: com.example.aureus.domain.repository.TransferLimits? = null,
    val error: String? = null,
    val successMessage: String? = null
)
```

### 4.2 Mettre à jour `HomeViewModel.kt`

**Fichier à modifier:** `app/src/main/java/com/example/aureus/ui/home/viewmodel/HomeViewModel.kt`

```kotlin
// Remplacer la méthode sendMoney existante par ceci:

/**
 * ✅ PHASE 4: sendMoney délégué à TransferRepository
 * Utilise Cloud Function pour transaction atomique
 */
fun sendMoneyToContact(contact: Contact, amount: Double): Flow<Result<String>> = flow {
    val userId = firebaseDataManager.currentUserId()
    if (userId == null) {
        emit(Result.failure(Exception("User not logged in")))
        return@flow
    }

    // Vérifier si le contact a un firebaseUserId
    if (contact.firebaseUserId == null) {
        emit(Result.failure(Exception("Ce contact ne peut pas recevoir d'argent")))
        return@flow
    }

    // Créer un TransferRepository (injecté via DI)
    val transferRepository = object : TransferRepository {
        // Implémentation temporaire - en prod, inject via constructor
        override suspend fun transferMoney(
            recipientUserId: String,
            amount: Double,
            description: String
        ): com.example.aureus.domain.model.Resource<com.example.aureus.domain.repository.TransferResult> {
            // Appel Cloud Function
            return try {
                val callable = com.google.firebase.functions.Firebase.functions.getHttpsCallable("executeWalletTransfer")
                val data = mapOf(
                    "recipientUserId" to recipientUserId,
                    "amount" to amount,
                    "description" to description
                )
                val result = callable.call(data).await()
                val resultMap = result.data as? Map<String, Any>

                if (resultMap?.get("success") == true) {
                    com.example.aureus.domain.model.Resource.Success(
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
                    com.example.aureus.domain.model.Resource.Error(
                        resultMap?.get("message") as? String ?: "Transfer failed"
                    )
                }
            } catch (e: Exception) {
                com.example.aureus.domain.model.Resource.Error(e.message ?: "Error", e)
            }
        }

        // Implémenter méthodes requises...
        override suspend fun createMoneyRequest(
            recipientUserId: String,
            amount: Double,
            reason: String
        ): com.example.aureus.domain.model.Resource<String> {
            TODO("Not implemented")
        }

        override suspend fun validateUserId(userId: String): com.example.aureus.domain.model.Resource<com.example.aureus.domain.repository.UserInfo> {
            TODO("Not implemented")
        }

        override fun getOutgoingTransfers(userId: String, limit: Int): kotlinx.coroutines.flow.Flow<List<Map<String, Any>>> {
            TODO("Not implemented")
        }

        override fun getIncomingTransfers(userId: String, limit: Int): kotlinx.coroutines.flow.Flow<List<Map<String, Any>>> {
            TODO("Not implemented")
        }

        override fun getMoneyRequestsReceived(userId: String, limit: Int): kotlinx.coroutines.flow.Flow<List<Map<String, Any>>> {
            TODO("Not implemented")
        }

        override fun getMoneyRequestsSent(userId: String, limit: Int): kotlinx.coroutines.flow.Flow<List<Map<String, Any>>> {
            TODO("Not implemented")
        }

        override suspend fun acceptMoneyRequest(requestId: String): com.example.aureus.domain.model.Resource<String> {
            TODO("Not implemented")
        }

        override suspend fun rejectMoneyRequest(requestId: String): com.example.aureus.domain.model.Resource<String> {
            TODO("Not implemented")
        }

        override suspend fun checkTransferLimits(userId: String): com.example.aureus.domain.model.Resource<com.example.aureus.domain.repository.TransferLimits> {
            TODO("Not implemented")
        }
    }

    // Exécuter le transfert
    val result = transferRepository.transferMoney(
        recipientUserId = contact.firebaseUserId!!,
        amount = amount,
        description = "Transfer to ${contact.name}"
    )

    when (result) {
        is com.example.aureus.domain.model.Resource.Success -> {
            // Track successful transfer
            analyticsManager.trackTransferSent(
                userId = userId,
                amount = amount,
                recipient = contact.name,
                method = "wallet_to_wallet"
            )

            emit(Result.success("Money sent to ${contact.name}!"))
        }
        is com.example.aureus.domain.model.Resource.Error -> {
            // Track failed transfer
            analyticsManager.trackTransactionFailed(
                userId = userId,
                error = result.message ?: "Transfer failed"
            )

            emit(Result.failure(
                Exception(result.message ?: "Impossible d'effectuer le transfert")
            ))
        }
    }
}
```

---

---

## 🚀 PHASE 5: UI LAYER - SEND MONEY

### 5.1 Modifier `SendMoneyScreenFirebase.kt` - Connecter au ViewModel

**Fichier à modifier:** `app/src/main/java/com/example/aureus/ui/transfer/SendMoneyScreenFirebase.kt`

```kotlin
package com.example.aureus.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aureus.domain.model.Contact
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.navigation.Screen
import com.example.aureus.ui.components.SecureBackHandler
import com.example.aureus.ui.transfer.viewmodel.TransferViewModel

/**
 * Send Money Screen - Firebase-based
 * ✅ PHASE 5: Intégration avec TransferViewModel pour transferts RÉELS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreenFirebase(
    navController: NavHostController? = null,
    viewModel: ContactViewModel = hiltViewModel(),
    transferViewModel: TransferViewModel = hiltViewModel(),  // ✅ NOUVEAU
    onNavigateBack: () -> Unit = {},
    onSendClick: (Contact, Double) -> Unit = { _, _ -> },
    onAddContactClick: () -> Unit = {}
) {
    val transferUiState by transferViewModel.uiState.collectAsState()
    val contactUiState by viewModel.uiState.collectAsState()
    val amount by transferViewModel.amount.collectAsState()
    val description by transferViewModel.description.collectAsState()
    val selectedContact by transferViewModel.selectedContact.collectAsState()

    // Success/Error Dialogs
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Load contacts when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
        viewModel.loadFavoriteContacts()
    }

    // Watch for successful transfer
    LaunchedEffect(transferUiState.transferSuccess) {
        if (transferUiState.transferSuccess) {
            showSuccessDialog = true
        }
    }

    // ✅ PHASE 6: Secure back handler - prevent accidental back when user has entered data
    SecureBackHandler(
        enabled = true,
        onBackRequest = {
            if (amount.isNotEmpty() || selectedContact != null) {
                // Show confirmation dialog if user has entered data
                showExitConfirmationDialog = true
            } else {
                onNavigateBack()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddContactClick) {
                        Icon(Icons.Default.PersonAdd, "Add Contact")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ✅ NEW: Show validation status for selected contact
            item {
                if (selectedContact != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (transferUiState.isContactAppUser)
                                SemanticGreen.copy(alpha = 0.1f)
                            else if (transferUiState.contactValidationError != null)
                                SemanticRed.copy(alpha = 0.1f)
                            else NeutralLightGray
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (transferUiState.isValidatingContact) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SecondaryGold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Validation du contact...", style = MaterialTheme.typography.bodyMedium)
                            } else if (transferUiState.isContactAppUser) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SemanticGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${selectedContact!!.getDisplayNameForTransfer()} ✓",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (transferUiState.contactUserInfo != null) {
                                        Text(
                                            "User ID: ${transferUiState.contactUserInfo!!.userId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeutralMediumGray
                                        )
                                    }
                                }
                            } else if (transferUiState.contactValidationError != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = SemanticRed
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    transferUiState.contactValidationError ?: "Contact invalide",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SemanticRed
                                )
                            }
                        }
                    }
                }
            }

            // Amount Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount",
                            fontSize = 14.sp,
                            color = NeutralMediumGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { transferViewModel.setAmount(it) },
                                placeholder = { Text("0.00", fontSize = 40.sp) },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryGold,
                                    unfocusedBorderColor = NeutralLightGray
                                ),
                                isError = transferUiState.amountValidationError != null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MAD",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGold
                            )
                        }

                        // ✅ NEW: Show amount validation error
                        if (transferUiState.amountValidationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = transferUiState.amountValidationError!!,
                                color = SemanticRed,
                                fontSize = 12.sp
                            )
                        }

                        // ✅ NEW: Show transfer limits if available
                        if (transferUiState.transferLimits != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = NeutralLightGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        "Limites disponibles:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Journalier: ${transferUiState.transferLimits!!.dailyRemaining.toInt()} MAD",
                                        fontSize = 11.sp,
                                        color = NeutralMediumGray
                                    )
                                    Text(
                                        "Mensuel: ${transferUiState.transferLimits!!.monthlyRemaining.toInt()} MAD",
                                        fontSize = 11.sp,
                                        color = NeutralMediumGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Favorites
            if (contactUiState.favoriteContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorites",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(contactUiState.favoriteContacts) { contact ->
                            FavoriteContactItem(
                                contact = contact,
                                isSelected = selectedContact?.id == contact.id,
                                onClick = { transferViewModel.selectContact(contact) }
                            )
                        }
                    }
                }
            }

            // All Contacts
            item {
                Text(
                    text = "All Contacts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }

            if (contactUiState.isLoading && contactUiState.contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SecondaryGold)
                    }
                }
            } else {
                items(contactUiState.contacts) { contact ->
                    // ✅ MARKER: Indiquer si le contact est un utilisateur de l'app
                    val isAppUser = contact.isAppUser()

                    ContactListItem(
                        contact = contact,
                        isSelected = selectedContact?.id == contact.id,
                        isUserBadge = isAppUser,
                        onClick = { transferViewModel.selectContact(contact) }
                    )
                }
            }

            if (contactUiState.contacts.isEmpty() && !contactUiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ContactPhone,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = NeutralMediumGray.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No contacts yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryNavyBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your first contact to send money",
                                fontSize = 14.sp,
                                color = NeutralMediumGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddContactClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                            ) {
                                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Contact")
                            }
                        }
                    }
                }
            }

            // Note
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { transferViewModel.setDescription(it) },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Send Button
            item {
                Button(
                    onClick = {
                        // ✅ PHASE 5: Navigate vers PIN verification AVANT transfert
                        when {
                            selectedContact == null -> {
                                transferViewModel.clearMessages()
                            }
                            amount.isBlank() -> {
                                transferViewModel.setAmount("")
                            }
                            transferUiState.contactValidationError != null -> {
                                // Show error alert
                            }
                            transferUiState.amountValidationError != null -> {
                                // Show error alert
                            }
                            else -> {
                                // ✅ PIN verification avant transfert
                                navController?.navigate(
                                    Screen.PinVerification.route.replace("{action}", "send_money")
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = amount.isNotEmpty() &&
                             selectedContact != null &&
                             transferUiState.isContactAppUser &&
                             transferUiState.contactValidationError == null &&
                             transferUiState.amountValidationError == null &&
                             !transferUiState.isTransferring,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (transferUiState.isTransferring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeutralWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Money", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ✅ NEW: Error Display
            if (transferUiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SemanticRed.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = SemanticRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                transferUiState.error!!,
                                color = SemanticRed,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Success Dialog
            item {
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessDialog = false
                            transferViewModel.clearMessages()
                        },
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SemanticGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = { Text("Transfer Successful!", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("Your money has been sent successfully.")
                                selectedContact?.let { contact ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("To: ${contact.name}", fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Amount: ${amount} MAD", fontWeight = FontWeight.SemiBold)

                                    if (transferUiState.transferResultData != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Your new balance: ${transferUiState.transferResultData!!.senderBalance} MAD",
                                            fontSize = 13.sp,
                                            color = NeutralMediumGray
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    transferViewModel.clearMessages()
                                    // Navigate to Dashboard
                                    navController?.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.SendMoney.route) { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)
                            ) {
                                Text("Done")
                            }
                        },
                        containerColor = NeutralWhite
                    )
                }
            }

            // ✅ NEW: Exit Confirmation Dialog
            item {
                if (showExitConfirmationDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitConfirmationDialog = false },
                        icon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = SecondaryGold,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = { Text("Discard Changes?", fontWeight = FontWeight.Bold) },
                        text = {
                            Text(
                                "You have entered some information. Are you sure you want to leave without sending money?"
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showExitConfirmationDialog = false
                                    transferViewModel.resetForm()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                            ) {
                                Text("Leave")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitConfirmationDialog = false }) {
                                Text("Stay")
                            }
                        },
                        containerColor = NeutralWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteContactItem(
    contact: Contact,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) SecondaryGold else NeutralLightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.getInitials(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryNavyBlue else NeutralMediumGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.getFirstName(),
            fontSize = 12.sp,
            color = PrimaryNavyBlue
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    isSelected: Boolean,
    isUserBadge: Boolean = false,  // ✅ NOUVEAU PARAMÈTRE
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SecondaryGold.copy(alpha = 0.1f) else NeutralWhite
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SecondaryGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.getInitials(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavyBlue
                        )

                        // ✅ NOUVEAU: Badge utilisateur de l'app
                        if (isUserBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SemanticGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "App User",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = contact.phone,
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = SemanticRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
```

---

## 🚀 PHASE 6: UI LAYER - REQUEST MONEY

### 6.1 Modifier `RequestMoneyScreenFirebase.kt`

**Mêmes modifications que SendMoneyScreenFirebase**, mais en appelant `transferViewModel.createMoneyRequest()` au lieu de `transferViewModel.executeTransfer()`.

Code similaire, remplacer:
- `onSendClick` par `onRequestClick`
- `transferViewModel.executeTransfer()` par `transferViewModel.createMoneyRequest()`
- Messages adaptés pour "Demande d'argent"

---

## 🚀 PHASE 7: NAVIGATION LOGIC

### 7.1 Modifier `Navigation.kt` - Connecter PIN au transfert

**Fichier à modifier:** `app/src/main/java/com/example/aureus/ui/navigation/Navigation.kt`

```kotlin
// === MODIFIER LA PARTIE PIN VERIFICATION ===

// Ajouter les imports nécessaires:
import com.example.aureus.ui.transfer.viewmodel.TransferViewModel

// Modifier le composable PinVerificationScreen:

// Pin Verification Screen (Phase 1 - Security)
composable(
    route = Screen.PinVerification.route,
    arguments = listOf(navArgument("action") { type = NavType.StringType })
) { backStackEntry ->
    val action = backStackEntry.arguments?.getString("action") ?: ""
    val pinSecurityManager: com.example.aureus.security.PinSecurityManager = hiltViewModel()
    val pinAttemptTracker: com.example.aureus.security.PinAttemptTracker = hiltViewModel()
    val transferViewModel: TransferViewModel = hiltViewModel()  // ✅ NOUVEAU

    // ✅ CHANGEMENT CRITIQUE: onSuccess maintenant exécute le transfert!

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

                    // Exécuter le transfert via ViewModel
                    val result = transferViewModel.executeTransfer()

                    // Observer le résultat
                    // (Le Flow est déjà géré dans le ViewModel via uiState)

                    // Attendre un instant pour laisser l'UI se mettre à jour
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(500)

                        // Naviguer vers Dashboard après succès
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SendMoney.route) { inclusive = true }
                        }
                    }
                }
                "request_money" -> {
                    // ✅ CRÉER LA DEMANDE RÉELLE!

                    val result = transferViewModel.createMoneyRequest()

                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(500)

                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.RequestMoney.route) { inclusive = true }
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
```

---

## 🚀 PHASE 8: VALIDATION & SECURITY

### 8.1 Ajouter middleware de validation dans FirebaseDataManager

**Fichier à modifier:** `app/src/main/java/com/example/aureus/data/remote/firebase/FirebaseDataManager.kt`

```kotlin
/**
 * ✅ PHASE 8: Méthode utilitaire pour obtenir le solde actuel
 */
suspend fun getCurrentBalance(userId: String): Result<Double> = onFirestore {
    val accounts = accountsCollection
        .whereEqualTo("userId", userId)
        .whereEqualTo("isActive", true)
        .limit(1)
        .get()
        .await()

    if (accounts.isEmpty) {
        throw Exception("No account found")
    }

    val balance = accounts.documents[0].getDouble("balance") ?: 0.0
    balance
}

/**
 * ✅ PHASE 8: Vérifier si un montant de transfert est valide
 */
suspend fun validateTransferAmount(userId: String, amount: Double): ValidationResult {
    // Récupérer le solde actuel
    val balanceResult = getCurrentBalance(userId)

    return when {
        amount <= 0 -> ValidationResult(invalid = true, message = "Le montant doit être positif")
        amount > 50000.0 -> ValidationResult(invalid = true, message = "Le montant maximum est de 50,000 MAD")
        balanceResult.isFailure -> ValidationResult(invalid = true, message = "Impossible de vérifier le solde")
        (balanceResult.getOrNull() ?: 0.0) < amount ->
            ValidationResult(invalid = true, message = "Solde insuffisant")
        else -> ValidationResult(invalid = false, message = "OK", balance = balanceResult.getOrNull() ?: 0.0)
    }
}

data class ValidationResult(
    val invalid: Boolean,
    val message: String,
    val balance: Double? = null
)
```

---

## 🚀 PHASE 9: TESTS

### 9.1 Tests unitaires pour TransferRepository

**Nouveau fichier à créer:** `app/src/test/java/com/example/aureus/ui/transfer/viewmodel/TransferViewModelTest.kt`

```kotlin
package com.example.aureus.ui.transfer.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * ✅ PHASE 9: Tests unitaires pour TransferViewModel
 */
class TransferViewModelTest {

    @Test
    fun `setAmount should update amount state`() = runTest {
        // TODO: Implement mock repository and test
    }

    @Test
    fun `setAmount with invalid input should show error`() = runTest {
        // TODO: Test validation
    }

    @Test
    fun `selectContact should validate user if firebaseUserId exists`() = runTest {
        // TODO: Test contact selection and validation
    }

    @Test
    fun `executeTransfer should call repository with correct params`() = runTest {
        // TODO: Test transfer execution
    }

    @Test
    fun `executeTransfer with insufficient balance should fail`() = runTest {
        // TODO: Test error handling
    }

    @Test
    fun `createMoneyRequest should call repository and return request ID`() = runTest {
        // TODO: Test request creation
    }
}
```

---

## 🚀 PHASE 10: MONITORING & LOGGING

### 10.1 Firebase Console Setup

1. **Activer Crashlytics** pour crash reporting des Cloud Functions
2. **Activer Performance Monitoring** pour monitoring des durées de transfert
3. **Activer Cloud Logging** pour logs des transactions:
```javascript
// Dans executeWalletTransfer():
console.log(`Transfer initiated: ${senderUserId} -> ${recipientUserId}, amount: ${amount}`);
```

### 10.2 Firestore Indexes

**Fichier à créer:** `firestore.indexes.json` (ou update)

```json
{
  "indexes": [
    // Transfers by sender
    {
      "collectionGroup": "transactions",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "senderUserId", "order": "ASCENDING" },
        { "fieldPath": "type", "order": "ASCENDING" },
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    // Transfers by recipient
    {
      "collectionGroup": "transactions",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "recipientUserId", "order": "ASCENDING" },
        { "fieldPath": "type", "order": "ASCENDING" },
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    // Money requests by target
    {
      "collectionGroup": "moneyRequests",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "targetUserId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## �� TABLEAU RÉCAPITULATIF DES MODIFICATIONS

| Phase | Fichier | Type de modification | Complexité |
|-------|---------|---------------------|-----------|
| 1 | `functions/index.js` | **AJOUT**: 3 Cloud Functions | 🔴 ÉLEVÉE |
| 2 | `Contact.kt` | **MODIF**: Ajout firebaseUserId, isVerifiedAppUser | 🟡 MOYENNE |
| 2 | `ContactEntity.kt` | **MODIF**: Synchronize model | 🟡 MOYENNE |
| 3 | `TransferRepository.kt` | **NOUVEAU**: Interface complète | 🟢 FAIBLE |
| 3 | `TransferRepositoryImpl.kt` | **NOUVEAU**: Implementation | 🔴 ÉLEVÉE |
| 3 | `ViewModelModule.kt` | **MODIF**: DI binding | 🟢 FAIBLE |
| 4 | `TransferViewModel.kt` | **NOUVEAU**: ViewModel complet | 🔴 ÉLEVÉE |
| 4 | `HomeViewModel.kt` | **MODIF**: Update sendMoney | 🟡 MOYENNE |
| 5 | `SendMoneyScreenFirebase.kt` | **REFAC**: Integration TransferViewModel | 🔴 ÉLEVÉE |
| 6 | `RequestMoneyScreenFirebase.kt` | **REFAC**: Integration TransferViewModel | 🔴 ÉLEVÉE |
| 7 | `Navigation.kt` | **MODIF**: Connect PIN to transfer execution | 🟡 MOYENNE |
| 8 | `FirebaseDataManager.kt` | **AJOUT**: Validation helpers | 🟡 MOYENNE |
| 9 | `TransferViewModelTest.kt` | **NOUVEAU**: Unit tests | 🟡 MOYENNE |
| 10 | `firestore.indexes.json` | **UPDATE**: Add indexes | 🟢 FAIBLE |

---

## ✅ CHECKLIST DE VALIDATION

Avant de marquer cette fonctionnalité comme "COMPLÈTE":

### Backend (Firebase Functions)
- [ ] `executeWalletTransfer` déployée et testée
- [ ] `createMoneyRequest` déployée et testée
- [ ] `validateUserId` déployée et testée
- [ ] Transaction atomique testée (success & failure cases)
- [ ] Notifications FCM envoyées
- [ ] Logs d'audit créés dans Firestore
- [ ] Indexes Firestore déployés

### Frontend (Kotlin)
- [ ] `Contact.kt` mis à jour avec firebaseUserId
- [ ] `ContactEntity.kt` synchronisé
- [ ] `TransferRepository` interface créée
- [ ] `TransferRepositoryImpl` implémentée
- [ ] `TransferViewModel` fonctionnel
- [ ] `SendMoneyScreenFirebase` intégré avec ViewModel
- [ ] `RequestMoneyScreenFirebase` intégré avec ViewModel
- [ ] `Navigation.kt` connecté au transfert
- [ ] Validation de solde avant transfert
- [ ] Messages d'erreur utilisateur-friendly

### Tests
- [ ] Tests unitaires TransferRepository
- [ ] Tests unitaires TransferViewModel
- [ ] Tests d'intégration E2E Send Money flow
- [ ] Tests d'intégration E2E Request Money flow
- [ ] Tests edge cases (insufficient funds, self-transfer, etc.)

### UX
- [ ] Badges "App User" pour contacts utilisateurs
- [ ] Indicateur de validation contact en temps réel
- [ ] Messages de succès détaillés (avec nouveaux soldes)
- [ ] Messages d'erreur clairs
- [ ] Loading states pendant transfert
- [ ] Dialog de confirmation de succès

### Security
- [ ] PIN verification obligatoire avant transfert
- [ ] Validation firebaseUserId côté serveur
- [ ] Limites de montant (max, daily, monthly)
- [ ] Logs d'audit pour compliance
- [ ] Pas de transfert à soi-même

---

## 🚀 ORDRE D'IMPLÉMENTATION RECOMMANDÉ

1. **Phase 1** (Backend)
   - Développer les 3 Cloud Functions
   - Déployer dans environnement test
   - Tester avec Firebase Console

2. **Phase 2** (Data Models)
   - Modifier Contact.kt
   - Modifier ContactEntity.kt
   - Migrations Room DB (si nécessaire)

3. **Phase 3** (Repository)
   - Créer TransferRepository interface
   - Implémenter TransferRepositoryImpl
   - Ajouter DI binding

4. **Phase 4** (ViewModels)
   - Créer TransferViewModel
   - Mettre à jour HomeViewModel

5. **Phase 5 + 6** (UI)
   - Modifier SendMoneyScreenFirebase
   - Modifier RequestMoneyScreenFirebase

6. **Phase 7** (Navigation)
   - Connecter PIN verification à l'exécution

7. **Phase 8** (Validation)
   - Ajouter helpers validation FirebaseDataManager

8. **Phase 9** (Tests)
   - Écrire tests unitaires
   - Tests d'intégration

9. **Phase 10** (Monitoring)
   - Setup Firebase monitoring
   - Déployer indexes

---

## 📝 NOTES IMPORTANTES

1. **Tests d'abord:** Tester chaque phase avant de passer à la suivante
2. **Rollback:** Garder le code existant en backup avant modifications
3. **Documentation:** Mettre à jour les README et API docs
4. **Communication:** Informer l'équipe des changements d'API
5. **Performance:** Monitorer les délais des Cloud Functions
6. **Scalability:** Prévoir pour des milliers de transferts/jour

---

**Document version:** 1.0
**Préparé par:** AI Code Review Agent
**Date:** 12 janvier 2026

Ce fichier devient très long. Laissez-moi continuer avec les phases restantes.<tool_call>todo_write<arg_key>todos</arg_key><arg_value>[{"activeForm": "Création du plan de correction complet", "content": "Créer le plan de correction complet", "status": "completed"}, {"activeForm": "Définition des étapes backend", "content": "Définir les étapes backend (Cloud Functions)", "status": "completed"}, {"activeForm": "Définition des étapes frontend", "content": "Définir les étapes frontend (Kotlin)", "status": "in_progress"}, {"activeForm": "Inclusion des tests et validations", "content": "Inclure les tests et validations", "status": "pending"}]