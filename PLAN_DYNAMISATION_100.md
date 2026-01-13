# Plan de Dynamisation 100% - Application Aureus Banking

## 📊 Analyse Actuelle

### État du Projet
L'application Aureus Banking utilise une architecture hybride avec des parties dynamiques (Firebase) et des parties statiques (StaticData).

### Architecture Principale
- **Framework**: Kotlin + Jetpack Compose
- **Pattern Architecture**: MVVM + Clean Architecture
- **Backend**: Firebase (Auth + Firestore + Storage)
- **Injection de Dépendances**: Hilt

### Parties Dynamiques (Déjà en place)
- ✅ `FirebaseDataManager.kt` - Gestion complète Firestore en temps réel
- ✅ `FirebaseAuthManager.kt` - Gestion Auth Firebase
- ✅ `HomeViewModel.kt` - Données utilisateur dynamiques
- ✅ `StatisticsViewModel.kt` - Statistiques calculées dynamiquement
- ✅ `CardsViewModel.kt` - Cartes bancaires dynamiques
- ✅ `AuthViewModel.kt` - Authentification Firebase

### Parties Statiques (À remplacer)
- ⚠️ `StaticData.kt` - Toutes les données hardcodées (Users, Cards, Transactions, Contacts, Statistics, Languages)
- ⚠️ `AuthRepositoryStaticImpl.kt` - Repository d'auth statique
- ⚠️ `SendMoneyScreen.kt` - Utilise `StaticContacts.contacts`
- ⚠️ `TransactionsFullScreen.kt` - Utilise `StaticTransactions.transactions`
- ⚠️ `MyCardsScreen.kt` - Utilise `StaticCards.cards`

---

## 🚀 Plan de Dynamisation par Phases

### PHASE 1: Migration Complete des Transactions
**Objectif**: Rendre toutes les transactions dynamiques (CRUD + Firestore Sync)

#### Étapes
1. **Créer TransactionRepository**
   - Créer `TransactionRepository.kt` dans `domain/repository/`
   - Définir l'interface avec les méthodes CRUD
   - Méthodes: `getTransactions()`, `createTransaction()`, `updateTransaction()`, `deleteTransaction()`

2. **Implémenter TransactionRepositoryImpl**
   - Créer `TransactionRepositoryImpl.kt` dans `data/repository/`
   - Utiliser `FirebaseDataManager` comme backend
   - Convertir les Map Firestore enobjets domain `Transaction`
   - Ajouter gestion d'erreurs robuste

3. **Créer TransactionViewModel**
   - Créer `TransactionViewModel.kt` dans `ui/transaction/viewmodel/`
   - StateFlow pour liste des transactions
   - Méthodes: filtrage par catégorie, date, type
   - Pagination support

4. **Migrer TransactionsFullScreen**
   - Remplacer `StaticTransactions.transactions` par `viewModel.transactions`
   - Connecter les filtres dynamiques (Income/Expense/All)
   - Ajouter refresh (pull-to-refresh)
   - Gérer les états de chargement/erreur

5. **Migrer TransactionDetailScreen**
   - Charger les détails depuis Firebase
   - Afficher métadonnées Firestore (createdAt, updatedAt)
   - Support export PDF

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/domain/repository/TransactionRepository.kt`
- `app/src/main/java/com/example/aureus/data/repository/TransactionRepositoryImpl.kt`
- `app/src/main/java/com/example/aureus/ui/transaction/viewmodel/TransactionViewModel.kt` (nouveau)

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/ui/transactions/TransactionsFullScreen.kt`
- `app/src/main/java/com/example/aureus/ui/transactions/TransactionDetailScreen.kt`

---

### PHASE 2: Migration Complete des Contacts
**Objectif**: Rendre les contacts dynamiques (User-managed avec Firebase)

#### Étapes
1. **Créer Contact Model (Domain)**
   - Créer `Contact.kt` dans `domain/model/`
   - Définir structure contact utilisateur
   - Champs: id, name, phone, email, avatar, accountNumber, isFavorite, isBankContact

2. **Créer ContactRepository**
   - Créer `ContactRepository.kt` dans `domain/repository/`
   - Méthodes CRUD pour contacts utilisateurs
   - Méthodes: `getContacts()`, `addContact()`, `updateContact()`, `deleteContact()`, `searchContacts()`

3. **Implémenter ContactRepositoryImpl**
   - Créer `ContactRepositoryImpl.kt` dans `data/repository/`
   - Stocker contacts dans sous-collection Firestore: `users/{userId}/contacts`
   - Synchronisation en temps réel
   - Support contacts favoris

4. **Créer ContactViewModel**
   - Créer `ContactViewModel.kt` dans `ui/contact/viewmodel/`
   - Gestion liste contacts
   - Recherche contacts par nom/téléphone
   - Filtrage favoris/récents

5. **Migrer SendMoneyScreen**
   - Remplacer `StaticContacts.contacts` par `viewModel.contacts`
   - Ajouter modal "Add New Contact"
   - Intégrer contacts depuis téléphone (Contacts API Android)
   - Suggestions de contacts basées sur transactions

6. **Ajouter ContactManagementScreen**
   - Écran complet gestion contacts
   - CRUD complet
   - Import/Export contacts
   - Catégorisation contacts (famille, amis, travail)

7. **Migrer RequestMoneyScreen**
   - Utiliser `ContactViewModel`
   - Interface similaire à SendMoneyScreen

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/domain/model/Contact.kt`
- `app/src/main/java/com/example/aureus/domain/repository/ContactRepository.kt`
- `app/src/main/java/com/example/aureus/data/repository/ContactRepositoryImpl.kt`
- `app/src/main/java/com/example/aureus/ui/contact/viewmodel/ContactViewModel.kt`
- `app/src/main/java/com/example/aureus/ui/contact/ContactManagementScreen.kt`

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/ui/transfer/SendMoneyScreen.kt`
- `app/src/main/java/com/example/aureus/ui/transfer/RequestMoneyScreen.kt`

---

### PHASE 3: Migration Complete des Cartes Bancaires
**Objectif**: Rendre toutes les cartes dynamiques avec Firebase

#### Étapes
1. **Enrichir BankCard Model (Domain)**
   - Ajouter champs manquants: `accountId`, `isActive`, `dailyLimit`, `monthlyLimit`, `spendingToday`
   - Enums complets: `CardType`, `CardColor`, `CardStatus`

2. **Créer CardRepository**
   - Créer `CardRepository.kt` dans `domain/repository/`
   - Méthodes: `getCards()`, `addCard()`, `updateCard()`, `setDefaultCard()`, `lockCard()`

3. **Implémenter CardRepositoryImpl**
   - Utiliser `FirebaseDataManager` existant
   - Logique pour gérer "isDefault" (une seule carte default)
   - Notification solde faible

4. **Enrichir CardsViewModel**
   - Ajouter méthodes CRUD
   - Gestion cache cartes offline-first
   - Calcul dépenses par carte

5. **Migrer AddCardScreen**
   - Formulaire complet ajouter carte
   - Validation numéros cartes (Luhn algorithm)
   - Scan carte (OCR)
   - Simuler ajout carte (Mode test)

6. **Migrer MyCardsScreen**
   - Remplacer `StaticCards.cards` par `viewModel.cards`
   - Ajouter options carte: Freeze card, Set limits, View PIN
   - Historique dépenses par carte
   - Notifications transactions carte

7. **CardDetailScreen** (Nouvelle)
   - Détails complets carte
   - Dépenses récentes
   - Paramètres sécurité (PIN, blocage)
   - Limite dépenses quotidien/mensuel

8. **FirebaseDataManager Enhancements**
   - Ajouter méthode `createTestCards(userId)` pour demo
   - Support virtual cards
   - Gestion cartes expirées

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/domain/repository/CardRepository.kt`
- `app/src/main/java/com/example/aureus/data/repository/CardRepositoryImpl.kt`
- `app/src/main/java/com/example/aureus/ui/cards/CardDetailScreen.kt`

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/domain/model/BankCard.kt` (Enrichir)
- `app/src/main/java/com/example/aureus/ui/cards/CardsViewModel.kt`
- `app/src/main/java/com/example/aureus/ui/cards/AddCardScreen.kt`
- `app/src/main/java/com/example/aureus/ui/cards/CardsScreen.kt`

---

### PHASE 4: Migration Complete des Statistiques
**Objectif**: Rendre toutes les statistiques dynamiques et calculées en temps réel

#### Étapes
1. **Créer Statistic Model**
   - Classes pour différents types de stats: `MonthlyStatistic`, `CategoryStatistic`, `SpendingTrend`
   - Enums pour périodes: `Daily`, `Weekly`, `Monthly`, `Yearly`

2. **Créer StatisticRepository**
   - Créer `StatisticRepository.kt` dans `domain/repository/`
   - Méthodes: `getMonthlyIncomeExpense()`, `getCategoryBreakdown()`, `getSpendingTrends()`

3. **Enrichir StatisticsViewModel**
   - Calculs complexes sur transactions Firestore
   - Cache intelligence (avoid repeated Firestore queries)
   - Support multi-périodes

4. **Migrer StatisticsScreen**
   - Charts dynamiques (VICO Chart library)
   - Filtres période interactifs
   - Export rapports PDF/CSV
   - Budget vs Spending comparison

5. **Ajouter Analytics Features**
   - Prédictions dépenses (Machine Learning basic)
   - Alertes budget exceeded
   - Savings goals tracking
   - Spending insights AI

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/domain/model/Statistic.kt`
- `app/src/main/java/com/example/aureus/domain/repository/StatisticRepository.kt`
- `app/src/main/java/com/example/aureus/data/repository/StatisticRepositoryImpl.kt`

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/ui/statistics/viewmodel/StatisticsViewModel.kt`
- `app/src/main/java/com/example/aureus/ui/statistics/StatisticsScreen.kt`

---

### PHASE 5: Migration Complete des Utilisateurs & Profils
**Objectif**: Rendre la gestion utilisateur 100% dynamique

#### Étapes
1. **Enrichir User Model (Domain)**
   - Ajouter champs: `profileImage`, `preferredLanguage`, `notificationSettings`, `securitySettings`
   - Support multi-devices

2. **Créer UserRepository**
   - Créer `UserRepository.kt` dans `domain/repository/`
   - Méthodes: `getUserProfile()`, `updateProfile()`, `uploadProfileImage()`, `deleteAccount()`

3. **Implémenter UserRepositoryImpl**
   - Utiliser Firebase Auth + Firestore
   - Firebase Storage pour images
   - Gestion offline-first pour profile

4. **Créer ProfileViewModel**
   - État profil utilisateur
   - Uploads images
   - Updates en temps réel

5. **Migrer ProfileAndSettingsScreen**
   - Éditer profile photo, nom, téléphone
   - Langue sélectionnée dynamique (suppr. SupportedLanguages statique)
   - Settings notifications/privacy

6. **Internationalization (i18n)**
   - String resources multi-langues
   - Support EN/FR/AR/ES/DE dynamique
   - RTL support pour Arabe

7. **Security Settings**
   - Modifier PIN
   - Biometrics (fingerprint/faceID)
   - Two-factor auth
   - Session management

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/domain/repository/UserRepository.kt`
- `app/src/main/java/com/example/aureus/data/repository/UserRepositoryImpl.kt`
- `app/src/main/java/com/example/aureus/ui/profile/viewmodel/ProfileViewModel.kt`

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/domain/model/User.kt` (Enrichir)
- `app/src/main/java/com/example/aureus/ui/profile/ProfileAndSettingsScreen.kt`
- `app/src/main/java/com/example/aureus/ui/profile/EditProfileScreen.kt`

---

### PHASE 6: Migration Complete de l'Authentification
**Objectif**: Supprimer AuthRepositoryStaticImpl, utiliser 100% Firebase

#### Étapes
1. **Créer AuthRepositoryImpl (Firebase)**
   - Remplacer `AuthRepositoryStaticImpl.kt`
   - Utiliser `FirebaseAuthManager` et `FirebaseDataManager`
   - Support login par email/password, Google, Phone

2. **Enrichir AuthViewModel**
   - Ajouter méthode `resetPassword()`
   - Email verification
   - Phone verification link email

3. **Gestion Session**
   - Token refresh automatique
   - Multi-device logout
   - Session timeout

4. **Suppression Compte Demo**
   - Retirer logique bypass `isDemoAccount()`
   - Créer script de données demo dans Firebase (seeds)
   - Admin panel pour créer comptes test

5. **Firebase Firestore Rules Update**
   - Règles robustes sécurité
   - Validation données côté serveur

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/data/repository/AuthRepositoryImpl.kt` (Remplacer statique)
- `app/src/main/java/com/example/aureus/ui/auth/viewmodel/AuthViewModel.kt`
- `firestore.rules`

#### Fichiers à Supprimer
- `app/src/main/java/com/example/aureus/data/TestAccount.kt` (partie démo)
- `app/src/main/java/com/example/aureus/data/repository/AuthRepositoryStaticImpl.kt`

---

### PHASE 7: Suppression Completes de StaticData.kt
**Objectif**: Nettoyage final, éliminer toutes les données statiques

#### Étapes
1. **Migration Résiduelle**
   - Vérifier tous les imports de `StaticData` dans les écrans restants
   - Remplacer manuellement chaque référence
   - Tests unitaires

2. **Créer Seed Data Script**
   - Script Kotlin pour peupler Firebase avec données de demo
   - Executé une seule fois au premier lancement app
   - Users, cards, transactions, contacts par défaut

3. **Suppression Fichier StaticData**
   - `app/src/main/java/com/example/aureus/data/StaticData.kt` → DELETE
   - Vérifier qu'aucun build error après

4. **Code Cleanup**
   - Supprimer imports non utilisés
   - Nettoyer commentaires obsolètes
   - Organiser architecture finale

5. **Documentation Update**
   - Mettre à jour `README.md`
   - Documenter l'architecture 100% dynamique
   - Guide setup Firebase

#### Fichiers à Supprimer
- `app/src/main/java/com/example/aureus/data/StaticData.kt` ❌

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/util/FirebaseSeedData.kt`

#### Fichiers à Modifier
- `README.md`

---

### PHASE 8: Offline-First Strategy
**Objectif**: App fonctionne parfaitement sans internet (localStorage + sync)

#### Étapes
1. **Room Database Enhancements**
   - Activer Room pour tous les domain models
   - Synchronisation automatique Firestore ↔ Room
   - Conflict resolution strategies

2. **Network Connectivity Monitor**
   - `ConnectivityManager` pour détecter offline
   - UI indication offline mode
   - Queue sync automatique quand online

3. **Cache Strategy**
   - TTL(Time To Live) pour données cachées
   - Cache manuel par écran
   - Invalidate cache sur actions utilisateur

4. **WorkManager Integration**
   - Background sync tasks
   - Scheduled data refresh
   - Push notifications sync

#### Fichiers à Créer
- `app/src/main/java/com/example/aureus/data/offline/OfflineSyncManager.kt`
- `app/src/main/java/com/example/aureus/data/offline/NetworkMonitor.kt`

#### Fichiers à Modifier
- `app/src/main/java/com/example/aureus/data/local/AppDatabase.kt`
- Tous les RepositoryImpl (ajouter sync Room)

---

## 📋 Checklist Finale

### Domain Layer
- [ ] `User.kt` - enrichi avec settings
- [ ] `BankCard.kt` - enrichi avec limits/locking
- [ ] `Contact.kt` - nouveau model
- [ ] `Transaction.kt` - déjà OK
- [ ] `Account.kt` - déjà OK
- [ ] `Statistic.kt` - nouveau model
- [ ] `AuthRepository.kt` - à vérifier
- [ ] `CardRepository.kt` - nouveau
- [ ] `ContactRepository.kt` - nouveau
- [ ] `TransactionRepository.kt` - nouveau
- [ ] `UserRepository.kt` - nouveau
- [ ] `StatisticRepository.kt` - nouveau
- [ ] `AccountRepository.kt` - déjà OK

### Data Layer
- [ ] `AuthRepositoryImpl.kt` - Firebase-only (remplacer statique)
- [ ] `CardRepositoryImpl.kt` - nouveau
- [ ] `ContactRepositoryImpl.kt` - nouveau
- [ ] `TransactionRepositoryImpl.kt` - nouveau
- [ ] `UserRepositoryImpl.kt` - nouveau
- [ ] `StatisticRepositoryImpl.kt` - nouveau
- [ ] `AccountRepositoryImpl.kt` - à vérifier
- [ ] `AppDatabase.kt` - enrichir pour offline-first
- [ ] `FirebaseSeedData.kt` - données demo
- [ ] `OfflineSyncManager.kt` - sync automatique
- [ ] `NetworkMonitor.kt` - detection offline

### UI Layer - ViewModels
- [ ] `AuthViewModel.kt` - enlever bypass démo
- [ ] `HomeViewModel.kt` - déjà OK
- [ ] `StatisticsViewModel.kt` - déjà OK
- [ ] `CardsViewModel.kt` - enrichir
- [ ] `TransactionViewModel.kt` - nouveau
- [ ] `ContactViewModel.kt` - nouveau
- [ ] `ProfileViewModel.kt` - nouveau

### UI Layer - Screens
- [ ] `LoginScreen.kt` - enlever demo account
- [ ] `RegisterScreen.kt` - connecter Firestore
- [ ] `HomeScreen.kt` - déjà OK
- [ ] `CardsScreen.kt` - migrer MyCardsScreen
- [ ] `AddCardScreen.kt` - enrichir
- [ ] `CardDetailScreen.kt` - nouveau
- [ ] `TransactionsFullScreen.kt` - migrer StaticTransactions
- [ ] `TransactionDetailScreen.kt` - connecter Firestore
- [ ] `SendMoneyScreen.kt` - migrer StaticContacts
- [ ] `RequestMoneyScreen.kt` - migrer StaticContacts
- [ ] `ContactManagementScreen.kt` - nouveau
- [ ] `ProfileAndSettingsScreen.kt` - migrer settings dynamiques
- [ ] `EditProfileScreen.kt` - enrichir
- [ ] `StatisticsScreen.kt` - charts dynamiques

### Fichiers à Supprimer
- [ ] `StaticData.kt` ❌
- [ ] `TestAccount.kt` (dans StaticData) ❌
- [ ] `AuthRepositoryStaticImpl.kt` ❌

---

## 🔍 Validation & Testing

### Unit Tests
- [ ] Tests pour tous les nouveaux Repository
- [ ] Tests pour tous les ViewModel
- [ ] Tests pour Model conversions

### Integration Tests
- [ ] Tests Firebase Auth flow
- [ ] Tests Firestore read/write/sync
- [ ] Tests offline mode

### UI Tests
- [ ] Compose tests pour chaque screen
- [ ] Navigation flow tests
- [ ] User journey tests (complete flows)

---

## 📌 Notes Importantes

### Firebase Security Rules
```javascript
// firestore.rules à mettre à jour
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // User contacts sub-collection
      match /contacts/{contactId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Cards collection
    match /cards/{cardId} {
      allow read: if request.auth != null && get(/databases/$(database)/documents/users/$(resource.data.userId)).userId == request.auth.uid;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null;
    }
    
    // Transactions collection
    match /transactions/{transactionId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
    
    // Accounts collection
    match /accounts/{accountId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

### Séries de Données Demo (Firebase Seed)
- Users: 5-10 comptes tests
- Cards: 2-3 cartes par user
- Transactions: 20-30 transactions par user
- Contacts: 10 contacts par user

### Performance Optimisations
- Firestore indexes pour queries fréquents
- Pagination pour longues listes
- Cache intelligent pour offline
- Lazy loading images

---

## 🎯 Estimated Timeline

| Phase | Durée Estimée | Priorité |
|----------------------|----------|
| Phase 1: Transactions | 3-4 jours | ⚡️ Haute |
| Phase 2: Contacts | 3-4 jours | ⚡️ Haute |
| Phase 3: Cartes | 2-3 jours | ⚡️ Haute |
| Phase 4: Statistiques | 2-3 jours | 🟡 Moyenne |
| Phase 5: Users/Profils | 3-4 jours | 🟡 Moyenne |
| Phase 6: Authentification | 2-3 jours | ⚡️ Haute |
| Phase 7: Nettoyage StaticData | 1-2 jours | ⚡️ Haute |
| Phase 8: Offline-First | 4-5 jours | 🟢 Basse (optionnelle) |

**Total estimé**: 20-28 jours (4-6 semaines)

---

## 🚨 Breaking Changes

When deleting `StaticData.kt`:
- Backup repository state before starting Phase 7
- Create feature branch for each phase
- Test thoroughly before merging
- Increment app version to 2.0.0

---

**Documentation created**: January 10, 2026
**Project**: Aureus Banking App
**Author**: Firebender AI Assistant