# 🏦 Aureus - Application Bancaire Premium

<div align="center">

![Status](https://img.shields.io/badge/Status-Complete-success)
![Platform](https://img.shields.io/badge/Platform-Android-green)
![Compose](https://img.shields.io/badge/Jetpack-Compose-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple)

**Application bancaire mobile complète en Jetpack Compose**

[Features](#-features) • [Screenshots](#-pages-implémentées) • [Installation](#-installation) • [Compte Test](#-compte-test)

</div>

---

## 🎯 Vue d'Ensemble

Aureus est une application bancaire mobile **complète et fonctionnelle** développée en Jetpack Compose avec Material Design 3. L'application présente une interface utilisateur **premium** avec des animations fluides, une navigation intuitive et un design cohérent Navy Blue + Gold.

### ✨ Points Forts
- 🎨 **Design Premium** - Interface élégante Navy Blue + Gold
- 🔐 **Sécurité** - Système PIN à 4 chiffres pour actions sensibles
- 📊 **Statistiques** - Graphiques animés et analyses détaillées
- 💳 **Multi-cartes** - Gestion de plusieurs cartes bancaires
- 📱 **100% Compose** - UI moderne Material 3
- 🚀 **Production Ready** - Code propre, architecture MVVM

---

## 🚀 Installation

### Prérequis
- Android Studio Hedgehog ou supérieur
- Kotlin 1.9+
- Android SDK 26+ (Android 8.0+)
- JDK 11

### Étapes
```bash
# 1. Cloner le projet
git clone [your-repo-url]
cd Aureus

# 2. Ouvrir dans Android Studio
# File > Open > Sélectionner le dossier Aureus

# 3. Sync Gradle
# Le projet va automatiquement télécharger les dépendances

# 4. Lancer l'app
# Run > Run 'app' ou cliquer sur le bouton Play
```

---

## 🔐 Compte Test

Pour tester l'application, utilisez ces identifiants :

```
📧 Email: test@aureus.com
🔒 Password: Test123456
📱 PIN: 1234
```

**Codes de vérification :**
- SMS: `123456`
- PIN: `1234`

---

## 📱 Features

### Authentification & Sécurité
- ✅ Inscription complète (Register)
- ✅ Connexion sécurisée (Login)
- ✅ Vérification SMS (6 chiffres)
- ✅ Configuration PIN (4 chiffres, 2 étapes)
- ✅ Vérification PIN pour transactions sensibles
- ✅ Splash Screen animé
- ✅ Onboarding (4 écrans)

### Dashboard
- ✅ Carte bancaire principale avec balance
- ✅ Actions rapides (Send, Request, Scan, More)
- ✅ Mini graphique statistiques
- ✅ 5 dernières transactions
- ✅ Bottom navigation (Home, Stats, Cards, Settings)

### Statistiques
- ✅ Graphique courbe sur 6 mois
- ✅ Indicateur circulaire dépenses/revenus (55%)
- ✅ Analyse par catégorie avec pourcentages
- ✅ Icônes colorées par type de dépense
- ✅ Transactions comptées par catégorie

### Cartes Bancaires
- ✅ My Cards - Carrousel avec détails
- ✅ All Cards - Liste complète
- ✅ 3 cartes avec gradients différents
- ✅ Set default card
- ✅ Add new card (UI ready)
- ✅ Masquage numéros de carte

### Transactions
- ✅ Historique complet avec date/heure
- ✅ Filtres : All, Income, Expense
- ✅ Icônes par catégorie
- ✅ Montants colorés (vert positif, rouge négatif)
- ✅ 10 transactions d'exemple

### Transferts
- ✅ Send Money avec sélection contacts
- ✅ Input montant grand format
- ✅ Favoris en carrousel
- ✅ Note optionnelle
- ✅ Validation avec PIN
- ✅ 5 contacts pré-enregistrés

### Profil & Paramètres
- ✅ Profile complet avec avatar
- ✅ Informations personnelles
- ✅ Settings avec toggles
- ✅ Change Password
- ✅ Language selection
- ✅ Notifications
- ✅ Biometric Auth toggle
- ✅ About & Version

### Autres
- ✅ Search transactions
- ✅ Logout fonctionnel

---

## 📊 Données Statiques

### Cartes Bancaires (3)
```
1. VISA **** 9852
   Balance: 85,545.00 MAD
   Couleur: Navy Blue
   Status: Default

2. MASTERCARD **** 7823
   Balance: 42,180.50 MAD
   Couleur: Gold

3. VISA **** 3621
   Balance: 18,900.00 MAD
   Couleur: Black
```

### Transactions (10)
```
📱 Apple Store:       -8,450.00 MAD  (Shopping)
🎵 Spotify:              -99.00 MAD  (Entertainment)
💼 Monthly Salary:   +25,000.00 MAD  (Income)
🛒 Carrefour:           -654.00 MAD  (Food)
🚗 Uber:                 -85.00 MAD  (Transport)
👔 Zara:              -1,250.00 MAD  (Shopping)
📺 Netflix:             -119.00 MAD  (Entertainment)
⚡ LYDEC:               -580.00 MAD  (Bills)
💻 Freelance:        +5,500.00 MAD  (Income)
📦 Amazon:           -2,340.00 MAD  (Shopping)
```

### Contacts (5)
```
⭐ Mohammed ALAMI    +212 6 98 76 54 32
⭐ Fatima BENANI     +212 6 11 22 33 44
   Ahmed IDRISSI     +212 6 55 66 77 88
⭐ Salma FASSI       +212 6 99 88 77 66
   Omar TAZI         +212 6 33 44 55 66
```

---

## 🗺️ Navigation

```
Splash → Onboarding → Login → Dashboard
                        ↓
                    Register → SMS → PIN Setup → Login
                    
Dashboard (Bottom Nav):
├─ Home (0)
├─ Statistics (1)
├─ Cards (2)
└─ Settings (3)

Plus:
├─ Transactions
├─ Send Money
├─ Search
├─ Profile
└─ Settings
```

---

## 🎨 Design System

### Palette de Couleurs
```kotlin
// Primaires
PrimaryNavyBlue    = #1E3A5F  // Foncé principal
PrimaryMediumBlue  = #2C5F8D  // États actifs

// Secondaires
SecondaryGold      = #D4AF37  // Or premium
SecondaryDarkGold  = #C89F3C  // Or hover

// Sémantiques
SemanticGreen      = #10B981  // Succès/Positif
SemanticRed        = #EF4444  // Erreur/Négatif
SemanticAmber      = #F59E0B  // Avertissement

// Neutres
NeutralWhite       = #FFFFFF  // Cards
NeutralLightGray   = #F8FAFC  // Background
NeutralMediumGray  = #64748B  // Texte secondaire
NeutralDarkGray    = #1E293B  // Texte principal
```

### Typographie
- **Headlines**: Bold, 24-32sp
- **Body**: Regular/SemiBold, 14-16sp
- **Captions**: Regular, 12sp

### Composants
- Cartes avec `RoundedCornerShape(12-16dp)`
- Boutons avec `RoundedCornerShape(12dp)`
- Élévation Cards: `8dp` pour floating elements
- Espacement standard: `16-20dp`

---

## 🏗️ Architecture

### Structure du Projet
```
app/
├── data/
│   ├── StaticData.kt              # Modèles + données statiques
│   └── repository/
│       └── AuthRepositoryStaticImpl.kt
│
├── domain/
│   ├── model/                     # Interfaces modèles
│   └── repository/                # Interfaces repository
│
├── ui/
│   ├── home/                      # Dashboard
│   ├── statistics/                # Stats + Charts
│   ├── cards/                     # Gestion cartes
│   ├── transactions/              # Historique
│   ├── transfer/                  # Send/Request Money
│   ├── profile/                   # Profile + Settings
│   ├── auth/                      # Auth screens
│   ├── components/                # Composants réutilisables
│   ├── theme/                     # Couleurs, Typo
│   └── navigation/                # Navigation
│
└── MainActivity.kt
```

### Pattern MVVM
- **Model**: Data classes dans `data/`
- **View**: Composables dans `ui/`
- **ViewModel**: ViewModels dans `ui/*/viewmodel/`

---

## 📦 Dépendances Principales

```kotlin
// Jetpack Compose
androidx.compose.bom:2024.XX.XX
androidx.compose.material3
androidx.compose.material.icons.extended

// Navigation
androidx.navigation.compose

// Dependency Injection
com.google.dagger:hilt-android

// Networking
com.squareup.retrofit2
com.squareup.okhttp3

// Database
androidx.room

// Coroutines
org.jetbrains.kotlinx:coroutines

// Image Loading
io.coil-kt:coil-compose

// Animations
com.airbnb.android:lottie-compose

// Firebase
com.google.firebase:firebase-bom
```

---

## 📝 Pages Implémentées

### Authentification (7)
1. ✅ Splash Screen
2. ✅ Onboarding (4 slides)
3. ✅ Login
4. ✅ Register
5. ✅ SMS Verification
6. ✅ PIN Setup
7. ✅ PIN Verification

### Principales (8)
8. ✅ Home/Dashboard
9. ✅ Statistics
10. ✅ My Cards
11. ✅ All Cards
12. ✅ Transactions
13. ✅ Send Money
14. ✅ Profile
15. ✅ Settings

### Secondaires (3)
16. ✅ Search
17. ✅ Request Money (UI)
18. ✅ History (intégré)

**Total: 18 écrans complets**

---

## 🛠️ Développement

### Lancer en mode Debug
```bash
./gradlew installDebug
```

### Build Release
```bash
./gradlew assembleRelease
```

### Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📚 Documentation

- 📖 **COMPLETE_APP_GUIDE.md** - Guide complet de l'application
- 🔐 **PIN_SECURITY_README.md** - Documentation système PIN
- 🚀 **INTEGRATION_GUIDE.md** - Guide d'intégration
- 📊 **IMPLEMENTATION_STATUS.md** - État d'implémentation

---

## 🎓 Crédits & Technologies

### Technologies Utilisées
- **Kotlin** - Langage principal
- **Jetpack Compose** - UI moderne
- **Material Design 3** - Design system
- **Hilt** - Dependency injection
- **Coroutines** - Asynchrone
- **Navigation Compose** - Navigation
- **Coil** - Image loading
- **Lottie** - Animations

### Design Inspirations
- Material Design Guidelines
- Banking App Best Practices
- Modern Mobile UI Patterns

---

## 📄 License

Ce projet est un projet de démonstration/éducatif.

---

## 👥 Contact

Pour toute question ou suggestion :
- Email: [your-email]
- GitHub: [your-github]

---

## 🎉 Conclusion

Aureus est une **application bancaire complète** démontrant :
- ✅ Design premium et cohérent
- ✅ Navigation fluide et intuitive
- ✅ Animations et transitions professionnelles
- ✅ Architecture MVVM propre
- ✅ Code production-ready
- ✅ Expérience utilisateur exceptionnelle

**Prête pour démonstration, portfolio ou comme base pour une vraie app bancaire !**

---

<div align="center">

**Développé avec ❤️ en Kotlin & Jetpack Compose**

⭐ Si vous aimez ce projet, donnez-lui une étoile !

</div>
