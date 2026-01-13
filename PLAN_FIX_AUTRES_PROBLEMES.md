# PLAN DE CORRECTION: Autres Problèmes (Sauf Notifications)
**Date de création:** 12 Janvier 2026
**Scope:** Correction des problèmes identifiés hors système de notifications

---

## 📊 RÉSUMÉ DES PROBLÈMES

| # | Problème | Fonctionnalité | Priorité | Estimation |
|---|----------|----------------|----------|------------|
| 1 | Inversion `isEmailVerified()` | Auth | 🟢 Faible | 5 min |
| 2 | Écran CardDetail manquant | Cartes | 🟡 Modéré | 2-3 heures |
| 3 | FAB Transactions non fonctionnel | Transactions | 🟢 Faible | 15-30 min |
| 4 | Success dialog non affiché | Transferts | 🟢 Faible | 10 min |
| 5 | Bouton "Scan QR" non fonctionnel | Home | 🟡 Modéré | 3-4 heures OU 5 min (suppression) |
| 6 | Bouton "More" non fonctionnel | Home | 🟢 Faible | 10 min OU 2 heures (menu) |

**Total estimé:** 2-4 heures (sans implémentation QR Scanner)

---

## 🟡 PHASE 1: Corrections Rapides (15-30 minutes)

### 1.1 Corriger `isEmailVerified()` ✅

**Fichier:** `app/src/main/java/com/example/aureus/ui/auth/viewmodel/AuthViewModel.kt`

**Problème:** Inversion logique ligne 300

**Correction:**
```kotlin
// ❌ ANCIEN CODE (ligne 300-302):
fun isEmailVerified(): Boolean {
    return authManager.currentUser?.isEmailVerified == false  // INVERSION!
}

// ✅ NOUVEAU CODE:
fun isEmailVerified(): Boolean {
    return authManager.currentUser?.isEmailVerified == true  // CORRECT
}
```

**Action:** Modifier la ligne 300

**Test:** Vérifier que la fonction retourne `true` quand l'email est vérifié

---

### 1.2 Afficher Success Dialog dans Send Money ✅

**Fichier:** `app/src/main/java/com/example/aureus/ui/transfer/SendMoneyScreenFirebase.kt`

**Problème:** Dialog de succès défini mais jamais déclenché

**Solution 1: Remplacer la navigation par le dialog**

Modifier la méthode `onSendClick` dans `Navigation.kt` pour ne plus naviguer automatiquement:

```1:478:app/src/main/java/com/example/aureus/ui/navigation/Navigation.kt
// DANS SendMoney route - ligne 296-307:
composable(Screen.SendMoney.route) {
    SendMoneyScreenFirebase(
        navController = navController,
        onNavigateBack = {
            navController.popBackStack()
        },
        onSendClick = { contact, amount ->
            // ✅ NE PLUS NAVIGUER, laisser le screen afficher le success dialog
        },
        onAddContactClick = {
            navController.navigate(Screen.ContactManagement.route)
        }
    )
}
```

**Solution 2: Ajouter un bouton "Done" dans success dialog**

Modifier le success dialog dans `SendMoneyScreenFirebase.kt` (ligne 336-349):

```kotlin
// Modifier le bouton confirmButton pour naviguer:
confirmButton = {
    Button(
        onClick = {
            showSuccessDialog = false
            // ✅ Naviguer vers Dashboard après succès
            navController?.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.SendMoney.route) { inclusive = true }
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)
    ) {
        Text("Done")
    }
}
```

**Solution RECOMMANDÉE:** Utiliser un Snackbar au lieu du dialog (plus moderne)

Ajouter après le bouton "Send Money" (ligne 274-310):

```kotlin
// Ajouter SnackbarHostState
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// Modifier dans Scaffold:
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    // ... reste inchangé

// Dans onSendClick, remplacer:
onSendClick = { contact, amount ->
    // ✅ Afficher Snackbar success
    scope.launch {
        snackbarHostState.showSnackbar(
            message = "Money sent successfully to ${contact.name}!",
            duration = SnackbarDuration.Short,
            actionLabel = "OK"
        )
    }
}
```

**Action:** Implémenter la solution Snackbar

---

### 1.3 Supprimer ou Implémenter FAB Transactions ✅

**Fichier:** `app/src/main/java/com/example/aureus/ui/transactions/TransactionsFullScreenFirebase.kt`

**Solution RECOMMANDÉE:** Supprimer le FAB car la fonctionnalité existe déjà au top (filter/search icons)

**Supprimer** lignes 87-94:

```kotlin
// ❌ SUPPRIMER ces lignes 87-94:
floatingActionButton = {
    FloatingActionButton(
        onClick = { /* Filter dialog */ },
        containerColor = SecondaryGold
    ) {
        Icon(Icons.Default.FilterList, "Filters")
    }
}
```

**Ou implémenter si nécessaire:**

```kotlin
// Option: Dialog de filtres avancés
var showAdvancedFilterDialog by remember { mutableStateOf(false) }

floatingActionButton = {
    FloatingActionButton(
        onClick = { showAdvancedFilterDialog = true },
        containerColor = SecondaryGold
    ) {
        Icon(Icons.Default.FilterList, "Advanced Filters")
    }
}
```

**Action:** Supprimer le FAB (solution la plus simple)

---

### 1.4 Bouton "More" - Implémenter menu simple ✅

**Fichier:** `app/src/main/java/com/example/aureus/ui/home/HomeScreen.kt`

**Solution:** Implémenté un menu Dropdown pour le bouton "More"

Modifier `QuickActionsRow` (lignes 509-541):

```kotlin
@Composable
private fun QuickActionsRow(
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToRequestMoney: () -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            onClick = onNavigateToSendMoney
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            label = "Request",
            onClick = onNavigateToRequestMoney
        )
        QuickActionButton(
            icon = Icons.Default.QrCodeScanner,
            label = "Scan",
            onClick = { /* Scan QR - Future feature */ }
        )

        // ✅ NOUVEAU: Bouton More avec Menu
        Box {
            QuickActionButton(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                onClick = { showMoreMenu = true }
            )

            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                    onClick = { showMoreMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Help") },
                    leadingIcon = { Icon(Icons.Default.Help, null) },
                    onClick = { showMoreMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    onClick = { showMoreMenu = false }
                )
            }
        }
    }
}
```

**Action:** Ajouter menu Dropdown

---

## 🟡 PHASE 2: Cartes - Créer CardDetailScreen (2-3 heures)

### 2.1 Créer le modèle de données pour les détails carte

**Fichier à créer:** `app/src/main/java/com/example/aureus/domain/model/CardDetail.kt`

```kotlin
package com.example.aureus.domain.model

import java.util.Date

/**
 * Détails complets d'une carte bancaire
 */
data class CardDetail(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,  // Partiellement masqué
    val cardType: CardType,
    val cardColor: CardColor,
    val isDefault: Boolean,
    val isActive: Boolean,
    val balance: Double,
    val currency: String,
    val dailyLimit: Double,
    val monthlyLimit: Double,
    val spendingToday: Double,
    val spendingMonth: Double,
    val status: CardStatus = CardStatus.ACTIVE,
    val createdAt: Date,
    val lastUsed: Date? = null,
    val securitySettings: CardSecuritySettings = CardSecuritySettings()
)

enum class CardType {
    VISA,
    MASTERCARD,
    AMERICAN_EXPRESS,
    DISCOVER
}

enum class CardStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED,
    FROZEN,
    PENDING
}

data class CardSecuritySettings(
    val requirePinForOnline: Boolean = true,
    val requirePinForContactless: Boolean = true,
    val maxDailyAmount: Double = 10000.0,
    val abroadEnabled: Boolean = false
)
```

---

### 2.2 Créer CardDetailViewModel

**Fichier à créer:** `app/src/main/java/com/example/aureus/ui/cards/viewmodel/CardDetailViewModel.kt`

```kotlin
package com.example.aureus.ui.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.CardDetail
import com.example.aureus.domain.model.CardStatus
import com.example.aureus.domain.mapToCardDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _cardDetailState = MutableStateFlow<CardDetailUiState>(CardDetailUiState.Loading)
    val cardDetailState: StateFlow<CardDetailUiState> = _cardDetailState.asStateFlow()

    private val _cardTransactions = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val cardTransactions: StateFlow<List<Map<String, Any>>> = _cardTransactions.asStateFlow()

    fun loadCardDetail(cardId: String) {
        viewModelScope.launch {
            _cardDetailState.value = CardDetailUiState.Loading

            try {
                val result = firebaseDataManager.getCardById(cardId)

                if (result.isSuccess) {
                    val cardData = result.getOrNull()!!
                    val cardDetail = mapToCardDetail(cardData)

                    _cardDetailState.value = CardDetailUiState.Success(cardDetail)

                    // Load transactions for this card
                    loadCardTransactions(cardId)
                } else {
                    _cardDetailState.value = CardDetailUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load card"
                    )
                }
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun loadCardTransactions(cardId: String) {
        viewModelScope.launch {
            val userId = authManager.currentUser?.uid
            if (userId != null) {
                firebaseDataManager.getTransactionsByCard(cardId, userId)
                    .collect { transactions ->
                        _cardTransactions.value = transactions
                    }
            }
        }
    }

    fun freezeCard(cardId: String) {
        viewModelScope.launch {
            try {
                firebaseDataManager.updateCard(cardId, mapOf(
                    "status" to "FROZEN",
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
                // Reload card detail
                loadCardDetail(cardId)
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to freeze card")
            }
        }
    }

    fun blockCard(cardId: String) {
        viewModelScope.launch {
            try {
                firebaseDataManager.updateCard(cardId, mapOf(
                    "status" to "BLOCKED",
                    "isActive" to false,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
                loadCardDetail(cardId)
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to block card")
            }
        }
    }

    fun setAsDefault(cardId: String) {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUser?.uid
                if (userId != null) {
                    firebaseDataManager.setDefaultCard(userId, cardId)
                    loadCardDetail(cardId)
                }
            } catch (e: Exception) {
                _cardDetailState.value = CardDetailUiState.Error("Failed to set as default")
            }
        }
    }
}

sealed class CardDetailUiState {
    object Loading : CardDetailUiState()
    data class Success(val card: CardDetail) : CardDetailUiState()
    data class Error(val message: String) : CardDetailUiState()
}
```

**Ajouter dans FirebaseDataManager.kt:**

```kotlin
/**
 * Obtenir les transactions pour une carte spécifique
 */
fun getTransactionsByCard(cardId: String, userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
    val listener = transactionsCollection
        .whereEqualTo("userId", userId)
        .whereEqualTo("cardId", cardId)
        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

// Dans companion object de User.kt, ajouter:
fun mapToCardDetail(cardData: Map<String, Any>): CardDetail {
    val date = com.google.firebase.Timestamp.now().toDate()
    return CardDetail(
        id = cardData["cardId"] as? String ?: "",
        userId = cardData["userId"] as? String ?: "",
        cardNumber = cardData["cardNumber"] as? String ?: "",
        cardHolder = cardData["cardHolder"] as? String ?: "",
        expiryDate = cardData["expiryDate"] as? String ?: "",
        cvv = "***",
        cardType = when (cardData["cardType"] as? String ?: "VISA") {
            "VISA" -> CardType.VISA
            "MASTERCARD" -> CardType.MASTERCARD
            "AMERICAN_EXPRESS" -> CardType.AMERICAN_EXPRESS
            else -> CardType.VISA
        },
        cardColor = when (cardData["cardColor"] as? String ?: "navy") {
            "navy" -> CardColor.NAVY
            "gold" -> CardColor.GOLD
            "black" -> CardColor.BLACK
            "blue" -> CardColor.BLUE
            else -> CardColor.NAVY
        },
        isDefault = cardData["isDefault"] as? Boolean ?: false,
        isActive = cardData["isActive"] as? Boolean ?: true,
        balance = cardData["balance"] as? Double ?: 0.0,
        currency = "MAD",
        dailyLimit = cardData["dailyLimit"] as? Double ?: 10000.0,
        monthlyLimit = cardData["monthlyLimit"] as? Double ?: 50000.0,
        spendingToday = cardData["spendingToday"] as? Double ?: 0.0,
        spendingMonth = 0.0,  // Calculé depuis transactions
        status = when ((cardData["status"] as? String) ?: "ACTIVE") {
            "ACTIVE" -> CardStatus.ACTIVE
            "BLOCKED" -> CardStatus.BLOCKED
            "EXPIRED" -> CardStatus.EXPIRED
            "FROZEN" -> CardStatus.FROZEN
            else -> CardStatus.ACTIVE
        },
        createdAt = date,
        lastUsed = date
    )
}
```

---

### 2.3 Créer CardDetailScreen

**Fichier à créer:** `app/src/main/java/com/example/aureus/ui/cards/CardDetailScreen.kt`

```kotlin
package com.example.aureus.ui.cards

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardStatus
import com.example.aureus.ui.cards.viewmodel.CardDetailViewModel
import com.example.aureus.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    LaunchedEffect(cardId) {
        viewModel.loadCardDetail(cardId)
    }

    val uiState by viewModel.cardDetailState.collectAsState()
    val cardTransactions by viewModel.cardTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CardDetailViewModel.CardDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                }
            }
            is CardDetailViewModel.CardDetailUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadCardDetail(cardId) }
                )
            }
            is CardDetailViewModel.CardDetailUiState.Success -> {
                CardDetailContent(
                    card = state.card,
                    transactions = cardTransactions,
                    onFreezeCard = { viewModel.freezeCard(cardId) },
                    onBlockCard = { viewModel.blockCard(cardId) },
                    onSetDefault = { viewModel.setAsDefault(cardId) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: com.example.aureus.domain.model.CardDetail,
    transactions: List<Map<String, Any>>,
    onFreezeCard: () -> Unit,
    onBlockCard: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralLightGray),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card Display
        item {
            CardDisplay(card = card)
        }

        // Balance and Limits
        item {
            BalanceAndLimitsCard(card = card)
        }

        // Status Badge
        item {
            StatusCard(
                status = card.status,
                isActive = card.isActive,
                onFreezeCard = onFreezeCard,
                onBlockCard = onBlockCard,
                onSetDefault = onSetDefault,
                isDefault = card.isDefault,
                cardId = card.id
            )
        }

        // Recent Transactions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = NeutralMediumGray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavyBlue
                        )
                    }
                }
            }
        } else {
            items(transactions.take(10)) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
private fun CardDisplay(card: com.example.aureus.domain.model.CardDetail) {
    val gradient = when (card.cardColor) {
        CardColor.NAVY -> Brush.horizontalGradient(
            listOf(PrimaryNavyBlue, PrimaryMediumBlue)
        )
        CardColor.GOLD -> Brush.horizontalGradient(
            listOf(SecondaryGold, SecondaryDarkGold)
        )
        else -> Brush.horizontalGradient(
            listOf(PrimaryNavyBlue, PrimaryMediumBlue)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = null,
                        tint = NeutralWhite.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column {
                    Text(
                        text = maskCardNumber(card.cardNumber),
                        color = NeutralWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CARD HOLDER",
                                color = NeutralWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = card.cardHolder,
                                color = NeutralWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXPIRES",
                                color = NeutralWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = card.expiryDate,
                                color = NeutralWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = card.cardType.name,
                            color = SecondaryGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceAndLimitsCard(card: com.example.aureus.domain.model.CardDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Balance & Limits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance
            StatRow("Available Balance", formatCurrency(card.balance), SecondaryGold)
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Daily Limit
            val dailyPercentage = (card.spendingToday / card.dailyLimit * 100).toInt()
            StatRow(
                "Daily Limit",
                "${formatCurrency(card.spendingToday)} / ${formatCurrency(card.dailyLimit)}",
                color = if (dailyPercentage > 80) SemanticRed else PrimaryNavyBlue
            )

            // Progress bar for daily limit
            LinearProgressIndicator(
                progress = { dailyPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = if (dailyPercentage > 80) SemanticRed else SecondaryGold,
                trackColor = NeutralLightGray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Monthly Limit
            MonthlyLimitRow(card)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = NeutralMediumGray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MonthlyLimitRow(card: com.example.aureus.domain.model.CardDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Monthly Limit",
            fontSize = 14.sp,
            color = NeutralMediumGray
        )
        Text(
            text = formatCurrency(card.monthlyLimit),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavyBlue
        )
    }
}

@Composable
private fun StatusCard(
    status: CardStatus,
    isActive: Boolean,
    onFreezeCard: () -> Unit,
    onBlockCard: () -> Unit,
    onSetDefault: () -> Unit,
    isDefault: Boolean,
    cardId: String
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = when (status) {
            CardStatus.ACTIVE -> SemanticGreen.copy(alpha = 0.1f)
            CardStatus.FROZEN -> SemanticRed.copy(alpha = 0.1f)
            CardStatus.BLOCKED -> color(android.graphics.Color.GRAY, alpha = 0.1f)
            CardStatus.EXPIRED -> color(android.graphics.Color.GRAY, alpha = 0.1f)
            else -> NeutralWhite
        }),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(when (status) {
                            CardStatus.ACTIVE -> SemanticGreen
                            CardStatus.FROZEN -> SemanticRed
                            CardStatus.BLOCKED -> color(android.graphics.Color.GRAY)
                            CardStatus.EXPIRED -> color(android.graphics.Color.GRAY)
                            else -> SecondaryGold
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (status) {
                            CardStatus.ACTIVE -> Icons.Default.CheckCircle
                            CardStatus.FROZEN -> Icons.Default.AcUnit
                            CardStatus.BLOCKED -> Icons.Default.Block
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = NeutralWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = status.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            CardStatus.ACTIVE -> SemanticGreen
                            else -> NeutralMediumGray
                        }
                    )
                    if (status == CardStatus.ACTIVE) {
                        Text(
                            text = "Card is active and ready to use",
                            fontSize = 12.sp,
                            color = NeutralMediumGray
                        )
                    } else {
                        Text(
                            text = when (status) {
                                CardStatus.FROZEN -> "Card is temporarily frozen"
                                CardStatus.BLOCKED -> "Card is permanently blocked"
                                CardStatus.EXPIRED -> "Card has expired"
                                else -> "Contact support"
                            },
                            fontSize = 12.sp,
                            color = NeutralMediumGray
                        )
                    }
                }
            }

            Row {
                if (isDefault) {
                    Chip(
                        onClick = {},
                        label = { Text("DEFAULT") },
                        colors = ChipDefaults.chipColors(
                            containerColor = SecondaryGold.copy(alpha = 0.2f),
                            labelColor = SecondaryGold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box {
                    IconButton(onClick = { showActionMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions"
                        )
                    }

                    DropdownMenu(
                        expanded = showActionMenu,
                        onDismissRequest = { showActionMenu = false }
                    ) {
                        if (status == CardStatus.ACTIVE && !isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as Default") },
                                leadingIcon = { Icon(Icons.Default.Star, null) },
                                onClick = {
                                    showActionMenu = false
                                    onSetDefault()
                                }
                            )
                        }
                        if (status == CardStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text("Freeze Card") },
                                leadingIcon = { Icon(Icons.Default.AcUnit, null) },
                                onClick = {
                                    showActionMenu = false
                                    onFreezeCard()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Block Card") },
                            leadingIcon = { Icon(Icons.Default.Block, null) },
                            onClick = {
                                showActionMenu = false
                                onBlockCard()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Map<String, Any>) {
    val amount = transaction["amount"] as? Double ?: 0.0
    val type = transaction["type"] as? String ?: "EXPENSE"
    val title = transaction["title"] as? String ?: "Transaction"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavyBlue
                )
                Text(
                    text = formatTransactionDate(transaction["createdAt"]),
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
            Text(
                text = if (type == "INCOME") "+${formatCurrency(amount)}" else formatCurrency(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (type == "INCOME") SemanticGreen else SemanticRed
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = SemanticRed.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = SemanticRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
            ) {
                Text("Retry")
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun maskCardNumber(cardNumber: String): String {
    return "**** **** **** " + cardNumber.takeLast(4)
}

private fun formatTransactionDate(timestamp: Any?): String {
    if (timestamp == null) return "Just now"
    val date = when (timestamp) {
        is com.google.firebase.Timestamp -> timestamp.toDate()
        is Date -> timestamp
        else -> return "Just now"
    }
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}
```

---

### 2.4 Ajouter la route et navigation

**Modifier** `app/src/main/java/com/example/aureus/ui/navigation/Navigation.kt`

**Ajouter route dans Screen sealed class:**
```kotlin
object CardDetail : Screen("card_detail/{cardId}")
```

**Ajouter dans AppNavigation:**
```kotlin
// Card Detail Screen
composable(
    route = Screen.CardDetail.route,
    arguments = listOf(
        navArgument("cardId") { type = NavType.StringType; nullable = false }
    )
) { backStackEntry ->
    val cardId = backStackEntry.arguments?.getString("cardId") ?: ""

    CardDetailScreen(
        cardId = cardId,
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}
```

**Modifier `CardsScreen.kt` pour utiliser la navigation:**

Dans `DetailedCardItem` (lignes 77-80):
```kotlin
DetailedCardItem(
    card = card,
    onClick = { cardId ->
        navController.navigate("card_detail/$cardId")
    }
)
```

Dans `FullCardDisplay` (lignes 197-200):
```kotlin
FullCardDisplay(
    card = card,
    onCardClick = { cardId ->
        navController.navigate("card_detail/$cardId")
    }
)
```

---

## 🟢 PHASE 3: Quick Fixes pour Home (5-30 minutes)

### 3.1 Bouton "Scan QR" - Deux options

**Option 1 (Recommandée): Supprimer le bouton**

Dans `HomeScreen.kt`, supprimer ou commenter lignes 530-534:

```kotlin
// ❌ SUPPRIMER ou COMMENTER:
QuickActionButton(
    icon = Icons.Default.QrCodeScanner,
    label = "Scan",
    onClick = { /* Scan QR - Future feature */ }
)
```

**Option 2: Implémenter un navigateur vers page "Coming Soon"**

```kotlin
QuickActionButton(
    icon = Icons.Default.QrCodeScanner,
    label = "Scan",
    onClick = { /* TODO: Navigate to QrScannerScreen */ }
)
```

**Action:** Supprimer le bouton (option la plus simple pour l'instant)

---

### 3.2 Bouton "More" - Déjà implémenté dans Phase 1.4

Vérifier que l'implémentation avec DropdownMenu fonctionne correctement.

---

## 📝 CHECKLIST DE VALIDATION

### Phase 1 - Corrections Rapides
- [ ] Corriger `isEmailVerified()` dans AuthViewModel
- [ ] Remplacer FAB par Snackbar dans TransactionsScreen
- [ ] Supprimer FAB non fonctionnel
- [ ] Implémenter menu "More" avec DropdownMenu

### Phase 2 - CardDetailScreen
- [ ] Créer `domain/model/CardDetail.kt`
- [ ] Créer `domain/model/CardStatus.kt` (ou inclure dans CardDetail)
- [ ] Créer `ui/cards/viewmodel/CardDetailViewModel.kt`
- [ ] Ajouter `mapToCardDetail()` dans User.kt
- [ ] Ajouter `getTransactionsByCard()` dans FirebaseDataManager.kt
- [ ] Créer `ui/cards/CardDetailScreen.kt`
- [ ] Ajouter route dans Navigation.kt
- [ ] Modifier onClick dans CardsScreen
- [ ] Tester toutes les actions (Freeze, Block, Set Default)

### Phase 3 - Home Quick Fixes
- [ ] Supprimer ou désactiver bouton "Scan QR"
- [ ] Tester bouton "More" avec menu

---

## 🎯 ESTIMATION DU TEMPS

| Phase | Temps estimé | Priorité |
|-------|-------------|----------|
| Phase 1 - Corrections Rapides | 15-30 min | 🟢 |
| Phase 2 - CardDetailScreen | 2-3 heures | 🟡 |
| Phase 3 - Home Fixes | 5 min | 🟢 |
| **TOTAL** | **2.5-4 heures** | |

---

## 📚 NOTES IMPORTANTES

### Dépendances nécessaires:
- Aucune nouvelle dépendance externe nécessaire
- Utilise les composants existants (Material3, Firebase)

### Tests à effectuer:
1. Test `isEmailVerified()` avec email vérifié vs non vérifié
2. Test Snackbar de succès après transfert
3. Test navigation vers CardDetail
4. Test Freeze/Block/Set Default actions
5. Test menu "More" dans Home

### UI/UX Considérations:
- Maintenir cohérence avec le design existant
- Utiliser SecondaryGold pour les actions principales
- Afficher des messages d'erreur clairs
- Ajouter loading states appropriés

---

## 🔥 PLAN RAPIDE (Quick Start)

Si vous voulez avancer vite, voici l'ordre recommandé:

### 1. Commencer par les corrections faciles (15 min)
- Corriger `isEmailVerified()` ✅
- Supprimer FAB Transactions ✅
- Implémenter menu "More" ✅

### 2. Créer CardDetailScreen (2-3 heures)
- Créer les fichiers nécessaires
- Tester la navigation
- Tester les actions

### 3. Optionnel: QR Scanner (Future)
- Pour l'instant, supprimer le bouton
- Future: Implémenter avec ML Kit Vision

---

**Fin du Plan**
**Date de création:** 12 Janvier 2026
**Prochaine étape:** Commencer par Phase 1 - Corrections Rapides