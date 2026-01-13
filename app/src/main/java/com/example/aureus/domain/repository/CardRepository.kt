package com.example.aureus.domain.repository

import com.example.aureus.domain.model.BankCard
import com.example.aureus.domain.model.CardType
import kotlinx.coroutines.flow.Flow

/**
 * CardRepository - Interface for card operations
 * Part of Phase 3: Cards Migration
 */
interface CardRepository {

    /**
     * Obtenir toutes les cartes d'un utilisateur en temps réel
     */
    fun getCards(userId: String): Flow<List<BankCard>>

    /**
     * Obtenir une carte spécifique par ID
     */
    suspend fun getCardById(cardId: String): Result<BankCard?>

    /**
     * Obtenir la carte par défaut d'un utilisateur
     */
    suspend fun getDefaultCard(userId: String): Result<BankCard?>

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
        cardType: CardType,
        cardColor: String,
        isDefault: Boolean = false
    ): Result<String>

    /**
     * Mettre à jour une carte existante
     */
    suspend fun updateCard(cardId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Supprimer une carte
     */
    suspend fun deleteCard(cardId: String): Result<Unit>

    /**
     * Définir une carte comme par défaut
     * (désactive l'ancienne carte par défaut)
     */
    suspend fun setDefaultCard(userId: String, cardId: String): Result<Unit>

    /**
     * Geler/dégeler une carte
     */
    suspend fun toggleCardFreeze(cardId: String, isFrozen: Boolean): Result<Unit>

    /**
     * Mettre à jour les limites de dépenses
     */
    suspend fun updateCardLimits(
        cardId: String,
        dailyLimit: Double,
        monthlyLimit: Double
    ): Result<Unit>

    /**
     * Créer des cartes de test pour un utilisateur
     */
    suspend fun createTestCards(userId: String): Result<Unit>
}