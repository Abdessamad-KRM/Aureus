# 🎨 Lottie Animations Guide

Ce document liste toutes les animations Lottie intégrées dans l'application Aureus et comment les utiliser.

## 📦 Installation

La dépendance Lottie est déjà configurée dans `build.gradle.kts`:

```kotlin
implementation("com.airbnb.android:lottie-compose:6.1.0")
```

## 🎭 Animations Disponibles

### 1. Onboarding Animations

Ces animations sont utilisées dans l'écran d'onboarding pour présenter les fonctionnalités principales de l'application.

| Animation | Description | URL | Usage |
|-----------|-------------|-----|-------|
| **Security** | Sécurité et protection des données | `LottieUrls.SECURITY` | Page 1 de l'onboarding |
| **Accounts** | Gestion des comptes bancaires | `LottieUrls.ACCOUNTS` | Page 2 de l'onboarding |
| **Notifications** | Alertes en temps réel | `LottieUrls.NOTIFICATIONS` | Page 3 de l'onboarding |

### 2. Empty States

Animations affichées lorsqu'aucune donnée n'est disponible.

| Animation | Description | URL | Exemple d'usage |
|-----------|-------------|-----|-----------------|
| **Empty List** | Liste vide générique | `LottieUrls.EMPTY_LIST_FALLBACK` | Liste de bénéficiaires vide |
| **No Transactions** | Aucune transaction | `LottieUrls.NO_TRANSACTIONS` | Historique vide |
| **No Cards** | Aucune carte | `LottieUrls.NO_CARDS` | Portefeuille de cartes vide |
| **No Data** | Données indisponibles | `LottieUrls.EMPTY_DATA_FALLBACK` | État vide générique |

### 3. Feedback Animations

Animations pour les retours utilisateur (succès, erreur, avertissement).

| Animation | Description | URL | Durée | Loop |
|-----------|-------------|-----|-------|------|
| **Success** | Transaction réussie | `LottieUrls.SUCCESS` | 2s | Non |
| **Error** | Erreur de transaction | `LottieUrls.ERROR` | 2s | Non |
| **Warning** | Avertissement (solde faible) | `LottieUrls.WARNING` | 2s | Non |

### 4. Loading Animations

Animations de chargement pour les opérations en cours.

| Animation | Description | URL | Usage |
|-----------|-------------|-----|-------|
| **Loading** | Chargement général | `LottieUrls.LOADING` | Chargement de données |
| **Processing** | Traitement de transaction | `LottieUrls.PROCESSING` | Validation de paiement |

### 5. Authentication Animations

Animations pour l'authentification biométrique.

| Animation | Description | URL | Usage |
|-----------|-------------|-----|-------|
| **Fingerprint** | Scan d'empreinte digitale | `LottieUrls.FINGERPRINT` | Authentification biométrique |
| **Face ID** | Reconnaissance faciale | `LottieUrls.FACE_ID` | Authentification faciale |

### 6. Feature Animations

Animations pour illustrer les fonctionnalités de l'application.

| Animation | Description | URL | Usage |
|-----------|-------------|-----|-------|
| **Wallet** | Portefeuille numérique | `LottieUrls.WALLET` | Écran de portefeuille |
| **Transfer** | Transfert d'argent | `LottieUrls.TRANSFER` | Écran de transfert |
| **Analytics** | Analyse financière | `LottieUrls.ANALYTICS` | Écran de statistiques |

## 🚀 Utilisation

### Composant Simple

Pour afficher une animation Lottie simple:

```kotlin
SimpleLottieAnimation(
    url = LottieUrls.LOADING,
    modifier = Modifier.size(120.dp)
)
```

### Empty State avec Animation

```kotlin
EmptyStateView(
    message = "Aucun bénéficiaire enregistré",
    actionText = "Ajouter un bénéficiaire",
    onActionClick = { /* Action */ },
    animationUrl = LottieUrls.EMPTY_LIST_FALLBACK
)
```

### Loading View

```kotlin
LoadingView(
    message = "Chargement en cours..."
)
```

### Success/Error Feedback

```kotlin
// Success
SuccessView(
    message = "Transaction effectu��e avec succès !",
    onDismiss = { /* Fermer */ }
)

// Error
ErrorView(
    message = "Une erreur est survenue",
    onDismiss = { /* Réessayer */ }
)
```

### Processing Transaction

```kotlin
ProcessingTransactionView(
    message = "Traitement de votre paiement..."
)
```

## 🎨 Personnalisation

### Contrôle de l'animation

```kotlin
val composition by rememberLottieComposition(
    LottieCompositionSpec.Url(url)
)
val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever, // ou 1 pour une seule fois
    speed = 1f // Vitesse de l'animation
)

LottieAnimation(
    composition = composition,
    progress = { progress },
    modifier = Modifier.size(200.dp)
)
```

### Paramètres personnalisables

- **iterations**: Nombre de répétitions (`LottieConstants.IterateForever` pour boucle infinie)
- **speed**: Vitesse de lecture (1f = normal, 2f = 2x plus rapide)
- **modifier**: Personnalisation de la taille et de l'apparence

## 🎭 Palette de Couleurs

Les animations sont conçues pour s'intégrer avec la palette de couleurs de l'application:

- **Primaire**: #1B365D (Bleu marine profond)
- **Secondaire**: #D4AF37 (Or)
- **Accent**: #FFFFFF (Blanc)
- **Neutre**: #F5F5F5 (Gris clair)

## 📝 Notes Importantes

1. **Fallback URLs**: Certaines URLs utilisent des animations vérifiées de LottieFiles qui sont garanties de fonctionner.

2. **Performance**: Les animations sont optimisées pour ne pas impacter les performances de l'application.

3. **Chargement**: Les animations sont chargées à la demande depuis des URLs, ce qui réduit la taille de l'application.

4. **Cache**: Lottie met en cache automatiquement les animations téléchargées.

## 🔄 Mise à jour des URLs

Pour mettre à jour une URL d'animation:

1. Trouvez l'animation sur [LottieFiles](https://lottiefiles.com)
2. Obtenez l'URL JSON de l'animation
3. Mettez à jour la constante correspondante dans `LottieUrls`

## 🎯 Composants Disponibles

Tous les composants sont disponibles dans:
```
app/src/main/java/com/example/aureus/ui/components/LottieAnimations.kt
```

### Composants principaux:

- `SimpleLottieAnimation` - Animation Lottie basique
- `EmptyStateView` - État vide avec animation
- `LoadingView` - Vue de chargement
- `SuccessView` - Retour de succès
- `ErrorView` - Retour d'erreur
- `ProcessingTransactionView` - Traitement de transaction

## 🚀 Onboarding

L'écran d'onboarding utilise 3 animations principales pour présenter l'application:

1. **Sécurité & Protection** - Rassure l'utilisateur sur la sécurité de ses données
2. **Gestion Simplifiée** - Présente la gestion des comptes
3. **Alertes Instantanées** - Met en avant les notifications en temps réel

Le composant est disponible dans:
```
app/src/main/java/com/example/aureus/ui/onboarding/OnboardingScreen.kt
```
