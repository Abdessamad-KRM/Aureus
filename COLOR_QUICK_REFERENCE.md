# 🎨 Quick Reference - Couleurs Aureus

Guide de référence rapide pour utiliser les couleurs dans l'application.

## 📦 Import

```kotlin
import com.example.aureus.ui.theme.*
```

## 🎯 Couleurs par Contexte

### Boutons

```kotlin
// Bouton principal
Button(colors = ButtonDefaults.buttonColors(
    containerColor = PrimaryNavyBlue,
    contentColor = NeutralWhite
))

// Bouton secondaire
Button(colors = ButtonDefaults.buttonColors(
    containerColor = SecondaryGold,
    contentColor = PrimaryNavyBlue
))

// Bouton de succès
Button(colors = ButtonDefaults.buttonColors(
    containerColor = SemanticGreen,
    contentColor = NeutralWhite
))

// Bouton de danger
Button(colors = ButtonDefaults.buttonColors(
    containerColor = SemanticRed,
    contentColor = NeutralWhite
))

// Bouton désactivé
Button(
    enabled = false,
    colors = ButtonDefaults.buttonColors(
        disabledContainerColor = ColorVariants.PrimaryNavyBlue20
    )
)
```

### Cards

```kotlin
// Card standard
Card(colors = CardDefaults.cardColors(
    containerColor = NeutralWhite
))

// Card avec bord coloré
Card(
    colors = CardDefaults.cardColors(containerColor = NeutralWhite),
    border = BorderStroke(1.dp, PrimaryNavyBlue)
)

// Card premium
Card(
    modifier = Modifier.background(
        brush = Brush.linearGradient(AppGradients.GoldGradient)
    )
)
```

### Textes

```kotlin
// Titre principal
Text(
    text = "Titre",
    color = NeutralDarkGray,
    fontWeight = FontWeight.Bold
)

// Texte secondaire
Text(
    text = "Description",
    color = NeutralMediumGray
)

// Montant positif
Text(
    text = "+500 €",
    color = SemanticGreen,
    fontWeight = FontWeight.Bold
)

// Montant négatif
Text(
    text = "-150 €",
    color = SemanticRed,
    fontWeight = FontWeight.Bold
)

// Solde premium
Text(
    text = "2,500 €",
    color = SecondaryGold,
    fontWeight = FontWeight.Bold
)
```

### Backgrounds

```kotlin
// Background principal
Surface(color = NeutralLightGray)

// Background card
Surface(color = NeutralWhite)

// Background avec overlay
Box(modifier = Modifier.background(
    ColorVariants.PrimaryNavyBlue50
))

// Background subtil
Box(modifier = Modifier.background(
    ColorVariants.PrimaryNavyBlue10
))
```

### Icons

```kotlin
// Icône principale
Icon(
    imageVector = Icons.Default.Home,
    tint = PrimaryNavyBlue
)

// Icône de succès
Icon(
    imageVector = Icons.Default.CheckCircle,
    tint = SemanticGreen
)

// Icône d'erreur
Icon(
    imageVector = Icons.Default.Error,
    tint = SemanticRed
)

// Icône d'avertissement
Icon(
    imageVector = Icons.Default.Warning,
    tint = SemanticAmber
)

// Icône premium
Icon(
    imageVector = Icons.Default.Star,
    tint = SecondaryGold
)

// Icône secondaire
Icon(
    imageVector = Icons.Default.Info,
    tint = NeutralMediumGray
)
```

### Badges

```kotlin
// Badge premium
Badge(containerColor = SecondaryGold) {
    Text("Premium", color = NeutralWhite)
}

// Badge actif
Badge(containerColor = SemanticGreen) {
    Text("Actif", color = NeutralWhite)
}

// Badge en attente
Badge(containerColor = SemanticAmber) {
    Text("En attente", color = NeutralWhite)
}

// Badge inactif
Badge(containerColor = SemanticRed) {
    Text("Inactif", color = NeutralWhite)
}
```

### Dividers

```kotlin
// Divider standard
Divider(color = NeutralLightGray)

// Divider visible
Divider(color = NeutralMediumGray.copy(alpha = 0.3f))

// Divider coloré
Divider(color = PrimaryNavyBlue.copy(alpha = 0.1f))
```

### Progress Indicators

```kotlin
// Indicateur primaire
CircularProgressIndicator(color = PrimaryNavyBlue)

// Indicateur de succès
LinearProgressIndicator(color = SemanticGreen)

// Indicateur premium
CircularProgressIndicator(color = SecondaryGold)
```

## 📊 Transactions

```kotlin
// Transaction entrante
Row {
    Icon(
        Icons.Default.ArrowDownward,
        tint = SemanticGreen
    )
    Text(
        "+250 €",
        color = SemanticGreen,
        fontWeight = FontWeight.Bold
    )
}

// Transaction sortante
Row {
    Icon(
        Icons.Default.ArrowUpward,
        tint = SemanticRed
    )
    Text(
        "-150 €",
        color = SemanticRed,
        fontWeight = FontWeight.Bold
    )
}
```

## 🎨 Gradients

```kotlin
// Gradient primaire (header)
Box(modifier = Modifier.background(
    brush = Brush.verticalGradient(
        colors = AppGradients.PrimaryGradient
    )
))

// Gradient or (card premium)
Box(modifier = Modifier.background(
    brush = Brush.horizontalGradient(
        colors = AppGradients.GoldGradient
    )
))

// Gradient premium (splash)
Box(modifier = Modifier.background(
    brush = Brush.linearGradient(
        colors = AppGradients.PremiumGradient
    )
))
```

## 🔔 Alertes & Notifications

```kotlin
// Alerte de succès
Card(
    colors = CardDefaults.cardColors(
        containerColor = ColorVariants.SemanticGreen20
    )
) {
    Row {
        Icon(Icons.Default.CheckCircle, tint = SemanticGreen)
        Text("Succès!", color = SemanticGreen)
    }
}

// Alerte d'erreur
Card(
    colors = CardDefaults.cardColors(
        containerColor = ColorVariants.SemanticRed20
    )
) {
    Row {
        Icon(Icons.Default.Error, tint = SemanticRed)
        Text("Erreur", color = SemanticRed)
    }
}

// Alerte d'avertissement
Card(
    colors = CardDefaults.cardColors(
        containerColor = ColorVariants.SemanticAmber20
    )
) {
    Row {
        Icon(Icons.Default.Warning, tint = SemanticAmber)
        Text("Attention", color = SemanticAmber)
    }
}
```

## 🏦 États de Compte

```kotlin
// Compte actif
StatusIndicator(
    color = SemanticGreen,
    text = "Actif"
)

// Compte suspendu
StatusIndicator(
    color = SemanticAmber,
    text = "Suspendu"
)

// Compte fermé
StatusIndicator(
    color = SemanticRed,
    text = "Fermé"
)

// Compte premium
StatusIndicator(
    color = SecondaryGold,
    text = "Premium"
)
```

## 🎯 Navigation

```kotlin
// Top App Bar
TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = PrimaryNavyBlue,
        titleContentColor = NeutralWhite,
        actionIconContentColor = SecondaryGold
    )
)

// Bottom Navigation
NavigationBar(
    containerColor = NeutralWhite
) {
    NavigationBarItem(
        selected = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryNavyBlue,
            selectedTextColor = PrimaryNavyBlue,
            unselectedIconColor = NeutralMediumGray,
            unselectedTextColor = NeutralMediumGray,
            indicatorColor = ColorVariants.PrimaryNavyBlue10
        ),
        // ...
    )
}

// Tab Row
TabRow(
    selectedTabIndex = 0,
    containerColor = NeutralWhite,
    contentColor = PrimaryNavyBlue,
    indicator = { tabPositions ->
        TabRowDefaults.Indicator(
            color = SecondaryGold
        )
    }
)
```

## 📋 Formulaires

```kotlin
// TextField standard
OutlinedTextField(
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryNavyBlue,
        unfocusedBorderColor = NeutralMediumGray,
        focusedLabelColor = PrimaryNavyBlue,
        cursorColor = PrimaryNavyBlue
    )
)

// TextField avec erreur
OutlinedTextField(
    isError = true,
    colors = OutlinedTextFieldDefaults.colors(
        errorBorderColor = SemanticRed,
        errorLabelColor = SemanticRed
    )
)

// Checkbox
Checkbox(
    colors = CheckboxDefaults.colors(
        checkedColor = PrimaryNavyBlue,
        checkmarkColor = NeutralWhite
    )
)

// Switch
Switch(
    colors = SwitchDefaults.colors(
        checkedThumbColor = SecondaryGold,
        checkedTrackColor = ColorVariants.SecondaryGold20
    )
)
```

## 💡 Tips

### ✅ À Faire

```kotlin
// Bon contraste
Text(text = "Titre", color = NeutralDarkGray)

// États clairs
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryNavyBlue,
        disabledContainerColor = ColorVariants.PrimaryNavyBlue20
    )
)

// Feedback visuel approprié
Text("+500€", color = SemanticGreen)
Text("-200€", color = SemanticRed)
```

### ❌ À Éviter

```kotlin
// Mauvais contraste
Text(text = "Titre", color = NeutralLightGray) // ❌

// Couleur sémantique incorrecte
Text("+500€", color = SemanticRed) // ❌

// Trop de couleurs différentes
Card {
    Text("Titre", color = PrimaryNavyBlue)
    Text("Sous-titre", color = SecondaryGold)
    Text("Description", color = SemanticGreen)
    Text("Note", color = SemanticAmber)
} // ❌ Trop chargé
```

## 🔍 Debugging

```kotlin
// Voir toutes les couleurs
Column {
    Text("Primary Navy", color = PrimaryNavyBlue)
    Text("Medium Blue", color = PrimaryMediumBlue)
    Text("Gold", color = SecondaryGold)
    Text("Dark Gold", color = SecondaryDarkGold)
    Text("Green", color = SemanticGreen)
    Text("Red", color = SemanticRed)
    Text("Amber", color = SemanticAmber)
    Text("Dark Gray", color = NeutralDarkGray)
    Text("Medium Gray", color = NeutralMediumGray)
}
```

## 📚 Documentation Complète

Pour plus de détails, voir `COLOR_PALETTE_GUIDE.md`

---

**Gardez ce fichier ouvert pendant le développement!** 🎨
