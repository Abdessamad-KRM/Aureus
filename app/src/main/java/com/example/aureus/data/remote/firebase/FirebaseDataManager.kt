package com.example.aureus.data.remote.firebase

import android.net.Uri
import com.example.aureus.util.TimeoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper pour exécuter les opérations Firestore sur IO dispatcher avec timeouts
 * Phase 2: Wrap Firestore operations with Dispatchers.IO
 * Phase 3: Add timeouts to prevent indefinite blocking
 */
private suspend fun <T> onFirestore(
    timeoutMs: Long = TimeoutManager.FIREBASE_READ_TIMEOUT,
    block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val result = TimeoutManager.withReadTimeout(timeoutMs) {
            block()
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Helper pour les opérations d'écriture Firestore avec timeout
 */
private suspend fun <T> onFirestoreWrite(
    timeoutMs: Long = TimeoutManager.FIREBASE_WRITE_TIMEOUT,
    block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val result = TimeoutManager.withWriteTimeout(timeoutMs) {
            block()
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Firebase DataManager - Primary data manager for the Aureus Banking App
 * Gère TOUTES les opérations Firestore en temps réel
 * 
 * NOTE: StaticData.kt has been Completely removed (Phase 7 - Migration 100% Dynamique)
 * All data is now managed through Firebase (Firestore + Authentication + Storage)
 */
@Singleton
class FirebaseDataManager @Inject constructor(
    private val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // ==================== COLLECTIONS ====================
    private val usersCollection: CollectionReference get() = firestore.collection("users")
    private val cardsCollection: CollectionReference get() = firestore.collection("cards")
    private val transactionsCollection: CollectionReference get() = firestore.collection("transactions")
    private val accountsCollection: CollectionReference get() = firestore.collection("accounts")

    fun currentUserId(): String? = auth.currentUser?.uid

    // ==================== AUTH ====================

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signOut() {
        auth.signOut()
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Créer un nouveau utilisateur après Firebase Auth signup
     */
    suspend fun createUser(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        pin: String
    ): Result<Unit> = onFirestore {
        // ✅ PHASE 1 CORRECTION: Ne PAS stocker PIN ici
        // Utiliser PinFirestoreManager pour stocker PIN hashé avec salt
        val userData = mapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "preferredLanguage" to "fr",
            "notificationEnabled" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "isEmailVerified" to false,
            "isPhoneVerified" to false,
            "country" to "Morocco",
            "pinHashed" to true, // ✅ Indicateur que PIN sera hashé
            "pinConfigured" to false // ✅ PIN sera configuré après registration
        )

        usersCollection.document(userId).set(userData).await()

        // Créer un compte par défaut pour l'utilisateur
        createDefaultAccount(userId)

        Unit
    }

    /**
     * Créer un compte par défaut pour l'utilisateur
     */
    private suspend fun createDefaultAccount(userId: String) {
        onFirestore {
            val accountId = "acc_${Date().time}"
            val accountData = mapOf(
                "accountId" to accountId,
                "userId" to userId,
                "balance" to 0.0,
                "currency" to "MAD",
                "accountType" to "Current",
                "isDefault" to true,
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            accountsCollection.document(accountId).set(accountData).await()
        }
    }

    /**
     * Obtenir l'utilisateur courant en temps réel
     */
    fun getUser(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.data)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Obtenir un utilisateur de manière synchrone avec timeout (Phase 3)
     */
    suspend fun getUserSync(userId: String): Result<Map<String, Any>> = try {
        val data = withContext(Dispatchers.IO) {
            TimeoutManager.withReadTimeout {
                usersCollection.document(userId).get().await().data
            }
        }
        Result.success(data ?: throw Exception("User not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Mettre à jour le profil utilisateur
     */
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = onFirestore {
        usersCollection.document(userId).update(
            updates + ("updatedAt" to FieldValue.serverTimestamp())
        ).await()
        Unit
    }

    // ==================== CARDS OPERATIONS ====================

    /**
     * Obtenir toutes les cartes d'un utilisateur en temps réel
     */
    fun getUserCards(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = cardsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .orderBy("isDefault", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cards = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(cards)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Ajouter une nouvelle carte avec timeout (Phase 3 + Sécurité)
     * ✅ SÉCURITÉ: CVV n'est PAS stocké, cardNumber est tokenisé
     */
    suspend fun addCard(
        userId: String,
        accountId: String,
        cardNumber: String,  // Tokenisé/masqué (ex: "**** **** **** 1234")
        cardHolder: String,
        expiryDate: String,
        cvv: String,  // ❌ NE PAS STOCKER (utilisé uniquement pour validation côté client)
        cardType: String,
        cardColor: String,
        isDefault: Boolean = false
    ): Result<String> = onFirestoreWrite {
        val cardId = "card_${Date().time}"

        // Si c'est la nouvelle carte par défaut, désactiver l'ancienne
        if (isDefault) {
            cardsCollection.whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    doc.reference.update("isDefault", false).await()
                }
        }

        // ✅ SÉCURITÉ: Ne pas stocker CVV, utiliser cardNumber déjà tokenisé
        val cardData = mapOf(
            "cardId" to cardId,
            "userId" to userId,
            "accountId" to accountId,
            "cardNumber" to cardNumber,  // Déjà tokenisé (ex: "**** **** **** 1234")
            "cardHolder" to cardHolder,
            "expiryDate" to expiryDate,
            // ❌ CVV non stocké conformément aux normes PCI-DSS
            "cardType" to cardType,
            "cardColor" to cardColor,
            "isDefault" to isDefault,
            "isActive" to true,
            "status" to "ACTIVE",
            "balance" to 0.0,
            "dailyLimit" to 10000.0,
            "monthlyLimit" to 50000.0,
            "spendingToday" to 0.0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        cardsCollection.document(cardId).set(cardData).await()
        cardId
    }

    /**
     * Créer des cartes de test pour l'utilisateur
     */
    suspend fun createDefaultCards(userId: String): Result<Unit> {
        return try {
            // Obtenir le compte par défaut
            val accounts = accountsCollection.whereEqualTo("userId", userId).get().await()
            if (accounts.isEmpty) return Result.failure(Exception("No account found"))

            val accountId = accounts.documents[0].id

            // ✅ Créer les cartes en parallèle avec async/await
            val cardId1 = "card_${Date().time}"
            val cardId2 = "card_${Date().time + 1}"

            coroutineScope {
                // Lancer les deux créations en parallèle
                val createCard1 = async {
                    val card1 = mapOf(
                        "cardId" to cardId1,
                        "userId" to userId,
                        "accountId" to accountId,
                        "cardNumber" to "4242",
                        "cardHolder" to "Test User",
                        "expiryDate" to "12/28",
                        "cvv" to "***",
                        "cardType" to "VISA",
                        "cardColor" to "navy",
                        "isDefault" to true,
                        "isActive" to true,
                        "dailyLimit" to 10000.0,
                        "monthlyLimit" to 50000.0,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    cardsCollection.document(cardId1).set(card1).await()
                }

                val createCard2 = async {
                    val card2 = mapOf(
                        "cardId" to cardId2,
                        "userId" to userId,
                        "accountId" to accountId,
                        "cardNumber" to "5555",
                        "cardHolder" to "Test User",
                        "expiryDate" to "06/29",
                        "cvv" to "***",
                        "cardType" to "MASTERCARD",
                        "cardColor" to "gold",
                        "isDefault" to false,
                        "isActive" to true,
                        "dailyLimit" to 15000.0,
                        "monthlyLimit" to 75000.0,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    cardsCollection.document(cardId2).set(card2).await()
                }

                // Attendre que les deux se terminent
                awaitAll(createCard1, createCard2)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== TRANSACTIONS OPERATIONS ====================

    /**
     * Obtenir les transactions d'un utilisateur en temps réel
     * C'est CRUCIAL pour les charts dynamiques !
     */
    fun getUserTransactions(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Obtenir les récentes transactions (pour HomeScreen)
     */
    fun getRecentTransactions(userId: String, limit: Int = 10): Flow<List<Map<String, Any>>> =
        getUserTransactions(userId, limit)

    /**
     * Créer une nouvelle transaction avec timeout (Phase 3)
     */
    suspend fun createTransaction(transactionData: Map<String, Any>): Result<String> = onFirestoreWrite {
        val transactionId = "trx_${Date().time}"

        val finalData = transactionData + mapOf(
            "transactionId" to transactionId,
            "status" to "COMPLETED",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        transactionsCollection.document(transactionId).set(finalData).await()

        // Mise à jour du solde du compte
        val accountId = transactionData["accountId"] as String
        val amount = transactionData["amount"] as Double
        val type = transactionData["type"] as String
        val balanceChange = if (type == "INCOME") amount else -amount
        accountsCollection
            .document(accountId)
            .update(
                mapOf("balance" to FieldValue.increment(balanceChange), "updatedAt" to FieldValue.serverTimestamp())
            )
            .await()

        transactionId
    }

    /**
     * Créer des transactions de test pour l'utilisateur
     */
    suspend fun createDefaultTransactions(userId: String): Result<Unit> {
        return try {
            // Obtenir le compte par défaut
            val accounts = accountsCollection.whereEqualTo("userId", userId).get().await()
            if (accounts.isEmpty) return Result.failure(Exception("No account found"))

            val accountId = accounts.documents[0].id

            val transactionCategories = listOf(
                "Shopping", "Food & Drink", "Transport", "Entertainment", "Health",
                "Bills", "Salary", "Transfer", "Cash Withdrawal", "Groceries"
            )

            val titles = listOf(
                "Netflix Subscription", "Uber Ride", "Starbucks Coffee", "Gym Membership",
                "Monthly Salary", "Transfer to Friend", "Grocery Shopping", "Restaurant Bill",
                "Electricity Bill", "Online Purchase"
            )

            val merchants = listOf(
                "Netflix", "Uber", "Starbucks", "Fitness First",
                "Employer", "Friend Account", "Carrefour", "Le Pain Quotidien",
                "ONEE", "Amazon"
            )

            // ✅ Créer les 10 transactions en parallèle avec async
            val now = System.currentTimeMillis()

            coroutineScope {
                val createTransactionTasks = (0 until 10).map { i ->
                    async {
                        val timeOffset = i * 86400000L // 1 day apart
                        val type = if (i == 4) "INCOME" else "EXPENSE"
                        val amount = when (type) {
                            "INCOME" -> 15000.0
                            else -> (50..800).random().toDouble()
                        }

                        val transactionData = mapOf(
                            "transactionId" to "trx_${now + i}",
                            "userId" to userId,
                            "accountId" to accountId,
                            "cardId" to null,
                            "type" to type,
                            "category" to transactionCategories[i],
                            "title" to titles[i],
                            "description" to "Transaction de test",
                            "amount" to amount,
                            "merchant" to merchants[i],
                            "recipientName" to null,
                            "recipientAccount" to null,
                            "status" to "COMPLETED",
                            "balanceAfter" to 0.0,
                            "createdAt" to Date(now - (10 - i) * timeOffset),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        transactionsCollection.document("trx_${now + i}").set(transactionData).await()
                    }
                }

                // Mettre à jour le solde du compte après toutes les transactions
                awaitAll(*createTransactionTasks.toTypedArray())

                val totalIncome = 15000.0
                val totalExpense = (50..800).random().toDouble() * 9
                val finalBalance = totalIncome - totalExpense
                accountsCollection.document(accountId)
                    .update(mapOf("balance" to finalBalance, "updatedAt" to FieldValue.serverTimestamp()))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtenir les transactions par catégorie (pour charts)
     */
    fun getTransactionsByCategory(userId: String, startDate: Date, endDate: Date): Flow<Map<String, Double>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .whereLessThanOrEqualTo("createdAt", endDate)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val categoryTotals = mutableMapOf<String, Double>()
                snapshot?.documents?.forEach { doc ->
                    val category = doc.getString("category") ?: "OTHER"
                    val amount = doc.getDouble("amount") ?: 0.0
                    categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                }

                trySend(categoryTotals)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Obtenir les statistiques mensuelles (pour line charts)
     */
    fun getMonthlyStatistics(userId: String, months: Int = 6): Flow<List<Map<String, Any>>> = callbackFlow {
        val startDate = Date(System.currentTimeMillis() - (months * 30L * 24 * 60 * 60 * 1000))

        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Grouper par mois
                val monthlyStats = mutableMapOf<String, MutableMap<String, Double>>()
                snapshot?.documents?.forEach { doc ->
                    val date = doc.getDate("createdAt")
                    val month = SimpleDateFormat("MMM", Locale.FRENCH).format(date)

                    if (!monthlyStats.containsKey(month)) {
                        monthlyStats[month] = mutableMapOf("income" to 0.0, "expense" to 0.0)
                    }

                    val type = doc.getString("type")
                    val amount = doc.getDouble("amount") ?: 0.0

                    if (type == "INCOME") {
                        monthlyStats[month]!!["income"] = monthlyStats[month]!!["income"]!! + amount
                    } else if (type == "EXPENSE") {
                        monthlyStats[month]!!["expense"] = monthlyStats[month]!!["expense"]!! + amount
                    }
                }

                val result = monthlyStats.map { (month, stats) ->
                    mapOf(
                        "month" to month,
                        "income" to (stats["income"] ?: 0.0),
                        "expense" to (stats["expense"] ?: 0.0)
                    )
                }

                trySend(result)
            }
        awaitClose { listener.remove() }
    }

    // ==================== ACCOUNTS OPERATIONS ====================

    /**
     * Obtenir le solde total d'un utilisateur (balance card)
     */
    fun getUserTotalBalance(userId: String): Flow<Double> = callbackFlow {
        val listener = accountsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val totalBalance = snapshot?.documents?.sumOf { doc ->
                    doc.getDouble("balance") ?: 0.0
                } ?: 0.0

                trySend(totalBalance)
            }
        awaitClose { listener.remove() }
    }

    // ==================== VALIDATION & SECURITY (PHASE 8) ====================

    /**
     * ✅ PHASE 8: Méthode utilitaire pour obtenir le solde actuel
     * Récupère le solde du compte actif d'un utilisateur
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
     * Effectue plusieurs validations: montant positif, maximum autorisé, solde suffisant
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

    // ==================== CONTACTS OPERATIONS ====================

    /**
     * Obtenir les contacts d'un utilisateur
     */
    fun getUserContacts(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = usersCollection.document(userId)
            .collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val contacts = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(contacts)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Ajouter un contact
     */
    suspend fun addContact(userId: String, contactData: Map<String, Any>): Result<String> = onFirestore {
        val contactId = contactData["id"] as? String ?: "contact_${Date().time}"
        usersCollection.document(userId)
            .collection("contacts")
            .document(contactId)
            .set(contactData)
            .await()
        contactId
    }

    /**
     * Mettre à jour un contact
     */
    suspend fun updateContact(userId: String, contactId: String, updates: Map<String, Any>): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .collection("contacts")
            .document(contactId)
            .update(updates + ("updatedAt" to FieldValue.serverTimestamp()))
            .await()
        Unit
    }

    /**
     * Supprimer un contact
     */
    suspend fun deleteContact(userId: String, contactId: String): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .collection("contacts")
            .document(contactId)
            .delete()
            .await()
        Unit
    }

    // ==================== TRANSACTIONS CRUD OPERATIONS ====================

    /**
     * Obtenir une transaction par ID
     */
    suspend fun getTransactionById(transactionId: String): Result<Map<String, Any>> = onFirestore {
        val snapshot = transactionsCollection.document(transactionId).get().await()
        snapshot.data ?: throw Exception("Transaction not found")
    }

    /**
     * Mettre à jour une transaction
     */
    suspend fun updateTransaction(transactionId: String, updates: Map<String, Any>): Result<Unit> = onFirestore {
        transactionsCollection.document(transactionId)
            .update(updates + ("updatedAt" to FieldValue.serverTimestamp()))
            .await()
        Unit
    }

    /**
     * Supprimer une transaction
     */
    suspend fun deleteTransaction(transactionId: String): Result<Unit> = onFirestore {
        transactionsCollection.document(transactionId)
            .delete()
            .await()
        Unit
    }

    /**
     * Obtenir les transactions par catégorie dans une période
     */
    fun getTransactionsByCategoryList(userId: String, category: String, startDate: Date, endDate: Date): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", category)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .whereLessThanOrEqualTo("createdAt", endDate)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    // ==================== CARDS CRUD OPERATIONS ====================

    /**
     * Obtenir une carte par ID
     */
    suspend fun getCardById(cardId: String): Result<Map<String, Any>> = onFirestore {
        val snapshot = cardsCollection.document(cardId).get().await()
        snapshot.data ?: throw Exception("Card not found")
    }

    /**
     * Mettre à jour une carte
     */
    suspend fun updateCard(cardId: String, updates: Map<String, Any>): Result<Unit> = onFirestore {
        cardsCollection.document(cardId)
            .update(updates + ("updatedAt" to FieldValue.serverTimestamp()))
            .await()
        Unit
    }

    /**
     * Supprimer une carte
     */
    suspend fun deleteCard(cardId: String): Result<Unit> = onFirestore {
        cardsCollection.document(cardId)
            .delete()
            .await()
        Unit
    }

    /**
     * Définir une carte comme par défaut
     */
    suspend fun setDefaultCard(userId: String, cardId: String): Result<Unit> = onFirestore {
        // Désactiver toutes les cartes par défaut
        cardsCollection.whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .await()
            .documents
            .forEach { doc ->
                doc.reference.update("isDefault", false).await()
            }

        // Activer la nouvelle carte par défaut
        cardsCollection.document(cardId)
            .update("isDefault", true, "updatedAt" to FieldValue.serverTimestamp())
            .await()

        Unit
    }

    /**
     * Obtenir les transactions pour une carte spécifique en temps réel
     * Utilisé pour CardDetailScreen
     */
    fun getTransactionsByCard(cardId: String, userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("cardId", cardId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    // ==================== SAVINGS GOALS OPERATIONS ====================

    /**
     * Obtenir les objectifs d'épargne d'un utilisateur
     */
    fun getSavingsGoals(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = usersCollection.document(userId)
            .collection("savingsGoals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val goals = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(goals)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Créer un objectif d'épargne
     */
    suspend fun createSavingsGoal(userId: String, goalData: Map<String, Any>): Result<String> = onFirestore {
        val goalId = goalData["id"] as? String ?: "goal_${Date().time}"
        val finalGoalData = goalData.toMutableMap()
        finalGoalData["createdAt"] = FieldValue.serverTimestamp()
        finalGoalData["updatedAt"] = FieldValue.serverTimestamp()
        usersCollection.document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .set(finalGoalData)
            .await()
        goalId
    }

    /**
     * Mettre à jour un objectif d'épargne
     */
    suspend fun updateSavingsGoal(userId: String, goalId: String, updates: Map<String, Any>): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .update(updates + ("updatedAt" to FieldValue.serverTimestamp()))
            .await()
        Unit
    }

    /**
     * Supprimer un objectif d'épargne
     */
    suspend fun deleteSavingsGoal(userId: String, goalId: String): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .delete()
            .await()
        Unit
    }

    // ==================== STATISTICS (REAL-TIME) ====================

    /**
     * Obtenir TOUTES les stats pour StatisticsScreen en temps réel
     */
    fun getUserStatistics(userId: String): Flow<Map<String, Any>> = callbackFlow {
        val startDate = Date()
        startDate.month = 0 // 1er janvier this year

        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                var totalIncome = 0.0
                var totalExpense = 0.0
                val categoryStats = mutableMapOf<String, Double>()
                val monthlyStats = mutableMapOf<Int, Pair<Double, Double>>() // month -> (income, expense)

                snapshot?.documents?.forEach { doc ->
                    val type = doc.getString("type")
                    val category = doc.getString("category") ?: "OTHER"
                    val amount = doc.getDouble("amount") ?: 0.0

                    if (type == "INCOME") {
                        totalIncome += amount
                    } else if (type == "EXPENSE") {
                        totalExpense += amount
                        categoryStats[category] = categoryStats.getOrDefault(category, 0.0) + amount
                    }

                    // Stats mensuelles
                    val date = doc.getDate("createdAt")
                    if (date != null) {
                        val month = date.month
                        val current = monthlyStats.getOrDefault(month, Pair(0.0, 0.0))
                        val newIncome = if (type == "INCOME") current.first + amount else current.first
                        val newExpense = if (type == "EXPENSE") current.second + amount else current.second
                        monthlyStats[month] = Pair(newIncome, newExpense)
                    }
                }

                val spendingPercentage = if (totalIncome > 0) {
                    (totalExpense / totalIncome) * 100
                } else 0.0

                val stats = mapOf(
                    "totalBalance" to (totalIncome - totalExpense),
                    "totalIncome" to totalIncome,
                    "totalExpense" to totalExpense,
                    "spendingPercentage" to spendingPercentage,
                    "categoryStats" to categoryStats,
                    "monthlyStats" to monthlyStats
                )

                trySend(stats)
            }
        awaitClose { listener.remove() }
    }

    // ==================== STORAGE (IMAGES) ====================

    /**
     * Upload une image de profil
     */
    suspend fun uploadProfileImage(userId: String, imageUri: String): Result<String> = onFirestore {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        val uploadTask = storageRef.putFile(Uri.parse(imageUri)).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        downloadUrl.toString()
    }

    // ==================== FCM TOKENS & NOTIFICATIONS ====================

    /**
     * Enregistrer un FCM token pour un utilisateur
     */
    suspend fun registerFcmToken(userId: String, token: String, deviceInfo: Map<String, Any>? = null): Result<Unit> = onFirestore {
        val tokenData = mutableMapOf(
            "token" to token,
            "createdAt" to FieldValue.serverTimestamp()
        )
        deviceInfo?.let { tokenData.putAll(it) }

        usersCollection.document(userId)
            .collection("fcmTokens")
            .document(token)
            .set(tokenData)
            .await()
        Unit
    }

    /**
     * Obtenir tous les FCM tokens d'un utilisateur
     */
    suspend fun getUserFcmTokens(userId: String): Result<List<String>> = onFirestore {
        val snapshot = usersCollection.document(userId)
            .collection("fcmTokens")
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.getString("token")
        }
    }

    /**
     * Mettre à jour les préférences de notification
     */
    suspend fun updateNotificationPreferences(
        userId: String,
        notificationsEnabled: Boolean
    ): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .update(
                mapOf(
                    "notificationEnabled" to notificationsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
        Unit
    }

    /**
     * Supprimer un FCM token
     */
    suspend fun removeFcmToken(userId: String, token: String): Result<Unit> = onFirestore {
        usersCollection.document(userId)
            .collection("fcmTokens")
            .document(token)
            .delete()
            .await()
        Unit
    }
}

/**
 * ✅ PHASE 8: Data class pour les résultats de validation
 * Utilisé pour valider les montants de transfert et autres opérations
 */
data class ValidationResult(
    val invalid: Boolean,
    val message: String,
    val balance: Double? = null
)