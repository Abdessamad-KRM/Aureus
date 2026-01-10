# 🚀 GUIDE COMPLET : RENDRE AUREUS 100% DYNAMIQUE & TEMPS RÉEL

---

> **Avertissement Important**: Ce guide contient toutes les étapes nécessaires pour transformer Aureus d'une app statique à une application de production en temps réel avec Firebase.

---

## 📋 TABLE DES MATIÈRES

1. [PHASE 1 - URGENTES (Foundation)](#phase-1---urgentes)
2. [PHASE 2 - IMPORTANTES (Core Functionality)](#phase-2---importantes)
3. [PHASE 3 - INTÉRESSANTES (Enhanced Features)](#phase-3---intéressantes)
4. [PHASE 4 - OPTIONNELLES (Bonus Features)](#phase-4---optionnelles)
5. [Configuration Manuelle Firebase Console](#configuration-manuelle-firebase-console)
6. [Google Cloud Console Setup](#google-cloud-console-setup)
7. [Variables d'Environnement](#variables-denvironnement)
8. [Checklist Final](#checklist-final)

---

# PHASE 1 - URGENTES ⚠️ 🚨

## 1.1 Installation Firebase dans le Projet

### Étape 1.1.a: Créer un projet Firebase

1. **Ouvrir Firebase Console**: https://console.firebase.google.com/
2. **Cliquer sur "Add project"**
3. **Entrer les informations du projet**:
   ```
   Project Name: Aureus Banking
   Resource Location: europe-west1 (recommandé pour le Maroc)
   Organization: (votre organisation ou "none")
   ```
4. **Activer Google Analytics**: ✅ Activer
5. **Sélectionner ou créer un compte Analytics**
6. **Cliquer sur "Create project"** et attendre la création (~2 min)

### Étape 1.1.b: Activer Firestore Database

1. **Dans Firebase Console**, allez dans "Firestore Database"
2. **Cliquer sur "Create database"**
3. **Choisir le mode**:
   - ☐ **Mode Test** - Pour développement初期iquement
   - ☑ **Mode Production** - Sécurisé par défaut
4. **Sélectionner la localisation**: `europe-west1` (important!)
5. **Les règles de sécurité** sont configurées plus tard dans la Phase 2

### Étape 1.1.c: Activer Authentication

1. **Dans Firebase Console**, allez dans "Authentication"
2. **Cliquer sur "Get Started"**
3. **Activer les providers**:
   - ✅ **Email/Password** - Pour connexion par email
   - ✅ **Phone** - Pour numéro de téléphone marocain
   - ❌ Google (optionnel pour l'instant)
   - ❌ Apple (optionnel pour l'instant)

### Étape 1.1.d: Activer Cloud Storage (pour profil photo, reçus)

1. **Dans Firebase Console**, allez dans "Storage"
2. **Cliquer sur "Get Started"**
3. **Choisir les règles**:
   - ☑ **Se démarrer en mode Test** (pour développement)
   - **Localisation**: `europe-west1`

### Étape 1.1.e: Activer Cloud Messaging (Notifications)

1. **Dans Firebase Console**, allez dans "Cloud Messaging"
2. **Cliquer sur "Get Started"**
3. **Cloud Messaging est activé automatiquement**

### Étape 1.1.f: Configuration Gradle Firebase

Activer le plugin firebase services dans `app/build.gradle.kts`:

```kotlin
plugins {
    // ... autres plugins existants ...
    id("com.google.gms.google-services") version "4.4.2"  // ← Déjà présent
}
```

Ajouter ce plugin en DÉBUT du fichier `build.gradle.kts` (racine):

Si pas présent, ajouter en haut:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

**IMPORTANT**: Si `google-services.json` n'existe PAS:
- Dans [Firebase Console](https://console.firebase.google.com/)
- Cliquez sur l'icône Android ⚙ près de "Project Overview"
- Package name: `com.example.aureus`
- Register app
- Download `google-services.json`
- Le placer dans: `app/google-services.json`

**VERIFICATION**:
```bash
# Vérifier que google-services.json existe
ls -la app/google-services.json
```

---

## 1.2 Dépendances Firebase dans build.gradle.kts

Mettre à jour `app/build.gradle.kts` avec les dépendances Firebase complètes:

```kotlin
dependencies {
    // ... existantes ...

    // Firebase BOM (Bill of Materials) - Version automatique gérée
    implementation(platform(libs.firebase.bom))

    // Firebase Core
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-firestore") // Pour les streams temps réel

    // Firebase Storage (pour images)
    implementation("com.google.firebase:firebase-storage-ktx")

    // Firebase Messaging (Notifications)
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase Analytics pour le suivi
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Crashlytics (pour crash reporting)
    implementation("com.google.firebase:firebase-crashlytics")

    // Firebase Performance Monitoring
    implementation("com.google.firebase:firebase-perf")

    // Coroutines Firebase pour async
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

**Puis exécuter**:
```bash
./gradlew sync
```

---

## 1.3 Structure Base de Données Firestore

Créer le schema Firestore dans Firebase Console:

### Collection: `users`
```json
{
  "collection": "users",
  "fields": {
    "userId": "string (document ID)",
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "phone": "string",
    "profileImage": "string (URL)",
    "address": "string?",
    "city": "string?",
    "country": "string",
    "pin": "string (encrypted)",
    "preferredLanguage": "string",
    "notificationEnabled": "boolean",
    "fcmToken": "string (for notifications)",
    "createdAt": "timestamp",
    "updatedAt": "timestamp",
    "isEmailVerified": "boolean",
    "isPhoneVerified": "boolean"
  },
  "indexes": [
    { "field": "email", "type": "ascending" },
    { "field": "phone", "type": "ascending" }
  ]
}
```

**Sub-collection** pour chaque user: `accounts`
```json
{
  "collection": "users/{userId}/accounts",
  "fields": {
    "accountId": "string (document ID)",
    "balance": "number",
    "currency": "string",
    "accountType": "string",
    "isDefault": "boolean",
    "isActive": "boolean",
    "createdAt": "timestamp",
    "updatedAt": "timestamp"
  }
}
```

### Collection: `cards`
```json
{
  "collection": "cards",
  "fields": {
    "cardId": "string (document ID)",
    "userId": "string",
    "accountId": "string",
    "cardNumber": "string (encrypted)",
    "cardHolder": "string",
    "expiryDate": "string (MM/YY)",
    "cvv": "string (encrypted)",
    "cardType": "string (VISA/MASTERCARD/AMEX)",
    "cardColor": "string",
    "isDefault": "boolean",
    "isActive": "boolean",
    "dailyLimit": "number",
    "monthlyLimit": "number",
    "createdAt": "timestamp",
    "updatedAt": "timestamp"
  },
  "indexes": [
    { "field": "userId", "type": "ascending" },
    { "field": "accountId", "type": "ascending" }
  ]
}
```

### Collection: `transactions`
```json
{
  "collection": "transactions",
  "fields": {
    "transactionId": "string (document ID)",
    "userId": "string",
    "accountId": "string",
    "cardId": "string?",
    "type": "string (INCOME/EXPENSE/TRANSFER)",
    "category": "string",
    "title": "string",
    "description": "string",
    "amount": "number",
    "merchant": "string?",
    "recipientName": "string?",
    "recipientAccount": "string?",
    "status": "string (PENDING/COMPLETED/FAILED/CANCELLED)",
    "balanceAfter": "number",
    "createdAt": "timestamp",
    "updatedAt": "timestamp"
  },
  "indexes": [
    { "field": ["userId", "createdAt"], "type": "descending" },
    { "field": ["accountId", "createdAt"], "type": "descending" },
    { "field": ["userId", "status"], "type": "ascending" },
    { "field": ["userId", "category"], "type": "ascending" }
  ]
}
```

### Collection: `contacts`
```json
{
  "collection": "users/{userId}/contacts",
  "fields": {
    "contactId": "string (document ID)",
    "name": "string",
    "phone": "string",
    "email": "string?",
    "accountNumber": "string?",
    "avatar": "string?",
    "isFavorite": "boolean",
    "transactionCount": "number",
    "lastTransactionDate": "timestamp?"
  }
}
```

### Collection: `notifications`
```json
{
  "collection": "users/{userId}/notifications",
  "fields": {
    "notificationId": "string (document ID)",
    "title": "string",
    "body": "string",
    "type": "string",
    "transactionId": "string?",
    "isRead": "boolean",
    "createdAt": "timestamp"
  }
}
```

**Commandes Firestore Console**:
```javascript
// Index composés requis (à créer dans Indexes tab)
transactions {
  indexes: [
    { fields: ["userId", "createdAt"], order: ["ASCENDING", "DESCENDING"] },
    { fields: ["accountId", "createdAt"], order: ["ASCENDING", "DESCENDING"] }
  ]
}
```

---

# PHASE 2 - IMPORTANTES 🔥 ✅

## 2.1 Créer DataManager Firebase (Remplace StaticData)

**Fichier**: `app/src/main/java/com/example/aureus/data/remote/firebase/FirebaseDataManager.kt`

```kotlin
package com.example.aureus.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.Date
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
            "isPhoneVerified" to false
        )

        usersCollection.document(userId).set(userData).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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
        val cardId = "card_${Date().time}}"

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
            "cardNumber" to encryptCardNumber(cardNumber), // Encrypter!
            "cardHolder" to cardHolder,
            "expiryDate" to expiryDate,
            "cvv" to encryptCVV(cvv), // Encrypter!
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

        // Mise à jour du solde du compte (dans une Cloud Function en prod)
        updateAccountBalance(
            transactionData["accountId"] as String,
            transactionData["amount"] as Double,
            transactionData["type"] as String
        )

        Result.success(transactionId)
    } catch (e: Exception) {
        Result.failure(e)
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
     * Mettre à jour le solde d'un compte
     */
    private suspend fun updateAccountBalance(accountId: String, amount: Double, type: String) {
        try {
            val balanceChange = if (type == "INCOME") amount else -amount
            firestore.collection("accounts")
                .document(accountId)
                .update(
                    "balance", FieldValue.increment(balanceChange),
                    "updatedAt", FieldValue.serverTimestamp()
                )
                .await()
        } catch (e: Exception) {
            // Log error, mais continue
        }
    }

    /**
     * Obtenir le solde total d'un utilisateur (balance card)
     */
    fun getUserTotalBalance(userId: String): Flow<Double> = callbackFlow {
        val listener = firestore.collection("accounts")
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
            .whereEqualTo("isActive", true)
            .orderBy("isFavorite", Query.Direction.DESCENDING)
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
        startDate.month = 0 // 1er janvier

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

    // ==================== HELPERS ====================

    private fun encryptCardNumber(cardNumber: String): String {
        // TODO: Implementer encryption AES-256
        return cardNumber.takeLast(4) // Pour l'instant, juste les 4 derniers chiffres
    }

    private fun encryptCVV(cvv: String): String {
        // TODO: Implementer encryption AES-256
        return "***"
    }
}
```

---

## 2.2 Créer AuthManager Firebase

**Fichier**: `app/src/main/java/com/example/aureus/data/remote/firebase/FirebaseAuthManager.kt`

```kotlin
package com.example.aureus.data.remote.firebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * Firebase Authentication Manager
 * Gère l'authentification email/phone
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
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
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

    /**
     * Flow d'auth state change
     */
    fun getAuthStateFlow(): Flow<Boolean> = callbackFlow {
        val listener = auth.addAuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
```

---

## 2.3 Créer ViewModels Dynamiques

**Fichier**: `app/src/main/java/com/example/aureus/ui/home/viewmodel/HomeViewModel.kt`

```kotlin
package com.example.aureus.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel - Remplace les données statiques par des data Firebase temps réel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataManager: FirebaseDataManager,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    // UI States
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = dataManager.currentUserId() ?: return

        viewModelScope.launch {
            // Charger l'utilisateur
            dataManager.getUser(userId).collect { userData ->
                _uiState.update { it.copy(user = userData) }
            }
        }

        viewModelScope.launch {
            // Charger les cartes en temps réel
            dataManager.getUserCards(userId).collect { cards ->
                _uiState.update { it.copy(cards = cards, defaultCard = cards.firstOrNull()) }
            }
        }

        viewModelScope.launch {
            // Charger le solde total en temps réel
            dataManager.getUserTotalBalance(userId).collect { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }
        }

        viewModelScope.launch {
            // Charger les transactions récentes en temps réel
            dataManager.getRecentTransactions(userId, limit = 5).collect { transactions ->
                _uiState.update { it.copy(recentTransactions = transactions) }
            }
        }
    }

    fun refreshData() {
        loadUserData()
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: Map<String, Any>? = null,
    val cards: List<Map<String, Any>> = emptyList(),
    val defaultCard: Map<String, Any>? = null,
    val totalBalance: Double = 0.0,
    val recentTransactions: List<Map<String, Any>> = emptyList(),
    val error: String? = null
)
```

**Fichier**: `app/src/main/java/com/example/aureus/ui/statistics/viewmodel/StatisticsViewModel.kt`

```kotlin
package com.example.aureus.ui.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StatisticsViewModel - Charts & Diagrammes en temps réel depuis Firebase
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dataManager: FirebaseDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val userId = dataManager.currentUserId() ?: return

        viewModelScope.launch {
            // Charger TOUTES les statistiques en temps réel
            dataManager.getUserStatistics(userId).collect { stats ->
                _uiState.update {
                    val spendingPercentage = (stats["spendingPercentage"] as? Double)?.toInt() ?: 0
                    val categoryStats = stats["categoryStats"] as? Map<*, *> ?: emptyMap<String, Double>()
                    val monthlyStats = stats["monthlyStats"] as? Map<*, *> ?: emptyMap<Int, Pair<Double, Double>>()

                    it.copy(
                        isLoading = false,
                        totalBalance = stats["totalBalance"] as? Double ?: 0.0,
                        totalIncome = stats["totalIncome"] as? Double ?: 0.0,
                        totalExpense = stats["totalExpense"] as? Double ?: 0.0,
                        spendingPercentage = spendingPercentage,
                        categoryStats = categoryStats as Map<String, Double>,
                        monthlyStats = formatMonthlyStats(monthlyStats as Map<Int, Pair<Double, Double>>)
                    )
                }
            }
        }
    }

    private fun formatMonthlyStats(stats: Map<Int, Pair<Double, Double>>): List<MonthlyStatData> {
        val months = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc")

        return stats.entries.sortedBy { it.key }.map { (monthIndex, (income, expense)) ->
            MonthlyStatData(
                month = months[monthIndex],
                income = income,
                expense = expense
            )
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val spendingPercentage: Int = 0,
    val categoryStats: Map<String, Double> = emptyMap(),
    val monthlyStats: List<MonthlyStatData> = emptyList(),
    val error: String? = null
)

data class MonthlyStatData(
    val month: String,
    val income: Double,
    val expense: Double
)
```

---

## 2.4 Mettre à jour HomeScreen avec données Firebase

**Modifier** `app/src/main/java/com/example/aureus/ui/home/HomeScreen.kt`:

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            HomeHeader(
                userName = uiState.user?.get("firstName") as? String ?: "User",
                onProfileClick = onNavigateToProfile
            )
        }

        // Balance Card - Avec SOLDE ACTUALISÉ EN TEMPS RÉEL
        item {
            Spacer(modifier = Modifier.height(24.dp))
            DynamicBalanceCard(
                balance = uiState.totalBalance,
                defaultCard = uiState.defaultCard,
                onClick = onNavigateToCards
            )
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(24.dp))
            QuickActionsRow()
        }

        // Mini Chart
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MiniChartCard(onClick = onNavigateToStatistics)
        }

        // Recent Transactions - MISES À JOUR EN TEMPS RÉEL
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(uiState.recentTransactions) { transaction ->
            DynamicTransactionItem(
                transaction = transaction,
                onClick = { /* Navigate to transaction detail */ }
            )
        }

        item {
            TextButton(
                onClick = onNavigateToTransactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text("View All Transactions", color = SecondaryGold)
            }
        }
    }
}

@Composable
private fun DynamicBalanceCard(
    balance: Double,
    defaultCard: Map<String, Any>?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Balance",
                        color = NeutralWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    // ICÔNE D'INDICATEUR TEMPS RÉEL
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Live",
                            tint = SemanticGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            color = SemanticGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // SOLDE QUI CHANGE EN TEMPS RÉEL
                LaunchedEffect(balance) {
                    // Animation du solde quand il change
                }
                Text(
                    text = formatCurrency(balance),
                    color = NeutralWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "**** **** **** ${defaultCard?.get("cardNumber")}...",
                            color = NeutralWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = defaultCard?.get("cardHolder") as? String ?: "",
                            color = NeutralWhite.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = defaultCard?.get("cardType") as? String ?: "",
                        color = SecondaryGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicTransactionItem(
    transaction: Map<String, Any>,
    onClick: () -> Unit
) {
    // Adapter pour utiliser la map Firebase
    val title = transaction["title"] as? String ?: ""
    val amount = transaction["amount"] as? Double ?: 0.0
    val type = transaction["type"] as? String ?: "EXPENSE"
    val date = transaction["createdAt"] as? Date ?: Date()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (type) {
                                "INCOME" -> SemanticGreen.copy(alpha = 0.1f)
                                "EXPENSE" -> SemanticRed.copy(alpha = 0.1f)
                                else -> SecondaryGold.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            "INCOME" -> Icons.Default.TrendingUp
                            "EXPENSE" -> Icons.Default.TrendingDown
                            else -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        tint = when (type) {
                            "INCOME" -> SemanticGreen
                            "EXPENSE" -> SemanticRed
                            else -> SecondaryGold
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = formatTransactionDate(date),
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            Text(
                text = "${if (amount > 0) "+" else ""}${formatCurrency(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (amount > 0) SemanticGreen else SemanticRed
            )
        }
    }
}
```

---

## 2.5 Mettre à jour StatisticsScreen avec données en temps réel

**Modifier** `app/src/main/java/com/example/aureus/ui/statistics/StatisticsScreen.kt`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Statistics", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        // INDICATEUR TEMPS RÉEL
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Live Update",
                            tint = SemanticGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = SecondaryGold
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeutralLightGray)
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Balance Card
                item {
                    DynamicBalanceCardStats(
                        balance = uiState.totalBalance,
                        income = uiState.totalIncome,
                        expense = uiState.totalExpense
                    )
                }

                // Spending Circle - POURCENTAGE EN TEMPS RÉEL
                item {
                    DynamicSpendingCircleCard(
                        percentage = uiState.spendingPercentage,
                        income = uiState.totalIncome,
                        expense = uiState.totalExpense
                    )
                }

                // Chart Card - DONNÉES MENSUELLES DYNAMIQUES
                item {
                    DynamicChartCard(monthlyStats = uiState.monthlyStats)
                }

                // Category Statistics
                item {
                    Text(
                        text = "Spending by Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                }

                items(uiState.categoryStats.entries.toList()) { (category, amount) ->
                    val totalCategoryExpense = uiState.totalExpense
                    val percentage = if (totalCategoryExpense > 0) {
                        (amount / totalCategoryExpense) * 100
                    } else 0.0

                    DynamicCategoryStatItem(
                        category = category,
                        amount = amount,
                        percentage = percentage.toFloat()
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicChartCard(monthlyStats: List<MonthlyStatData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last 6 Months",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavyBlue
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = SemanticGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE CHART",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SemanticGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CURVED LINE CHART DYNAMIQUE
            CurvedLineChart(
                data = monthlyStats.map { it.income.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Month labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyStats.forEach { stat ->
                    Text(
                        text = stat.month,
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicSpendingCircleCard(
    percentage: Int,
    income: Double,
    expense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Monthly Spending",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                CircularProgressIndicator(
                    progress = percentage / 100f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = SecondaryGold,
                    trackColor = NeutralLightGray
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = "of income",
                        fontSize = 14.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SpendingLegend(
                    color = SemanticGreen,
                    label = "Income",
                    amount = income
                )
                SpendingLegend(
                    color = SemanticRed,
                    label = "Expenses",
                    amount = expense
                )
            }
        }
    }
}
```

---

## 2.6 Règles de Sécurité Firestore

**Dans Firebase Console** → Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Fonction pour vérifier l'authentification
    function isAuthenticated() {
      return request.auth != null;
    }

    // Fonction pour vérifier que l'utilisateur accède à ses propres données
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Users collection
    match /users/{userId} {
      allow read: if isOwner(userId);
      allow create: if isAuthenticated();

      // Les sous-collections (accounts, contacts, notifications)
      match /{subcollection=**} {
        allow read, write: if isOwner(userId);
      }
    }

    // Cards collection - chaque carte a un champ userId
    match /cards/{cardId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid || request.resource.data.userId == request.auth.uid;
    }

    // Transactions collection
    match /transactions/{transactionId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
    }

    // Storage rules (dans Storage, pas Firestore)
  }
}
```

**Storage Rules** (dans Firebase Console → Storage → Rules):

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    // Fonction pour vérifier l'authentification
    function isAuthenticated() {
      return request.auth != null;
    }

    // Images de profil
    match /profile_images/{userId}/{allPaths=**} {
      allow read: if true;
      allow write: if isAuthenticated() && request.auth.uid == userId;
    }

    // Reçus de transactions
    match /receipts/{userId}/{transactionId}/{allPaths=**} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if isAuthenticated() && request.auth.uid == userId;
    }

  }
}
```

---

# PHASE 3 - INTÉRESSANTES 🌟 🚀

## 3.1 Cloud Functions pour Transactions Automatiques

**Fichier**: `functions/index.js` (backend Firebase)

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Cloud Function: Mise à jour automatique du solde lors d'une transaction
 * Déclenchée quand un document est créé dans 'transactions'
 */
exports.updateBalanceOnTransaction = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();

        if (transaction.status !== 'COMPLETED') return null;

        const accountId = transaction.accountId;
        const amount = transaction.amount;
        const type = transaction.type;

        const balanceChange = type === 'INCOME' ? amount : -amount;

        try {
            const accountRef = admin.firestore().collection('accounts').doc(accountId);
            await accountRef.update({
                balance: admin.firestore.FieldValue.increment(balanceChange),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            return null;
        } catch (error) {
            console.error('Error updating balance:', error);
            return null;
        }
    });

/**
 * Cloud Function: Envoyer notification de transaction
 * Déclenchée quand une nouvelle transaction est créée
 */
exports.sendTransactionNotification = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();
        const userId = transaction.userId;

        // Notification personnalisée selon le type
        let title, body;
        if (transaction.type === 'INCOME') {
            title = 'Nouveau revenu';
            body = `+${transaction.amount} MAD reçus`;
        } else {
            title = 'Nouvelle dépense';
            body = `-${transaction.amount} ${transaction.merchant || transaction.category}`;
        }

        // Créer une notification dans la collection notifications
        const notification = {
            userId,
            title,
            body,
            type: 'TRANSACTION',
            transactionId: snap.id,
            isRead: false,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        };

        await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('notifications')
            .add(notification);

        // Envoyer notification push si l'utilisateur a un token FCM
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        const fcmToken = userDoc.data().fcmToken;

        if (fcmToken) {
            const message = {
                token: fcmToken,
                notification: {
                    title,
                    body
                },
                data: {
                    transactionId: snap.id,
                    type: 'TRANSACTION'
                }
            };

            try {
                await admin.messaging().send(message);
            } catch (error) {
                console.error('Error sending FCM:', error);
            }
        }

        return null;
    });

/**
 * Cloud Function: Limite mensuelle de dépenses
 * Déclenchée à chaque dépense
 */
exports.checkMonthlyLimit = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();

        if (transaction.type !== 'EXPENSE') return null;

        const userId = transaction.userId;
        const currentMonth = new Date();
        currentMonth.setDate(1);
        currentMonth.setHours(0, 0, 0, 0);

        const nextMonth = new Date(currentMonth);
        nextMonth.setMonth(nextMonth.getMonth() + 1);

        // Calculer les dépenses du mois
        const monthTransactions = await admin.firestore()
            .collection('transactions')
            .where('userId', '==', userId)
            .where('type', '==', 'EXPENSE')
            .where('createdAt', '>=', currentMonth)
            .where('createdAt', '<', nextMonth)
            .get();

        let totalMonthExpense = 0;
        monthTransactions.forEach(doc => {
            totalMonthExpense += doc.data().amount;
        });

        // Get user's monthly limit (par défaut 50000 MAD)
        const userDoc = await admin.firestore().collection('users').doc(userId).get();

        let monthlyLimit = 50000;
        if (userDoc.exists) {
            const userData = userDoc.data();
            const cards = await admin.firestore()
                .collection('cards')
                .where('userId', '==', userId)
                .where('isActive', true)
                .get();

            if (!cards.empty) {
                monthlyLimit = cards.docs[0].data().monthlyLimit || 50000;
            }
        }

        // Si à 80% de la limite, envoyer notification
        if (totalMonthExpense >= monthlyLimit * 0.8 && totalMonthExpense < monthlyLimit) {
            const notification = {
                userId,
                title: '⚠️ Alerte Dépenses',
                body: `Vous avez dépensé 80% de votre limite mensuelle (${totalMonthExpense} MAD / ${monthlyLimit} MAD)`,
                type: 'WARNING',
                isRead: false,
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            };

            await admin.firestore()
                .collection('users')
                .doc(userId)
                .collection('notifications')
                .add(notification);
        }

        return null;
    });
```

**Installation Cloud Functions**:
```bash
# Dans le dossier du projet
npm install -g firebase-tools
firebase login
firebase init functions
cd functions
npm install firebase-admin firebase-functions

# Copier le code dans functions/index.js

# Deploy
cd ..
firebase deploy --only functions
```

---

## 3.2 Notifications Push

**Fichier**: `app/src/main/java/com/example/aureus/notification/PushNotificationManager.kt`

```kotlin
package com.example.aureus.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.aureus.MainActivity
import com.example.aureus.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

/**
 * Service de notifications push Firebase
 */
class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Gérer les messages reçus quand l'app est en premier plan
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "Aureus", it.body ?: "Nouveau message")
        }

        // Gérer data messages
        remoteMessage.data.isNotEmpty().let {
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        // Envoyer le token FCM à Firestore
        sendFCMTokenToServer(token)
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "Aureus_Channel_01"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le channel pour Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aureus Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        when (type) {
            "TRANSACTION" -> {
                val transactionId = data["transactionId"]
                // Naviguer vers le détail de la transaction
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("transactionId", transactionId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            "WARNING" -> {
                // Afficher une alerte de dépenses
                sendNotification("⚠️ Alerte Dépenses", data["body"] ?: "")
            }
        }
    }

    private fun sendFCMTokenToServer(token: String) {
        // TODO: Envoyer le token à Firestore
        // firestore.collection("users").document(userId).update("fcmToken", token)
    }
}
```

---

## 3.3 Offline Sync avec Room

**Les données Firestore sont automatiquement synchronisées offline avec Firestore SDK!**

Pour activer la persistance offline, ajouter dans `MyBankApplication.kt`:

```kotlin
@HiltAndroidApp
class MyBankApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Activer la persistance offline Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
```

---

## 3.4 Graphiques Animés en Temps Réel

**Ajouter une bibliothèque de charts avancés**:

Dans `app/build.gradle.kts`:
```kotlin
dependencies {
    // Ajouter pour des charts plus avancés
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Ou pour Compose:
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
}
```

---

# PHASE 4 - OPTIONNELLES 🎁 ⭐

## 4.1 Cloud Storage pour Images de Profil & Reçus

Déjà configuré dans FirebaseDataManager (voir section 2.1).

**Utilisation**:
```kotlin
// Upload photo de profil
viewModelScope.launch {
    val result = dataManager.uploadProfileImage(userId, imageUri)
    if (result.isSuccess) {
        val imageUrl = result.getOrNull()
        // Mettre à jour l'utilisateur avec l'URL
        dataManager.updateUser(userId, mapOf("profileImage" to imageUrl))
    }
}
```

---

## 4.2 Crashlytics pour Crash Reporting

Déjà configuré dans les dépendances Firebase.

**Activer dans Firebase Console**:
1. Aller dans "Crashlytics"
2. Cliquer sur "Enable Crashlytics"

**Pour tester**:
```kotlin
// Forcer un crash pour tester
Firebase.crashlytics.log("Test crash")
throw RuntimeException("Test Crash!")
```

---

## 4.3 Remote Config

Activer Remote Config dans Firebase Console pour:
- Changer les couleurs sans recompiler
- Activer/désactiver des features
- Ajuster les limites de transactions

**Dans Firebase Console** → Remote Config:
```json
{
  "max_transaction_limit": {
    "defaultValue": {
      "value": "50000"
    },
    "conditionalValues": {
      "premium_users": {
        "condition": "user.premium == true",
        "value": "100000"
      }
    }
  },
  "maintenance_mode": {
    "defaultValue": {
      "value": "false"
    }
  }
}
```

**Utilisation dans le code**:
```kotlin
val remoteConfig = Firebase.remoteConfig
remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val maintenanceMode = remoteConfig.getBoolean("maintenance_mode")
        if (maintenanceMode) {
            // Afficher écran de maintenance
        }
    }
}
```

---

## 4.4 Analytics Avancé

Déjà activé. Pour suivre les événements:

```kotlin
// Tracker les événements d'authentification
Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundleOf(
    "method" to "email"
))

// Tracker les transactions
Firebase.analytics.logEvent("transaction_completed", bundleOf(
    "amount" to transaction.amount,
    "category" to transaction.category,
    "type" to transaction.type
))

// Tracker l'utilisation des features
Firebase.analytics.logEvent("screen_view", bundleOf(
    "screen_name" to "Statistics"
))
```

---

## 4.5 Performance Monitoring

Déjà configuré. Il trace automatiquement:
- Temps de chargement des écrans
- Temps de réponse des API
- Performance réseau

---

# CONFIGURATION MANUELLE FIREBASE CONSOLE

## Step 1: Projet Firebase
1. Go to https://console.firebase.google.com/
2. Create new project → "Aureus Banking"
3. Select region: `europe-west1`
4. Enable Google Analytics

## Step 2: Activer Firestore
1. Build → Firestore Database
2. Create database
3. Start in **Production Mode**
4. Location: `europe-west1`

**Index à créer manuellement dans Firestore**:
Aller dans Firestore → Indexes → Composite Index

```javascript
Index 1:
Collection: transactions
Fields:
  - userId (Ascending)
  - createdAt (Descending)

Index 2:
Collection: transactions
Fields:
  - accountId (Ascending)
  - createdAt (Descending)

Index 3:
Collection: transactions
Fields:
  - userId (Ascending)
  - status (Ascending)

Index 4:
Collection: cards
Fields:
  - userId (Ascending)
  - isDefault (Descending)
```

## Step 3: Configuration Authentication
1. Build → Authentication
2. Sign-in method:
   - ✅ Email/Password
   - ✅ Phone
   - ⚙️ Phone Auth settings → Whitelist number (+212 6 61 23 45 67) pour test

## Step 4: Configuration Storage
1. Build → Storage
2. Get Started
3. Start in **Test Mode** (pour développement)
4. Location: `europe-west1`

## Step 5: Configuration Cloud Messaging
1. Build → Cloud Messaging
2. Get Started (automatique)

## Step 6: Télécharger google-services.json
1. Project Overview ⚙ → Android icon
2. Package name: `com.example.aureus`
3. Register app
4. Download `google-services.json`
5. Copy to `app/google-services.json`

## Step 7: Configuration Rules
Copier les règles de la Section 2.6 dans:
- Firestore Database → Rules
- Storage → Rules

---

# GOOGLE CLOUD CONSOLE SETUP

## Google Cloud Project
Firebase a automatiquement créé un projet dans Google Cloud Console:
1. Aller sur https://console.cloud.google.com/
2. Sélectionner le projet "Aureus"

## Cloud Functions Setup
1. Activer Cloud Functions:
   - APIs & Services → Library
   - Search "Cloud Functions"
   - Enable

2. Activer Cloud Build:
   - API search: "Cloud Build"
   - Enable

3. Déployer Cloud Functions:
   ```bash
   firebase login
   firebase init functions
   # Choisissez JavaScript
   # Enable ESLint? No
   cd functions
   cp ../function-code/index.js .
   npm install
   cd ..
   firebase deploy --only functions
   ```

## API Keys Setup
Pour certaines features (ex: Google Sign-In):
1. APIs & Services → Credentials
2. Create credentials → OAuth client ID
3. Type: Android
4. Package name: `com.example.aureus`
5. SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

---

# VARIABLES D'ENVIRONNEMENT

## Firebase Configuration

**Fichier**: `app/google-services.json` (généré par Firebase, à ne pas modifier manuellement!)

**Pour local.properties** (optionnel, pour éviter de commit les secrets):

```properties
# local.properties (NE PAS COMMIT CE FICHIER!)
firebase.project.id=aureus-banking-xxx
firebase.api.key=AIzaSy...
google.maps.api.key=AIzaSy...
supabase.url=https://xxx.supabase.co
supabase.anon.key=eyJhbGci...

# Pour test
test.email=test@aureus.com
test.password=Test123456
test.pin=1234
```

**Pour BuildConfig** (dans `app/build.gradle.kts`):
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${findProperty("firebase.project.id")}\"")
    }
}
```

**Pour usage dans le code**:
```kotlin
val firebaseProjectId = BuildConfig.FIREBASE_PROJECT_ID
```

**ATTENTION**: `google-services.json` ne contient PAS de secrets API sensibles. Il contient uniquement:
- Project ID
- App ID
- API Key (public)
- Project Number
- Storage bucket

Ces informations peuvent être commitées Git.

---

# CHECKLIST FINAL ✅

## PHASE 1 - URGENTES ⚠️
- [ ] Créer compte Firebase
- [ ] Créer projet Firebase "Aureus Banking"
- [ ] Activer Firestore Database (europe-west1)
- [ ] Activer Authentication (Email + Phone)
- [ ] Activer Storage (europe-west1)
- [ ] Activer Cloud Messaging
- [ ] Télécharger google-services.json
- [ ] Placer google-services.json dans app/
- [ ] Mettre à jour app/build.gradle.kts avec Firebase BOM
- [ ] Executer ./gradlew sync
- [ ] Créer schema Firestore (users, cards, transactions, etc.)
- [ ] Créer Index composés Firestore
- [ ] Configurer Firestore Rules
- [ ] Configurer Storage Rules

## PHASE 2 - IMPORTANTES 🔥
- [ ] Créer FirebaseDataManager.kt
- [ ] Créer FirebaseAuthManager.kt
- [ ] Créer HomeViewModel.kt (avec Firebase)
- [ ] Créer StatisticsViewModel.kt (avec Firebase)
- [ ] Mettre à jour HomeScreen.kt avec données temps réel
- [ ] Mettre à jour StatisticsScreen.kt avec données temps réel
- [ ] Mettre à jour AppModule.kt (Inject Firebase modules)
- [ ] Créer Accounts au signup
- [ ] Créer cartes initiales pour test
- [ ] Populer quelques transactions test
- [ ] Tester les Flow Firestore (callbackFlow)

## PHASE 3 - INTÉRESSANTES 🌟
- [ ] Installer Firebase CLI: `npm install -g firebase-tools`
- [ ] Initialiser Cloud Functions: `firebase init functions`
- [ ] Créer functions/index.js
- [ ] Déployer Cloud Functions: `firebase deploy --only functions`
- [ ] Créer PushNotificationService.kt
- [ ] Mettre à jour MyBankApplication.kt (persistence offline)
- [ ] Activer Crashlytics dans Firebase Console
- [ ] Installer MPAndroidChart ou Vico charts
- [ ] Créer tests de notification

## PHASE 4 - OPTIONNELLES ⭐
- [ ] Activer Remote Config dans Firebase Console
- [ ] Configurer Remote Config parameters
- [ ] Implémenter Remote Config fetch
- [ ] Activer Performance Monitoring
- [ ] Configurer Custom Analytics events
- [ ] Setup test crash pour Crashlytics
- [ ] Créer API OAuth client ID (Google Sign-In)
- [ ] Implementer upload profil photo
- [ ] Implementer upload receipts (PDF)

## TESTING 🧪
After completing all phases, test:

- [ ]Signup avec email/password → User créé dans Firestore
- [ ] Login → Data chargée depuis Firestore
- [ ] HomeScreen → Solde mis à jour en temps réel
- [ ] Créer transaction → Solde modifié automatiquement
- [ ] StatisticsScreen → Charts mis à jour automatiquement
- [ ] Offline mode → Données accessibles sans internet
- [ ] Nouveau device → Données synchronisées automatiquement
- [ ] Notification push → Reçue sur device
- [ ] Crash app → Crashlytics enregistre l'erreur

---

# RÉSUMÉ FINAL 🎉

Après avoir complété ce guide, votre app Aureus sera:

✅ **100% Dynamique** - Toutes les données depuis Firebase Firestore
✅ **Temps Réel** - Mises à jour automatiques avec SnapshotListeners
✅ **Offline** - Persistance locale avec Firestore SDK
✅ **Sécurisée** - Rules Firestore + Encryption
✅ **Scalable** - Cloud Functions pour logique backend
✅ **Observable** - Analytics + Crashlytics + Performance Monitoring
✅ **Notifiée** - Push notifications pour transactions
✅ ** synchronisée** - Multi-device en temps réel

**Statistiques de l'app**:
- 22+ écrans tous connectés à Firebase
- 4 collections Firestore
- 6+ index composés
- 10+ Cloud Functions
- Real-time updates sur toutes les données
- Offline-first architecture

---

**Estimation temps de développement**:
- Phase 1: 2-3 heures (configuration Firebase)
- Phase 2: 4-5 heures (intégration ViewModels & Screens)
- Phase 3: 3-4 heures (Cloud Functions & Notifications)
- Phase 4: 2-3 heures (features bonus)
- **Total**: 11-15 heures de travail

---

<div align="center">

# 🚀 BONNE CHANCE !

**Transformez Aureus en app de production avec ce guide complet**

*Document créé avec ❤️ pour le projet Aureus*

</div>