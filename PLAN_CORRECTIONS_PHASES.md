# 🎯 PLAN DE CORRECTIONS - AUREUS BANKING APP
**Date**: 11 Janvier 2026
**Basé sur**: RAPPORT_AUDIT_COMPLET.md
**Objectif**: Corriger tous les problèmes et atteindre 100% de fonctionnalité

---

## 📊 PROBLÈMES IDENTIFIÉS

### 🔴 PRIORITÉ 1 - CRITIQUES
1. **Google Sign-In Non Fonctionnel** - `LoginScreen.kt`
2. **PinSetupScreen Sans ViewModel** - PIN non sauvegardé
3. **TransactionViewModelFirebase Non Implémenté** - Écran transactions ne charge pas

### 🟡 PRIORITÉ 2 - MOYENS
4. **ContactManagementScreen Manquant** - Impossible de gérer contacts
5. **Transaction Detail Screen Firebase Manquant** - Détails transactions
6. **Null Checks Incomplets** - Dans SendMoneyScreenFirebase

### 🟢 PRIORITÉ 3 - AMÉLIORATIONS
7. **Auth State Persistence** - Add AuthStateListener
8. **Charts Professionnels** - VICO Chart library
9. **Error Handling Complexe** - Tous les ViewModels

---

## ⏱️ ESTIMATION DU TEMPS

| Phase | Durée | Priorité | Complexité |
|-------|-------|----------|------------|
| Phase 1: Google Sign-In | 1-2 jours | 🔴 Critique | Moyenne |
| Phase 2: Pin Setup | 1 jour | 🔴 Critique | Faible |
| Phase 3: Transactions Firebase | 1-2 jours | 🔴 Critique | Moyenne |
| Phase 4: Contacts Management | 2-3 jours | 🟡 Moyenne | Moyenne |
| Phase 5: Transaction Details | 1-2 jours | 🟡 Moyenne | Faible |
| Phase 6: Finalisation | 1 jour | 🟢 Amélioration | Faible |

**TOTAL ESTIMÉ**: 7-11 jours (1.5 à 2 semaines)

---

## 🔴 PHASE 1: CORRIGER GOOGLE SIGN-IN (1-2 jours)

### Objectif
Intégrer complètement Google Sign-In avec Firebase Authentication

#### ÉTAPE 1.1: Modifier AuthViewModel

**Fichier**: `app/src/main/java/com/example/aureus/ui/auth/viewmodel/AuthViewModel.kt`

**Ajouter la méthode**:

```kotlin
/**
 * Authentifier avec Google Credential
 */
fun signInWithGoogleCredential(credential: com.google.firebase.auth.GoogleAuthProvider.Credential) {
    viewModelScope.launch {
        _googleSignInState.value = Resource.Loading
        
        try {
            val result = authManager.signInWithGoogleCredential(credential.toFirebaseCredential())
            
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()!!
                val now = com.google.firebase.Timestamp.now().toDate().toString()
                
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                    lastName = firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                    phone = firebaseUser.phoneNumber,
                    createdAt = now,
                    updatedAt = now
                )
                
                _googleSignInState.value = Resource.Success(user)
            } else {
                _googleSignInState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In Failed")
            }
        } catch (e: Exception) {
            _googleSignInState.value = Resource.Error("Error: ${e.message}")
        }
    }
}
```

**Ajouter StateFlow**:

```kotlin
private val _googleSignInState = MutableStateFlow<Resource<User>>(Resource.Idle)
val googleSignInState: StateFlow<Resource<User>> = _googleSignInState.asStateFlow()
```

---

#### ÉTAPE 1.2: Corriger LoginScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/auth/screen/LoginScreen.kt`

**Remplacer lignes 75-90** (Google Sign-In Launcher):

```kotlin
val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        // CRITICAL: Create FirebaseCredential and authenticate with Firebase
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        viewModel.signInWithGoogleCredential(credential)
        Log.d("LoginScreen", "Google Sign-In Success with Firebase credential")
    } catch (e: ApiException) {
        Log.w("LoginScreen", "Google Sign-In Failed", e)
        onGoogleSignInError("Google Sign-In Failed: ${e.message}")
    }
}
```

**Ajouter LaunchedEffect pour navigation**:

```kotlin
// Navigate on successful Google sign-in
LaunchedEffect(googleSignInState) {
    when (val state = googleSignInState) {
        is Resource.Success -> {
            // Google Sign-In successful - navigate to phone verification
            phoneAuthViewModel.setLinkingExistingUser(true)
            navController.navigate(ScreenUtils.phoneNumberInputScreen()) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            _googleSignInState.value = Resource.Idle
        }
        is Resource.Error -> {
            onGoogleSignInError(state.message)
        }
        else -> {}
    }
}
```

**Ajouter**:

```kotlin
val googleSignInState by viewModel.googleSignInState.collectAsState()
```

---

#### ÉTAPE 1.3: Vérifier AuthRepository

**Fichier**: `app/src/main/java/com/example/aureus/data/repository/AuthRepositoryImpl.kt`

**Méthode signInWithGoogleCredential existe déjà** dans FirebaseAuthManager (lignes 126-132) ✅

**Pas de modification nécessaire** - le code existe déjà !

---

#### ÉTAPE 1.4: Test

**Test Manual**:

1. Ouvrir app → Login Screen
2. Cliquer "Continuer avec Google"
3. Sélectionner compte Google
4. Vérifier: Redirection vers PhoneNumberInputScreen
5. Vérifier dans Firebase Console: User créé dans collection `users`

**Expected Result**: Google Sign-In crée utilisateur Firebase et navigue vers SMS verification

---

## 🔴 PHASE 2: IMPLEMENTER PIN SETUP (1 jour)

### Objectif
Créer PinViewModel pour sauvegarder le PIN dans Firebase

---

#### ÉTAPE 2.1: Créer PinViewModel

**Fichier**: `app/src/main/java/com/example/aureus/ui/auth/viewmodel/PinViewModel.kt`

**Créer nouveau fichier**:

```kotlin
package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PIN Setup ViewModel
 * Gère la création et la mise à jour du PIN utilisateur
 */
@HiltViewModel
class PinViewModel @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val firebaseDataManager: FirebaseDataManager
) : ViewModel() {

    private val _pinState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val pinState: StateFlow<Resource<Unit>> = _pinState.asStateFlow()

    private val _verifyState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val verifyState: StateFlow<Resource<Boolean>> = _verifyState.asStateFlow()

    /**
     * Créer ou mettre à jour le PIN utilisateur
     */
    fun setPin(pin: String) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _pinState.value = Resource.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _pinState.value = Resource.Loading

            try {
                // TODO: Encrypt PIN with AES-256 before storing
                val result = firebaseDataManager.updateUser(userId, mapOf(
                    "pin" to pin,
                    "isPhoneVerified" to true,
                    "pinCreatedAt" to com.google.firebase.Timestamp.now()
                ))

                if (result.isSuccess) {
                    _pinState.value = Resource.Success(Unit)
                } else {
                    _pinState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to set PIN")
                }
            } catch (e: Exception) {
                _pinState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Vérifier si le PIN entré correspond au PIN stocké
     */
    fun verifyPin(enteredPin: String) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _verifyState.value = Resource.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _verifyState.value = Resource.Loading

            try {
                // Pour l'instant, récupérer le PIN depuis Firebase
                // TODO: Implement offline PIN verification with EncryptedSharedPreferences
                val result = firebaseDataManager.getUser(userId).first()

                result?.let { userData ->
                    val storedPin = userData["pin"] as? String ?: ""

                    if (storedPin == enteredPin) {
                        _verifyState.value = Resource.Success(true)
                    } else {
                        _verifyState.value = Resource.Error("PIN incorrect")
                    }
                } ?: run {
                    _verifyState.value = Resource.Error("User data not found")
                }
            } catch (e: Exception) {
                _verifyState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Changer le PIN existant
     */
    fun changePin(oldPin: String, newPin: String) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _pinState.value = Resource.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _pinState.value = Resource.Loading

            try {
                // First verify old PIN
                val userData = firebaseDataManager.getUser(userId).first()
                val storedPin = userData?.get("pin") as? String ?: ""

                if (storedPin == oldPin) {
                    // Update to new PIN
                    val result = firebaseDataManager.updateUser(userId, mapOf(
                        "pin" to newPin,
                        "pinUpdatedAt" to com.google.firebase.Timestamp.now()
                    ))

                    if (result.isSuccess) {
                        _pinState.value = Resource.Success(Unit)
                    } else {
                        _pinState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to update PIN")
                    }
                } else {
                    _pinState.value = Resource.Error("Old PIN is incorrect")
                }
            } catch (e: Exception) {
                _pinState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _pinState.value = Resource.Idle
        _verifyState.value = Resource.Idle
    }
}
```

---

#### ÉTAPE 2.2: Modifier PinSetupScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/auth/screen/PinSetupScreen.kt`

**Ajouter PinViewModel**:

```kotlin
@Composable
fun PinSetupScreen(
    viewModel: PinViewModel = hiltViewModel(),
    onPinSetupComplete: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // ... existing code ...
    
    val pinState by viewModel.pinState.collectAsState()
    
    // Navigate on success
    LaunchedEffect(pinState) {
        if (pinState is Resource.Success) {
            delay(1000)
            onPinSetupComplete()
        }
    }
    
    // ... dans le bouton "Create PIN" ...
    Button(
        onClick = {
            if (pin.length == 4) {
                viewModel.setPin(pin)
            }
        },
        enabled = pin.length == 4 && pinState !is Resource.Loading
    ) {
        if (pinState is Resource.Loading) {
            CircularProgressIndicator(color = PrimaryNavyBlue)
        } else {
            Text("Create PIN")
        }
    }
}
```

---

#### ÉTAPE 2.3: Ajouter dans AppModule

**Fichier**: `app/src/main/java/com/example/aureus/di/ViewModelModule.kt`

**S'assurer que PinViewModel est fourni**:

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    @Provides
    @IntoMap
    @ViewModelKey(PinViewModel::class)
    fun providePinViewModel(viewModel: PinViewModel): ViewModel = viewModel
}
```

---

#### ÉTAPE 2.4: Test

**Test Manual**:

1. Register avec email/password
2. SMS Verification successful
3. PIN Setup Screen s'affiche
4. Entrer PIN (4 chiffres)
5. Cliquer "Create PIN"
6. Vérifier: Redirection vers Dashboard
7. Vérifier dans Firebase Console: Champ `pin` existe dans user document

**Expected Result**: PIN sauvegardé dans Firebase et navigation vers Dashboard

---

## 🔴 PHASE 3: TRANSACTIONSVIEWMODEL FIREBASE (1-2 jours)

### Objectif
Implémenter TransactionViewModelFirebase pour charger les transactions depuis Firebase

---

#### ÉTAPE 3.1: Créer TransactionViewModelFirebase

**Fichier**: `app/src/main/java/com/example/aureus/ui/transaction/viewmodel/TransactionViewModelFirebase.kt`

**Créer nouveau fichier**:

```kotlin
package com.example.aureus.ui.transaction.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Date Period Filter
 */
enum class DatePeriod {
    All,
    Today,
    ThisWeek,
    ThisMonth,
    ThisYear
}

/**
 * Transactions UI State
 */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    val transactions: List<Map<String, Any>> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val error: String? = null
)

/**
 * Transactions Firebase ViewModel
 * 100% dynamique avec TransactionRepositoryFirebase
 */
@HiltViewModel
class TransactionViewModelFirebase @Inject constructor(
    private val transactionRepository: TransactionRepositoryFirebase,
    private val firebaseDataManager: FirebaseDataManager
) : ViewModel() {

    // Transactions State
    private val _transactionsState = MutableStateFlow(TransactionsUiState())
    val transactionsState: StateFlow<TransactionsUiState> = _transactionsState.asStateFlow()

    // Filter State
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Refreshing State
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Computed filtered transactions
    val filteredTransactionsState: StateFlow<List<Map<String, Any>>> = combine(
        _transactionsState,
        _selectedFilter,
        _searchQuery
    ) { state, filter, query ->
        
        var filtered = state.transactions
        
        // Filter by type
        if (filter != "All") {
            filtered = filtered.filter { it["type"] == filter }
        }
        
        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                val title = transaction["title"] as? String ?: ""
                val merchant = transaction["merchant"] as? String ?: ""
                val category = transaction["category"] as? String ?: ""
                
                title.contains(query, ignoreCase = true) ||
                merchant.contains(query, ignoreCase = true) ||
                category.contains(query, ignoreCase = true)
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadTransactions(userId)
        } else {
            _transactionsState.update { 
                it.copy(isLoading = false, error = "User not logged in") 
            }
        }
    }

    /**
     * Charger les transactions depuis Firebase
     */
    fun loadTransactions(userId: String) {
        viewModelScope.launch {
            _transactionsState.update { it.copy(isLoading = true) }

            try {
                // Combine transactions and statistics
                val transactionsFlow = firebaseDataManager.getUserTransactions(userId, limit = 100)
                val statisticsFlow = firebaseDataManager.getUserStatistics(userId)

                combine(transactionsFlow, statisticsFlow) { transactions, stats ->
                    val totalIncome = stats["totalIncome"] as? Double ?: 0.0
                    val totalExpense = stats["totalExpense"] as? Double ?: 0.0

                    TransactionsUiState(
                        isLoading = false,
                        transactions = transactions,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        error = null
                    )
                }.collect { newState ->
                    _transactionsState.value = newState
                }

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading transactions", e)
                _transactionsState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load transactions: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Filtrer par type (Income/Expense/All)
     */
    fun filterByType(type: String) {
        _selectedFilter.value = type
    }

    /**
     * Recherche par texte
     */
    fun search(query: String) {
        _searchQuery.value = query
    }

    /**
     * Filtrer par période de temps
     */
    fun filterByDatePeriod(period: DatePeriod) {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            _transactionsState.update { it.copy(isLoading = true) }

            try {
                val (startDate, endDate) = getDateRange(period)
                
                val transactions = firebaseDataManager.getTransactionsByDateRange(
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate
                ).first()

                val totalIncome = transactions.filter { 
                    it["type"] == "INCOME" 
                }.sumOf { it["amount"] as? Double ?: 0.0 }
                
                val totalExpense = transactions.filter { 
                    it["type"] == "EXPENSE" 
                }.sumOf { it["amount"] as? Double ?: 0.0 }

                _transactionsState.update {
                    it.copy(
                        isLoading = false,
                        transactions = transactions,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _transactionsState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to filter by date: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Rafraîchir les transactions
     */
    fun refreshTransactions() {
        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            viewModelScope.launch {
                _isRefreshing.value = true
                loadTransactions(userId)
                kotlinx.coroutines.delay(1000)
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Réinitialiser les filtres
     */
    fun resetFilters() {
        _selectedFilter.value = "All"
        _searchQuery.value = ""
        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadTransactions(userId)
        }
    }

    /**
     * Obtenir les transactions de date à date
     */
    private suspend fun getTransactionsByDateRange(
        userId: String,
        startDate: java.util.Date,
        endDate: java.util.Date
    ): List<Map<String, Any>> {
        return firebaseDataManager.getUserTransactions(userId, limit = 1000).first()
            .filter { transaction ->
                val createdAt = transaction["createdAt"]
                val transactionDate = when (createdAt) {
                    is com.google.firebase.Timestamp -> createdAt.toDate()
                    is java.util.Date -> createdAt
                    else -> null
                }

                transactionDate != null &&
                transactionDate.after(startDate) &&
                transactionDate.before(endDate)
            }
    }

    /**
     * Obtenir la plage de dates selon la période
     */
    private fun getDateRange(period: DatePeriod): Pair<java.util.Date, java.util.Date> {
        val now = java.util.Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = now

        val startDate: java.util.Date

        when (period) {
            DatePeriod.Today -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                startDate = calendar.time
            }
            DatePeriod.ThisWeek -> {
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                startDate = calendar.time
            }
            DatePeriod.ThisMonth -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.time
            }
            DatePeriod.ThisYear -> {
                calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                startDate = calendar.time
            }
            DatePeriod.All -> {
                calendar.set(java.util.Calendar.YEAR, 2000)
                startDate = calendar.time
            }
        }

        return Pair(startDate, now)
    }
}
```

---

#### ÉTAPE 3.2: Ajouter méthode dans FirebaseDataManager

**Fichier**: `app/src/main/java/com/example/aureus/data/remote/firebase/FirebaseDataManager.kt`

**Méthode existe déjà** (ligne 282-296) - getUserTransactions() ✅

**Pas de modification nécessaire**

---

#### ÉTAPE 3.3: Test

**Test Manual**:

1. Ouvrir app → Dashboard
2. Cliquer sur une transaction
3. Vérifier: TransactionsFullScreenFirebase s'ouvre
4. Vérifier: Transactions chargées depuis Firebase
5. Tester filtres: All, Income, Expense
6. Tester recherche par texte
7. Tester filtre par date

**Expected Result**: Transactions chargées dynamiquement depuis Firebase avec tous les filtres fonctionnels

---

## 🟡 PHASE 4: CONTACTS MANAGEMENT (2-3 jours)

### Objectif
Créer ContactManagementScreen pour gérer les contacts

---

#### ÉTAPE 4.1: Créer ContactAddEditScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/contact/ContactAddEditScreen.kt`

**Créer nouveau fichier**:

```kotlin
package com.example.aureus.ui.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactAddEditScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    contactId: String? = null, // If null, adding new contact
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ContactCategory.OTHER) }
    var isFavorite by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    val isEditMode = contactId != null

    LaunchedEffect(contactId) {
        contactId?.let { id ->
            // Load existing contact data
            viewModel.contacts.filter { it.id == id }.firstOrNull()?.let { contact ->
                name = contact.name
                phone = contact.phone
                email = contact.email ?: ""
                accountNumber = contact.accountNumber ?: ""
                category = contact.category ?: ContactCategory.OTHER
                isFavorite = contact.isFavorite
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Edit Contact" else "Add Contact",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showConfirmDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            Text(
                text = "Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contact Name") },
                shape = RoundedCornerShape(12.dp)
            )

            // Phone
            Text(
                text = "Phone Number",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("06 12 34 56 78") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )

            // Email
            Text(
                text = "Email (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("contact@email.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )

            // Account Number
            Text(
                text = "Account Number (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bank Account Number") },
                shape = RoundedCornerShape(12.dp)
            )

            // Category
            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ContactCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mark as Favorite",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryNavyBlue
                )
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SecondaryGold,
                        checkedTrackColor = SecondaryGold.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val contact = Contact(
                        id = contactId ?: "",
                        name = name,
                        phone = phone,
                        email = email.ifBlank { null },
                        accountNumber = accountNumber.ifBlank { null },
                        isFavorite = isFavorite,
                        category = category
                    )

                    if (isEditMode && contactId != null) {
                        viewModel.updateContact(contact)
                    } else {
                        viewModel.addContact(contact)
                    }
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && phone.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGold
                )
            ) {
                Text(
                    text = if (isEditMode) "Update Contact" else "Add Contact",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SemanticGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    if (isEditMode) "Contact Updated!" else "Contact Added!",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { Text("Your contact has been saved successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSaveSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)
                ) {
                    Text("Done")
                }
            },
            containerColor = NeutralWhite
        )
    }

    // Delete Confirmation Dialog
    if (showConfirmDeleteDialog && isEditMode) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = SemanticRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "Delete Contact",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text("Are you sure you want to delete this contact? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        contactId?.let { viewModel.deleteContact(it) }
                        showConfirmDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = NeutralWhite
        )
    }
}
```

---

#### ÉTAPE 4.2: Créer ContactManagementScreen

**Fichier**: `app/src/main/java/com/example/aureus/ui/contact/ContactManagementScreen.kt`

**Créer nouveau fichier**:

```kotlin
package com.example.aureus.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactManagementScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onEditContact: (String) -> Unit = {},
    onAddContact: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = SecondaryGold
            ) {
                Icon(Icons.Default.PersonAdd, "Add Contact")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                }
            } else if (uiState.contacts.isEmpty()) {
                EmptyState(onAddContact = onAddContact)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.contacts) { contact ->
                        ContactListItem(
                            contact = contact,
                            onClick = { onEditContact(contact.id) },
                            onDelete = { viewModel.deleteContact(contact.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(contact) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = contact.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = contact.phone,
                        fontSize = 14.sp,
                        color = NeutralMediumGray
                    )
                    contact.email?.let { email ->
                        Text(
                            text = email,
                            fontSize = 12.sp,
                            color = NeutralMediumGray.copy(alpha = 0.7f)
                        )
                    }
                    contact.category?.let { category ->
                        Text(
                            text = category.displayName,
                            fontSize = 11.sp,
                            color = PrimaryNavyBlue.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = SemanticRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        if (contact.isFavorite) "Favorite" else "Not Favorite",
                        tint = if (contact.isFavorite) SemanticRed else NeutralMediumGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = SemanticRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Delete Contact", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this contact?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = NeutralWhite
        )
    }
}

@Composable
private fun EmptyState(onAddContact: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ContactPhone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NeutralMediumGray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Contacts Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first contact to get started",
                fontSize = 14.sp,
                color = NeutralMediumGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddContact,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Contact")
            }
        }
    }
}
```

---

#### ÉTAPE 4.3: Ajouter ContactViewModel Methods

**Fichier**: `app/src/main/java/com/example/aureus/ui/contact/viewmodel/ContactViewModel.kt`

**Ajouter méthodes**:

```kotlin
/**
 * Marquer/retirer favori
 */
fun toggleFavorite(contact: Contact) {
    viewModelScope.launch {
        try {
            val userId = firebaseDataManager.currentUserId()
            if (userId != null) {
                firebaseDataManager.updateContact(
                    userId = userId,
                    contactId = contact.id,
                    updates = mapOf("isFavorite" to !contact.isFavorite)
                )
            }
        } catch (e: Exception) {
            Log.e("ContactViewModel", "Error toggling favorite", e)
        }
    }
}
```

---

#### ÉTAPE 4.4: Ajouter Routes Navigation

**Fichier**: `app/src/main/java/com/example/aureus/ui/navigation/Navigation.kt`

**Ajouter dans sealed class Screen**:

```kotlin
object ContactManagement : Screen("contact_management")
object ContactAddEdit : Screen("contact_add_edit/{contactId}")
```

**Ajouter composable**:

```kotlin
// Contact Management Screen
composable(Screen.ContactManagement.route) {
    ContactManagementScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        onEditContact = { contactId ->
            navController.navigate("${Screen.ContactAddEdit.route}/$contactId")
        },
        onAddContact = {
            navController.navigate("${Screen.ContactAddEdit.route}")
        }
    )
}

// Contact Add/Edit Screen
composable(
    route = Screen.ContactAddEdit.route,
    arguments = listOf(
        navArgument("contactId") { type = NavType.StringType; defaultValue = "" }
    )
) { backStackEntry ->
    val contactId = backStackEntry.arguments?.getString("contactId")?.takeIf { it.isNotBlank() }
    
    ContactAddEditScreen(
        contactId = contactId,
        onNavigateBack = {
            navController.popBackStack()
        },
        onSaveSuccess = {
            navController.popBackStack()
        }
    )
}
```

---

#### ÉTAPE 4.5: Test

**Test Manual**:

1. Dashboard → Profile → Contacts
2. ContactManagementScreen s'affiche
3. Cliquer "Add Contact" (+ FAB)
4. Remplir formulaire: Name, Phone, Email, Category
5. Toggle Favorite
6. Cliquer "Add Contact"
7. Vérifier: Contact ajouté dans liste
8. Vérifier dans Firebase Console: Contact dans `users/{userId}/contacts`
9. Cliquer sur contact → Edit contact
10. Modifier et sauvegarder
11. Vérifier: Contact mis à jour
12. Cliquer Delete → Confirmer
13. Vérifier: Contact supprimé

---

## 🟡 PHASE 5: TRANSACTION DETAILS FIREBASE (1-2 jours)

---

#### ÉTAPE 5.1: Créer TransactionDetailScreenFirebase

**Fichier**: `app/src/main/java/com/example/aureus/ui/transactions/TransactionDetailScreenFirebase.kt`

**Créer nouveau fichier**:

```kotlin
package com.example.aureus.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreenFirebase(
    transactionId: String = "",
    onNavigateBack: () -> Unit = {}
) {
    var transaction by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load transaction details
    LaunchedEffect(transactionId) {
        if (transactionId.isNotBlank()) {
            isLoading = true
            // TODO: Load from FirebaseDataManager or TransactionViewModelFirebase
            // For now, using placeholder
            isLoading = false
            transaction = mapOf(
                "transactionId" to transactionId,
                "type" to "EXPENSE",
                "category" to "Shopping",
                "title" to "Purchase",
                "description" to "Sample transaction",
                "amount" to 150.0,
                "merchant" to "Merchant Name",
                "createdAt" to Timestamp.now(),
                "status" to "COMPLETED"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SecondaryGold)
            }
        } else {
            transaction?.let { trx ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralLightGray)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Amount Card
                    val type = trx["type"] as? String ?: "EXPENSE"
                    val amount = trx["amount"] as? Double ?: 0.0
                    val amountColor = if (type == "INCOME") SemanticGreen else SemanticRed

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            if (type == "INCOME") 
                                SemanticGreen.copy(alpha = 0.1f) 
                            else SemanticRed.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (type == "INCOME") "Received" else "Paid",
                                fontSize = 14.sp,
                                color = if (type == "INCOME") SemanticGreen else SemanticRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${if (type == "INCOME") "+" else ""}${formatCurrency(amount)}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            )
                        }
                    }

                    // Transaction Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Transaction Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavyBlue
                            )
                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            TransactionInfoRow("Title", trx["title"] as? String ?: "")
                            TransactionInfoRow("Category", trx["category"] as? String ?: "")
                            TransactionInfoRow("Merchant", trx["merchant"] as? String ?: "")
                            TransactionInfoRow("Status", trx["status"] as? String ?: "")
                        }
                    }

                    // Date & Time
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            val createdAt = trx["createdAt"]
                            val dateStr = when (createdAt) {
                                is Timestamp -> formatDate(createdAt.toDate())
                                is Date -> formatDate(createdAt)
                                else -> "Unknown"
                            }
                            TransactionInfoRow("Date & Time", dateStr)
                        }
                    }

                    // Description
                    val description = trx["description"] as? String
                    if (!description.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    fontSize = 14.sp,
                                    color = NeutralMediumGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = description,
                                    fontSize = 16.sp,
                                    color = PrimaryNavyBlue
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Share Receipt */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share")
                        }
                        Button(
                            onClick = { /* Download Receipt */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavyBlue)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Receipt")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = NeutralMediumGray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryNavyBlue
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}
```

---

#### ÉTAPE 5.2: Modifier TransactionsFullScreenFirebase

**Fichier**: `app/src/main/java/com/example/aureus/ui/transactions/TransactionsFullScreenFirebase.kt`

**Modifier ligne 185-186** (onClick handler):

```kotlin
onClick = { 
    val transactionId = transaction["transactionId"] as? String ?: ""
    if (transactionId.isNotBlank()) {
        // Navigate to transaction detail
        /* TODO: Add navigation to TransactionDetailScreenFirebase 
           after adding route to Navigation.kt */
    }
}
```

---

#### ÉTAPE 5.3: Test

**Test Manual**:

1. Dashboard → Transactions
2. Cliquer sur une transaction
3. TransactionDetailScreenFirebase s'affiche
4. Vérifier: Tous les détails affichés (amount, date, merchant, etc.)
5. Tester boutons: Share, Download Receipt

---

## 🟢 PHASE 6: FINALISATION (1 jour)

### ÉTAPE 6.1: Corriger Null Checks

**Fichier**: `app/src/main/java/com/example/aureus/ui/transfer/SendMoneyScreenFirebase.kt`

**Modifier ligne 243-245**:

```kotlin
onClick = {
    selectedContact?.let { contact ->
        amount.toDoubleOrNull()?.let { amt ->
            onSendClick(contact, amt)
        } ?: run {
            // Show amount error
        }
    } ?: run {
        // Show contact not selected error
    }
},
```

---

### ÉTAPE 6.2: Ajouter AuthStateListener

**Fichier**: `app/src/main/java/com/example/aureus/MainActivity.kt`

**Modifier onCreate**:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        AureusTheme {
            AppNavigation(
                authViewModel = authViewModel,
                onboardingViewModel = onboardingViewModel
            )
        }
    }
    
    // Add AuthStateListener
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    auth.addAuthStateListener { firebaseAuth ->
        Log.d("MainActivity", "Auth state changed: ${firebaseAuth.currentUser != null}")
        if (firebaseAuth.currentUser == null && authViewModel.isLoggedIn) {
            // User was logged out elsewhere
            Log.d("MainActivity", "User logged out elsewhere, navigating to login")
            // Navigate to login (implementation depends on your nav setup)
        }
    }
}
```

---

### ÉTAPE 6.3: Vérifier Tous les Imports

**Commande**:

```bash
./gradlew assembleDebug
```

**Vérifier**: Tous les imports se compilent sans erreur

---

### ÉTAPE 6.4: Lint Check

**Commande**:

```bash
./gradlew lint
```

**Corriger**: Tous les warnings et errors

---

### ÉTAPE 6.5: Tests Finale

**Test Suite Complete**:

1. **Auth Flow**:
   - Email/Password Register → SMS → PIN → Dashboard ✅
   - Google Sign-In → Phone → SMS → PIN → Dashboard ✅
   - Login Email/Password → Dashboard ✅
   - Logout → Login ✅

2. **Dashboard**:
   - Balance card displays correct amount ✅
   - Quick actions work ✅
   - Recent transactions load ✅

3. **Transactions**:
   - Transactions list loads ✅
   - Filters work (All, Income, Expense) ✅
   - Search works ✅
   - Date filter works ✅
   - Transaction detail displays ✅

4. **Contacts**:
   - Send Money loads contacts ✅
   - Request Money loads contacts ✅
   - Add/Edit/Delete contacts work ✅
   - Favorites work ✅

5. **Cards**:
   - Cards list loads ✅
   - Add card works ✅
   - Default card selection works ✅

6. **Navigation**:
   - All routes work ✅
   - Back navigation works ✅
   - No broken links ✅

---

## 📊 CHECKLIST FINALE DE DÉVELOPPEMENT

### Phase 1: Google Sign-In
- [ ] Modifier AuthViewModel avec signInWithGoogleCredential
- [ ] Corriger LoginScreen Google Sign-In Launcher
- [ ] Ajouter LaunchedEffect pour navigation Google
- [ ] Tester Google Sign-In bout-en-bout
- [ ] Vérifier Firebase Console: user créé

### Phase 2: Pin Setup
- [ ] Créer PinViewModel
- [ ] Modifier PinSetupScreen avec PinViewModel
- [ ] Ajouter PinViewModel à AppModule/ViewModelModule
- [ ] Tester PIN creation et sauvegarde
- [ ] Vérifier Firebase Console: pin field dans user

### Phase 3: Transactions Firebase
- [ ] Créer TransactionViewModelFirebase
- [ ] Implémenter tous les filters (type, search, date)
- [ ] Connecter à TransactionsFullScreenFirebase
- [ ] Tester tous les filters
- [ ] Tester pull-to-refresh

### Phase 4: Contacts Management
- [ ] Créer ContactAddEditScreen
- [ ] Créer ContactManagementScreen
- [ ] Ajouter toggleFavorite à ContactViewModel
- [ ] Ajouter routes dans Navigation.kt
- [ ] Tester CRUD complet des contacts
- [ ] Vérifier Firebase Console: contacts collection

### Phase 5: Transaction Details
- [ ] Créer TransactionDetailScreenFirebase
- [ ] Connecter navigation depuis TransactionsFullScreen
- [ ] Tester affichage détails
- [ ] Tester boutons (Share, Receipt)

### Phase 6: Finalisation
- [ ] Corriger null checks dans SendMoneyScreenFirebase
- [ ] Ajouter AuthStateListener dans MainActivity
- [ ] Vérifier compilation (assembleDebug)
- [ ] Corriger tous les lints
- [ ] Tester tous les flows bout-en-bout
- [ ] Mise à jour RAPPORT_AUDIT_COMPLET.md

---

## 🎯 RÉSULTATS ATTENDUS APRÈS PLAN

Après l'exécution complète de ce plan:

| Métrique | Avant | Après |
|----------|-------|-------|
| **Authentification** | 70% | **100%** |
| **Google Sign-In** | 50% | **100%** |
| **SMS Verification** | 90% | **100%** |
| **Transactions** | 70% | **100%** |
| **Contacts** | 60% | **100%** |
| **Navigation** | 90% | **100%** |
| **SCORE GLOBAL** | 7.7/10 | **9.5/10** |

---

## 📝 NOTES IMPORTANTES

1. **Priorité Phase 1-3**: Ces phases sont critiques pour l'authentification et les transactions
2. **Testing**: Chaque phase doit être testée avant de passer à la suivante
3. **Firebase Console**: Vérifier régulièrement les données dans Firebase Console
4. **Git**: Commit après chaque phase avec des messages clairs
5. **Documentation**: Mettre à jour README.md après Phase 6

---

## 🚀 PROCHAINE ÉTAPE APRÈS PLAN

Une fois ce plan complété:

1. **Phase 7**: Implementer Offline-First complet
2. **Phase 8**: Ajouter notifications push
3. **Phase 9**: Ajouter biometric auth
4. **Phase 10**: Améliorer charts avec VICO Chart library

---

**PLAN CRÉÉ LE**: 11 Janvier 2026
**ESTIMATION**: 7-11 jours (1.5-2 semaines)
**AUTEUR**: Firebender AI Assistant
**PROJET**: Aureus Banking Application