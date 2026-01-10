# 🔐 PIN Security System - Aureus Banking App

## Vue d'ensemble

Système de sécurité par code PIN à 4 chiffres pour protéger les transactions et actions sensibles dans l'application bancaire Aureus.

---

## 📱 Écrans Implémentés

### 1. **PinSetupScreen** - Configuration du PIN
Écran de configuration initiale du code PIN en deux étapes :
- **Étape 1** : Création du code PIN (4 chiffres)
- **Étape 2** : Confirmation du code PIN
- Validation que les deux codes correspondent
- Feedback visuel et haptique

**Navigation :**
```
Register → SMS Verification → PIN Setup → Login
```

### 2. **PinVerificationScreen** - Vérification plein écran
Écran complet de vérification du PIN pour les opérations majeures.

**Utilisation :**
```kotlin
PinVerificationScreen(
    onPinVerified = { /* Action après vérification */ },
    onCancel = { /* Annulation */ },
    title = "Confirmer le transfert",
    subtitle = "Transférer 5000 MAD",
    correctPin = "1234" // À récupérer depuis un stockage sécurisé
)
```

### 3. **PinVerificationDialog** - Dialog de vérification
Dialog modal pour les actions sensibles ponctuelles.

**Utilisation :**
```kotlin
if (showPinDialog) {
    PinVerificationDialog(
        onPinVerified = { /* Action sécurisée */ },
        onDismiss = { showPinDialog = false },
        title = "Confirmer l'opération",
        subtitle = "Entrez votre code PIN"
    )
}
```

---

## 🎨 Design & Animations

### Palette de couleurs
- **Background** : Dégradé Navy Blue (matching splash screen)
- **Accent** : Gold (#D4AF37)
- **Success** : Green (#10B981)
- **Error** : Red (#EF4444)

### Animations incluses
- ✨ Cercle tournant autour de l'icône de cadenas
- 📍 Points pulsants pour indiquer la position active
- 🔄 Animation de secousse en cas d'erreur
- ✅ Animation de succès avec mise à l'échelle
- 📱 Feedback haptique sur chaque touche

### Caractéristiques UX
- Clavier numérique optimisé (0-9 + Backspace)
- Auto-validation à 4 chiffres
- Compteur de tentatives
- Lien "Code PIN oublié"
- États visuels clairs (focus, erreur, succès)

---

## 🛠️ Composants Utilitaires

### 1. PinProtectedAction
Wrapper simple pour protéger une action par PIN.

**Exemple :**
```kotlin
var showPinDialog by remember { mutableStateOf(false) }

Button(onClick = { showPinDialog = true }) {
    Text("Action Sensible")
}

PinProtectedAction(
    showDialog = showPinDialog,
    onDismiss = { showPinDialog = false },
    title = "Confirmer",
    subtitle = "Entrez votre PIN",
    onSuccess = {
        showPinDialog = false
        // Exécuter l'action protégée
        performSensitiveAction()
    }
)
```

### 2. PinProtectedActionState
État pour gérer plusieurs actions protégées par PIN.

**Exemple complet :**
```kotlin
@Composable
fun TransferScreen() {
    val pinState = rememberPinProtectedActionState()
    
    Button(onClick = {
        pinState.requestPin(
            title = "Confirmer le transfert",
            subtitle = "Transférer 5000 MAD"
        ) {
            // Code exécuté après vérification réussie
            performTransfer()
            showSuccessMessage()
        }
    }) {
        Text("Transférer")
    }
    
    // Handler unique pour tous les dialogs PIN
    PinProtectedActionHandler(state = pinState)
}
```

---

## 📂 Structure des Fichiers

```
app/src/main/java/com/example/aureus/
├── ui/auth/screen/
│   ├── PinSetupScreen.kt              # Configuration initiale du PIN
│   ├── PinVerificationScreen.kt       # Vérification du PIN
│   └── PinScreensPreviews.kt          # Previews Android Studio
│
├── ui/components/
│   ├── PinProtectedAction.kt          # Composants utilitaires
│   └── PinProtectedActionExample.kt   # Exemples d'utilisation
│
└── ui/navigation/
    └── Navigation.kt                   # Intégration navigation
```

---

## 🔒 Sécurité

### ⚠️ Important - Code statique pour démo
Les écrans utilisent actuellement un PIN statique (`"1234"`) pour la démonstration.

### 🎯 À implémenter en production :

1. **Stockage sécurisé du PIN**
```kotlin
// Utiliser Android Keystore ou EncryptedSharedPreferences
object PinManager {
    fun savePinSecurely(pin: String)
    fun verifyPin(pin: String): Boolean
    fun isPinSet(): Boolean
}
```

2. **Hachage du PIN**
```kotlin
// Ne jamais stocker le PIN en clair
fun hashPin(pin: String): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(pin.toByteArray())
        .toHexString()
}
```

3. **Limite de tentatives**
```kotlin
// Implémenter un verrouillage après X tentatives
class PinAttemptManager {
    private var failedAttempts = 0
    private val maxAttempts = 3
    private var lockoutUntil: Long? = null
    
    fun recordFailedAttempt()
    fun isLockedOut(): Boolean
    fun getRemainingTime(): Long
}
```

4. **Biométrie en option**
```kotlin
// Ajouter Touch ID / Face ID comme alternative
BiometricPrompt.authenticate(
    onSuccess = { /* Bypass PIN */ },
    onError = { /* Fallback to PIN */ }
)
```

---

## 🔄 Flux de Navigation

### Premier lancement (Inscription)
```
1. Splash Screen
2. Onboarding
3. Register (Inscription)
4. SMS Verification (Code à 6 chiffres)
5. PIN Setup (Création du code PIN)
6. Login
7. Dashboard
```

### Lancement suivant (Connexion)
```
1. Splash Screen
2. Login
3. Dashboard
```

### Actions sécurisées dans l'app
```
Dashboard → Action sensible → PIN Verification Dialog → Action exécutée
```

---

## 📋 Exemples de Cas d'Usage

### 1. Transfert d'argent
```kotlin
Button(onClick = {
    pinState.requestPin(
        title = "Confirmer le transfert",
        subtitle = "Transférer 5000 MAD à Mohammed ALAMI"
    ) {
        transferMoney(amount = 5000, to = "Mohammed ALAMI")
    }
})
```

### 2. Modification des paramètres
```kotlin
Button(onClick = {
    pinState.requestPin(
        title = "Modifier les informations",
        subtitle = "Confirmez votre identité"
    ) {
        updateUserSettings()
    }
})
```

### 3. Ajout de bénéficiaire
```kotlin
Button(onClick = {
    pinState.requestPin(
        title = "Ajouter un bénéficiaire",
        subtitle = "Sécurisez cette action"
    ) {
        addBeneficiary(name, rib)
    }
})
```

### 4. Changement de limite
```kotlin
Button(onClick = {
    pinState.requestPin(
        title = "Modifier la limite",
        subtitle = "Nouvelle limite: 10000 MAD/jour"
    ) {
        updateDailyLimit(10000)
    }
})
```

---

## 🧪 Tests

### Code PIN de test
**PIN statique pour la démo : `1234`**

### Scenarios de test
1. ✅ Création de PIN avec confirmation identique
2. ❌ Création de PIN avec confirmation différente
3. ✅ Vérification avec PIN correct
4. ❌ Vérification avec PIN incorrect
5. 🔄 Plusieurs tentatives échouées
6. ↩️ Navigation arrière pendant la configuration

---

## 🎯 Roadmap

### Phase 1 - ✅ Complété
- [x] Écran de configuration du PIN
- [x] Écran de vérification plein écran
- [x] Dialog de vérification
- [x] Composants utilitaires
- [x] Intégration navigation
- [x] Design matching l'app

### Phase 2 - 🔜 À venir
- [ ] Stockage sécurisé (Keystore)
- [ ] Hachage du PIN
- [ ] Limite de tentatives avec verrouillage
- [ ] Biométrie (Touch ID / Face ID)
- [ ] Changement de PIN
- [ ] Récupération de PIN oublié
- [ ] Tests unitaires
- [ ] Analytics (tentatives échouées)

---

## 💡 Notes Techniques

### Performance
- Animations optimisées avec `remember` et `LaunchedEffect`
- Pas de recomposition inutile
- Feedback haptique léger

### Accessibilité
- Labels clairs pour screen readers
- Contraste élevé des couleurs
- Taille des boutons adaptée (70dp minimum)
- Feedback visuel en plus de l'haptique

### Compatibilité
- Android 8.0+ (API 26+)
- Compose Material 3
- Mode sombre/clair supporté via theme

---

## 📞 Support

Pour toute question ou suggestion concernant le système PIN :
- Consulter les exemples dans `PinProtectedActionExample.kt`
- Voir les previews dans Android Studio
- Tester le flux complet depuis le register

---

**Développé avec ❤️ pour Aureus Banking App**
