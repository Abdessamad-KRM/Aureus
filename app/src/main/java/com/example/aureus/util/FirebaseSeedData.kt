package com.example.aureus.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Seed Data Script
 * Peuple Firebase avec des données de démo pour les tests
 * À exécuter une seule fois au premier lancement de l'application
 */
@Singleton
class FirebaseSeedData @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Crée les données de démo pour tous les utilisateurs
     */
    suspend fun seedDemoData(userId: String): Result<Unit> {
        return try {
            // Vérifier si les données existent déjà
            val existingCards = firestore.collection("cards")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents

            if (existingCards.isNotEmpty()) {
                // Les données existent déjà, ne pas recréer
                return Result.success(Unit)
            }

            // Créer les cartes de démo
            createDemoCards(userId)

            // Créer les transactions de démo
            createDemoTransactions(userId)

            // Créer les contacts de démo
            createDemoContacts(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crée les cartes bancaires de démo
     */
    private suspend fun createDemoCards(userId: String) {
        val demoCards = listOf(
            mapOf(
                "cardId" to "card_001",
                "userId" to userId,
                "cardNumber" to "4562 1122 4945 9852",
                "cardHolder" to "Demo User",
                "expiryDate" to "12/28",
                "cvv" to "***",
                "balance" to 85545.0,
                "currency" to "MAD",
                "cardType" to "VISA",
                "cardColor" to "NAVY",
                "isDefault" to true,
                "isActive" to true,
                "status" to "ACTIVE",
                "dailyLimit" to 10000.0,
                "monthlyLimit" to 50000.0,
                "spendingToday" to 0.0,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "cardId" to "card_002",
                "userId" to userId,
                "cardNumber" to "4562 1122 4945 7823",
                "cardHolder" to "Demo User",
                "expiryDate" to "09/29",
                "cvv" to "***",
                "balance" to 42180.5,
                "currency" to "MAD",
                "cardType" to "MASTERCARD",
                "cardColor" to "GOLD",
                "isDefault" to false,
                "isActive" to true,
                "status" to "ACTIVE",
                "dailyLimit" to 10000.0,
                "monthlyLimit" to 50000.0,
                "spendingToday" to 0.0,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "cardId" to "card_003",
                "userId" to userId,
                "cardNumber" to "4562 1122 4945 3621",
                "cardHolder" to "Demo User",
                "expiryDate" to "05/30",
                "cvv" to "***",
                "balance" to 18900.0,
                "currency" to "MAD",
                "cardType" to "VISA",
                "cardColor" to "BLACK",
                "isDefault" to false,
                "isActive" to true,
                "status" to "ACTIVE",
                "dailyLimit" to 10000.0,
                "monthlyLimit" to 50000.0,
                "spendingToday" to 0.0,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
        )

        demoCards.forEach { cardData ->
            firestore.collection("cards").document(cardData["cardId"] as String).set(cardData).await()
        }
    }

    /**
     * Crée les transactions de démo
     */
    private suspend fun createDemoTransactions(userId: String) {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour

        val demoTransactions = listOf(
            mapOf(
                "transactionId" to "trx_001",
                "userId" to userId,
                "title" to "Marjane",
                "description" to "Courses alimentaires mensuelles",
                "amount" to -2850.0,
                "type" to "EXPENSE",
                "category" to "FOOD",
                "date" to (now - oneDay),
                "cardId" to "card_001",
                "recipientName" to "Marjane Californie",
                "status" to "COMPLETED",
                "createdAt" to (now - oneDay),
                "updatedAt" to (now - oneDay)
            ),
            mapOf(
                "transactionId" to "trx_002",
                "userId" to userId,
                "title" to "Meditel",
                "description" to "Recharge téléphone mobile",
                "amount" to -200.0,
                "type" to "EXPENSE",
                "category" to "BILLS",
                "date" to (now - 2 * oneDay),
                "cardId" to "card_001",
                "recipientName" to "Meditel",
                "status" to "COMPLETED",
                "createdAt" to (now - 2 * oneDay),
                "updatedAt" to (now - 2 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_003",
                "userId" to userId,
                "title" to "Salaire Mensuel",
                "description" to "OCP Group - Ingénieur",
                "amount" to 18500.0,
                "type" to "INCOME",
                "category" to "SALARY",
                "date" to (now - 5 * oneDay),
                "cardId" to null,
                "recipientName" to "OCP Group",
                "status" to "COMPLETED",
                "createdAt" to (now - 5 * oneDay),
                "updatedAt" to (now - 5 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_004",
                "userId" to userId,
                "title" to "Acima",
                "description" to "Achat électroménager",
                "amount" to -5400.0,
                "type" to "EXPENSE",
                "category" to "SHOPPING",
                "date" to (now - 6 * oneDay),
                "cardId" to "card_002",
                "recipientName" to "Acima Anfa",
                "status" to "COMPLETED",
                "createdAt" to (now - 6 * oneDay),
                "updatedAt" to (now - 6 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_005",
                "userId" to userId,
                "title" to "Careem",
                "description" to "Course vers bureau",
                "amount" to -45.0,
                "type" to "EXPENSE",
                "category" to "TRANSPORT",
                "date" to (now - 6 * oneDay),
                "cardId" to "card_001",
                "recipientName" to "Careem",
                "status" to "COMPLETED",
                "createdAt" to (now - 6 * oneDay),
                "updatedAt" to (now - 6 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_006",
                "userId" to userId,
                "title" to "Zara Maroc",
                "description" to "Achat vêtements",
                "amount" to -980.0,
                "type" to "EXPENSE",
                "category" to "SHOPPING",
                "date" to (now - 7 * oneDay),
                "cardId" to "card_002",
                "recipientName" to "Zara Morocco Mall",
                "status" to "COMPLETED",
                "createdAt" to (now - 7 * oneDay),
                "updatedAt" to (now - 7 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_007",
                "userId" to userId,
                "title" to "Café Maure",
                "description" to "Restaurant Ain Diab",
                "amount" to -320.0,
                "type" to "EXPENSE",
                "category" to "FOOD",
                "date" to (now - 7 * oneDay),
                "cardId" to "card_001",
                "recipientName" to "Café Maure Ain Diab",
                "status" to "COMPLETED",
                "createdAt" to (now - 7 * oneDay),
                "updatedAt" to (now - 7 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_008",
                "userId" to userId,
                "title" to "LYDEC",
                "description" to "Facture eau et électricité",
                "amount" to -890.0,
                "type" to "EXPENSE",
                "category" to "BILLS",
                "date" to (now - 8 * oneDay),
                "cardId" to "card_001",
                "recipientName" to "LYDEC Casablanca",
                "status" to "COMPLETED",
                "createdAt" to (now - 8 * oneDay),
                "updatedAt" to (now - 8 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_009",
                "userId" to userId,
                "title" to "Mission Freelance",
                "description" to "Développement app mobile",
                "amount" to 8500.0,
                "type" to "INCOME",
                "category" to "OTHER",
                "date" to (now - 9 * oneDay),
                "cardId" to null,
                "recipientName" to "Client Rabat",
                "status" to "COMPLETED",
                "createdAt" to (now - 9 * oneDay),
                "updatedAt" to (now - 9 * oneDay)
            ),
            mapOf(
                "transactionId" to "trx_010",
                "userId" to userId,
                "title" to "Jumia",
                "description" to "Achat en ligne - smartphone",
                "amount" to -3200.0,
                "type" to "EXPENSE",
                "category" to "SHOPPING",
                "date" to (now - 10 * oneDay),
                "cardId" to "card_002",
                "recipientName" to "Jumia Maroc",
                "status" to "COMPLETED",
                "createdAt" to (now - 10 * oneDay),
                "updatedAt" to (now - 10 * oneDay)
            )
        )

        demoTransactions.forEach { transactionData ->
            firestore.collection("transactions").document(transactionData["transactionId"] as String).set(transactionData).await()
        }
    }

    /**
     * Crée les contacts de démo dans la sous-collection contacts de l'utilisateur
     */
    private suspend fun createDemoContacts(userId: String) {
        val demoContacts = listOf(
            mapOf(
                "contactId" to "contact_001",
                "name" to "Mohammed EL ALAMI",
                "phone" to "+212 6 61 45 78 90",
                "email" to "m.elalami@gmail.com",
                "accountNumber" to "007 820 0012345678 19",
                "isFavorite" to true,
                "isBankContact" to true,
                "avatar" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "contactId" to "contact_002",
                "name" to "Fatima-Zahra BENANI",
                "phone" to "+212 6 62 33 44 55",
                "email" to "fz.benani@hotmail.com",
                "accountNumber" to "007 820 0087654321 25",
                "isFavorite" to true,
                "isBankContact" to true,
                "avatar" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "contactId" to "contact_003",
                "name" to "Ahmed IDRISSI",
                "phone" to "+212 6 70 12 34 56",
                "email" to "ahmed.idrissi@outlook.com",
                "accountNumber" to "007 820 0045678912 33",
                "isFavorite" to false,
                "isBankContact" to false,
                "avatar" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "contactId" to "contact_004",
                "name" to "Salma EL FASSI",
                "phone" to "+212 6 77 88 99 00",
                "email" to "salma.elfassi@gmail.com",
                "accountNumber" to "007 820 0098765432 41",
                "isFavorite" to true,
                "isBankContact" to false,
                "avatar" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            mapOf(
                "contactId" to "contact_005",
                "name" to "Omar TAZI",
                "phone" to "+212 6 68 55 44 33",
                "email" to "omar.tazi@yahoo.fr",
                "accountNumber" to "007 820 0011223344 58",
                "isFavorite" to false,
                "isBankContact" to false,
                "avatar" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
        )

        demoContacts.forEach { contactData ->
            firestore.collection("users/$userId/contacts").document(contactData["contactId"] as String).set(contactData).await()
        }
    }
}