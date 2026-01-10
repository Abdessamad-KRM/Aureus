# 🎨 Guide de la Palette de Couleurs Aureus

Documentation complète de la palette de couleurs pour l'application bancaire Aureus.

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Couleurs Primaires](#couleurs-primaires)
3. [Couleurs Secondaires](#couleurs-secondaires)
4. [Couleurs Sémantiques](#couleurs-sémantiques)
5. [Couleurs Neutres](#couleurs-neutres)
6. [Variantes et Opacités](#variantes-et-opacités)
7. [Gradients](#gradients)
8. [Guide d'utilisation](#guide-dutilisation)
9. [Exemples de code](#exemples-de-code)

---

## Vue d'ensemble

La palette de couleurs Aureus est conçue pour transmettre **confiance, prestige et professionnalisme** dans une application bancaire moderne.

### Philosophie des Couleurs

- **Bleu Marine**: Confiance, sécurité, professionnalisme
- **Or**: Prestige, qualité premium, valeur
- **Vert**: Succès, croissance, positif
- **Rouge**: Attention, urgence, négatif
- **Ambre**: Prudence, avertissement
- **Neutres**: Clarté, lisibilité, élégance

---

## Couleurs Primaires

### 🔵 Primary Navy Blue `#1E3A5F`

**Couleur principale de la marque**

```kotlin
import com.example.aureus.ui.theme.PrimaryNavyBlue
```

**Utilisations:**
- ✅ Boutons d'action principaux
- ✅ En-têtes et barres de navigation
- ✅ Éléments de branding
- ✅ Icônes importantes
- ✅ Liens et éléments interactifs

**Exemples:**
```kotlin
Button(colors = ButtonDefaults.buttonColors(
    containerColor = PrimaryNavyBlue
)) { Text("Transférer") }

TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
    containerColor = PrimaryNavyBlue
))
```

---

### 🔷 Primary Medium Blue `#2C5F8D`

**États actifs et hover**

```kotlin
import com.example.aureus.ui.theme.PrimaryMediumBlue
```

**Utilisations:**
- ✅ États actifs (tabs, selections)
- ✅ Hover states sur éléments primaires
- ✅ États "pressed" sur boutons
- ✅ Indicateurs de focus

**Exemples:**
```kotlin
TabRow(
    selectedTabIndex = selectedTab,
    containerColor = PrimaryMediumBlue
)

Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryNavyBlue,
        // Hover state
        pressedContainerColor = PrimaryMediumBlue
    )
)
```

---

## Couleurs Secondaires

### 🟡 Secondary Gold `#D4AF37`

**Accent premium et prestige**

```kotlin
import com.example.aureus.ui.theme.SecondaryGold
```

**Utilisations:**
- ✅ Éléments premium (comptes premium, offres spéciales)
- ✅ Soldes positifs et montants importants
- ✅ Badges et labels spéciaux
- ✅ Highlights et éléments de mise en valeur
- ✅ Icônes de récompenses

**Exemples:**
```kotlin
// Badge premium
Badge(
    containerColor = SecondaryGold
) { Text("Premium") }

// Solde positif
Text(
    text = "+1,500 €",
    color = SecondaryGold,
    fontWeight = FontWeight.Bold
)

// Icône de carte premium
Icon(
    imageVector = Icons.Default.CreditCard,
    tint = SecondaryGold
)
```

---

### 🟨 Secondary Dark Gold `#C89F3C`

**États hover pour éléments dorés**

```kotlin
import com.example.aureus.ui.theme.SecondaryDarkGold
```

**Utilisations:**
- ✅ Hover states sur boutons dorés
- ✅ États pressed sur éléments premium
- ✅ Variante plus foncée pour contraste

**Exemples:**
```kotlin
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = SecondaryGold,
        pressedContainerColor = SecondaryDarkGold
    )
) { Text("Activer Premium") }
```

---

## Couleurs Sémantiques

### 🟢 Semantic Green `#10B981`

**Positif, succès, validation**

```kotlin
import com.example.aureus.ui.theme.SemanticGreen
```

**Utilisations:**
- ✅ Transactions entrantes (+)
- ✅ Messages de succès
- ✅ Validations
- ✅ Soldes en hausse
- ✅ États "Actif" ou "Validé"

**Exemples:**
```kotlin
// Transaction entrante
Row {
    Icon(Icons.Default.ArrowDownward, tint = SemanticGreen)
    Text("+250 €", color = SemanticGreen)
}

// Message de succès
AlertDialog(
    containerColor = SemanticGreen.copy(alpha = 0.1f),
    title = { Text("Succès!", color = SemanticGreen) }
)

// Indicateur de statut
StatusBadge(
    text = "Actif",
    backgroundColor = SemanticGreen,
    textColor = NeutralWhite
)
```

---

### 🔴 Semantic Red `#EF4444`

**Négatif, erreurs, alertes**

```kotlin
import com.example.aureus.ui.theme.SemanticRed
```

**Utilisations:**
- ✅ Transactions sortantes (-)
- ✅ Messages d'erreur
- ✅ Alertes critiques
- ✅ Actions destructives (supprimer, annuler)
- ✅ Soldes négatifs

**Exemples:**
```kotlin
// Transaction sortante
Row {
    Icon(Icons.Default.ArrowUpward, tint = SemanticRed)
    Text("-150 €", color = SemanticRed)
}

// Bouton de suppression
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = SemanticRed
    )
) { Text("Supprimer") }

// Message d'erreur
Text(
    text = "Transaction échouée",
    color = SemanticRed,
    style = MaterialTheme.typography.bodySmall
)
```

---

### 🟠 Semantic Amber `#F59E0B`

**Avertissements, attention**

```kotlin
import com.example.aureus.ui.theme.SemanticAmber
```

**Utilisations:**
- ✅ Avertissements
- ✅ Solde faible
- ✅ Actions requises
- ✅ Informations importantes
- ✅ États "En attente"

**Exemples:**
```kotlin
// Alerte solde faible
Card(
    colors = CardDefaults.cardColors(
        containerColor = SemanticAmber.copy(alpha = 0.1f)
    )
) {
    Row {
        Icon(Icons.Default.Warning, tint = SemanticAmber)
        Text("Solde faible", color = SemanticAmber)
    }
}

// Badge en attente
Badge(
    containerColor = SemanticAmber
) { Text("En attente") }
```

---

## Couleurs Neutres

### ⚪ Neutral White `#FFFFFF`

**Backgrounds cards et surfaces**

```kotlin
import com.example.aureus.ui.theme.NeutralWhite
```

**Utilisations:**
- ✅ Fond des cartes
- ✅ Dialogs et modals
- ✅ Surfaces élevées
- ✅ Backgrounds de contenus

---

### ⬜ Neutral Light Gray `#F8FAFC`

**Background général de l'application**

```kotlin
import com.example.aureus.ui.theme.NeutralLightGray
```

**Utilisations:**
- ✅ Fond d'écran principal
- ✅ Fond de sections
- ✅ Séparateurs subtils

---

### 🔘 Neutral Medium Gray `#64748B`

**Textes secondaires**

```kotlin
import com.example.aureus.ui.theme.NeutralMediumGray
```

**Utilisations:**
- ✅ Textes secondaires
- ✅ Descriptions
- ✅ Labels de formulaire
- ✅ Placeholders

---

### ⚫ Neutral Dark Gray `#1E293B`

**Textes principaux**

```kotlin
import com.example.aureus.ui.theme.NeutralDarkGray
```

**Utilisations:**
- ✅ Textes principaux
- ✅ Titres
- ✅ Contenus importants
- ✅ Montants financiers

---

## Variantes et Opacités

Pour les états désactivés, overlays et effets subtils:

```kotlin
import com.example.aureus.ui.theme.ColorVariants

// Opacités primaires
ColorVariants.PrimaryNavyBlue10  // 10% opacité
ColorVariants.PrimaryNavyBlue20  // 20% opacité
ColorVariants.PrimaryNavyBlue50  // 50% opacité
ColorVariants.PrimaryNavyBlue70  // 70% opacité

// Opacités secondaires
ColorVariants.SecondaryGold10
ColorVariants.SecondaryGold20
ColorVariants.SecondaryGold50

// Opacités sémantiques
ColorVariants.SemanticGreen10
ColorVariants.SemanticGreen20
ColorVariants.SemanticRed10
ColorVariants.SemanticRed20
ColorVariants.SemanticAmber10
ColorVariants.SemanticAmber20

// Opacités neutres
ColorVariants.NeutralMediumGray50
ColorVariants.NeutralMediumGray70
```

**Exemples d'utilisation:**

```kotlin
// Background subtil
Box(
    modifier = Modifier.background(ColorVariants.PrimaryNavyBlue10)
)

// Overlay
Box(
    modifier = Modifier.background(ColorVariants.PrimaryNavyBlue50)
)

// Badge avec fond transparent
Surface(
    color = ColorVariants.SemanticGreen20,
    shape = RoundedCornerShape(8.dp)
) {
    Text("Nouveau", color = SemanticGreen)
}
```

---

## Gradients

Gradients prédéfinis pour les fonds premium:

```kotlin
import com.example.aureus.ui.theme.AppGradients
import androidx.compose.ui.graphics.Brush

// Gradient primaire (Bleu marine → Bleu moyen)
val primaryGradient = Brush.linearGradient(
    colors = AppGradients.PrimaryGradient
)

// Gradient or (Or → Or foncé)
val goldGradient = Brush.linearGradient(
    colors = AppGradients.GoldGradient
)

// Gradient succès
val successGradient = Brush.linearGradient(
    colors = AppGradients.SuccessGradient
)

// Gradient premium (Bleu → Or)
val premiumGradient = Brush.linearGradient(
    colors = AppGradients.PremiumGradient
)
```

**Exemples d'utilisation:**

```kotlin
// Carte avec gradient
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(
            brush = Brush.linearGradient(
                colors = AppGradients.PremiumGradient
            )
        )
)

// Bouton avec gradient
Button(
    modifier = Modifier.background(
        brush = Brush.horizontalGradient(
            colors = AppGradients.GoldGradient
        ),
        shape = RoundedCornerShape(12.dp)
    )
) { Text("Upgrade Premium") }
```

---

## Guide d'utilisation

### Hiérarchie Visuelle

```
1. Primaire (PrimaryNavyBlue) → Actions principales, navigation
2. Secondaire (SecondaryGold) → Accents premium, highlights
3. Sémantique → Feedback utilisateur
4. Neutres → Textes, backgrounds
```

### Règles de Contraste

Pour l'accessibilité (WCAG 2.1):

| Couleur | Sur fond blanc | Sur fond foncé | Ratio minimum |
|---------|---------------|----------------|---------------|
| PrimaryNavyBlue | ✅ Excellent | ❌ Éviter | 4.5:1 |
| SecondaryGold | ⚠️ Acceptable | ✅ Bon | 3:1 |
| SemanticGreen | ✅ Bon | ⚠️ Moyen | 4.5:1 |
| SemanticRed | ✅ Bon | ⚠️ Moyen | 4.5:1 |
| NeutralDarkGray | ✅ Excellent | ❌ Éviter | 7:1 |

### Combinaisons Recommandées

**Boutons primaires:**
```kotlin
containerColor = PrimaryNavyBlue
contentColor = NeutralWhite
```

**Boutons secondaires:**
```kotlin
containerColor = SecondaryGold
contentColor = PrimaryNavyBlue
```

**Cards:**
```kotlin
containerColor = NeutralWhite
contentColor = NeutralDarkGray
border = ColorVariants.PrimaryNavyBlue10
```

**Headers:**
```kotlin
containerColor = PrimaryNavyBlue
contentColor = NeutralWhite
accent = SecondaryGold
```

---

## Exemples de code

### Exemple 1: Card de compte bancaire

```kotlin
@Composable
fun AccountCard(account: Account) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NeutralWhite
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête avec type de compte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = account.name,
                    color = NeutralDarkGray,
                    fontWeight = FontWeight.Bold
                )
                if (account.isPremium) {
                    Badge(containerColor = SecondaryGold) {
                        Text("Premium", color = NeutralWhite)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Numéro de compte
            Text(
                text = account.number,
                color = NeutralMediumGray,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Solde
            Text(
                text = formatAmount(account.balance),
                color = if (account.balance >= 0) SemanticGreen else SemanticRed,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

### Exemple 2: Transaction Item

```kotlin
@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icône selon le type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (transaction.type == TransactionType.INCOMING)
                            ColorVariants.SemanticGreen20
                        else
                            ColorVariants.SemanticRed20,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == TransactionType.INCOMING)
                        Icons.Default.ArrowDownward
                    else
                        Icons.Default.ArrowUpward,
                    tint = if (transaction.type == TransactionType.INCOMING)
                        SemanticGreen
                    else
                        SemanticRed,
                    contentDescription = null
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = transaction.description,
                    color = NeutralDarkGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.date,
                    color = NeutralMediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Montant
        Text(
            text = "${if (transaction.type == TransactionType.INCOMING) "+" else "-"}${transaction.amount} €",
            color = if (transaction.type == TransactionType.INCOMING)
                SemanticGreen
            else
                SemanticRed,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### Exemple 3: Bouton d'action principal

```kotlin
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryNavyBlue,
            contentColor = NeutralWhite,
            disabledContainerColor = ColorVariants.PrimaryNavyBlue20,
            disabledContentColor = ColorVariants.NeutralMediumGray50
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

### Exemple 4: Alert Banner

```kotlin
@Composable
fun AlertBanner(
    message: String,
    type: AlertType
) {
    val (backgroundColor, textColor, icon) = when (type) {
        AlertType.SUCCESS -> Triple(
            ColorVariants.SemanticGreen20,
            SemanticGreen,
            Icons.Default.CheckCircle
        )
        AlertType.ERROR -> Triple(
            ColorVariants.SemanticRed20,
            SemanticRed,
            Icons.Default.Error
        )
        AlertType.WARNING -> Triple(
            ColorVariants.SemanticAmber20,
            SemanticAmber,
            Icons.Default.Warning
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

---

## 📚 Ressources

### Fichiers liés
- `app/src/main/java/com/example/aureus/ui/theme/Color.kt` - Définitions des couleurs
- `app/src/main/java/com/example/aureus/ui/theme/Theme.kt` - Thème de l'application

### Outils recommandés
- [Coolors](https://coolors.co) - Générateur de palettes
- [Adobe Color](https://color.adobe.com) - Roue chromatique
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/) - Vérification accessibilité

---

## ✅ Checklist d'utilisation

Avant d'utiliser une couleur, vérifiez:

- [ ] La couleur correspond au contexte (action, feedback, info)
- [ ] Le contraste est suffisant pour l'accessibilité
- [ ] La couleur suit la hiérarchie visuelle
- [ ] La couleur est cohérente avec le design system
- [ ] Utilisation des variantes appropriées (opacité, hover)

---

**Note**: Cette palette est le fondement du design system Aureus. Toute modification doit être documentée et validée pour maintenir la cohérence de l'application. 🎨
