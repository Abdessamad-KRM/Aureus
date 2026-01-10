# 🚀 Guide du Splash Screen Aureus

Documentation complète du Splash Screen avec effets visuels premium.

---

## 🎨 Vue d'ensemble

Deux versions du Splash Screen ont été créées:

1. **`SplashScreen.kt`** - Version standard avec animations élégantes
2. **`SplashScreenAdvanced.kt`** - Version premium avec effets visuels avancés ⭐ **UTILISÉE**

---

## ✨ Fonctionnalités (Version Advanced)

### Effets Visuels

✅ **Gradient de fond animé**
- Dégradé vertical de bleu marine profond à bleu moyen
- Couleurs: `#0A1628` → `PrimaryNavyBlue` → `PrimaryMediumBlue`

✅ **Logo animé avec effets multiples**
- Animation de scale avec spring bounce
- Rotation d'entrée (-180° → 0°)
- Fade in progressif
- Effet de glow radial doré
- Cercle doré tournant autour du logo
- Animation de pulse subtile

✅ **Cercles concentriques animés**
- 4 cercles concentriques en arrière-plan
- Rotation lente et continue
- Opacité subtile pour effet de profondeur

✅ **Particules flottantes**
- 5 particules dorées animées
- Mouvements circulaires indépendants
- Effet de profondeur et dynamisme

✅ **Texte "AUREUS" avec effet shimmer**
- Taille: 52sp, ExtraBold
- Espacement de lettres: 6sp
- Effet de brillance animé
- Couleur: Blanc

✅ **Ligne dorée animée**
- Largeur: 140dp
- Effet de lumière qui traverse
- Animation continue

✅ **Slogans**
- "Votre Banque Digitale" - Or, 18sp
- "Prestige & Confiance" - Blanc transparent, 13sp

✅ **Indicateur de progression**
- 3 points dorés animés
- Animation en vague
- Gradient radial or → or foncé

---

## 🎬 Séquence d'Animation

### Timeline (3,5 secondes)

```
0.0s  ─┬─> Début des animations
      │
0.5s  ├─> Logo visible à 50%
      │   Cercles concentriques apparaissent
      │
0.8s  ├─> Texte commence à apparaître
      │
1.2s  ├─> Logo complètement visible
      │   Texte visible à 80%
      │
1.5s  ├─> Toutes les animations d'entrée terminées
      │   
      │   [Animations infinies actives]
      │   - Rotation des cercles
      │   - Particules flottantes
      │   - Shimmer sur texte
      │   - Ligne dorée animée
      │   - Pulse du logo
      │   - Indicateur de progression
      │
3.5s  └─> Navigation vers écran suivant
```

---

## 🎨 Palette de Couleurs Utilisée

| Élément | Couleur | Variable |
|---------|---------|----------|
| Background gradient | `#0A1628`, Navy, Medium Blue | Custom + Theme |
| Logo glow | Or avec 40% alpha | `SecondaryGold` |
| Cercle tournant | Or avec 80% alpha | `SecondaryGold` |
| Cercles concentriques | Or avec 5% alpha | `SecondaryGold` |
| Particules | Or avec 30% alpha | `SecondaryGold` |
| Texte principal | Blanc | `NeutralWhite` |
| Slogan principal | Or | `SecondaryGold` |
| Sous-titre | Blanc 80% | `NeutralWhite` |
| Ligne dorée | Or + gradient | `SecondaryGold` |
| Indicateur | Or + Or foncé | Gradient |

---

## 📱 Structure du Code

### SplashScreenAdvanced.kt

```
SplashScreenAdvanced()
├── AnimatedConcentricCircles() - Cercles en arrière-plan
├── FloatingParticles() - Particules flottantes
├── Column (contenu principal)
│   ├── Box (Logo container)
│   │   ├── Canvas (Glow radial)
│   │   ├── Canvas (Cercle doré tournant)
│   │   └── Image (Logo)
│   └── Column (Textes)
│       ├── ShimmerText("AUREUS")
│       ├── AnimatedGoldenLine()
│       ├── Text (Slogan)
│       └── Text (Sous-titre)
└── ProgressIndicatorGold() - Indicateur en bas
```

### Composants Réutilisables

1. **`AnimatedConcentricCircles`**
   - Cercles concentriques animés
   - Paramètres: rotation, alpha

2. **`FloatingParticles`**
   - Particules avec mouvement circulaire
   - Paramètre: alpha

3. **`ShimmerText`**
   - Texte avec effet de brillance
   - Paramètres: text, fontSize, fontWeight, letterSpacing

4. **`AnimatedGoldenLine`**
   - Ligne avec effet de lumière
   - Paramètre: width

5. **`ProgressIndicatorGold`**
   - 3 points animés en vague
   - Aucun paramètre

---

## 🔧 Configuration

### Durée d'Affichage

```kotlin
LaunchedEffect(Unit) {
    startAnimation = true
    delay(3500) // Durée en millisecondes
    onSplashFinished()
}
```

**Modifier la durée:**
```kotlin
delay(3500) // Changer cette valeur (ms)
```

### Navigation

Le Splash détermine automatiquement l'écran suivant:

```kotlin
val nextRoute = when {
    !onboardingViewModel.isOnboardingCompleted() -> Screen.Onboarding.route
    authViewModel.isLoggedIn -> Screen.Dashboard.route
    else -> Screen.Login.route
}
```

---

## 🎯 Personnalisation

### Changer le Logo

Remplacez `logo.png` dans `/res/drawable/` par votre logo.

Taille recommandée: **512x512px** minimum

### Modifier les Couleurs

```kotlin
// Gradient de fond
.background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A1628),      // Bleu très foncé
            PrimaryNavyBlue,        // Bleu marine
            PrimaryMediumBlue       // Bleu moyen
        )
    )
)

// Couleur du glow
SecondaryGold.copy(alpha = 0.4f)

// Couleur des particules
SecondaryGold.copy(alpha = 0.3f)
```

### Ajuster les Animations

```kotlin
// Vitesse de rotation des cercles
animation = tween(20000) // Plus petit = plus rapide

// Vitesse du shimmer
animation = tween(2000) // Ajuster ici

// Intensité du pulse
initialValue = 0.95f,
targetValue = 1.05f, // Augmenter pour plus de pulse
```

---

## 📊 Performance

### Optimisations Appliquées

✅ **Canvas au lieu de composables**
- Cercles et particules dessinés sur Canvas
- Meilleure performance

✅ **Alpha pour désactiver les animations**
- Les éléments invisibles ne sont pas animés

✅ **Transitions infinies limitées**
- Nombre minimal de transitions infinies

✅ **Remember pour éviter recompositions**
- Utilisation de `remember` et `LaunchedEffect`

### Impact sur la Performance

| Effet | Impact | Note |
|-------|--------|------|
| Gradient de fond | Minimal | ✅ |
| Logo animé | Faible | ✅ |
| Cercles concentriques | Faible | ✅ |
| Particules (5) | Faible | ✅ |
| Shimmer texte | Minimal | ✅ |
| Ligne animée | Minimal | ✅ |
| Indicateur progression | Minimal | ✅ |
| **TOTAL** | **Faible** | ✅ Optimisé |

---

## 🎬 Versions du Splash

### Version Standard (`SplashScreen.kt`)

**Effets:**
- Animation scale + fade du logo
- Texte animé avec slide
- Points de chargement animés
- Gradient de fond
- Effet de glow simple

**Quand l'utiliser:**
- Appareils bas de gamme
- Préférence pour simplicité
- Durée d'affichage courte (<2s)

### Version Advanced (`SplashScreenAdvanced.kt`) ⭐

**Effets:**
- Tous les effets de la version standard
- Cercles concentriques animés
- Particules flottantes
- Cercle doré tournant
- Effet shimmer sur texte
- Ligne dorée animée
- Animation de pulse
- Indicateur de progression stylisé

**Quand l'utiliser:**
- Application premium (notre cas)
- Appareils moyens/haut de gamme
- Expérience visuelle riche souhaitée

---

## 🔄 Changement de Version

### Utiliser la Version Standard

Dans `Navigation.kt`:

```kotlin
import com.example.aureus.ui.splash.SplashScreen

// Dans composable
composable(Screen.Splash.route) {
    SplashScreen( // Au lieu de SplashScreenAdvanced
        onSplashFinished = { /* ... */ }
    )
}
```

### Utiliser la Version Advanced (Actuel)

```kotlin
import com.example.aureus.ui.splash.SplashScreenAdvanced

composable(Screen.Splash.route) {
    SplashScreenAdvanced(
        onSplashFinished = { /* ... */ }
    )
}
```

---

## 🎨 Exemples de Personnalisation

### Exemple 1: Splash Minimaliste

```kotlin
// Supprimer les particules
// Commenter FloatingParticles()

// Réduire les cercles concentriques
// Modifier l'alpha à 0.1f

// Texte simple sans shimmer
Text(
    text = "AUREUS",
    fontSize = 52.sp,
    color = NeutralWhite
)
```

### Exemple 2: Splash Ultra Premium

```kotlin
// Ajouter plus de particules
val particles = listOf(
    // ... ajouter plus d'Offset
)

// Augmenter l'intensité du glow
drawCircle(
    brush = Brush.radialGradient(
        colors = listOf(
            SecondaryGold.copy(alpha = 0.6f), // Augmenter
            // ...
        )
    )
)
```

### Exemple 3: Changer le Gradient

```kotlin
// Gradient doré
.background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2C1810),
            SecondaryDarkGold,
            SecondaryGold
        )
    )
)
```

---

## 📱 Tests & Preview

### Tester le Splash

1. **Run l'application**
   - Le splash s'affiche automatiquement au démarrage

2. **Forcer l'affichage**
   - Clear app data
   - Redémarrer l'application

3. **Ajuster la durée pour tests**
   ```kotlin
   delay(1000) // 1 seconde pour tests rapides
   ```

### Preview dans Android Studio

Le Splash ne peut pas être prévisualisé directement car il utilise:
- Navigation
- ViewModels
- Resources (logo)

**Alternative:** Créer un composant de preview:

```kotlin
@Preview
@Composable
fun PreviewSplashEffects() {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedConcentricCircles(rotation = 45f, alpha = 1f)
    }
}
```

---

## 🐛 Troubleshooting

### Le logo ne s'affiche pas

**Problème:** Image non trouvée
**Solution:** Vérifier que `logo.png` existe dans `/res/drawable/`

### Animations saccadées

**Problème:** Performance
**Solutions:**
1. Utiliser la version standard
2. Réduire le nombre de particules
3. Simplifier les animations

### Navigation ne fonctionne pas

**Problème:** Route incorrecte
**Solution:** Vérifier `Screen.Splash.route` dans Navigation.kt

### Durée trop courte/longue

**Problème:** Timing
**Solution:** Ajuster `delay(3500)` dans LaunchedEffect

---

## ✅ Checklist d'Intégration

- [x] Splash Screen standard créé
- [x] Splash Screen advanced créé
- [x] Navigation mise à jour
- [x] Route Splash ajoutée
- [x] Logo présent dans drawable
- [x] Couleurs du thème utilisées
- [x] Animations optimisées
- [x] Documentation complète

---

## 🎉 Résultat

**Version Advanced** est actuellement active avec:
- ✅ Gradient de fond élégant
- ✅ Logo avec 5 effets visuels
- ✅ Cercles concentriques animés
- ✅ 5 particules flottantes
- ✅ Texte avec effet shimmer
- ✅ Ligne dorée animée
- ✅ Indicateur de progression stylisé
- ✅ Navigation automatique intelligente
- ✅ Durée optimale de 3,5 secondes

**Expérience utilisateur premium garantie!** 🚀✨

---

*Design System Aureus v1.0 - Janvier 2026* 🎨
