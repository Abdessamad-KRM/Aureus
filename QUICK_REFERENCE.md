# ⚡ Quick Reference - Animations Lottie

Guide de référence rapide pour utiliser les animations Lottie dans l'application Aureus.

## 🎨 Palette de Couleurs

```kotlin
import com.example.aureus.ui.theme.*

val primary = NavyBlue    // #1B365D
val secondary = Gold      // #D4AF37
val accent = White        // #FFFFFF
val background = LightGray // #F5F5F5
```

## 📦 Import des Animations

```kotlin
import com.example.aureus.ui.components.LottieUrls
import com.example.aureus.ui.components.*
```

## 🎭 Animations Disponibles

### URLs Rapides

```kotlin
// Onboarding
LottieUrls.SECURITY
LottieUrls.ACCOUNTS
LottieUrls.NOTIFICATIONS

// Empty States
LottieUrls.EMPTY_LIST_FALLBACK
LottieUrls.EMPTY_DATA_FALLBACK
LottieUrls.NO_TRANSACTIONS
LottieUrls.NO_CARDS

// Feedback
LottieUrls.SUCCESS
LottieUrls.ERROR
LottieUrls.WARNING

// Loading
LottieUrls.LOADING
LottieUrls.PROCESSING

// Auth
LottieUrls.FINGERPRINT
LottieUrls.FACE_ID

// Features
LottieUrls.WALLET
LottieUrls.TRANSFER
LottieUrls.ANALYTICS
```

## 🚀 Utilisation Rapide

### 1. Animation Simple

```kotlin
SimpleLottieAnimation(
    url = LottieUrls.LOADING,
    modifier = Modifier.size(120.dp)
)
```

### 2. Empty State

```kotlin
EmptyStateView(
    message = "Aucune donnée disponible",
    actionText = "Recharger",
    onActionClick = { /* Action */ },
    animationUrl = LottieUrls.EMPTY_DATA_FALLBACK
)
```

### 3. Loading

```kotlin
if (isLoading) {
    LoadingView(message = "Chargement...")
}
```

### 4. Success/Error

```kotlin
if (showSuccess) {
    SuccessView(
        message = "Opération réussie !",
        onDismiss = { showSuccess = false }
    )
}

if (showError) {
    ErrorView(
        message = "Une erreur est survenue",
        onDismiss = { showError = false }
    )
}
```

### 5. Processing

```kotlin
if (isProcessing) {
    ProcessingTransactionView(
        message = "Traitement en cours..."
    )
}
```

## 🎯 Cas d'Usage Courants

### Liste Vide de Transactions

```kotlin
if (transactions.isEmpty()) {
    EmptyStateView(
        message = "Aucune transaction",
        actionText = "Effectuer une transaction",
        onActionClick = { navigateToTransfer() },
        animationUrl = LottieUrls.NO_TRANSACTIONS
    )
}
```

### Liste Vide de Cartes

```kotlin
if (cards.isEmpty()) {
    EmptyStateView(
        message = "Aucune carte enregistrée",
        actionText = "Ajouter une carte",
        onActionClick = { navigateToAddCard() },
        animationUrl = LottieUrls.NO_CARDS
    )
}
```

### Chargement de Données

```kotlin
LaunchedEffect(Unit) {
    viewModel.loadData()
}

when (uiState) {
    is UiState.Loading -> LoadingView()
    is UiState.Success -> ShowData(uiState.data)
    is UiState.Error -> ErrorView(
        message = uiState.message,
        onDismiss = { viewModel.retry() }
    )
}
```

### Transaction Flow

```kotlin
var state by remember { mutableStateOf(TransactionState.IDLE) }

when (state) {
    TransactionState.PROCESSING -> {
        ProcessingTransactionView()
    }
    TransactionState.SUCCESS -> {
        SuccessView(
            message = "Transaction réussie !",
            onDismiss = { state = TransactionState.IDLE }
        )
    }
    TransactionState.ERROR -> {
        ErrorView(
            message = "Transaction échouée",
            onDismiss = { state = TransactionState.IDLE }
        )
    }
}
```

## 🔧 Personnalisation Avancée

### Animation Contrôlée

```kotlin
val composition by rememberLottieComposition(
    LottieCompositionSpec.Url(LottieUrls.SUCCESS)
)
val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = 1, // Une seule fois
    speed = 2f // 2x plus rapide
)

LottieAnimation(
    composition = composition,
    progress = { progress },
    modifier = Modifier.size(200.dp)
)
```

### Animation avec État

```kotlin
var isPlaying by remember { mutableStateOf(false) }

val progress by animateLottieCompositionAsState(
    composition = composition,
    isPlaying = isPlaying,
    iterations = LottieConstants.IterateForever
)

Button(onClick = { isPlaying = !isPlaying }) {
    Text(if (isPlaying) "Pause" else "Play")
}
```

## 📝 Patterns Recommandés

### Loading State

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingView()
            uiState.data.isEmpty() -> EmptyStateView(
                message = "Aucune donnée",
                actionText = "Recharger",
                onActionClick = { viewModel.reload() }
            )
            else -> ShowContent(uiState.data)
        }
    }
}
```

### Dialog avec Animation

```kotlin
if (showDialog) {
    Dialog(onDismissRequest = { showDialog = false }) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleLottieAnimation(
                    url = LottieUrls.SUCCESS,
                    modifier = Modifier.size(120.dp)
                )
                Text("Opération réussie!")
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        }
    }
}
```

### Bottom Sheet avec Animation

```kotlin
ModalBottomSheet(onDismissRequest = { /* ... */ }) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleLottieAnimation(
            url = LottieUrls.WARNING,
            modifier = Modifier.size(100.dp)
        )
        Text("Attention!")
        Text("Votre solde est faible")
    }
}
```

## 🎨 Styling Tips

### Avec Background

```kotlin
Box(
    modifier = Modifier
        .size(200.dp)
        .clip(CircleShape)
        .background(NavyBlue.copy(alpha = 0.1f)),
    contentAlignment = Alignment.Center
) {
    SimpleLottieAnimation(
        url = LottieUrls.WALLET,
        modifier = Modifier.size(160.dp)
    )
}
```

### Avec Ombre

```kotlin
Card(
    elevation = CardDefaults.cardElevation(8.dp),
    modifier = Modifier.size(200.dp)
) {
    SimpleLottieAnimation(
        url = LottieUrls.TRANSFER,
        modifier = Modifier.fillMaxSize()
    )
}
```

## 🔍 Debugging

### Vérifier si l'animation charge

```kotlin
val composition by rememberLottieComposition(
    LottieCompositionSpec.Url(url)
)

LaunchedEffect(composition) {
    if (composition == null) {
        Log.e("Lottie", "Failed to load animation from: $url")
    } else {
        Log.d("Lottie", "Animation loaded successfully")
    }
}
```

### Fallback si l'animation échoue

```kotlin
val composition by rememberLottieComposition(
    LottieCompositionSpec.Url(url)
)

if (composition != null) {
    LottieAnimation(composition = composition, ...)
} else {
    // Fallback UI
    Icon(Icons.Default.Error, contentDescription = null)
}
```

## 📱 Performance Tips

1. **Réutiliser les compositions**
```kotlin
// ❌ Mauvais - charge à chaque recomposition
@Composable
fun Bad() {
    SimpleLottieAnimation(url = LottieUrls.LOADING)
}

// ✅ Bon - cache la composition
val composition = rememberLottieComposition(...)
```

2. **Limiter les animations simultanées**
```kotlin
// Évitez d'avoir trop d'animations en même temps
// Max 2-3 animations Lottie simultanées
```

3. **Désactiver quand invisible**
```kotlin
val isVisible = remember { mutableStateOf(true) }

if (isVisible.value) {
    SimpleLottieAnimation(url = url)
}
```

## 🎯 Exemples Complets

Voir `AnimationExamples.kt` pour des exemples complets et prêts à l'emploi.

## 📚 Documentation

- **Setup complet**: `ONBOARDING_SETUP.md`
- **Liste animations**: `LOTTIE_ANIMATIONS.md`
- **Installation**: `INSTALLATION_STEPS.md`
- **Flow diagram**: `ONBOARDING_FLOW.txt`

## ⚡ Commandes Rapides

```bash
# Sync Gradle
./gradlew --refresh-dependencies

# Clean build
./gradlew clean build

# Install debug
./gradlew installDebug

# Clear app data (pour revoir l'onboarding)
adb shell pm clear com.example.aureus
```

---

**Tip**: Gardez ce fichier ouvert pendant le développement pour un accès rapide aux composants! 🚀
