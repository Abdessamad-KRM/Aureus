# 🚀 Guide d'Intégration de l'Onboarding

Ce document explique comment l'onboarding a été intégré dans l'application Aureus avec les animations Lottie et la palette de couleurs définie.

## 🎨 Palette de Couleurs Utilisée

L'onboarding respecte strictement la palette "Prestige & Confiance":

- **Primaire**: `#1B365D` (Bleu marine profond) - NavyBlue
- **Secondaire**: `#D4AF37` (Or) - Gold
- **Accent**: `#FFFFFF` (Blanc) - White
- **Neutre**: `#F5F5F5` (Gris clair) - LightGray

Ces couleurs sont définies dans `app/src/main/java/com/example/aureus/ui/theme/Color.kt`

## 📦 Fichiers Créés

### 1. Composants Onboarding

#### `OnboardingData.kt`
Contient les données des 3 pages d'onboarding:
- **Page 1**: Sécurité & Protection
- **Page 2**: Gestion Simplifiée
- **Page 3**: Alertes Instantanées

#### `OnboardingScreen.kt`
L'écran d'onboarding principal avec:
- HorizontalPager pour naviguer entre les pages
- Animations Lottie intégrées
- Indicateurs de page animés
- Boutons de navigation (Retour, Suivant, Commencer)
- Bouton "Passer" pour sauter l'onboarding

#### `OnboardingViewModel.kt`
ViewModel gérant:
- L'état de complétion de l'onboarding
- Sauvegarde dans SharedPreferences

### 2. Composants d'Animation

#### `LottieAnimations.kt`
Bibliothèque complète de composants réutilisables:
- `SimpleLottieAnimation` - Animation basique
- `EmptyStateView` - États vides
- `LoadingView` - Chargement
- `SuccessView` / `ErrorView` - Feedback
- `ProcessingTransactionView` - Traitement

#### `AnimationExamples.kt`
Exemples d'utilisation pour:
- Listes vides (bénéficiaires, transactions, cartes)
- Chargement de données
- Traitement de paiements
- Dialogs de succès/erreur
- Authentification biométrique
- Flux de transaction complet

### 3. Configuration

#### Dépendances ajoutées

Dans `gradle/libs.versions.toml`:
```toml
lottie = "6.1.0"
lottie-compose = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }
```

Dans `app/build.gradle.kts`:
```kotlin
implementation(libs.lottie.compose)
```

### 4. Navigation

#### Modifications dans `Navigation.kt`
- Ajout de la route `Screen.Onboarding`
- Intégration du OnboardingViewModel
- Logique de démarrage: Onboarding → Login → Dashboard

#### Modifications dans `MainActivity.kt`
- Ajout du OnboardingViewModel
- Passage du ViewModel à AppNavigation

### 5. Persistence

#### Modifications dans `SharedPreferencesManager.kt`
Ajout de 2 méthodes:
- `setOnboardingCompleted(Boolean)`
- `isOnboardingCompleted(): Boolean`

## 🎭 Animations Lottie Intégrées

### Onboarding (3 animations)
1. **Banking Security** - Protection des données
2. **Money Management** - Gestion des comptes
3. **Real Time Notifications** - Alertes instantanées

### Empty States (4 animations)
- Liste vide
- Aucune transaction
- Aucune carte
- Pas de données

### Feedback (3 animations)
- Succès (checkmark)
- Erreur (alert)
- Avertissement

### Loading (2 animations)
- Chargement général
- Traitement de transaction

### Authentication (2 animations)
- Empreinte digitale
- Reconnaissance faciale

### Features (3 animations)
- Portefeuille numérique
- Transfert d'argent
- Analyses financières

**Total: 17 animations Lottie prêtes à l'emploi**

## 🚀 Flux de l'Application

```
Démarrage
    ↓
Onboarding complété?
    ↓ Non
Onboarding Screen (3 pages)
    ↓ Oui / Terminé
Utilisateur connecté?
    ↓ Non
Login Screen
    ↓ Oui
Dashboard
```

## ✨ Fonctionnalités de l'Onboarding

### Design
- ✅ Palette de couleurs respectée
- ✅ Animations Lottie fluides et légères
- ✅ Design moderne avec Material 3
- ✅ Transitions animées entre pages
- ✅ Indicateurs de page avec animation Spring

### Navigation
- ✅ Swipe horizontal entre les pages
- ✅ Bouton "Passer" sur les 2 premières pages
- ✅ Bouton "Retour" à partir de la page 2
- ✅ Bouton "Suivant" transformé en "Commencer" sur la dernière page
- ✅ Navigation fluide avec animations

### UX
- ✅ Affiché uniquement au premier lancement
- ✅ État sauvegardé dans SharedPreferences
- ✅ Peut être sauté à tout moment
- ✅ Design responsive et adaptatif

## 📱 Utilisation

### Réinitialiser l'onboarding (pour test)

Pour tester l'onboarding à nouveau:

```kotlin
// Dans votre code de test ou debug
sharedPreferencesManager.setOnboardingCompleted(false)
// Redémarrez l'application
```

### Personnaliser l'onboarding

Pour modifier les pages d'onboarding, éditez `OnboardingData.kt`:

```kotlin
object OnboardingData {
    val pages = listOf(
        OnboardingPage(
            title = "Votre titre",
            description = "Votre description",
            lottieUrl = "URL de votre animation"
        ),
        // Ajoutez plus de pages...
    )
}
```

### Utiliser les animations ailleurs

```kotlin
// Empty state
EmptyStateView(
    message = "Aucune donnée",
    actionText = "Recharger",
    onActionClick = { /* Action */ },
    animationUrl = LottieUrls.EMPTY_DATA_FALLBACK
)

// Loading
LoadingView(message = "Chargement...")

// Success
SuccessView(
    message = "Opération réussie !",
    onDismiss = { /* Fermer */ }
)
```

## 🎯 Points Clés

1. **Performance**: Les animations sont chargées depuis des URLs, pas intégrées dans l'APK
2. **Cache**: Lottie met automatiquement en cache les animations
3. **Fallback**: Des URLs de secours (LottieFiles vérifiées) sont disponibles
4. **Réutilisabilité**: Tous les composants sont réutilisables dans l'app
5. **Cohérence**: Design uniforme avec la palette de couleurs

## 📚 Documentation Complète

Pour plus de détails sur les animations:
- Voir `LOTTIE_ANIMATIONS.md` pour la liste complète des animations
- Voir `AnimationExamples.kt` pour des exemples d'utilisation

## 🔧 Maintenance

### Ajouter une nouvelle animation

1. Trouvez l'animation sur [LottieFiles](https://lottiefiles.com)
2. Ajoutez l'URL dans `LottieUrls`
3. Créez un composant dans `LottieAnimations.kt` si nécessaire
4. Utilisez-la dans vos écrans

### Modifier le style

Les styles de l'onboarding sont définis dans:
- Couleurs: `ui/theme/Color.kt`
- Typographie: `ui/theme/Type.kt`
- Thème général: `ui/theme/Theme.kt`

## ✅ Checklist d'Intégration

- [x] Dépendance Lottie ajoutée
- [x] OnboardingScreen créé avec 3 pages
- [x] Animations Lottie intégrées (17 au total)
- [x] Navigation configurée
- [x] SharedPreferences pour la persistence
- [x] Composants réutilisables créés
- [x] Exemples d'utilisation documentés
- [x] Palette de couleurs respectée
- [x] Documentation complète

## 🎉 Résultat

L'application dispose maintenant d'un onboarding professionnel et moderne avec:
- 3 pages explicatives animées
- 17 animations Lottie pour différents cas d'usage
- Design cohérent avec la palette "Prestige & Confiance"
- Composants réutilisables dans toute l'application
- Documentation complète pour la maintenance

Pour toute question ou personnalisation, référez-vous aux fichiers de documentation et aux exemples fournis.
