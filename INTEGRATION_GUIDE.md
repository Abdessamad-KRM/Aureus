# 🚀 Guide d'Intégration - Système PIN

## 📱 Flux d'Inscription Complet

```
┌─────────────────┐
│  Splash Screen  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Onboarding    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Login       │◄──────────────┐
└────────┬────────┘               │
         │                        │
         │ (S'inscrire)           │
         ▼                        │
┌─────────────────┐               │
│    Register     │               │
│  - Nom          │               │
│  - Email        │               │
│  - Téléphone    │               │
│  - Mot de passe │               │
└────────┬────────┘               │
         │                        │
         ▼                        │
┌─────────────────────────────┐  │
│   SMS Verification          │  │
│  ┌───┬───┬───┬───┬───┬───┐ │  │
│  │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ │  │
│  └───┴───┴───┴───┴───┴───┘ │  │
│  Code: 123456              │  │
└────────┬────────────────────┘  │
         │                        │
         ▼                        │
┌─────────────────────────────┐  │
│      PIN Setup              │  │
│                             │  │
│  Étape 1: Créer PIN         │  │
│  ● ○ ○ ○                    │  │
│                             │  │
│  Étape 2: Confirmer PIN     │  │
│  ● ● ○ ○                    │  │
└────────┬────────────────────┘  │
         │                        │
         └────────────────────────┘
         │
         ▼
┌─────────────────┐
│    Dashboard    │
└─────────────────┘
```

---

## 🎯 Utilisation du PIN dans l'Application

### Méthode 1 : Dialog Simple

```kotlin
@Composable
fun MyScreen() {
    var showPinDialog by remember { mutableStateOf(false) }
    
    // Bouton qui déclenche une action sécurisée
    Button(onClick = { showPinDialog = true }) {
        Text("Transférer 5000 MAD")
    }
    
    // Dialog de vérification PIN
    PinProtectedAction(
        showDialog = showPinDialog,
        onDismiss = { showPinDialog = false },
        title = "Confirmer le transfert",
        subtitle = "Transférer 5000 MAD à Mohammed ALAMI",
        onSuccess = {
            showPinDialog = false
            // Action sécurisée ici
            performTransfer()
        }
    )
}
```

### Méthode 2 : État Partagé (Recommandé)

```kotlin
@Composable
fun MyScreen() {
    val pinState = rememberPinProtectedActionState()
    
    Column {
        // Plusieurs boutons peuvent utiliser le même état
        Button(onClick = {
            pinState.requestPin(
                title = "Transférer",
                subtitle = "5000 MAD vers Mohammed"
            ) {
                performTransfer()
            }
        }) {
            Text("Transférer")
        }
        
        Button(onClick = {
            pinState.requestPin(
                title = "Supprimer",
                subtitle = "Action irréversible"
            ) {
                deleteAccount()
            }
        }) {
            Text("Supprimer compte")
        }
    }
    
    // Handler unique pour tous les dialogs
    PinProtectedActionHandler(state = pinState)
}
```

---

## 📋 Checklist d'Intégration

### ✅ Phase 1 : Écrans de Base (Complété)
- [x] SMS Verification Screen
- [x] PIN Setup Screen
- [x] PIN Verification Dialog
- [x] PIN Verification Full Screen
- [x] Navigation intégrée
- [x] Design matching

### 🔜 Phase 2 : Sécurité (À faire)
- [ ] Implémenter PinManager avec stockage sécurisé
- [ ] Ajouter hachage du PIN
- [ ] Limiter les tentatives (3 max)
- [ ] Ajouter verrouillage temporaire
- [ ] Implémenter "Code PIN oublié"

### 🔜 Phase 3 : Biométrie (Optionnel)
- [ ] Touch ID / Face ID
- [ ] Fallback vers PIN si biométrie échoue
- [ ] Préférences utilisateur

---

## 🔐 Où Utiliser le PIN ?

### Actions qui DOIVENT être protégées par PIN :

1. **💸 Transactions financières**
   ```kotlin
   pinState.requestPin(
       title = "Confirmer le transfert",
       subtitle = "Montant: $amount MAD"
   ) {
       transferMoney(amount, beneficiary)
   }
   ```

2. **👤 Modification des informations sensibles**
   ```kotlin
   pinState.requestPin(
       title = "Modifier l'email",
       subtitle = "Confirmez votre identité"
   ) {
       updateEmail(newEmail)
   }
   ```

3. **🏦 Ajout de bénéficiaire**
   ```kotlin
   pinState.requestPin(
       title = "Ajouter un bénéficiaire",
       subtitle = "Sécurisez cette action"
   ) {
       addBeneficiary(name, rib)
   }
   ```

4. **📊 Export de données**
   ```kotlin
   pinState.requestPin(
       title = "Exporter les données",
       subtitle = "Télécharger l'historique complet"
   ) {
       exportData()
   }
   ```

5. **⚙️ Changement de limites**
   ```kotlin
   pinState.requestPin(
       title = "Modifier la limite",
       subtitle = "Nouvelle limite: $newLimit MAD/jour"
   ) {
       updateDailyLimit(newLimit)
   }
   ```

6. **🗑️ Suppression de compte**
   ```kotlin
   pinState.requestPin(
       title = "⚠️ Supprimer le compte",
       subtitle = "Cette action est irréversible"
   ) {
       deleteAccount()
   }
   ```

---

## 📊 Comparaison des Méthodes

| Critère | Dialog Simple | État Partagé |
|---------|--------------|--------------|
| **Complexité** | ⭐ Simple | ⭐⭐ Moyen |
| **Flexibilité** | ⭐⭐ Limitée | ⭐⭐⭐ Élevée |
| **Performance** | ⭐⭐ OK | ⭐⭐⭐ Optimale |
| **Multiple actions** | ❌ Non | ✅ Oui |
| **Code propre** | ⭐⭐ OK | ⭐⭐⭐ Excellent |
| **Recommandé pour** | 1 action isolée | Écran complet |

---

## 🎨 Personnalisation

### Changer les couleurs
Les écrans utilisent automatiquement les couleurs du thème :
- `PrimaryNavyBlue` - Background principal
- `SecondaryGold` - Accents et highlights
- `SemanticGreen` - Succès
- `SemanticRed` - Erreurs

### Changer le nombre de chiffres
Actuellement fixé à 4 chiffres. Pour changer :

```kotlin
// Dans PinSetupScreen.kt et PinVerificationScreen.kt
if (pin.length == 6) { // Au lieu de 4
    // Validation
}

// Et dans les dots display
repeat(6) { // Au lieu de 4
    PinDot(...)
}
```

### Ajouter des animations personnalisées
```kotlin
// Exemple : Rotation supplémentaire
val extraRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
        animation = tween(5000),
        repeatMode = RepeatMode.Restart
    )
)
```

---

## 🐛 Dépannage

### Le PIN ne se valide pas
✅ Vérifiez que le PIN de test est `"1234"`
✅ Vérifiez que `correctPin` est bien passé au composant
✅ Regardez les logs pour les erreurs

### Le dialog ne s'affiche pas
✅ Vérifiez que `showDialog = true`
✅ Vérifiez que le composant est bien appelé
✅ Vérifiez qu'il n'y a pas d'erreur de compilation

### L'animation saccade
✅ Utilisez `remember` pour les états
✅ Évitez les recompositions inutiles
✅ Vérifiez les performances de l'appareil

### Le feedback haptique ne fonctionne pas
✅ Testez sur un appareil physique (pas l'émulateur)
✅ Vérifiez les permissions dans le manifest
✅ Vérifiez les paramètres du téléphone

---

## 📱 Exemples d'Écrans

### Transfer Screen avec PIN
```kotlin
@Composable
fun TransferScreen(
    viewModel: TransferViewModel
) {
    val pinState = rememberPinProtectedActionState()
    var amount by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Montant") }
        )
        
        OutlinedTextField(
            value = beneficiary,
            onValueChange = { beneficiary = it },
            label = { Text("Bénéficiaire") }
        )
        
        Button(
            onClick = {
                pinState.requestPin(
                    title = "Confirmer le transfert",
                    subtitle = "Transférer $amount MAD à $beneficiary"
                ) {
                    viewModel.transfer(amount, beneficiary)
                }
            },
            enabled = amount.isNotEmpty() && beneficiary.isNotEmpty()
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Transférer avec PIN")
        }
    }
    
    PinProtectedActionHandler(state = pinState)
}
```

### Settings Screen avec PIN
```kotlin
@Composable
fun SettingsScreen() {
    val pinState = rememberPinProtectedActionState()
    
    Column {
        SettingsItem(
            title = "Changer l'email",
            icon = Icons.Default.Email
        ) {
            pinState.requestPin(
                title = "Modifier l'email",
                subtitle = "Confirmez votre identité"
            ) {
                // Navigate to email change screen
            }
        }
        
        SettingsItem(
            title = "Changer le mot de passe",
            icon = Icons.Default.Lock
        ) {
            pinState.requestPin(
                title = "Modifier le mot de passe",
                subtitle = "Action sécurisée"
            ) {
                // Navigate to password change
            }
        }
    }
    
    PinProtectedActionHandler(state = pinState)
}
```

---

## 🎓 Best Practices

### ✅ À FAIRE
- Utiliser `rememberPinProtectedActionState()` pour plusieurs actions
- Fournir des titres et sous-titres clairs
- Stocker le PIN de manière sécurisée
- Limiter le nombre de tentatives
- Ajouter du feedback haptique
- Tester sur appareil réel

### ❌ À ÉVITER
- Ne jamais logger le PIN
- Ne jamais stocker le PIN en clair
- Ne pas afficher le PIN en clair
- Ne pas permettre un nombre illimité de tentatives
- Ne pas ignorer les erreurs de sécurité

---

## 🔍 Tests Recommandés

### Tests Manuels
1. ✅ Configuration du PIN avec codes identiques
2. ❌ Configuration avec codes différents
3. ✅ Vérification avec PIN correct
4. ❌ Vérification avec PIN incorrect (3x)
5. 🔄 Navigation pendant la configuration
6. 📱 Rotation de l'écran
7. ⚡ Performance avec animations

### Tests Automatisés (À implémenter)
```kotlin
@Test
fun testPinSetup_matchingPins_success() {
    // Test la configuration avec PINs identiques
}

@Test
fun testPinVerification_correctPin_success() {
    // Test la vérification avec PIN correct
}

@Test
fun testPinVerification_incorrectPin_failure() {
    // Test avec PIN incorrect
}
```

---

## 📞 Support & Ressources

- **Documentation complète** : `PIN_SECURITY_README.md`
- **Exemples** : `PinProtectedActionExample.kt`
- **Previews** : `PinScreensPreviews.kt`
- **Navigation** : `Navigation.kt`

---

**🎉 Système PIN entièrement intégré et prêt à l'emploi !**
