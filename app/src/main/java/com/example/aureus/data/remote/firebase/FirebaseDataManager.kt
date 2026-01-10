package com.example.aureus.data.remote.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase DataManager - Remplace tous les StaticData
 * Gère TOUTES les opérations Firestore en temps réel
 */
@Singleton
class FirebaseDataManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
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
    ): Result<Unit> = try {
        val userData = mapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "pin" to pin, // TODO: Encrypter avec AES-256
            "preferredLanguage" to "fr",
            "notificationEnabled" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "isEmailVerified" to false,
            "isPhoneVerified" to false,
            "country" to "Morocco"
        )

        usersCollection.document(userId).set(userData).await()

        // Créer un compte par défaut pour l'utilisateur
        createDefaultAccount(userId)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Créer un compte par défaut pour l'utilisateur
     */
    private suspend fun createDefaultAccount(userId: String) = try {
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
    } catch (e: Exception) {
        // Log error mais continue
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
     * Mettre à jour le profil utilisateur
     */
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersCollection.document(userId).update(
            updates + ("updatedAt" to FieldValue.serverTimestamp())
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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
     * Ajouter une nouvelle carte
     */
    suspend fun addCard(
        userId: String,
        accountId: String,
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String,
        cardType: String,
        cardColor: String,
        isDefault: Boolean = false
    ): Result<String> = try {
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

        val cardData = mapOf(
            "cardId" to cardId,
            "userId" to userId,
            "accountId" to accountId,
            "cardNumber" to cardNumber.takeLast(4), // Simplified - use last 4 digits
            "cardHolder" to cardHolder,
            "expiryDate" to expiryDate,
            "cvv" to "***", // Masked
            "cardType" to cardType,
            "cardColor" to cardColor,
            "isDefault" to isDefault,
            "isActive" to true,
            "dailyLimit" to 10000.0,
            "monthlyLimit" to 50000.0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        cardsCollection.document(cardId).set(cardData).await()
        Result.success(cardId)
    } catch (e: Exception) {
        Result.failure(e)
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

            // Créer une carte principale par défaut
            val cardId1 = "card_${Date().time}"
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

        // Créer une carte secondaire
        val cardId2 = "card_${Date().time + 1}"
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
     * Créer une nouvelle transaction
     */
    suspend fun createTransaction(transactionData: Map<String, Any>): Result<String> = try {
        val transactionId = "trx_${Date().time}"

        val finalData = transactionData + mapOf(
            "transactionId" to transactionId,
            "status" to "COMPLETED",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        transactionsCollection.document(transactionId).set(finalData).await()

        // Mise à jour du solde du compte
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

        Result.success(transactionId)
    } catch (e: Exception) {
        Result.failure(e)
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

            // Créer 10 transactions variées
            val now = System.currentTimeMillis()
            for (i in 0 until 10) {
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

            // Mettre à jour le solde du compte
            val totalIncome = 15000.0
            val totalExpense = (50..800).random().toDouble() * 9 // Approximation
            val finalBalance = totalIncome - totalExpense
            accountsCollection.document(accountId)
                .update(mapOf("balance" to finalBalance, "updatedAt" to FieldValue.serverTimestamp()))
                .await()

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
    suspend fun uploadProfileImage(userId: String, imageUri: String): Result<String> = try {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        val uploadTask = storageRef.putFile(Uri.parse(imageUri)).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}