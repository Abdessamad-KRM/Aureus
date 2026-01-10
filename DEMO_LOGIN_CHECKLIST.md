# 🔐 Aureus App - Compte Démo & Guide de Connexion

---

## 📱 **COMPTE DÉMO POUR PRÉSENTATION CLIENT**

### ✅ **Identifiants de Connexion Direct (Bypass SMS/PIN)**

```
📧 Email:    demo@aureus.ma
🔑 Password: Demo1234!
```

**Ce compte permet de:**
- ✅ Se connecter directement au Dashboard
- ✅ Bypass la vérification SMS
- ✅ Bypass la configuration du PIN
- ✅ Accéder à TOUTES les fonctionnalités avec données fictives

---

## 🎯 **COMMENT UTILISER L'APP POUR PRÉSENTATION**

### **Option 1: Mode Démo Direct (Recommandé)**

1. **Lancer l'app**
2. **Sur l'écran Login:**
   - Email: `demo@aureus.ma`
   - Password: `Demo1234!`
3. **Cliquer "Sign In"**
4. **VOUS ÊTES DANS LE DASHBOARD!** 🎉

**C'est tout! Aucune étape supplémentaire.**

---

### **Option 2: Mode inscription Normal**

Pour montrer le flux complet au client:

1. **Lancer l'app**
2. **Passer l'Onboarding** (Get Started)
3. **Cliquer "Don't have an account? Sign up"**
4. **Remplir le formulaire:**
   - First Name: `Yassir`
   - Last Name: `Hamzaoui`
   - Email: `yassir.test@gmail.com`
   - Phone: `+212 6 61 23 45 67`
   - Password: `Test123456`
   - Confirm: `Test123456`
5. **SMS Verification:** Entrer `123456`
6. **PIN Setup:** Créer et confirmer `1234`
7. **Vous êtes dans le Dashboard!**

---

## 📊 **DONNÉES DISPONIBLES DANS L'APP**

### **Utilisateur Démo**
```
Nom:        Yassir Hamzaoui
Email:      demo@aureus.ma
Téléphone:  +212 6 61 23 45 67
Adresse:    Résidence Al Wifaq, Apt 12, Boulevard Zerktouni
Ville:      Casablanca
Pays:       Maroc
```

### **Cartes Bancaires (3 cartes)**

| Carte | Type | Solde | Couleur |
|-------|------|-------|---------|
| VISA **** 9852 | VISA | 85,545.00 MAD | Navy Blue |
| MASTERCARD **** 7823 | MASTERCARD | 42,180.50 MAD | Gold |
| VISA **** 3621 | VISA | 18,900.00 MAD | Black |

**Total Balance:** 146,625.50 MAD

### **Transactions Récentes (5)**

1. **Marjane** -2,850.00 MAD (Courses alimentaires)
2. **Meditel** -200.00 MAD (Recharge téléphone)
3. **Salaire Mensuel** +18,500.00 MAD (OCP Group)
4. **Acima** -5,400.00 MAD (Électroménager)
5. **Careem** -45.00 MAD (Course bureau)

### **Contacts Favoris (3)**

- ⭐ Mohammed EL ALAMI (+212 6 61 45 78 90)
- ⭐ Fatima-Zahra BENANI (+212 6 62 33 44 55)
- ⭐ Salma EL FASSI (+212 6 77 88 99 00)

### **Statistics (6 mois)**

- **Total Income:** 27,000.00 MAD (Juin)
- **Total Expense:** 14,685.00 MAD (Juin)
- **Dépenses:** 54% du revenu
- **Top catégories:** Shopping (33%), Food (28%), Bills (18%)

---

## 🎯 **CE QUE VOUS POUVEZ MONTRER AU CLIENT**

### **✅ Page Home/Dashboard**
- Solde total avec carte bancaire principale
- Actions rapides: Send, Request, Scan, More
- Mini graphique des dépenses
- Transactions récentes

### **✅ Page Statistics**
- Cercle animé de dépenses (54%)
- Graphique linéaire sur 6 mois
- Dépenses par catégorie avec icônes
- Légende Income vs Expenses

### **✅ Page My Cards**
- Vue de carte avec preview en grand
- Sélection entre 3 cartes (dots)
- Détails: numéro, holder, expiry, type, balance
- Bouton "Set as Default" et "Add New Card"

### **✅ Fonctionnalité Send Money**
- Entrer montant (MAD)
- Sélectionner contact parmi favoris
- Ajouter note optionnelle
- Confirmation avec PIN

### **✅ Fonctionnalité Request Money**
- Sélectionner contact chez qui demander
- Entrer montant
- Ajouter raison
- Envoi de la demande

### **✅ Add New Card**
- Entrer numéro (formatage automatique)
- Entrer card holder
- Entrer expiry MM/YY
- Entrer CVV
- Preview live de la carte

### **✅ All Transactions**
- Liste complète (10 transactions)
- Filtres: All, Income, Expense
- Couleurs: vert (revenus), rouge (dépenses)
- Recherche

### **✅ Settings**
- Change Password
- Language (5 langues)
- Notifications toggle
- Biometric toggle
- Terms & Conditions
- Logout

---

## 🎨 **DESIGN À MONTRER**

### **Palette de Couleurs**
- **Primary:** Navy Blue (#1E3A5F)
- **Accent:** Gold (#D4AF37)
- **Success:** Green (#10B981)
- **Error:** Red (#EF4444)
- **Background:** Light Gray (#F8FAFC)

### **Éléments UX**
- Bottom navigation avec 4 onglets
- Cards avec arrondis 16dp
- Gradients sur les cartes bancaires
- Animations fluides
- Badges "DEMO" visibles

---

## ⚠️ **IMPORTANT POUR PRÉSENTATION**

### **Ne PAS MONTRER (ou expliquer limites):**
- Données fictives/pré-enregistrées
- Pas de connexion backend réelle
- Pas de synchronisation avec serveur
- Transactions non persistées

### **À PRÉSENTER COMME:**
- Application avec mode "Démo / Offline"
- Cache local des données pour performance
- Interface complète et fonctionnelle

---

## 📞 **SUPPORT PENDANTS LA PRÉSENTATION**

Si vous rencontrez:
- ** Crash de l'app:** Vérifier Android Studio Logcat
- **Login ne marche pas:** Utiliser `demo@aureus.ma` / `Demo1234!`
- **Écran bloqué:** Vérifier que le compte démo est utilisé
- **Navigation ne marche pas:** Vérifier Navigation.kt

---

## 🎉 **RÉSUMÉ PRÉSENTATION CLIENT**

### **Flux Recommandé (3 min max)**

1. **Login avec compte démo** (30 sec)
   - Email: demo@aureus.ma
   - Password: Demo1234!
   - Direct au Dashboard

2. **Montrer Home** (45 sec)
   - Balance 146,625.50 MAD
   - Carte VISA Navy
   - 5 transactions récentes

3. **Montrer Statistics** (30 sec)
   - Cercle 54%
   - Graphique 6 mois
   - Catégories

4. **Montrer Send Money** (45 sec)
   - Sélectionner contact Mohammed
   - Montant 500 MAD
   - Confirmer

5. **Montrer Cards** (30 sec)
   - 3 cartes avec gradients
   - Changer entre cartes
   - Ajouter nouvelle carte

**Total:** 3 minutes complète!

---

**Bon courage pour votre présentation! 🚀**

L'application est prête à impressionner votre client! 💼✨