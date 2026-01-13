package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.BankCard
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardStatus
import com.example.aureus.domain.model.CardType
import com.example.aureus.domain.repository.CardRepository
import com.example.aureus.security.EncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CardRepositoryImpl - Implementation using Firebase
 * Part of Phase 3: Cards Migration + Security encryption
 */
@Singleton
class CardRepositoryImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val encryptionService: EncryptionService  // ✅ SÉCURITÉ
) : CardRepository {

    override fun getCards(userId: String): Flow<List<BankCard>> {
        return firebaseDataManager.getUserCards(userId).map { cardMaps ->
            cardMaps.mapNotNull { mapToBankCard(it) }
        }
    }

    override suspend fun getCardById(cardId: String): Result<BankCard?> {
        return try {
            val result = firebaseDataManager.getCardById(cardId)
            if (result.isSuccess) {
                val cardData = result.getOrNull()
                val card = cardData?.let { mapToBankCard(it) }
                Result.success(card)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get card"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDefaultCard(userId: String): Result<BankCard?> {
        return try {
            // Get all cards and filter for default
            // ✅ .first() prend le premier émission, pas d'attente infinie
            val cards = firebaseDataManager.getUserCards(userId).first()
            val defaultCard = cards.find { it["isDefault"] == true }
            Result.success(defaultCard?.let { mapToBankCard(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCard(
        userId: String,
        accountId: String,
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String,
        cardType: CardType,
        cardColor: String,
        isDefault: Boolean
    ): Result<String> {
        return try {
            // ✅ SÉCURITÉ: Tokeniser le numéro de carte (ne stocker que les 4 derniers chiffres)
            val tokenizedCardNumber = encryptionService.tokenizeCardNumber(cardNumber)

            // ⚠️ ATTENTION: NE JAMAIS STOCKER LE CVV, même chiffré
            // Le CVV est utilisé uniquement pour validation et doit être effacé après

            // Données de carte sécurisées CVV vide)
            val secureCardData = mapOf(
                "cardId" to "card_${System.currentTimeMillis()}",
                "userId" to userId,
                "accountId" to accountId,
                "cardNumber" to tokenizedCardNumber,  // ✅ Tokenisé/sécurisé
                "cardHolder" to cardHolder,
                "expiryDate" to expiryDate,
                "cardType" to cardType.name,
                "cardColor" to cardColor,
                "isDefault" to isDefault,
                "isActive" to true,
                "status" to CardStatus.ACTIVE.name,
                "balance" to 0.0,
                "dailyLimit" to 10000.0,
                "monthlyLimit" to 50000.0,
                "spendingToday" to 0.0,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            // Sauvegarder via FirebaseDataManager avec CVV vide
            val result = firebaseDataManager.addCard(
                userId = userId,
                accountId = accountId,
                cardNumber = tokenizedCardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cvv = "",  // ✅ SÉCURITÉ: CVV vide - pas stocké
                cardType = cardType.name,
                cardColor = cardColor,
                isDefault = isDefault
            )

            if (result.isSuccess) {
                Result.success("card_${System.currentTimeMillis()}")
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to add card"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCard(cardId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val result = firebaseDataManager.updateCard(cardId, updates)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to update card"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCard(cardId: String): Result<Unit> {
        return try {
            val result = firebaseDataManager.deleteCard(cardId)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to delete card"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDefaultCard(userId: String, cardId: String): Result<Unit> {
        return try {
            val result = firebaseDataManager.setDefaultCard(userId, cardId)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to set default card"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleCardFreeze(cardId: String, isFrozen: Boolean): Result<Unit> {
        val status = if (isFrozen) CardStatus.FROZEN else CardStatus.ACTIVE
        return updateCard(cardId, mapOf(
            "isActive" to !isFrozen,
            "status" to status.name
        ))
    }

    override suspend fun updateCardLimits(
        cardId: String,
        dailyLimit: Double,
        monthlyLimit: Double
    ): Result<Unit> {
        return updateCard(cardId, mapOf(
            "dailyLimit" to dailyLimit,
            "monthlyLimit" to monthlyLimit
        ))
    }

    override suspend fun createTestCards(userId: String): Result<Unit> {
        return firebaseDataManager.createDefaultCards(userId)
    }

    /**
     * Convert Firestore Map to BankCard domain model
     */
    private fun mapToBankCard(map: Map<String, Any>): BankCard? {
        return try {
            BankCard(
                id = map["cardId"] as? String ?: return null,
                cardNumber = "**** **** **** ${(map["cardNumber"] as? String) ?: "0000"}",
                cardHolder = map["cardHolder"] as? String ?: return null,
                expiryDate = map["expiryDate"] as? String ?: return null,
                balance = (map["balance"] as? Number)?.toDouble() ?: 0.0,
                cardType = try {
                    CardType.valueOf((map["cardType"] as? String) ?: "VISA")
                } catch (e: Exception) {
                    CardType.VISA
                },
                cardColor = try {
                    CardColor.valueOf((map["cardColor"] as? String ?: "navy").uppercase())
                } catch (e: Exception) {
                    CardColor.NAVY
                },
                isDefault = map["isDefault"] as? Boolean ?: false,
                accountId = map["accountId"] as? String ?: "",
                isActive = map["isActive"] as? Boolean ?: true,
                status = try {
                    CardStatus.valueOf((map["status"] as? String) ?: "ACTIVE")
                } catch (e: Exception) {
                    CardStatus.ACTIVE
                },
                dailyLimit = (map["dailyLimit"] as? Number)?.toDouble() ?: 10000.0,
                monthlyLimit = (map["monthlyLimit"] as? Number)?.toDouble() ?: 50000.0,
                spendingToday = (map["spendingToday"] as? Number)?.toDouble() ?: 0.0,
                createdAt = map["createdAt"]?.toString() ?: "",
                updatedAt = map["updatedAt"]?.toString() ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}