# 🛠️ PLAN DE CORRECTION - STATISTIQUES TEMPS RÉEL

**Date**: 13 Janvier 2026
**Objectif**: Corriger tous les problèmes pour rendre les graphiques temps réel fonctionnels
**Difficulté**: Moyenne (6 corrections à effectuer)
**Temps estimé**: 30-45 minutes

---

## 📋 RÉCAPITULATIF DES PROBLÈMES

| ID | Problème | Fichier | Gravité | Priorité |
|----|----------|---------|---------|----------|
| #1 | `totalBalance` utilise `.first()` | StatisticsViewModel.kt | 🔴 CRITIQUE | #1 |
| #2 | `totalIncome` utilise `.first()` | StatisticsViewModel.kt | 🔴 CRITIQUE | #2 |
| #3 | `totalExpense` utilise `.first()` | StatisticsViewModel.kt | 🔴 CRITIQUE | #3 |
| #4 | Pas d'annulation de coroutines | StatisticsViewModel.kt | 🟠 MOYEN | #4 |
| #5 | Pas de refresh automatique après transaction | Multiple | 🟠 MOYEN | #5 |
| #6 | Coroutines recrées sans optimisation | StatisticsViewModel.kt | 🟡 FAIBLE | #6 |

---

## 🎯 PLAN DE CORRECTION - ÉTAPE PAR ÉTAPE

### ÉTAPE 1: Sauvegarde avant modifications

⚠️ IMPORTANT: Faire un commit git avant les modifications

```bash
git add .
git commit -m "Backup avant correction statistiques temps réel"
```

---

## 🔧 ÉTAPE 2: CORRECTION CRITIQUE #1 - TOTALBALANCE

### Fichier à modifier: `StatisticsViewModel.kt`
### Lignes concernées: 60-68

### ✅ CODE ACTUEL (Buggé):
```kotlin
private fun loadStatistics(userId: String) {
    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
        // Charger le solde total en priorité (blocking) - Critical for UI
        val totalBalanceAsync = async {
            statisticRepository.getTotalBalance(userId).first()
        }
        _uiState.value = _uiState.value.copy(
            totalBalance = totalBalanceAsync.await()
        )

        // Charger les autres stats en parallèle (non-blocking) - Performance optimization
        async {
            val calendar = Calendar.getInstance()
            val endTime = calendar.time
            calendar.add(Calendar.MONTH, -6)
            val startTime = calendar.time

            // Revenus et dépenses totaux (en parallèle)
            val incomeAsync = async { statisticRepository.getTotalIncome(userId, startTime, endTime).first() }
            val expenseAsync = async { statisticRepository.getTotalExpense(userId, startTime, endTime).first() }

            _uiState.value = _uiState.value.copy(
                totalIncome = incomeAsync.await(),
                totalExpense = expenseAsync.await()
            )

            // Pourcentage de dépenses
            launch {
                statisticRepository.getSpendingPercentage(userId, startTime, endTime).collect { percentage ->
                    _uiState.update { it.copy(spendingPercentage = percentage) }
                }
            }

            // Statistiques par catégorie
            launch {
                statisticRepository.getCategoryBreakdown(userId, startTime, endTime).collect { categories ->
                    _uiState.update { it.copy(categoryStats = categories.map { it.category to it.amount }) }
                }
            }

            // Statistiques mensuelles
            launch {
                statisticRepository.getMonthlyIncomeExpense(userId, 6).collect { monthlyStats ->
                    val monthlyStatsList = monthlyStats.map { stat ->
                        MonthlyStatData(
                            month = monthToName(stat.month),
                            income = stat.income,
                            expense = stat.expense,
                            year = stat.year,
                            monthIndex = stat.month
                        )
                    }
                    _uiState.update {
                        it.copy(
                            monthlyStats = monthlyStatsList,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }

            // Tendances de dépenses
            launch {
                statisticRepository.getSpendingTrends(userId, StatisticPeriod.MONTHLY).collect { trend ->
                    _uiState.update { it.copy(spendingTrend = trend) }
                }
            }

            // Insights
            launch {
                statisticRepository.getSpendingInsights(userId, StatisticPeriod.MONTHLY).collect { insights ->
                    _uiState.update { it.copy(insights = insights) }
                }
            }
        }
    }
}
```

### ✨ NOUVEAU CORRIGÉ:

```kotlin
private fun loadStatistics(userId: String) {
    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val endTime = calendar.time
        calendar.add(Calendar.MONTH, -6)
        val startTime = calendar.time

        // ✅ CORRECTION #1: totalBalance avec .collect() pour mise à jour automatique
        launch {
            statisticRepository.getTotalBalance(userId).collect { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }
        }

        // ✅ CORRECTION #2: totalIncome avec .collect() pour mise à jour automatique
        launch {
            statisticRepository.getTotalIncome(userId, startTime, endTime).collect { income ->
                _uiState.update { it.copy(totalIncome = income) }
            }
        }

        // ✅ CORRECTION #3: totalExpense avec .collect() pour mise à jour automatique
        launch {
            statisticRepository.getTotalExpense(userId, startTime, endTime).collect { expense ->
                _uiState.update { it.copy(totalExpense = expense) }
            }
        }

        // Pourcentage de dépenses (déjà correct avec .collect())
        launch {
            statisticRepository.getSpendingPercentage(userId, startTime, endTime).collect { percentage ->
                _uiState.update { it.copy(spendingPercentage = percentage) }
            }
        }

        // Statistiques par catégorie (déjà correct avec .collect())
        launch {
            statisticRepository.getCategoryBreakdown(userId, startTime, endTime).collect { categories ->
                _uiState.update { it.copy(categoryStats = categories.map { it.category to it.amount }) }
            }
        }

        // Statistiques mensuelles (déjà correct avec .collect())
        launch {
            statisticRepository.getMonthlyIncomeExpense(userId, 6).collect { monthlyStats ->
                val monthlyStatsList = monthlyStats.map { stat ->
                    MonthlyStatData(
                        month = monthToName(stat.month),
                        income = stat.income,
                        expense = stat.expense,
                        year = stat.year,
                        monthIndex = stat.month
                    )
                }
                _uiState.update {
                    it.copy(
                        monthlyStats = monthlyStatsList,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }

        // Tendances de dépenses (déjà correct avec .collect())
        launch {
            statisticRepository.getSpendingTrends(userId, StatisticPeriod.MONTHLY).collect { trend ->
                _uiState.update { it.copy(spendingTrend = trend) }
            }
        }

        // Insights (déjà correct avec .collect())
        launch {
            statisticRepository.getSpendingInsights(userId, StatisticPeriod.MONTHLY).collect { insights ->
                _uiState.update { it.copy(insights = insights) }
            }
        }
    }
}
```

---

## 🔧 ÉTAPE 3: AJOUTER GESTION DES COROUTINES (PROBLÈME #4)

### Fichier à modifier: `StatisticsViewModel.kt`

### ✨ AJOUTER CES PROPRIÉTÉS AU DÉBUT DE LA CLASSE:

Après la ligne 39, ajouter:

```kotlin
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val statisticRepository: StatisticRepository,
    private val database: AppDatabase,
    private val offlineSyncManager: OfflineSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(selectedPeriod = StatisticPeriod.MONTHLY))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _exportResult = MutableSharedFlow<Resource<String>>()
    val exportResult: SharedFlow<Resource<String>> = _exportResult.asSharedFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // ✅ NOUVEAU: Job pour gérer et annuler les coroutines de statistiques
    private var statisticsJobs: Job? = null
```

### ✨ MODIFIER LA MÉTHODE `loadStatistics`:

```kotlin
private fun loadStatistics(userId: String) {
    // ✅ CORRECTION #4: Annuler les anciens coroutines avant de créer les nouveaux
    statisticsJobs?.cancel()
    _uiState.update { it.copy(isLoading = true) }

    // ✅ CORRECTION #4: Créer un nouveau Job group
    statisticsJobs = viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val endTime = calendar.time
        calendar.add(Calendar.MONTH, -6)
        val startTime = calendar.time

        // ✅ CORRECTION #1: totalBalance avec .collect()
        launch {
            statisticRepository.getTotalBalance(userId).collect { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }
        }

        // ✅ CORRECTION #2: totalIncome avec .collect()
        launch {
            statisticRepository.getTotalIncome(userId, startTime, endTime).collect { income ->
                _uiState.update { it.copy(totalIncome = income) }
            }
        }

        // ✅ CORRECTION #3: totalExpense avec .collect()
        launch {
            statisticRepository.getTotalExpense(userId, startTime, endTime).collect { expense ->
                _uiState.update { it.copy(totalExpense = expense) }
            }
        }

        // Pourcentage de dépenses
        launch {
            statisticRepository.getSpendingPercentage(userId, startTime, endTime).collect { percentage ->
                _uiState.update { it.copy(spendingPercentage = percentage) }
            }
        }

        // Statistiques par catégorie
        launch {
            statisticRepository.getCategoryBreakdown(userId, startTime, endTime).collect { categories ->
                _uiState.update { it.copy(categoryStats = categories.map { it.category to it.amount }) }
            }
        }

        // Statistiques mensuelles
        launch {
            statisticRepository.getMonthlyIncomeExpense(userId, 6).collect { monthlyStats ->
                val monthlyStatsList = monthlyStats.map { stat ->
                    MonthlyStatData(
                        month = monthToName(stat.month),
                        income = stat.income,
                        expense = stat.expense,
                        year = stat.year,
                        monthIndex = stat.month
                    )
                }
                _uiState.update {
                    it.copy(
                        monthlyStats = monthlyStatsList,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }

        // Tendances de dépenses
        launch {
            statisticRepository.getSpendingTrends(userId, StatisticPeriod.MONTHLY).collect { trend ->
                _uiState.update { it.copy(spendingTrend = trend) }
            }
        }

        // Insights
        launch {
            statisticRepository.getSpendingInsights(userId, StatisticPeriod.MONTHLY).collect { insights ->
                _uiState.update { it.copy(insights = insights) }
            }
        }
    }
}
```

### ✨ AJOUTER LA MÉTHODE `onCleared` (si elle n'existe pas):

À la fin de la classe StatisticsViewModel, ajouter:

```kotlin
    override fun onCleared() {
        super.onCleared()
        // ✅ CORRECTION #4: Annuler les coroutines quand ViewModel est détruit
        statisticsJobs?.cancel()
    }
```

---

## 🔧 ÉTAPE 4: AJOUTER REFRESH AUTOMATIQUE APRÈS TRANSACTION (PROBLÈME #5)

### Option A: Créer un EventBus (RECOMMANDÉ)

### ÉTAPE 4.1: Créer le fichier `TransactionEventBus.kt`

**Emplacement**: `app/src/main/java/com/example/aureus/util/TransactionEventBus.kt`

```kotlin
package com.example.aureus.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event Bus pour les événements de transaction
 * Permet aux ViewModels de communiquer entre eux sans couplage direct
 */
object TransactionEventBus {

    private val _transactionCreated = MutableSharedFlow<Unit>(replay = 1)
    val transactionCreated: SharedFlow<Unit> = _transactionCreated.asSharedFlow()

    private val _transactionUpdated = MutableSharedFlow<Unit>(replay = 1)
    val transactionUpdated: SharedFlow<Unit> = _transactionUpdated.asSharedFlow()

    private val _transactionDeleted = MutableSharedFlow<Unit>(replay = 1)
    val transactionDeleted: SharedFlow<Unit> = _transactionDeleted.asSharedFlow()

    /**
     * Émettre un événement quand une transaction est créée
     */
    suspend fun emitTransactionCreated() {
        _transactionCreated.emit(Unit)
    }

    /**
     * Émettre un événement quand une transaction est mise à jour
     */
    suspend fun emitTransactionUpdated() {
        _transactionUpdated.emit(Unit)
    }

    /**
     * Émettre un événement quand une transaction est supprimée
     */
    suspend fun emitTransactionDeleted() {
        _transactionDeleted.emit(Unit)
    }
}
```

### ÉTAPE 4.2: Modifier `FirebaseDataManager.kt`

**Fichier**: `app/src/main/java/com/example/aureus/data/remote/firebase/FirebaseDataManager.kt`
**Ligne**: 373-398 (méthode `createTransaction`)

Ajouter l'import au début du fichier:
```kotlin
import com.example.aureus.util.TransactionEventBus
```

Modifier la méthode `createTransaction`:

```kotlin
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

    // ✅ CORRECTION #5: Émettre l'événement de transaction créée
    // Les Flow s'en chargeront, donc pas besoin ici
    // Mais l'événement peut être utile pour d'autres ViewModels

    transactionId
}
```

### ÉTAPE 4.3: Modifier `StatisticRepositoryImpl.kt` - Optionnelle

Si vous voulez que les Repositories émettent aussi des événements:

**Fichier**: `app/src/main/java/com/example/aureus/data/repository/StatisticRepositoryImpl.kt`

Ajouter l'import:
```kotlin
import com.example.aureus.util.TransactionEventBus
```

Modifier la méthode `getTotalBalance` pour émettre des events (optionnel, car les Flow le font déjà):

```kotlin
override fun getTotalBalance(userId: String): Flow<Double> {
    return transactionRepository.getTransactions(userId, 1000).map { transactions ->
        val balance = transactions.sumOf { if (it.type.name == "CREDIT") it.amount else -it.amount }
        balance
    }
}
```

**REMARQUE**: Pas besoin de modifier cette méthode car les Flow Firebase sont déjà temps réel ✅

### ÉTAPE 4.4: Modifier `StatisticsViewModel.kt` pour écouter les événements

**Fichier**: `app/src/main/java/com/example/aureus/ui/statistics/viewmodel/StatisticsViewModel.kt`

Ajouter l'import:
```kotlin
import com.example.aureus.util.TransactionEventBus
```

Dans `init()`, ajouter l'écoute des événements:

```kotlin
    init {
        // Observer le statut de sync via Pattern Observer
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
            }
        }

        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadStatistics(userId)

            // ✅ CORRECTION #5: Écouter les événements de transaction créée
            viewModelScope.launch {
                TransactionEventBus.transactionCreated.collect {
                    // Les Flows sont déjà en écoute, donc les stats se mettent à jour automatiquement
                    // Juste s'assurer que isLoading est bien à false
                    if (_uiState.value.isLoading) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }
```

### Option B: Alternative - Écouter directement les transactions (PLUS SIMPLE)

Une approche plus simple sans EventBus est d'écouter directement les transactions dans StatisticsViewModel:

**Dans `StatisticsViewModel.kt` init():**

```kotlin
    init {
        // Observer le statut de sync via Pattern Observer
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
            }
        }

        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadStatistics(userId)

            // ✅ CORRECTION #5 ALTERNATIVE: Écouter directement les transactions
            viewModelScope.launch {
                firebaseDataManager.getUserTransactions(userId, 1000).collect { transactions ->
                    // Les FLOWs de statistiques sont déjà en écoute, donc ils se mettront à jour automatiquement
                    // Juste vérifier si isLoading doit être false
                    if (_uiState.value.isLoading) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }
```

**RECOMMANDATION**: Utiliser Option B (plus simple) pour commencer. Option A (EventBus) peut être ajoutée plus tard si besoin d'autres ViewModels.

---

## 🔧 ÉTAPE 5: OPTIMISER REFRESHSTATISTICS (PROBLÈME #6)

### Fichier à modifier: `StatisticsViewModel.kt`
### Lignes concernées: 138-158

### ✨ CORRECTION DE LA MÉTHODE `refreshStatistics`:

```kotlin
    fun refreshStatistics() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            // ✅ CORRECTION #6: Juste vérifier l'��tat de sync, pas besoin de recharger toute la logique
            // Les FLOWs sont déjà en écoute, donc les stats se mettront à jour automatiquement
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val syncResult = offlineSyncManager.syncNow()
                when (syncResult) {
                    is com.example.aureus.data.offline.SyncResult.Success -> {
                        // Les FLOWs se chargeront automatiquement depuis Firestore
                        // Pas besoin de rappeler loadStatistics()
                        _uiState.update { it.copy(isOfflineMode = false) }
                    }
                    is com.example.aureus.data.offline.SyncResult.Error -> {
                        // Sync failed, les FLOWs chargeront depuis le cache
                        _uiState.update { it.copy(isOfflineMode = true) }
                    }
                }
            } else {
                _uiState.update { it.copy(isOfflineMode = true) }
            }
        }
    }
```

---

## 🧪 ÉTAPE 6: TESTS DE VALIDATION

### TEST #1: Vérifier la mise à jour du solde

1. **Nettoyer les données de test** (optionnel)
2. **Ouvrir StatisticsScreen**
3. **Noter le solde affiché** (ex: 5000 MAD)
4. **Ouvrir SendMoneyScreen**
5. **Cr��er/charger un contact existant**
6. **Envoyer 1000 MAD**
7. **Revenir sur StatisticsScreen** (sans refresh!)
8. **Vérifier**: Le solde doit être 4000 MAD (5000 - 1000)
9. **Si le solde est 5000 → ÉCHEC** ❌
10. **Si le solde est 4000 → SUCCÈS** ✅

### TEST #2: Vérifier la mise à jour des dépenses par catégorie

1. **Ouvrir StatisticsScreen**
2. **Noter les catégories affichées**
3. **Créer une nouveau type de transaction** (ex: "TRAVEL" avec 2000 MAD)
4. **Revenir sur StatisticsScreen** (sans refresh!)
5. **Vérifier**: La catégorie "TRAVEL" doit apparaître dans Pie Chart
6. **Si pas visible → ÉCHEC** ❌
7. **Si visible → SUCCÈS** ✅

### TEST #3: Vérifier la mise à jour des revenus et dépenses

1. **Ouvrir StatisticsScreen**
2. **Noter Total Income et Total Expense**
3. **Créer une transaction INCOME** (ex: SALARY 5000 MAD)
4. **Revenir sur StatisticsScreen**
5. **Vérifier**: Total Income doit augmenter de 5000
6. **Si inchangé → ÉCHEC** ❌
7. **Si augmenté → SUCCÈS** ✅

### TEST #4: Vérifier la mise à jour des statistiques mensuelles (Line Chart)

1. **Ouvrir StatisticsScreen**
2. **Créer plusieurs transactions dans le mois en cours**
3. **Revenir sur StatisticsScreen**
4. **Vérifier**: Le Line Chart doit montrer les nouvelles valeurs pour le mois en cours
5. **Si inchangé → ÉCHEC** ❌
6. **Si mis à jour → SUCCÈS** ✅

### TEST #5: Vérifier le refresh manuel

1. **Ouvrir StatisticsScreen en mode hors ligne**
2. **Noter les données**
3. **Aller en ligne**
4. **Appuyer sur refresh**
5. **Vérifier**: Les données doivent se mettre à jour depuis Firestore
6. **Si échec → ÉCHEC** ❌
7. **Si succès → SUCCÈS** ✅

---

## ✅ CHECKLIST DE VALIDATION

Avant de déclarer les corrections terminées:

- [ ] Les modifications du Problem #1 (totalBalance) sont appliquées
- [ ] Les modifications du Problem #2 (totalIncome) sont appliquées
- [ ] Les modifications du Problem #3 (totalExpense) sont appliquées
- [ ] La propriété `statisticsJobs` est ajoutée
- [ ] La méthode `onCleared()` est ajoutée
- [ ] L'écoute des transactions est ajoutée dans `init()`
- [ ] La méthode `refreshStatistics()` est optimisée
- [ ] Test #1 réussi (mise à jour solde)
- [ ] Test #2 réussi (mise à jour catégories)
- [ ] Test #3 réussi (mise à jour revenus/dépenses)
- [ ] Test #4 réussi (mise à jour stats mensuelles)
- [ ] Test #5 réussi (refresh manuel)
- [ ] Aucun warning ou erreur de compilation
- [ ] L'application build et lance correctement

---

## 🚀 ÉTAPE 7: DÉPLOIEMENT

### Commit des modifications:

```bash
git add app/src/main/java/com/example/aureus/ui/statistics/viewmodel/StatisticsViewModel.kt
git add app/src/main/java/com/example/aureus/util/TransactionEventBus.kt  # Si créé
git commit -m "✨ Fix statistiques temps réel

- Remplacement de .first() par .collect() pour totalBalance, totalIncome, totalExpense
- Ajout de gestion des coroutines avec Job cancellation
- Ajout d'écoute des transactions en temps réel
- Optimisation de refreshStatistics()

Problèmes corrigés:
- #1: totalBalance ne se mettait pas à jour automatiquement
- #2: totalIncome ne se mettait pas à jour automatiquement
- #3: totalExpense ne se mettait pas à jour automatiquement
- #4: Pas d'annulation des coroutines
- #5: Pas de refresh automatique après transaction
- #6: Coroutines recrées sans optimisation

Les graphs se mettent maintenant à jour en temps réel."
```

### Push vers repository:

```bash
git push origin main
```

---

## 📊 RÉSUMÉ DES MODIFICATIONS

| Fichier | Modifications | Lignes modifiées |
|---------|----------------|------------------|
| `StatisticsViewModel.kt` | Remplacement `.first()` par `.collect()` | ~60-85 |
| `StatisticsViewModel.kt` | Ajout propriété `statisticsJobs` | ~40 |
| `StatisticsViewModel.kt` | Ajout méthode `onCleared()` | ~5 |
| `StatisticsViewModel.kt` | Ajout écoute transactions dans `init()` | ~10 |
| `StatisticsViewModel.kt` | Optimisation `refreshStatistics()` | ~20 |
| `TransactionEventBus.kt` | Nouveau fichier (optionnel) | ~45 |

**Total estimé**: ~150 lignes de code modifiées
**Temps de mise en œuvre**: 30-45 minutes
**Impact**: Tous les graphiques se mettront à jour en temps réel après les transactions ✅

---

## 🔮 ÉTAPES FUTURES (OPTIONNELLES)

Si les corrections actuelles ne sont pas suffisantes:

1. **Phase 2**: Ajouter des logs pour déboguer les Flow
2. **Phase 3**: Optimiser la performance des calculs statistiques
3. **Phase 4**: Ajouter des tests unitaires pour les statistiques
4. **Phase 5**: Implémenter le cache local Room pour les stats offline
5. **Phase 6**: Ajouter des animations de transition pour les graphiques

---

## 📞 SUPPORT

Si des problèmes surviennent pendant l'implémentation:

1. **Erreurs de compilation**: Vérifier les imports et la syntaxe Kotlin
2. **Problèmes de Build**: Clean build: `./gradlew clean build`
3. **Tests échouent**: Vérifier que les flows Firebase sont correctement connectés
4. **Problèmes de performance**: Profiler avec Android Studio Profiler

---

## 🎯 SUCCÈS CRITÈRES

Les corrections sont considérées comme réussies si:

✅ Le solde se met à jour automatiquement après chaque transaction
✅ Les graphiques reflètent les changements sans refresh manuel
✅ Aucun leak de mémoire (coroutines correctement annulés)
✅ Les tests de validation réussissent (5/5)
✅ Aucune régression dans l'application
✅ Performance satisfaisante (< 500ms pour mise à jour UI)

---

**Plan créé le 13 Janvier 2026**
**Auteur**: Firebender AI Agent
**Statut**: Prêt pour implémentation
**Version**: 1.0