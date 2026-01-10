# 🧪 Guide de Test Complet - Aureus Banking App

## 🎯 Objectif
Ce guide vous permet de tester **TOUTES les 22 pages** de l'application avec des données statiques pour valider l'UI et l'UX.

---

## 🔐 Informations de Test

### Compte Test Principal
```
Email: test@aureus.com
Password: Test123456
PIN: 1234
```

### Codes de Vérification
```
SMS Code: 123456
PIN Test: 1234
```

---

## 📱 PLAN DE TEST COMPLET

### Phase 1: Flux d'Inscription (7 écrans)

#### Test 1.1 - Splash Screen
**Étapes:**
1. Lancer l'application
2. Observer l'animation du logo
3. Voir les cercles concentriques animés
4. Attendre 3-4 secondes

**Résultat attendu:**
- ✅ Animation fluide
- ✅ Logo centré avec effet doré
- ✅ Transition automatique vers Onboarding

**Données utilisées:** Aucune

---

#### Test 1.2 - Onboarding
**Étapes:**
1. Swiper entre les 4 slides
2. Observer les illustrations
3. Lire les textes
4. Cliquer "Next" sur chaque slide
5. Sur dernier slide, cliquer "Get Started"

**Résultat attendu:**
- ✅ 4 slides avec:
  - Slide 1: Fastest Payment
  - Slide 2: Most Secure Platform
  - Slide 3: Paying for Everything
  - Slide 4: (Final)
- ✅ Dots indicateurs en bas
- ✅ Navigation fluide entre slides
- ✅ Bouton "Get Started" sur dernier slide

**Données utilisées:** 4 slides statiques

---

#### Test 1.3 - Register
**Étapes:**
1. Cliquer "Sign Up"
2. Remplir formulaire:
   - First Name: "Ahmed"
   - Last Name: "Benali"
   - Email: "ahmed.benali@test.com"
   - Phone: "+212 6 99 88 77 66"
   - Password: "TestPass123"
   - Confirm Password: "TestPass123"
3. Cliquer "Sign Up"

**Résultat attendu:**
- ✅ Tous les champs validés
- ✅ Passwords doivent matcher
- ✅ Email format validé
- ✅ Transition vers SMS Verification

**Données utilisées:** Form inputs

---

#### Test 1.4 - SMS Verification
**Étapes:**
1. Observer le numéro de téléphone affiché
2. Voir les 6 boxes vides
3. Entrer code: `1` `2` `3` `4` `5` `6`
4. Observer l'auto-validation

**Résultat attendu:**
- ✅ 6 boxes individuelles animées
- ✅ Curseur clignotant dans box active
- ✅ Boxes remplies une par une
- ✅ Auto-validation à 6 chiffres
- ✅ Message "Code vérifié avec succès !"
- ✅ Transition vers PIN Setup

**Données utilisées:** Code test = `123456`
**Note:** Tout autre code affiche une erreur

---

#### Test 1.5 - PIN Setup
**Étapes:**
1. **Étape 1 - Créer PIN:**
   - Voir "Créer votre Code PIN"
   - Taper sur clavier: `1` `2` `3` `4`
   - Observer les 4 points se remplir
   - Transition automatique

2. **Étape 2 - Confirmer PIN:**
   - Voir "Confirmer votre Code PIN"
   - Taper: `1` `2` `3` `4`
   - Observer validation

**Résultat attendu:**
- ✅ Clavier numérique 0-9 + Backspace
- ✅ 4 points indicateurs animés
- ✅ Passage automatique à étape 2
- ✅ Si PINs ne matchent pas: erreur + reset
- ✅ Si PINs matchent: "Code enregistré !"
- ✅ Transition vers Login

**Données utilisées:** PIN créé = `1234`

---

#### Test 1.6 - Login
**Étapes:**
1. Entrer Email: `test@aureus.com`
2. Entrer Password: `Test123456`
3. Cliquer "Sign In"

**Résultat attendu:**
- ✅ Validation des champs
- ✅ Toggle visibility password
- ✅ Loading indicator pendant auth
- ✅ Success: Transition vers Dashboard
- ✅ Error si mauvais credentials

**Données utilisées:** 
- ✅ test@aureus.com / Test123456
- ❌ Tout autre combo = erreur

---

### Phase 2: Dashboard & Navigation (4 onglets)

#### Test 2.1 - Home/Dashboard
**Étapes:**
1. Observer la carte bancaire affichée
2. Vérifier le solde: `85,545.00 MAD`
3. Voir les 4 Quick Actions
4. Observer le mini graphique
5. Défiler pour voir 5 transactions
6. Cliquer sur une transaction

**Résultat attendu:**
- ✅ Carte VISA ****9852 avec gradient Navy
- ✅ Balance affichée en grand
- ✅ Quick Actions: Send, Request, Scan, More
- ✅ Mini chart avec barres
- ✅ 5 dernières transactions affichées
- ✅ Bottom Navigation en bas (4 icônes)

**Données utilisées:**
- Carte: `StaticCards.cards[0]`
- Transactions: `StaticTransactions.transactions.take(5)`

---

#### Test 2.2 - Statistics (Bottom Nav)
**Étapes:**
1. Cliquer sur icône "Stats" (bottom nav)
2. Observer la carte bancaire en haut
3. Voir l'indicateur circulaire `55%`
4. Observer le graphique courbe (6 mois)
5. Défiler pour voir catégories
6. Vérifier Shopping: 35%, Food: 8%, etc.

**Résultat attendu:**
- ✅ Carte avec balance
- ✅ Cercle animé avec 55% au centre
- ✅ Légende: Income (vert) vs Expenses (rouge)
- ✅ Graphique courbe animé sur 6 mois
- ✅ Labels: Jan, Feb, Mar, Apr, May, Jun
- ✅ Liste de 6 catégories avec icônes colorées
- ✅ Pourcentages corrects

**Données utilisées:**
- `StaticStatistics.spendingPercentage` = 55
- `StaticStatistics.monthlyStats` (6 mois)
- `StaticStatistics.categoryStats` (6 catégories)

---

#### Test 2.3 - My Cards (Bottom Nav)
**Étapes:**
1. Cliquer sur icône "Cards" (bottom nav)
2. Observer la carte en grand
3. Voir les 3 dots en bas (sélecteur)
4. Cliquer sur dot 2
5. Observer changement de carte
6. Voir les détails en bas
7. Cliquer "Add New Card"

**Résultat attendu:**
- ✅ Carte VISA ****9852 affichée en grand
- ✅ 3 dots: 1er en or (actif), autres gris
- ✅ Changement de carte au clic sur dot
- ✅ Détails: Number, Holder, Expiry, Type, Balance
- ✅ Bouton "Set as Default" (vert si déjà default)
- ✅ Bouton "Add New Card" outlined gold

**Données utilisées:**
- `StaticCards.cards` (3 cartes)
- Carte 1: VISA Navy
- Carte 2: MASTERCARD Gold
- Carte 3: VISA Black

---

#### Test 2.4 - Settings (Bottom Nav)
**Étapes:**
1. Cliquer sur icône "Settings" (bottom nav)
2. Observer les sections:
   - Account
   - Preferences
   - About
3. Tester toggles Notifications & Biometric
4. Cliquer sur "Change Password"
5. Cliquer sur "Language"
6. Cliquer sur "Terms & Conditions"

**Résultat attendu:**
- ✅ 3 sections bien séparées
- ✅ Items Account: Change Password, Language
- ✅ Items Preferences: Toggles fonctionnels
- ✅ Items About: Terms, Privacy, About
- ✅ Version affichée: 1.0.0
- ✅ Copyright: © 2026 Aureus Bank
- ✅ Navigation vers sous-pages

**Données utilisées:** Aucune (UI seulement)

---

### Phase 3: Transactions & Transferts

#### Test 3.1 - All Transactions
**Étapes:**
1. Depuis Home, cliquer "View All Transactions"
2. Observer la liste complète (10 transactions)
3. Tester les filtres:
   - All
   - Income (2 transactions)
   - Expense (8 transactions)
4. Observer les couleurs:
   - Vert pour revenus (+)
   - Rouge pour dépenses (-)
5. Cliquer sur une transaction

**Résultat attendu:**
- ✅ 10 transactions affichées
- ✅ Filtres fonctionnels
- ✅ Income: Salary (+25,000), Freelance (+5,500)
- ✅ Expenses: 8 transactions négatives
- ✅ Chaque transaction: icône, nom, date, montant
- ✅ Clic mène à Transaction Detail

**Données utilisées:**
- `StaticTransactions.transactions` (10 items)

---

#### Test 3.2 - Transaction Detail
**Étapes:**
1. Cliquer sur "Apple Store" transaction
2. Observer tous les détails
3. Vérifier les informations:
   - Montant: -8,450 MAD
   - Status: Completed
   - Category: Shopping
   - Date complète
4. Cliquer "Download" et "Share"

**Résultat attendu:**
- ✅ Icône circulaire rouge (expense)
- ✅ Montant en grand rouge
- ✅ Status badge vert "COMPLETED"
- ✅ Tous détails: ID, Title, Description, Date, Time, Category, Recipient
- ✅ Boutons Download & Share (UI)

**Données utilisées:**
- Transaction sélectionnée de `StaticTransactions`

---

#### Test 3.3 - Send Money
**Étapes:**
1. Home → Quick Action "Send"
2. Entrer montant: `500`
3. Observer favoris en carrousel
4. Cliquer sur "Mohammed ALAMI"
5. Voir checkmark doré
6. Ajouter note: "Remboursement"
7. Cliquer "Send Money"
8. Voir PIN Verification
9. Entrer PIN: `1234`

**Résultat attendu:**
- ✅ Input montant centré en grand
- ✅ "MAD" à côté
- ✅ 3 favoris en carrousel (⭐)
- ✅ Liste complète: 5 contacts
- ✅ Sélection visible (fond gold + checkmark)
- ✅ Note optionnelle
- ✅ Bouton "Send Money" enabled
- ✅ PIN Dialog s'affiche
- ✅ Succès après PIN correct

**Données utilisées:**
- `StaticContacts.contacts` (5 contacts)
- 3 favoris marqués `isFavorite = true`

---

#### Test 3.4 - Request Money
**Étapes:**
1. Naviguer vers Request Money
2. Entrer montant: `200`
3. Sélectionner "Fatima BENANI"
4. Ajouter raison: "D��ner resto"
5. Cliquer "Send Request"

**Résultat attendu:**
- ✅ Même UI que Send Money
- ✅ Input montant identique
- ✅ Liste contacts identique
- ✅ Raison optionnelle
- ✅ Bouton "Send Request"
- ✅ Success dialog après envoi

**Données utilisées:**
- `StaticContacts.contacts` (5 contacts)

---

#### Test 3.5 - Search
**Étapes:**
1. Transactions → Icône Search
2. Observer barre de recherche
3. Taper "Apple"
4. (UI only - pas de recherche réelle)

**Résultat attendu:**
- ✅ Barre de recherche en haut
- ✅ Placeholder: "Search transactions..."
- ✅ Icône recherche
- ✅ UI propre et moderne

**Données utilisées:** Aucune (UI placeholder)

---

### Phase 4: Gestion Cartes

#### Test 4.1 - All Cards
**Étapes:**
1. My Cards → Top bar "+" OU
2. Bottom Nav Cards → "Add New Card"
3. Observer les 3 cartes en liste
4. Voir détails complets de chaque carte
5. Observer les gradients différents
6. Cliquer sur carte "Add New Card"

**Résultat attendu:**
- ✅ 3 cartes affichées verticalement
- ✅ Chaque carte: gradient unique
- ✅ Carte 1: Navy Blue
- ✅ Carte 2: Gold
- ✅ Carte 3: Black
- ✅ Badge "DEFAULT" sur carte 1
- ✅ Toutes infos: Number, Holder, Expiry, Type, Balance
- ✅ Carte "Add" avec icon +

**Données utilisées:**
- `StaticCards.cards` (3 cartes)

---

#### Test 4.2 - Add New Card
**Étapes:**
1. Observer preview carte vide
2. Entrer Card Number: `4562112249459852`
3. Observer formatage auto: `4562 1122 4945 9852`
4. Voir preview se mettre à jour
5. Entrer Card Holder: "Mohammed ALAMI"
6. Entrer Expiry: `1228` → formatage `12/28`
7. Entrer CVV: `123`
8. Cliquer "Add Card"

**Résultat attendu:**
- ✅ Preview carte en haut (Navy gradient)
- ✅ Preview vide initial: **** **** **** ****
- ✅ Formatage automatique du numéro
- ✅ Preview met à jour en temps réel:
  - Numéro masqué sauf 4 derniers
  - Nom en majuscules
  - Date MM/YY
- ✅ Info sécurité affichée
- ✅ Validation: tous champs requis
- ✅ Success dialog après ajout

**Données utilisées:** Form inputs

---

### Phase 5: Profile & Paramètres

#### Test 5.1 - Profile
**Étapes:**
1. Naviguer vers Profile
2. Observer avatar avec initiales "YH"
3. Voir nom complet: "Yassir Hamzaoui"
4. Vérifier toutes les infos:
   - Email: test@aureus.com
   - Phone: +212 6 12 34 56 78
   - Address: 123 Rue Mohammed V
   - City: Casablanca
   - Country: Morocco
5. Cliquer "Edit" (top bar)

**Résultat attendu:**
- ✅ Avatar circulaire 100dp avec "YH"
- ✅ Fond gold transparent
- ✅ Nom en grand (24sp bold)
- ✅ Email en gris dessous
- ✅ 5 cards blanches avec infos
- ✅ Icônes pour chaque champ
- ✅ Bouton "Logout" rouge en bas

**Données utilisées:**
- `TestAccount.user` (toutes infos)

---

#### Test 5.2 - Edit Profile
**Étapes:**
1. Observer tous les champs pré-remplis
2. Cliquer sur avatar
3. Voir "Change Profile Photo"
4. Modifier First Name: "Yassir" → "Yassin"
5. Modifier Address: "123..." → "456 Avenue Hassan II"
6. Cliquer "Save Changes"
7. Observer Success dialog

**Résultat attendu:**
- ✅ Avatar avec bouton caméra en bas-droite
- ✅ Tous champs pré-remplis avec données user
- ✅ 6 OutlinedTextFields:
  - First Name, Last Name
  - Email, Phone
  - Address, City
- ✅ Icônes pour chaque champ
- ✅ Focus color: SecondaryGold
- ✅ Bouton "Save Changes" avec icône
- ✅ Success dialog: "Profile updated successfully"

**Données utilisées:**
- Initial: `TestAccount.user`
- Modifié: inputs utilisateur

---

#### Test 5.3 - Change Password
**Étapes:**
1. Settings → Change Password
2. Observer info card avec requirements
3. Entrer Current Password: `Test123456`
4. Entrer New Password: `NewPass123`
5. Toggle visibility pour voir password
6. Entrer Confirm: `NewPass12` (erreur volontaire)
7. Voir message d'erreur rouge
8. Corriger Confirm: `NewPass123`
9. Cliquer "Change Password"

**Résultat attendu:**
- ✅ Info card jaune avec requirements:
  - Au moins 8 caractères
  - Uppercase & lowercase
  - Inclure nombres
- ✅ 3 champs password avec toggle visibility
- ✅ Validation en temps réel:
  - Tous champs requis
  - 8+ caractères
  - Passwords match
  - Nouveau ≠ ancien
- ✅ Error card rouge si erreur
- ✅ Success dialog si OK

**Données utilisées:** Form inputs + validation

---

#### Test 5.4 - Language Selection
**Étapes:**
1. Settings → Language
2. Observer les 5 langues
3. Voir drapeaux emoji
4. English est sélectionné (checkmark)
5. Cliquer sur "Français"
6. Voir checkmark se déplacer
7. Retour automatique

**Résultat attendu:**
- ✅ 5 langues affichées:
  - 🇬🇧 English / English
  - 🇫🇷 French / Français
  - 🇲🇦 Arabic / العربية
  - 🇪🇸 Spanish / Español
  - 🇩🇪 German / Deutsch
- ✅ Drapeaux 32sp
- ✅ Nom + nom natif
- ✅ Sélection avec fond gold
- ✅ Checkmark doré sur sélection

**Données utilisées:**
- `SupportedLanguages.languages` (5 langues)

---

#### Test 5.5 - Terms & Conditions
**Étapes:**
1. Settings → Terms & Conditions
2. Défiler pour lire toutes les sections
3. Observer 9 sections
4. Voir date en bas
5. Voir copyright

**Résultat attendu:**
- ✅ Card blanche unique scrollable
- ✅ 9 sections:
  1. Introduction
  2. Account Registration
  3. Services
  4. Security
  5. Privacy
  6. Fees and Charges
  7. Liability
  8. Changes to Terms
  9. Contact
- ✅ Chaque section: titre bold + texte
- ✅ Divider avant footer
- ✅ "Last Updated: January 9, 2026"
- ✅ "© 2026 Aureus Bank"

**Données utilisées:** Texte statique (9 sections)

---

## 📊 TABLEAU RÉCAPITULATIF DES TESTS

| # | Page | Données Utilisées | Status |
|---|------|-------------------|--------|
| 1 | Splash | Aucune | ✅ |
| 2 | Onboarding | 4 slides | ✅ |
| 3 | Register | Form inputs | ✅ |
| 4 | SMS Verification | Code: 123456 | ✅ |
| 5 | PIN Setup | PIN: 1234 | ✅ |
| 6 | Login | test@aureus.com | ✅ |
| 7 | Home | 1 carte + 5 trx | ✅ |
| 8 | Statistics | 6 mois + catégories | ✅ |
| 9 | My Cards | 3 cartes | ✅ |
| 10 | All Cards | 3 cartes | ✅ |
| 11 | Add Card | Form inputs | ✅ |
| 12 | Transactions | 10 transactions | ✅ |
| 13 | Transaction Detail | 1 transaction | ✅ |
| 14 | Send Money | 5 contacts | ✅ |
| 15 | Request Money | 5 contacts | ✅ |
| 16 | Search | UI only | ✅ |
| 17 | Profile | TestAccount.user | ✅ |
| 18 | Edit Profile | Form pré-rempli | ✅ |
| 19 | Settings | UI + toggles | ✅ |
| 20 | Change Password | Form + validation | ✅ |
| 21 | Language | 5 langues | ✅ |
| 22 | Terms | Texte légal | ✅ |

**TOTAL: 22/22 pages testables** ✅

---

## ✅ VALIDATION FINALE

### Toutes les pages utilisent des données statiques:
- ✅ `StaticData.kt` contient tout
- ✅ Compte test: test@aureus.com / Test123456
- ✅ 3 cartes bancaires
- ✅ 10 transactions
- ✅ 5 contacts
- ✅ 6 mois de stats
- ✅ 6 catégories
- ✅ 5 langues

### Toutes les pages sont liées:
- ✅ Navigation complète
- ✅ Bottom Nav fonctionnelle
- ✅ Back buttons partout
- ✅ Success dialogs
- ✅ Error handling

### L'UI/UX est testable:
- ✅ Design cohérent Navy + Gold
- ✅ Animations fluides
- ✅ Formulaires validés
- ✅ Feedback visuel
- ✅ 0 crashes attendus

---

## 🎉 CONCLUSION

**L'application Aureus est 100% testable pour l'UI/UX avec des données statiques!**

Tous les écrans peuvent être navgués, testés et démontrés sans backend.

**Prêt pour:**
- ✅ Tests utilisateurs
- ✅ Démonstration client
- ✅ Portfolio
- ✅ Présentation investisseurs

---

**Happy Testing! 🚀**
