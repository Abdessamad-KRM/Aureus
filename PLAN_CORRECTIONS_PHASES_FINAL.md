# 🏆 PLAN DE CORRECTIONS - FINALE (PHASES 11-15)
**Date**: 11 Janvier 2026
**Basé sur**: PLAN_CORRECTIONS_PHASES.md + SUITE
**Objectif**: Finaliser l'app avec production-ready features

---

## 📊 SITUATION ACTUELLE APRÈS PHASES 1-10

Après avoir complété les phases 1-10, l'app devrait être:
- ✅ **100% fonctionnelle** pour toutes les features core
- ✅ **100% dynamique** avec Firebase
- ✅ **Offline-First** avec Room
- ✅ **Notifications Push** FCM
- ✅ **Biometric Auth** (Fingerprint/FaceID)
- ✅ **Charts Pro** avec VICO
- ⚠️ **Manque**: Analytics, Dark Mode complet, i18n, Tests, Optimization

---

## 📈 PHASE 11: ANALYTICS ET MONITORING (2-3 jours)

### Objectif
Implémenter Firebase Analytics pour tracker les behaviors utilisateurs et Firebase Performance Monitoring

---

#### ÉTAPE 11.1: Configuration Firebase Analytics

**Ajouter à build.gradle.kts (app)**:

```kotlin
dependencies {
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    
    // Firebase Performance Monitoring
    implementation("com.google.firebase:firebase-perf-ktx:20.4.0")
    
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.4.0")
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
}
```

**Modifier AndroidManifest.xml**:

```xml
<manifest>
    <application>
        <!-- Performance Monitoring -->
        <meta-data
            android:name="firebase_performance_enabled"
            android:value="true" />
        
        <!-- Crashlytics -->
        <meta-data
            android:name="firebase_crashlytics_enabled"
            android:value="true" />
    </application>
</manifest>
```

---

#### ÉTAPE 11.2: Créer AnalyticsManager

**Fichier**: `app/src/main/java/com/example/aureus/analytics/AnalyticsManager.kt`

```kotlin
package com.example.aureus.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics Manager - Gère tous les events et metrics
 */
@Singleton
class AnalyticsManager @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics = Firebase.crashlytics

    // ==================== AUTH EVENTS ====================

    /**
     * Tracker l'inscription utilisateur
     */
    fun trackSignUp(method: String, userId: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SIGN_UP,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker le login utilisateur
     */
    fun trackLogin(method: String, userId: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker logout
     */
    fun trackLogout(userId: String) {
        firebaseAnalytics.logEvent(
            "logout",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    // ==================== TRANSACTION EVENTS ====================

    /**
     * Tracker création de transaction
     */
    fun trackTransactionCreated(
        userId: String,
        type: String,
        category: String,
        amount: Double,
        method: String // "card", "transfer", "cash"
    ) {
        firebaseAnalytics.logEvent(
            "transaction_created",
            Bundle().apply {
                putString("user_id", userId)
                putString("type", type)
                putString("category", category)
                putDouble("amount", amount)
                putString("payment_method", method)
            }
        )
    }

    /**
     * Tracker échec transaction
     */
    fun trackTransactionFailed(userId: String, error: String) {
        firebaseAnalytics.logEvent(
            "transaction_failed",
            Bundle().apply {
                putString("user_id", userId)
                putString("error", error)
            }
        )
    }

    // ==================== TRANSFER EVENTS ====================

    /**
     * Tracker l'envoi d'argent
     */
    fun trackTransferSent(
        userId: String,
        amount: Double,
        recipient: String,
        method: String
    ) {
        firebaseAnalytics.logEvent(
            "transfer_sent",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("amount", amount)
                putString("recipient", recipient)
                putString("method", method)
            }
        )
    }

    /**
     * Tracker réception d'argent
     */
    fun trackTransferReceived(
        userId: String,
        amount: Double,
        sender: String
    ) {
        firebaseAnalytics.logEvent(
            "transfer_received",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("amount", amount)
                putString("sender", sender)
            }
        )
    }

    // ==================== CARD EVENTS ====================

    /**
     * Tracker ajout de carte
     */
    fun trackCardAdded(userId: String, cardType: String) {
        firebaseAnalytics.logEvent(
            "card_added",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_type", cardType)
            }
        )
    }

    /**
     * Tracker blocage de carte
     */
    fun trackCardBlocked(userId: String, cardId: String, reason: String) {
        firebaseAnalytics.logEvent(
            "card_blocked",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_id", cardId)
                putString("reason", reason)
            }
        )
    }

    // ==================== BILL PAYMENT EVENTS ====================

    /**
     * Tracker paiement de facture
     */
    fun trackBillPaid(
        userId: String,
        billType: String,
        amount: Double,
        method: String
    ) {
        firebaseAnalytics.logEvent(
            "bill_paid",
            Bundle().apply {
                putString("user_id", userId)
                putString("bill_type", billType)
                putDouble("amount", amount)
                putString("payment_method", method)
            }
        )
    }

    // ==================== CONTACT EVENTS ====================

    /**
     * Tracker ajout de contact
     */
    fun trackContactAdded(userId: String) {
        firebaseAnalytics.logEvent(
            "contact_added",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker supression de contact
     */
    fun trackContactRemoved(userId: String) {
        firebaseAnalytics.logEvent(
            "contact_removed",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    // ==================== BIOMETRIC EVENTS ====================

    /**
     * Tracker utilisation biométrie
     */
    fun trackBiometricUsed(userId: String, success: Boolean) {
        firebaseAnalytics.logEvent(
            "biometric_auth",
            Bundle().apply {
                putString("user_id", userId)
                putString("success", success.toString())
            }
        )
    }

    // ==================== SCREEN VIEW EVENTS ====================

    /**
     * Tracker vue d'écran
     */
    fun trackScreenView(screenName: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString("screen_class", screenName)
            }
        )
    }

    // ==================== ERROR TRACKING ====================

    /**
     * Logger une erreur (non-fatal)
     */
    fun trackError(
        tag: String,
        message: String,
        userId: String?
    ) {
        crashlytics.apply {
            setCustomKey("tag", tag)
            setCustomKey("user_id", userId ?: "guest")
            recordException(Exception(message))
        }
    }

    /**
     * Logger une exception avec contexte
     */
    fun trackException(exception: Exception, userId: String?, context: Map<String, String> = emptyMap()) {
        crashlytics.apply {
            userId?.let { setUserId(it) }
            context.forEach { (key, value) ->
                setCustomKey(key, value)
            }
            recordException(exception)
        }
    }

    // ==================== PERFORMANCE TRACKING ====================

    /**
     * Démarrer un trace pour performance
     */
    fun startTrace(traceName: String): Trace {
        return FirebasePerformance.getInstance().newTrace(traceName).apply {
            start()
        }
    }

    /**
     * Ajouter un attribut à un trace
     */
    fun putTraceAttribute(trace: Trace, key: String, value: String) {
        trace.putAttribute(key, value)
    }

    /**
     * Tracker une opération avec trace
     */
    inline fun <T> trackOperation(
        operation: String,
        userId: String?,
        block: () -> T
    ): T {
        val trace = FirebasePerformance.getInstance().newTrace(operation).apply {
            start()
            if (userId != null) {
                putAttribute("user_id", userId)
            }
        }
        
        return try {
            val result = block()
            trace.stop()
            result
        } catch (e: Exception) {
            trace.putAttribute("error", e.message ?: "Unknown error")
            trace.stop()
            throw e
        }
    }

    // ==================== CUSTOM EVENTS ====================

    /**
     * Tracker balance check
     */
    fun trackBalanceCheck(userId: String, balance: Double, source: String) {
        firebaseAnalytics.logEvent(
            "balance_check",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("balance", balance)
                putString("source", source)
            }
        )
    }

    /**
     * Tracker setting change
     */
    fun trackSettingChanged(userId: String, setting: String, oldValue: String, newValue: String) {
        firebaseAnalytics.logEvent(
            "setting_changed",
            Bundle().apply {
                putString("user_id", userId)
                putString("setting", setting)
                putString("old_value", oldValue)
                putString("new_value", newValue)
            }
        )
    }

    /**
     * Tracker notification opened
     */
    fun trackNotificationOpened(userId: String, notificationType: String) {
        firebaseAnalytics.logEvent(
            "notification_opened",
            Bundle().apply {
                putString("user_id", userId)
                putString("notification_type", notificationType)
            }
        )
    }

    // ==================== USER PROPERTIES ====================

    /**
     * Définir User ID pour analytics
     */
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    /**
     * Définir des User Properties personnalisés
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Initialiser les User Properties d'un utilisateur
     */
    fun setUserProperties(
        userId: String,
        accountType: String,
        country: String,
        preferredLanguage: String
    ) {
        setUserId(userId)
        setUserProperty("account_type", accountType)
        setUserProperty("country", country)
        setUserProperty("preferred_language", preferredLanguage)
    }
}
```

---

#### ÉTAPE 11.3: Intégrer Analytics dans les écrans

**Exemple: HomeScreen**

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    analyticsManager: AnalyticsManager = hiltViewModel(),
    ...
) {
    val userId = viewModel.getCurrentUserId()
    
    // Track screen view
    LaunchedEffect(Unit) {
        analyticsManager.trackScreenView("HomeScreen")
    }
    
    // Track balance check
    LaunchedEffect(uiState.totalBalance) {
        analyticsManager.trackBalanceCheck(
            userId = userId,
            balance = uiState.totalBalance,
            source = "home_dashboard"
        )
    }
    
    // ...
}
```

**Exemple: Transaction Actions**

```kotlin
fun sendMoney(amount: Double, recipient: String, userId: String) {
    analyticsManager.trackOperation(
        operation = "send_money",
        userId = userId
    ) {
        val success = performTransfer(amount, recipient)
        if (success) {
            analyticsManager.trackTransferSent(
                userId = userId,
                amount = amount,
                recipient = recipient,
                method = "wallet_to_wallet"
            )
        } else {
            analyticsManager.trackTransactionFailed(
                userId = userId,
                error = "transfer_failed"
            )
        }
        success
    }
}
```

---

#### ÉTAPE 11.4: Configurer Firebase Console

1. **Activer Analytics**:
   - Firebase Console → Project Settings → Analytics → Enabled

2. **Activer Performance Monitoring**:
   - Firebase Console → Project Settings → Performance → Enabled

3. **Activer Crashlytics**:
   - Firebase Console → Project Settings → Crashlytics → Enabled

4. **Créer BigQuery Export**:
   - Firebase Console → Analytics → BigQuery → Link project
   - Export data pour analyse avancée

---

#### ÉTAPE 11.5: Test Analytics

**Test Scenarios**:

1. **Auth Events**:
   - Register → Firebase Console → Analytics → Sign Up event
   - Login → Login event
   - Logout → Logout event

2. **Transaction Events**:
   - Create transaction → transaction_created event
   - Failed transaction → transaction_failed event

3. **Performance**:
   - Ouvrir Firebase Console → Performance
   - Vérifier traces de chargement des données
   - Vérifier latence des appels API

4. **Crashlytics**:
   - Forcer crash dans app
   - Vérifier Firebase Console → Crashlytics
   - Vérifier stacktrace et contexte utilisateur

---

## 🌙 PHASE 12: DARK MODE COMPLET (2 jours)

### Objectif
Implémenter un dark mode complet avec Material 3 et persistance des préférences

---

#### ÉTAPE 12.1: Définir Theme Colors Dark

**Fichier**: `app/src/main/java/com/example/aureus/ui/theme/Color.kt`

```kotlin
/**
 * Dark Theme Colors
 */
object DarkThemeColors {
    // Primary Colors
    val PrimaryNavyBlueDark = Color(0xFF0F172A)
    val PrimaryMediumBlueDark = Color(0xFF1E293B)
    val PrimaryLightBlueDark = Color(0xFF334155)
    
    // Secondary
    val SecondaryGoldDark = Color(0xFFD4AF37)
    val SecondaryDarkGoldDark = Color(0xFFB8960C)
    
    // Neutral Colors
    val NeutralBlackDark = Color(0xFF000000)
    val NeutralDarkGrayDark = Color(0xFF1F2937)
    val NeutralMediumGrayDark = Color(0xFF4B5563)
    val NeutralLightGrayDark = Color(0xFF9CA3AF)
    val NeutralWhiteDark = Color(0xFFE5E7EB)
    
    // Semantic Colors
    val SemanticGreenDark = Color(0xFF10B981)
    val SemanticRedDark = Color(0xFFEF4444)
    val SemanticYellowDark = Color(0xFFF59E0B)
    val SemanticBlueDark = Color(0xFF3B82F6)
}
```

---

#### ÉTAPE 12.2: Créer Dark Theme

**Modifier `Theme.kt`**:

```kotlin
@Composable
fun AureusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val typography = if (darkTheme) DarkTypography else LightTypography
    
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}

private val DarkColorPalette = darkColorScheme(
    primary = DarkThemeColors.PrimaryNavyBlueDark,
    secondary = DarkThemeColors.SecondaryGoldDark,
    tertiary = DarkThemeColors.PrimaryLightBlueDark,
    background = DarkThemeColors.PrimaryNavyBlueDark,
    surface = DarkThemeColors.PrimaryMediumBlueDark,
    error = DarkThemeColors.SemanticRedDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkThemeColors.NeutralWhiteDark,
    onSurface = DarkThemeColors.NeutralWhiteDark,
    onError = Color.White,
)

interface AppColorScheme {
    val primaryNavyBlue: Color
    val primaryMediumBlue: Color
    val primaryLightBlue: Color
    val secondaryGold: Color
    val secondaryDarkGold: Color
    val neutralBlack: Color
    val neutralDarkGray: Color
    val neutralMediumGray: Color
    val neutralLightGray: Color
    val neutralWhite: Color
    val semanticGreen: Color
    val semanticRed: Color
    val semanticYellow: Color
    val semanticBlue: Color
    val isDark: Boolean
}

private class AppColors(
    isDark: Boolean
) : AppColorScheme {
    override val primaryNavyBlue = if (isDark) DarkThemeColors.PrimaryNavyBlueDark else LightThemeColors.PrimaryNavyBlue
    override val primaryMediumBlue = if (isDark) DarkThemeColors.PrimaryMediumBlueDark else LightThemeColors.PrimaryMediumBlue
    override val primaryLightBlue = if (isDark) DarkThemeColors.PrimaryLightBlueDark else LightThemeColors.PrimaryLightBlue
    override val secondaryGold = if (isDark) DarkThemeColors.SecondaryGoldDark else LightThemeColors.SecondaryGold
    override val secondaryDarkGold = if (isDark) DarkThemeColors.SecondaryDarkGoldDark else LightThemeColors.SecondaryDarkGold
    override val neutralBlack = if (isDark) DarkThemeColors.NeutralBlackDark else LightThemeColors.NeutralBlack
    override val neutralDarkGray = if (isDark) DarkThemeColors.NeutralDarkGrayDark else LightThemeColors.NeutralDarkGray
    override val neutralMediumGray = if (isDark) DarkThemeColors.NeutralMediumGrayDark else LightThemeColors.NeutralMediumGray
    override val neutralLightGray = if (isDark) DarkThemeColors.NeutralLightGrayDark else LightThemeColors.NeutralLightGray
    override val neutralWhite = if (isDark) DarkThemeColors.NeutralWhiteDark else LightThemeColors.NeutralWhite
    override val semanticGreen = if (isDark) DarkThemeColors.SemanticGreenDark else LightThemeColors.SemanticGreen
    override val semanticRed = if (isDark) DarkThemeColors.SemanticRedDark else LightThemeColors.SemanticRed
    override val semanticYellow = if (isDark) DarkThemeColors.SemanticYellowDark else LightThemeColors.SemanticYellow
    override val semanticBlue = if (isDark) DarkThemeColors.SemanticBlueDark else LightThemeColors.SemanticBlue
    override val isDark = isDark
}
```

---

#### ÉTAPE 12.3: Créer ThemeManager

**Fichier**: `app/src/main/java/com/example/aureus/ui/theme/ThemeManager.kt`

```kotlin
package com.example.aureus.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

/**
 * Gestionnaire du theme (Dark/Light)
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Obtenir le flux du mode sombre
     */
    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    
    /**
     * Changer le mode sombre
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
        
        // Appliquer le thème système
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Appliquer le thème système actuel
     */
    suspend fun applySystemTheme() {
        val isSystemDark = isSystemInDarkTheme()
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isSystemDark
        }
    }
    
    private fun isSystemInDarkTheme(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_AUTO -> {
                // Check system theme
                val systemTheme = android.content.res.Configuration.UI_MODE_NIGHT_MASK
                val nightMode = context.resources.configuration.uiMode and systemTheme
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
}
```

---

#### ÉTAPE 12.4: Créer ThemeToggle Component

**Fichier**: `app/src/main/java/com/example/aureus/ui/components/ThemeToggle.kt`

```kotlin
package com.example.aureus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aureus.ui.theme.*

/**
 * Toggle Theme Switcher
 */
@Composable
fun ThemeToggle(
    themeManager: ThemeManager,
    isDark: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E293B) else Color(0xFFF3F4F6),
        label = "background"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (isDark) SecondaryGold else PrimaryNavyBlue,
        label = "thumb"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDark) 1.1f else 1f,
        label = "scale"
    )

    val icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isDark) "Dark" else "Light",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) NeutralWhiteDark else NeutralDarkGray
        )

        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isDark) NeutralMediumGrayDark else NeutralMediumGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxHeight()
                    .width(24.dp)
                    .align(
                        if (isDark) Alignment.CenterEnd else Alignment.CenterStart
                    )
                    .scale(scale)
                    .background(
                        color = thumbColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Theme",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
```

---

#### ÉTAPE 12.5: Intégrer Theme dans MainActivity

**Modifier `MainActivity.kt`**:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    
    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Observer le thème
        lifecycleScope.launch {
            themeManager.darkMode.collect { isDark ->
                setAppTheme(isDark)
            }
        }
        
        setContent {
            AureusTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    onboardingViewModel = onboardingViewModel
                )
            }
        }
        
        // ... existing code ...
    }
}

private fun ComponentActivity.setAppTheme(isDark: Boolean) {
    when {
        isDark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
```

---

#### ÉTAPE 12.6: Test Dark Mode

**Test Scenarios**:

1. **Toggle Switch**:
   - Settings → Theme Toggle
   - Cliquer sur toggle
   - Vérifier: Theme change instantanément

2. **System Theme**:
   - Settings → "Use System Theme"
   - Changer theme système device
   - Vérifier: App s'adapte automatiquement

3. **All Screens**:
   - Vérifier: Tous les écrans en dark mode
   - Vérifier: Contraste suffisant (accessibilité)

4. **Persistence**:
   - Changer thème
   - Fermer app
   - Rouvrir app
   - Vérifier: Thème sauvegardé

---

## 🌍 PHASE 13: INTERNATIONALIZATION (I18N) (3-4 jours)

### Objectif
Supporter plusieurs langues (FR, EN, AR, ES, DE) avec persistance des préférences

---

#### ÉTAPE 13.1: Créer Fichiers Strings

**FR - Français (existant)**: `app/src/main/res/values/strings.xml`

**EN - Anglais**: `app/src/main/res/values-en/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Aureus</string>
    
    <!-- Authentication -->
    <string name="login">Login</string>
    <string name="register">Sign Up</string>
    <string name="email">Email Address</string>
    <string name="password">Password</string>
    <string name="first_name">First Name</string>
    <string name="last_name">Last Name</string>
    <string name="phone">Phone Number</string>
    <string name="logout">Logout</string>
    
    <!-- Dashboard -->
    <string name="welcome_back">Welcome back</string>
    <string name="total_balance">Total Balance</string>
    
    <!-- Transactions -->
    <string name="transactions">Transactions</string>
    <string name="recent_transactions">Recent Transactions</string>
    <string name="income">Income</string>
    <string name="expense">Expense</string>
    
    <!-- Cards -->
    <string name="my_cards">My Cards</string>
    <string name="add_card">Add New Card</string>
    
    <!-- Transfer -->
    <string name="send_money">Send Money</string>
    <string name="request_money">Request Money</string>
    
    <!-- Statistics -->
    <string name="statistics">Statistics</string>
    <string name="monthly_trends">Monthly Trends</string>
    
    <!-- General -->
    <string name="loading">Loading...</string>
    <string name="error">Error</string>
    <string name="retry">Retry</string>
    <string name="cancel">Cancel</string>
    <string name="confirm">Confirm</string>
    <string name="delete">Delete</string>
    
    <!-- Biometric -->
    <string name="biometric_title">Unlock Aureus Banking</string>
    <string name="biometric_subtitle">Use your fingerprint to continue</string>
    <string name="biometric_description">Touch the sensor to verify your identity</string>
    
    <!-- Offline -->
    <string name="offline_mode">Offline Mode</string>
    <string name="offline_message">You are offline. Some features may be limited.</string>
    <string name="connecting">Connecting...</string>
</resources>
```

**AR - Arabe**: `app/src/main/res/values-ar/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">أوريوس</string>
    
    <string name="login">تسجيل الدخول</string>
    <string name="register">إنشاء حساب</string>
    <string name="email">البريد الإلكتروني</string>
    <string name="password">كلمة المرور</string>
    <string name="first_name">الاسم الأول</string>
    <string name="last_name">اسم العائلة</string>
    <string name="phone">رقم الهاتف</string>
    <string name="logout">تسجيل الخروج</string>
    
    <string name="welcome_back">مرحبًا بعودتك</string>
    <string name="total_balance">الرصيد الإجمالي</string>
    
    <string name="transactions">المعاملات</string>
    <string name="recent_transactions">المعاملات الأخيرة</string>
    <string name="income">الدخل</string>
    <string name="expense">المصروفات</string>
    
    <string name="my_cards">بطاقاتي</string>
    <string name="add_card">إضافة بطاقة جديدة</string>
    
    <string name="send_money">إرسال الأموال</string>
    <string name="request_money">طلب الأموال</string>
    
    <string name="statistics">الإحصائيات</string>
    <string name="monthly_trends">الاتجاهات الشهرية</string>
    
    <!-- General -->
    <string name="loading">جاري التحميل...</string>
    <string name="error">خطأ</string>
    <string name="retry">إعادة المحاولة</string>
    <string name="cancel">إلغاء</string>
    <string name="confirm">تأكيد</string>
    <string name="delete">حذف</string>
    
    <string name="biometric_title">إلغاء قفل أوريوس بنك</string>
    <string name="biometric_subtitle">استخدم بصمة إصبعك للمتابعة</string>
    <string name="biometric_description">المس على المستشعر للتحقق من هويتك</string>
    
    <string name="offline_mode">وضع عدم الاتصال</string>
    <string name="offline_message">أنت غير متصل. بعض الميزات قد تكون محدودة.</string>
</resources>
```

**ES - Espagnol**: `app/src/main/res/values-es/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Aureus</string>
    
    <string name="login">Iniciar Sesión</string>
    <string name="register">Registrarse</string>
    <string name="email">Correo Electrónico</string>
    <string name="password">Contraseña</string>
    <string name="first_name">Nombre</string>
    <string name="last_name">Apellido</string>
    <string name="phone">Número de Teléfono</string>
    <string name="logout">Cerrar Sesión</string>
    
    <string name="welcome_back">Bienvenido de nuevo</string>
    <string name="total_balance">Saldo Total</string>
    
    <string name="transactions">Transacciones</string>
    <string name="recent_transactions">Transacciones Recientes</string>
    <string name="income">Ingresos</string>
    <string name="expense">Gastos</string>
    
    <string name="my_cards">Mis Tarjetas</string>
    <string name="add_card">Agregar Nueva Tarjeta</string>
    
    <string name="send_money">Enviar Dinero</string>
    <string name="request_money">Solicitar Dinero</string>
    
    <string name="statistics">Estadísticas</string>
    <string name="monthly_trends">Tendencias Mensuales</string>
    
    <!-- General -->
    <string name="loading">Cargando...</string>
    <string name="error">Error</string>
    <string name="retry">Reintentar</string>
    <string name="cancel">Cancelar</string>
    <string name="confirm">Confirmar</string>
    <string name="delete">Eliminar</string>
    
    <string name="biometric_title">Desbloquear Aureus Banking</string>
</resources>
```

**DE - Allemand**: `app/src/main/res/values-de/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Aureus</string>
    
    <string name="login">Anmelden</string>
    <string name="register">Registrieren</string>
    <string name="email">E-Mail-Adresse</string>
    <string name="password">Passwort</string>
    <string name="first_name">Vorname</string>
    <string name="last_name">Nachname</string>
    <string name="phone">Telefonnummer</string>
    <string name="logout">Abmelden</string>
    
    <string name="welcome_back">Willkommen zurück</string>
    <string name="total_balance">Gesamtsaldo</string>
    
    <string name="transactions">Transaktionen</string>
    <string name="recent_transactions">Letzte Transaktionen</string>
    <string name="income">Einkommen</string>
    <string name="expense">Ausgaben</string>
    
    <string name="my_cards">Meine Karten</string>
    <string name="add_card">Neue Karte Hinzufügen</string>
    
    <string name="send_money">Geld Senden</string>
    <string name="request_money">Geld Anfordern</string>
    
    <string name="statistics">Statistiken</string>
    <string name="monthly_trends">Monatliche Trends</string>
    
    <!-- General -->
    <string name="loading">Wird geladen...</string>
    <string name="error">Fehler</string>
    <string name="retry">Wiederholen</string>
    <string name="cancel">Abbrechen</string>
    <string name="confirm">Bestätigen</string>
    <string name="delete">Löschen</string>
    
    <string name="biometric_title">Aureus Banking Entsperren</string>
</resources>
```

---

#### ÉTAPE 13.2: Créer LanguageManager

**Fichier**: `app/src/main/java/com/example/aureus/i18n/LanguageManager.kt`

```kotlin
package com.example.aureus.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

private val LANGUAGE_KEY = stringPreferencesKey("language")

/**
 * Langues supportées
 */
enum class Language(val code: String, val displayName: String, val flag: String) {
    FRENCH("fr", "Français", "🇫🇷"),
    ENGLISH("en", "English", "🇬🇧"),
    ARABIC("ar", "العربية", "🇲🇦"),
    SPANISH("es", "Español", "🇪🇸"),
    GERMAN("de", "Deutsch", "🇩🇪");
    
    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * Gestionnaire de langue
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Obtenir la langue actuelle
     */
    val currentLanguage: Flow<Language> = context.languageDataStore.data
        .map { preferences ->
            Language.fromCode(preferences[LANGUAGE_KEY] ?: "fr")
        }
    
    /**
     * Changer la langue
     */
    suspend fun setLanguage(languageCode: String) {
        val language = Language.fromCode(languageCode)
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        
        applyLanguage(language)
    }
    
    /**
     * Appliquer la langue
     */
    fun applyLanguage(language: Language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Pour RTL (Arabe)
        if (language == Language.ARABIC) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
    
    /**
     * Localiser une chaîne
     */
    fun getString(resId: Int): String {
        return context.getString(resId)
    }
    
    /**
     * Localiser une chaîne avec formatage
     */
    fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
    
    /**
     * Vérifier si la langue sera RTL
     */
    fun isRTL(): Boolean {
        val direction = context.resources.configuration.layoutDirection
        return direction == android.view.View.LAYOUT_DIRECTION_RTL
    }
    
    /**
     * Créer une configuration RTL pour Arabe
     */
    fun createConfiguration(language: Language): Configuration {
        return Configuration().apply {
            setLocale(Locale(language.code))
            if (language == Language.ARABIC) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    setLayoutDirection(android.view.View.LAYOUT_DIRECTION_RTL)
                }
            }
        }
    }
}
```

---

#### ÉTAPE 13.3: Créer LanguageSelector

**Fichier**: `app/src/main/java/com/example/aureus/ui/components/LanguageSelector.kt`

```kotlin
package com.example.aureus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aureus.i18n.Language
import com.example.aureus.i18n.LanguageManager
import com.example.aureus.ui.theme.*

/**
 * Sélecteur de langue
 */
@Composable
fun LanguageSelector(
    languageManager: LanguageManager,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onLanguageSelected: (Language) -> Unit = {}
) {
    val currentLanguage by languageManager.currentLanguage.collectAsState(
        initial = Language.FRENCH
    )
    
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (languageManager.isRTL()) PrimaryNavyBlue else NeutralWhite
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Language / Langue / اللغة / Idioma / Sprache",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (languageManager.isRTL()) NeutralWhiteDark else PrimaryNavyBlue
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(
                        color = if (languageManager.isRTL()) NeutralWhiteDark.copy(alpha = 0.2f) else NeutralMediumGray.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Language.values()) { language ->
                            LanguageItem(
                                language = language,
                                isSelected = language == selectedLanguage,
                                onClick = {
                                    selectedLanguage = language
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            onLanguageSelected(selectedLanguage)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Confirm / Confirmer / تأكيد / Confirmar / Bestätigen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                SecondaryGold.copy(alpha = 0.2f) 
            else if (language == Language.ARABIC) 
                PrimaryNavyBlue 
            else NeutralWhite
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(
                1.dp, 
                SecondaryGold
            )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = language.flag,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = language.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (language == Language.ARABIC) NeutralWhiteDark else PrimaryNavyBlue
                    )
                    Text(
                        text = language.code.uppercase(),
                        fontSize = 12.sp,
                        color = if (language == Language.ARABIC) NeutralWhiteDark.copy(alpha = 0.7f) else NeutralMediumGray
                    )
                }
            }
            
            if (isSelected) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (language == Language.ARABIC) NeutralWhiteDark else PrimaryNavyBlue
                )
            }
        }
    }
}
```

---

#### ÉTAPE 13.4: Intégrer RTL Support

**Modifier les layouts pour RTL**:

```kotlin
@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val languageManager: LanguageManager = hiltViewModel()
    val isRTL by languageManager.isRTL().collectAsState(false)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = if (isRTL) Alignment.End else Alignment.Start
        ),
        content = content
    )
}

// Exemple d'utilisation dans SendMoneyScreenFirebase.kt
@Composable
fun SendMoneyScreenFirebase(
    viewModel: ContactViewModel,
    languageManager: LanguageManager,
    ...
) {
    val isRTL by languageManager.isRTL().collectAsState(false)
    
    // Utiliser Arrangement selon RTL
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isRTL) {
            Arrangement.spacedBy(8.dp, Alignment.End)
        } else {
            Arrangement.spacedBy(8.dp, Alignment.Start)
        }
    ) {
        // Content
    }
}
```

---

#### ÉTAPE 13.5: Test Internationalization

**Test Scenarios**:

1. **Language Selection**:
   - Settings → Language
   - Sélectionner EN/FR/AR/ES/DE
   - Vérifier: Langue appliquée instantanément

2. **FR (Français)**:
   - Texte en français
   - LTR layout

3. **AR (Arabe)**:
   - Texte en arabe
   - RTL layout
   - Icons à droite

4. **Persistence**:
   - Changer langue
   - Fermer app
   - Rouvrir app
   - Vérifier: Langue sauvegardée

5. **All Screens**:
   - Vérifier: Tous les screens localisés
   - Vérifier: Pas de hardcoded strings

---

## ���� PHASE 14: UNIT TESTS + UI TESTS (3-4 jours)

### Objectif
Créer des tests unitaires et UI tests pour garantir la qualité du code

---

#### ÉTAPE 14.1: Configuration Tests

**Ajouter à build.gradle.kts (app)**:

```kotlin
dependencies {
    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("app.cash.sqldelight:android-driver:2.0.0")
    
    // UI Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.47")
    kaptTest("com.google.dagger:hilt-android-compiler:2.47")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.47")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.47")
}
```

---

#### ÉTAPE 14.2: Créer HiltTestRule

**Fichier**: `app/src/androidTest/java/com/example/aureus/HiltTestRule.kt`

```kotlin
package com.example.aureus

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

@HiltTestApplication
class HiltTestApplication : Application()

// Custom Test Runner
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: android.content.Context?
    ): Application {
        return HiltTestApplication()
    }
}
```

---

#### ÉTAPE 14.3: Créer Tests ViewModels

**Tous les ViewModels**:

Exemple: `app/src/test/java/com/example/aureus/ui/home/viewmodel/HomeViewModelTest.kt`

```kotlin
package com.example.aureus.ui.home.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var firebaseDataManager: FirebaseDataManager

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(firebaseDataManager.currentUserId()).thenReturn("test_user_id")
        
        viewModel = HomeViewModel(firebaseDataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCurrentUserName should return first name when available`() = runTest {
        // Given
        val mockUserData = mapOf(
            "firstName" to "John",
            "lastName" to "Doe"
        )
        
        // When
        setupViewModelWithUser(mockUserData)
        val result = viewModel.getCurrentUserName()
        
        // Then
        assertEquals("John", result)
    }

    @Test
    fun `getCurrentUserName should return User when no first name`() = runTest {
        // Given
        val mockUserData = mapOf(
            "firstName" to "",
            "lastName" to ""
        )
        
        // When
        setupViewModelWithUser(mockUserData)
        val result = viewModel.getCurrentUserName()
        
        // Then
        assertEquals("User", result)
    }

    @Test
    fun `sendMoney should return success on valid transaction`() = runTest {
        // Given
        whenever(
            firebaseDataManager.createTransaction(any())
        ).thenReturn(Result.success("txn_123"))

        // When
        val result = viewModel.sendMoney(100.0, "recipient@test.com").collect(
            initial = null,
            transform = { it }
        )

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Money sent to recipient@test.com!", result.getOrNull())
    }

    @Test
    fun `sendMoney should return error when user not logged in`() = runTest {
        // Given
        whenever(firebaseDataManager.currentUserId()).thenReturn(null)

        // When
        val result = viewModel.sendMoney(100.0, "recipient@test.com").collect(
            initial = null,
            transform = { it }
        )

        // Then
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `refreshData should reload all data`() = runTest {
        // When
        viewModel.refreshData()

        // Then
        verify(firebaseDataManager, atLeastOnce()).getUserCards("test_user_id")
        verify(firebaseDataManager, atLeastOnce()).getUserTransactions("test_user_id")
    }

    private fun setupViewModelWithUser(userData: Map<String, Any>) {
        // Setup mock data flows
        val userFlow = kotlinx.coroutines.flowOf(userData)
        whenever(firebaseDataManager.getUser("test_user_id")).thenReturn(userFlow)
        
        val cardsFlow = kotlinx.coroutines.flowOf(listOf(mockCardData()))
        whenever(firebaseDataManager.getUserCards("test_user_id")).thenReturn(cardsFlow)
        
        val transactionsFlow = kotlinx.coroutines.flowOf(listOf(mockTransactionData()))
        whenever(firebaseDataManager.getUserTransactions("test_user_id", any())).thenReturn(transactionsFlow)
        
        viewModel = HomeViewModel(firebaseDataManager)
    }

    private fun mockCardData(): Map<String, Any> {
        return mapOf(
            "cardId" to "card_123",
            "cardNumber" to "4242",
            "cardHolder" to "John Doe",
            "expiryDate" to "12/28",
            "cardType" to "VISA",
            "isDefault" to true
        )
    }

    private fun mockTransactionData(): Map<String, Any> {
        return mapOf(
            "transactionId" to "txn_123",
            "type" to "EXPENSE",
            "title" to "Coffee",
            "amount" to 25.0,
            "category" to "Food & Drink",
            "createdAt" to Timestamp.now()
        )
    }
}
```

---

#### ÉTAPE 14.4: Créer UI Tests

**Tous les écrans principaux**:

Exemple: `app/src/androidTest/java/com/example/aureus/ui/auth/LoginScreenTest.kt`

```kotlin
package com.example.aureus.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aureus.ui.auth.screen.LoginScreen
import com.example.aureus.ui.theme.AureusTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var onLoginSuccessCalled: Boolean
    private lateinit var onNavigateToRegisterCalled: Boolean

    @Before
    fun setup() {
        hiltRule.inject()
        onLoginSuccessCalled = false
        onNavigateToRegisterCalled = false
    }

    @Test
    fun loginScreen_displaysTitle() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { onLoginSuccessCalled = true },
                    onNavigateToRegister = { onNavigateToRegisterCalled = true }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsEmailAndPasswordFields() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { },
                    onNavigateToRegister = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Email Address")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Password")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_enablesSignInButtonWithValidCredentials() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { },
                    onNavigateToRegister = { }
                )
            }
        }

        // Enter valid credentials
        composeTestRule
            .onNodeWithText("your@email.com")
            .performTextInput("test@example.com")
        
        composeTestRule
            .onNodeWithText("•••••••••")
            .performTextInput("password123")

        // Then
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_disablesSignInButtonWithoutCredentials() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { },
                    onNavigateToRegister = { }
                )
            }
        }

        // Then - button should be disabled with empty fields
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_showsGoogleSignInButton() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { },
                    onNavigateToRegister = { }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Continuer avec Google")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_signUpLinkNavigatesToRegister() {
        // Given
        composeTestRule.setContent {
            AureusTheme {
                LoginScreen(
                    viewModel = mockAuthViewModel(),
                    onLoginSuccess = { },
                    onNavigateToRegister = { onNavigateToRegisterCalled = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Sign Up")
            .performClick()

        // Then
        assert(onNavigateToRegisterCalled)
    }

    // Helper function for mocking viewModel
    private fun mockAuthViewModel(): com.example.aureus.ui.auth.viewmodel.AuthViewModel {
        // Return mock or real Hilt-provided viewmodel
        // Note: This would need proper setup with Hilt test modules
        return hiltViewModel<com.example.aureus.ui.auth.viewmodel.AuthViewModel>()
    }
}
```

---

#### ÉTAPE 14.5: Créer Tests de Repository

**Tous les repositories**:

Exemple: `app/src/test/java/com/example/aureus/data/repository/AuthRepositoryImplTest.kt`

```kotlin
package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class AuthRepositoryImplTest {

    private lateinit var authRepository: AuthRepositoryImpl

    @Mock
    private lateinit var authManager: FirebaseAuthManager

    @Mock
    private lateinit var dataManager: FirebaseDataManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepositoryImpl(authManager, dataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login should return success with valid credentials`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(
            uid = "user_123",
            email = "test@example.com",
            displayName = "Test User"
        )
        whenever(authManager.loginWithEmail("test@example.com", "password"))
            .thenReturn(Result.success(mockUser))
        whenever(dataManager.createUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.login("test@example.com", "password")

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("user_123", (result as Resource.Success).data?.id)
        assertEquals("test@example.com", (result as Resource.Success).data?.email)
    }

    @Test
    fun `login should return error with invalid credentials`() = runTest {
        // Given
        whenever(authManager.loginWithEmail(any(), any()))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        // When
        val result = authRepository.login("wrong@example.com", "wrongpassword")

        // Then
        assertTrue(result is Resource.Error)
        assertNotNull((result as Resource.Error).message)
    }

    @Test
    fun `register should create user in Firebase`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(
            uid = "new_user_456",
            email = "newuser@example.com"
        )
        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.success(mockUser))
        whenever(dataManager.createUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.success(Unit))
        whenever(dataManager.createDefaultCards(any()))
            .thenReturn(Result.success(Unit))
        whenever(dataManager.createDefaultTransactions(any()))
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.register(
            email = "newuser@example.com",
            password = "password123",
            firstName = "New",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("new_user_456", (result as Resource.Success).data?.id)
        
        // Verify Firebase operations
        verify(authManager, times(1)).registerWithEmail(
            "newuser@example.com", "password123", "New", "User", "+212600000000"
        )
        verify(dataManager, times(1)).createUser(any(), any(), any(), any(), any(), any())
        verify(dataManager, times(1)).createDefaultCards("new_user_456")
        verify(dataManager, times(1)).createDefaultTransactions("new_user_456")
    }

    @Test
    fun `register should rollback auth if Firestore fails`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(uid = "temp_user", email = "temp@example.com")
        
        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.success(mockUser))
        
        whenever(dataManager.createUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("Firestore error")))

        // When
        val result = authRepository.register(
            email = "temp@example.com",
            password = "password123",
            firstName = "Temp",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("Firestore") == true)
        
        // Verify rollback
        verify(mockUser, times(1)).delete()
    }

    @Test
    fun `logout should sign out from Firebase`() = runTest {
        // Given
        whenever(authManager.signOut()).thenReturn(Unit)

        // When
        val result = authRepository.logout()

        // Then
        assertTrue(result is Resource.Success)
        verify(authManager, times(1)).signOut()
    }

    @Test
    fun `isLoggedIn should return true when user is logged in`() {
        // Given
        whenever(authManager.isUserLoggedIn()).thenReturn(true)

        // When
        val result = authRepository.isLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isLoggedIn should return false when user is not logged in`() {
        // Given
        whenever(authManager.isUserLoggedIn()).thenReturn(false)

        // When
        val result = authRepository.isLoggedIn()

        // Then
        assertFalse(result)
    }

    // Helper function
    private fun mockFirebaseUser(
        uid: String,
        email: String = "",
        displayName: String = ""
    ): com.google.firebase.auth.FirebaseUser {
        val mock = org.mockito.kotlin.mock(com.google.firebase.auth.FirebaseUser::class.java)
        org.mockito.kotlin.whenever(mock.uid).thenReturn(uid)
        org.mockito.kotlin.whenever(mock.email).thenReturn(email)
        org.mockito.kotlin.whenever(mock.displayName).thenReturn(displayName)
        return mock
    }
}
```

---

#### ÉTAPE 14.6: Lancer Tests

**Commandes Gradle**:

```bash
# Lister tous les tests unitaires
./gradlew test

# Lister tous les tests instrumentés
./gradlew connectedAndroidTest

# Lister tests spécifiques
./gradlew test --tests "*.AuthRepositoryImplTest"
./gradlew connectedAndroidTest --tests "*.LoginScreenTest"

# Générer rapport de couverture
./gradlew jacocoTestReport
```

---

## ⚡ PHASE 15: PERFORMANCE OPTIMIZATION (2-3 jours)

### Objectif
Optimiser les performances de l'app: startup time, memory usage, UI smoothness

---

#### ÉTAPE 15.1: Optimiser Startup Time

**Modifier AndroidManifest.xml**:

```xml
<application
    android:name=".AureusApplication"
    android:allowBackup="false"
    android:fullBackupContent="@xml/backup_rules"
    android:dataExtractionRules="@xml/backup_rules"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Aureus.Starting"
    android:hardwareAccelerated="true">
    
    <!-- Default activity launcher -->
    <activity
        android:name=".SplashActivity"
        android:exported="true"
        android:theme="@style/Theme.Aureus.Splash">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    
    <!-- Use process:background for heavy operations -->
    <service
        android:name=".sync.FirebaseSyncService"
        android:process=":background" />
    
    <!-- Configure WorkManager using initialize -->
    <provider
        android:name="androidx.startup.InitializationProvider"
        android:authorities="${applicationId}.androidx-startup"
        android:exported="false"
        tools:replace="android:authorities">
        <meta-data
            android:name="androidx.work.WorkManagerInitializer"
            android:value="androidx.work.impl.WorkManagerInitializer" />
    </provider>
</application>
```

---

#### ÉTAPE 15.2: Optimiser Compose Performance

**Utiliser androidx.compose.foundation.lazy.LazyColumn**:

```kotlin
// BAD: Column inside LazyColumn items
LazyColumn {
    items(items) { item ->
        Column(
            modifier = Modifier.clickable { }
        ) {
            Text(item.title)
            Text(item.description)
        }
    }
}

// GOOD: Flat LazyColumn with click area on outer element
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = items,
        key = { it.id }
    ) { item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(item) },
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                Text(item.title)
                Text(item.description)
            }
        }
    }
}
```

---

#### ÉTAPE 15.3: Optimiser Images

**Utiliser Coil pour le chargement d'images**:

```kotlin
dependencies {
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
}

// Usage
AsyncImage(
    model = imageUrl,
    contentDescription = null,
    modifier = Modifier.size(48.dp),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(R.drawable.ic_placeholder),
    error = painterResource(R.drawable.ic_error),
    crossfade(true),
    animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy)
)
```

---

#### ÉTAPE 15.4: Optimiser Firestore Queries

**Ajouter indexes Firestore**:

```javascript
// firestore.indexes.json
{
  "indexes": [
    {
      "collectionGroup": "transactions",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "userId", "order": "ASCENDING"},
        {"fieldPath": "createdAt", "order": "DESCENDING"}
      ]
    },
    {
      "collectionGroup": "cards",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "userId", "order": "ASCENDING"},
        {"fieldPath": "isDefault", "order": "DESCENDING"}
      ]
    },
    {
      "collectionGroup": "contacts",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "userId", "order": "ASCENDING"},
        {"fieldPath": "isFavorite", "order": "DESCENDING"}
      ]
    }
  ],
  "fieldOverrides": []
}
```

**Deploy indexes**:

```bash
firebase deploy --only firestore:indexes
```

---

#### ÉTAPE 15.5: Optimiser Memory

**Utiliser CompositionLocalProvider**:

```kotlin
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    // Local state for expensive computations
    var displayedItemCount by remember { mutableIntStateOf(50) }
    var selectedItem by remember { mutableStateOf<String?>(null) }
    
    // Donnée lourde - utiliser remembered pour éviter recalc
    val currencyFormatter by remember {
        mutableStateOf(NumberFormat.getCurrencyInstance(Locale("fr", "MA")))
    }
    
    // Pattern: items with key
    LazyColumn {
        items(
            items = transactions,
            key = { it.transactionId }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                formatter = currencyFormatter,
                onClick = { selectedItem = transaction.transactionId }
            )
        }
    }
}
```

---

#### ÉTAPE 15.6: Profiler Integration

**Installer Profiler Tools**:

```kotlin
dependencies {
    // Firebase Performance
    implementation("com.google.firebase:firebase-perf-ktx:20.4.0")
    
    // Memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

---

#### ÉTAPE 15.7: Test Performance

**Outils à utiliser**:

1. **Android Studio Profiler**:
   - CPU Profiler: Analyser le CPU usage
   - Memory Profiler: Détecter memory leaks
   - Network Profiler: Analyser les appels réseau

2. **Firebase Performance**:
   - Monitoring: Surveillance des traces API
   - Network Requests: Latence des appels Firestore

3. **Profiler Compose**:
   - Recomposition detection
   - Composable stability

**Metrics cibles**:
- Startup time: < 3 seconds
- Memory usage: < 150 MB
- FPS: > 60 fps constant
- Load time transactions: < 1 second

---

## 📊 RÉSUMÉ DES PHASES 11-15

| Phase | Durée | Complexité | Impact |
|-------|-------|------------|--------|
| Phase 11: Analytics | 2-3 jours | Moyenne | ★★★★☆ |
| Phase 12: Dark Mode | 2 jours | Basse | ★★★★☆ |
| Phase 13: I18n | 3-4 jours | Haute | ★★★★☆ |
| Phase 14: Tests | 3-4 jours | Haute | ★★★★★ |
| Phase 15: Performance | 2-3 jours | Moyenne | ★★★★☆ |

**TOTAL**: 12-17 jours (2-3 semaines)

---

## 🎯 SCORE FINAL ATTENDU

Après TOUTES les Phases (1-15):

| Métrique | Après Phase 10 | Après Phase 15 |
|----------|---------------|----------------|
| Core Features | 100% | 100% |
| Offline Support | 100% | 100% |
| Notifications | 100% | 100% |
| Biometric Auth | 100% | 100% |
| Charts Pro | 100% | 100% |
| **Analytics** | 0% | **100%** |
| **Dark Mode** | 20% | **100%** |
| **I18n** | 10% | **100%** |
| **Tests** | 0% | **80%** |
| **Performance** | 70% | **100%** |
| **SCORE GLOBAL** | 9.5/10 | **10/10** 🏆🏆🏆 |

---

## 🏆 APPLICATION PRODUCTION-READY

Après Phase 15, Aureus Banking App sera:

✅ **100% Fonctionnelle** - Toutes les features core implémentées  
✅ **100% Dynamique** - Toutes les données depuis Firebase  
✅ **100%Offline Ready** - Fonctionne sans internet  
✅ **Notifications** - Alertes en temps réel  
✅ **Biometric** - Access sécurisé rapide  
✅ **Professional Charts** - Visualisations pro  
✅ **Analytics** - Tracking complet des utilisateurs  
✅ **Dark Mode** - Experience UI complète  
✅ **Multi-langues** - FR, EN, AR, ES, DE (RTL support)  
✅ **Testée** - Couverture tests élevée  
✅ **Optimisée** - Performance excellent  
✅ **Production-Ready** - Prête pour le Play Store  

---

## 📝 CHECKLIST PHASES 11-15

### Phase 11: Analytics & Monitoring
- [ ] Configurer Firebase Analytics
- [ ] Configurer Performance Monitoring
- [ ] Configurer Crashlytics
- [ ] Créer AnalyticsManager
- [ ] Intégrer dans tous les écrans
- [ ] Tracker auth events
- [ ] Tracker transaction events
- [ ] Tracker transfer events
- [ ] Performance traces
- [ ] Crash reporting
- [ ] Firebase Console setup

### Phase 12: Dark Mode
- [ ] Définir Dark Theme colors
- [ ] Créer DarkColorPalette
- [ ] Créer ThemeManager
- [ ] Créer ThemeToggle
- [ ] Intégrer dans MainActivity
- [ ] Tester toggle theme
- [ ] Tester system theme
- [ ] Tester persistence
- [ ] Vérifier tous les écrans en dark mode

### Phase 13: Internationalization
- [ ] Créer strings.xml (EN)
- [ ] Créer strings.xml (AR)
- [ ] Créer strings.xml (ES)
- [ ] Créer strings.xml (DE)
- [ ] Créer LanguageManager
- [ ] Créer LanguageSelector
- [ ] RTL support (Arabe)
- [ ] Test toutes les langues
- [ ] Test RTL layout
- [ ] Vérifier persistence langue

### Phase 14: Tests
- [ ] Configurer test dependencies
- [ ] Créer HiltTestRule
- [ ] Créer tests ViewModels + Repos
- [ ] Créer UI Tests
- [ ] Lancer tests unitaires
- [ ] Lancer tests instrumentés
- [ ] Vérifier couverture > 80%
- [ ] Fixer tests échoués

### Phase 15: Performance
- [ ] Optimiser startup time
- [ ] Optimiser Compose LazyColumn
- [ ] Optimiser images (Coil)
- [ ] Ajouter Firestore indexes
- [ ] Optimiser memory usage
- [ ] Configurer Profiler
- [ ] Memory leak testing
- [ ] Performance profiling
- [ ] Atteindre metrics cibles

---

## 🚀 DERNIERS CHECKLIST AVANT RELEASE

### ✅ Fonctionnalités
- [ ] Authentification complète (Email, Google, SMS, PIN, Biometric)
- [ ] Transaction CRUD complet
- [ ] Transfer Send/Receive
- [ ] Cards management
- [ ] Contacts management
- [ ] Statistics dynamiques
- [ ] Notifications push
- [ ] Offline mode
- [ ] Multi-langues (5+)
- [ ] Dark Mode

### ✅ Qualité
- [ ] Tests unitaires > 80% couverture
- [ ] Tests UI pour écrans critiques
- [ ] Pas de memory leaks
- [ ] Startup time < 3s
- [ ] FPS > 60 constant
- [ ] Pas de crashes connus

### ✅ Sécurité
- [ ] PIN crypté (AES-256)
- [ ] Biometric auth
- [ ] Firestore rules sécurisées
- [ ] HTTPS partout
- [ ] Sensitive data masquée
- [ ] Session management

### ✅ UX/UI
- [ ] Material 3 design
- [ ] Dark mode complet
- [ ] RTL support
- [ ] Animations fluides
- [ ] Accessibilité (TalkBack)
- [ ] Error_handling clair
- [ ] Loading states visibles

### ✅ Release
- [ ] App icon optimisé
- [ ] Splash screen professionnel
- [ ] Store listing text
- [ ] Screenshots (en anglais)
- [ ] Privacy policy URL
- [ ] Terms of service URL
- [ ] Version correcte (x.y.z)

---

🎯 **CONCLUSION**: 

Ce plan complet (15 phases, 29-43 jours) transforme Aureus Banking App d'une version 7.7/10 avec des problèmes à une application **production-ready de 10/10** avec toutes les features modernes d'une banking app professionnelle !

**PLAN FINAL CRÉÉ LE**: 11 Janvier 2026  
**ESTIMATION PHASES 11-15**: 12-17 jours (2-3 semaines)  
**AUTEUR**: Firebender AI Assistant  
**PROJET**: Aureus Banking Application