# 📋 Étapes d'Installation - Onboarding & Animations Lottie

## ⚠️ Important - Sync Gradle Requis

Les erreurs de linter actuelles sont normales et seront résolues après la synchronisation Gradle.

## 🔧 Étapes d'Installation

### 1. Synchroniser Gradle

**Dans Android Studio:**

1. Cliquez sur **File** → **Sync Project with Gradle Files**
   
   OU
   
2. Cliquez sur l'icône **Sync Now** qui apparaît en haut de l'éditeur
   
   OU
   
3. Utilisez le raccourci: `Cmd + Shift + A` (Mac) ou `Ctrl + Shift + A` (Windows/Linux)
   - Tapez "sync gradle"
   - Sélectionnez "Sync Project with Gradle Files"

**Durée estimée**: 30 secondes à 2 minutes selon votre connexion internet

### 2. Vérifier l'Installation

Après le sync Gradle, vérifiez que:
- ✅ Aucune erreur de compilation dans `OnboardingScreen.kt`
- ✅ Aucune erreur de compilation dans `LottieAnimations.kt`
- ✅ Les imports de `com.airbnb.lottie.compose.*` sont reconnus

### 3. Build l'Application

```bash
./gradlew clean build
```

Ou dans Android Studio:
- **Build** → **Clean Project**
- **Build** → **Rebuild Project**

### 4. Lancer l'Application

```bash
./gradlew installDebug
```

Ou dans Android Studio:
- Cliquez sur le bouton **Run** (▶️)
- Ou utilisez `Shift + F10` (Windows/Linux) ou `Ctrl + R` (Mac)

## 📱 Tester l'Onboarding

### Premier Lancement
L'onboarding s'affichera automatiquement au premier lancement de l'application.

### Tester à Nouveau

Si vous voulez revoir l'onboarding:

**Option 1: Supprimer les données de l'app**
```bash
adb shell pm clear com.example.aureus
```

**Option 2: Dans le code**

Ajoutez temporairement dans `MainActivity.onCreate()`:
```kotlin
// Pour test uniquement - à retirer après
sharedPreferencesManager.setOnboardingCompleted(false)
```

## 🔍 Vérifications Post-Installation

### Vérifier que Lottie est bien installé

Créez un test simple dans n'importe quel écran:

```kotlin
import com.airbnb.lottie.compose.*

@Composable
fun TestLottie() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(
            "https://assets8.lottiefiles.com/packages/lf20_yyytpim5.json"
        )
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(200.dp)
    )
}
```

Si cela compile et s'exécute sans erreur, Lottie est correctement installé! ✅

## 🚨 Problèmes Courants

### Erreur: "Unresolved reference: airbnb"

**Solution**: Synchronisez Gradle
```bash
./gradlew --refresh-dependencies
```

### Erreur de Build

**Solution**: Nettoyez et rebuilder
```bash
./gradlew clean
./gradlew build
```

### Les animations ne s'affichent pas

**Causes possibles**:
1. **Pas de connexion Internet**: Les animations sont chargées depuis des URLs
2. **URL invalide**: Utilisez les URLs de fallback dans `LottieUrls`
3. **Permissions manquantes**: Vérifiez `AndroidManifest.xml`

**Vérifier les permissions Internet**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### L'onboarding ne s'affiche pas

**Vérifications**:
1. Supprimez les données de l'app pour réinitialiser
2. Vérifiez que `isOnboardingCompleted()` retourne `false`
3. Vérifiez les logs Android Studio pour les erreurs

## 📊 Structure du Projet

```
app/src/main/java/com/example/aureus/
├── ui/
│   ├── onboarding/
│   │   ├── OnboardingData.kt        # Données des pages
│   │   ├── OnboardingScreen.kt      # UI de l'onboarding
│   │   └── OnboardingViewModel.kt   # Logique et état
│   ├── components/
│   │   ├── LottieAnimations.kt      # Composants réutilisables
│   │   └── AnimationExamples.kt     # Exemples d'usage
│   ├── navigation/
│   │   └── Navigation.kt            # Routes et navigation
│   └── theme/
│       └── Color.kt                 # Palette de couleurs
├── util/
│   └── SharedPreferencesManager.kt  # Persistence
└── MainActivity.kt                  # Point d'entrée
```

## 🎯 Prochaines Étapes

Après l'installation réussie:

1. **Personnaliser l'onboarding**
   - Modifier les textes dans `OnboardingData.kt`
   - Changer les animations si nécessaire

2. **Intégrer les animations dans vos écrans**
   - Utiliser `EmptyStateView` pour les listes vides
   - Utiliser `LoadingView` pendant les chargements
   - Utiliser `SuccessView/ErrorView` pour le feedback

3. **Tester sur différents appareils**
   - Émulateurs Android
   - Appareils physiques
   - Différentes tailles d'écran

## 📚 Ressources

- **Documentation Lottie**: https://airbnb.io/lottie/
- **LottieFiles**: https://lottiefiles.com/
- **Animations du projet**: Voir `LOTTIE_ANIMATIONS.md`
- **Guide complet**: Voir `ONBOARDING_SETUP.md`

## ✅ Checklist d'Installation

- [ ] Gradle synchronisé sans erreur
- [ ] Build réussi
- [ ] Application lancée
- [ ] Onboarding s'affiche au premier lancement
- [ ] Les 3 pages d'onboarding fonctionnent
- [ ] Les animations Lottie se chargent
- [ ] La navigation fonctionne correctement
- [ ] L'état est sauvegardé (pas d'onboarding au 2e lancement)

## 🆘 Besoin d'Aide?

Si vous rencontrez des problèmes:
1. Vérifiez les logs dans **Logcat** (Android Studio)
2. Relisez ce guide étape par étape
3. Consultez `ONBOARDING_SETUP.md` pour plus de détails
4. Vérifiez que toutes les dépendances sont correctes dans `build.gradle.kts`

---

**Note**: Après la synchronisation Gradle, toutes les erreurs de linter disparaîtront et l'application sera prête à être utilisée! 🚀
