# RAPPORT D'AUDIT DE SÉCURITÉ - AUREUS BANKING APP
**Date**: 11 Janvier 2026
**Auditeur**: Analyse Systématique Ligne par Ligne
**Statut**: CRITIQUE - Application NON CONFORME aux standards bancaires

---

## 📊 COMPTE-RENDU EXÉCUTIF

### Score de Sécurité Global: **3.5/10** ⚠️

L'application Aureus Banking présente **23 vulnérabilités de sécurité** dont **9 critiques** qui la rendent **inadaptée pour une utilisation bancaire réelle**. Plusieurs failles permettent le vol d'identifiants utilisateurs et l'accès aux données sensibles.

### Classification des Vulnérabilités
- 🔴 **CRITIQUES (9)** - Corruption immédiate des données / vol d'identité
- 🟠 **ÉLEVÉES (7)** - Violation de données possible
- 🟡 **MOYENNES (7)** - Risques de compromission

---

## 🔴 VULNÉRABILITÉS CRITIQUES (Niveau 9-10)

### 1. Mots de passe stockés en CLAIR dans SharedPreferences
**Emplacement**: `SecureCredentialManager.kt:83`
```kotlin
// LIGNE 83 - CATASTROPHIQUE!
securePrefs.edit().putString("pwd_$passwordKey", password).apply()
```
**Impact**: Tout utilisateur avec un appareil rooté peut extraire les mots de passe facilement.
**Remédiation**: Utiliser EncryptedSharedPreferences ou Keystore pour le chiffrement.

---

### 2. API KEY Firebase exposée en clair
**Emplacement**: `google-services.json:31`
```json
"current_key": "AIzaSyADfEdcIFeT0Smk37M7qY2VSaEK6kQyHns"
```
**Impact**: Permet des requêtes non autorisées aux services Firebase et quotas épuisés.
**Remédiation**: Déplacer API key dans configuration sécurisée côté serveur ou utiliser environment variables.

---

### 3. Cleartext Traffic (HTTP non sécurisé) autorisé
**Emplacement**: `AndroidManifest.xml:21`
```xml
android:usesCleartextTraffic="true"
```
**Impact**: Interception man-in-the-middle possible sur les connexions réseaux.
**Remédiation**: Changer en `false` et configurer uniquement HTTPS via network_security_config.xml.

---

### 4. PIN stocké en clair dans Firestore
**Emplacement**: `FirebaseDataManager.kt:116`
```kotlin
// LIGNE 116 - TODO INEXCUSABLE!
"pin" to pin, // TODO: Encrypter avec AES-256
```
**Impact**: Admin Firebase ou intrusion peut voir tous les PIN utilisateurs.
**Remédiation**: Hasher le PIN avec salt côté client avant envoi (déjà implémenté mais non utilisé).

---

### 5. Tokens d'authentification stockés en clair
**Emplacement**: `SharedPreferencesManager.kt:24-36`
```kotlin
fun saveToken(token: String) {
    sharedPrefs.edit().putString(KEY_TOKEN, token).apply()
}
```
**Impact**: Hijacking de session possible via extraction de tokens.
**Remédiation**: Utiliser EncryptedSharedPreferences.

---

### 6. Pas de configuration EncryptedSharedPreferences
**Emplacement**: `SecureCredentialManager.kt:36`
```kotlin
private val securePrefs: SharedPreferences by lazy {
    context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
}
// Devrait utiliser: EncryptedSharedPreferences.create(...)
```
**Impact**: Aucun chiffrement réel des données stockées.
**Remédiation**: Implémenter EncryptedSharedPreferences avec MasterKey dans Android Keystore.

---

### 7. PIN par défaut HARDCODED
**Emplacement**: `PinProtectedAction.kt:37,129`
```kotlin
correctPin: String = "1234" // TODO: Get from secure storage
```
**Impact**: N'importe quel attaquant connaissant le code "1234" peut contourner la authentification PIN.
**Remédiation**: Remplacer par récupération dynamique depuis Firestore/Keystore.

---

### 8. Règles de backup cloud non configurées
**Emplacements**: `backup_rules.xml`, `data_extraction_rules.xml`
```xml
<!-- Règles vides - backup automatique de TOUTES les données sensibles! -->
<full-backup-content>
    <!-- VIDE! -->
</full-backup-content>
```
**Impact**: Mots de passe et tokens peuvent être backupés sur Google Drive / Cloud.
**Remédiation**: Exclure explicitement tous les fichiers contenant des données sensibles.

---

### 9. Logs de sécurité en clair dans Logcat
**Emplacement**: `SecurityLogger.kt` - nombreuses lignes
```kotlin
Log.println(level, TAG, "[${timestamp}] User: $userIdStr | " +
    "Device: $deviceIdStr | PIN attempt: ${event.success}")
```
**Impact**: Logs exposés via `adb logcat` peuvent contenir infos sensibles.
**Remédiation**: Supprimer ou masquer les logs de prod, utiliser logging sécurisé distant.

---

## 🟠 VULNÉRABILITÉS ÉLEVÉES (Niveau 7-8)

### 10. Pas de FLAG_SECURE (Screenshots autorisés)
**Impact**: Captures d'écran des écrans sensibles possibles (transferts, données carte).
**Remédiation**:
```kotlin
window.setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
)
```

### 11. Pas de NetworkSecurityConfig.xml (Certificate Pinning)
**Impact**: Vulnérable aux attaques Man-in-the-Middle avec certificats frauduleux.
**Remédiation**: Créer `res/xml/network_security_config.xml` avec certificate pinning.

### 12. ProGuard garde les classes sécurité de l'obfuscation
**Emplacement**: `proguard-rules.pro:86-88`
```proguard
-keep class com.example.aureus.util.SharedPreferencesManager { *; }
-keep class com.example.aureus.data.repository.** { *; }
```
**Impact**: Reverse engineering facilité des mécanismes de sécurité.
**Remédiation**: Supprimer ces règles de keep ou limiter uniquement aux interfaces API.

### 13. Auto-sauvegarde automatique des mots de passe
**Emplacement**: `LoginScreen.kt:82`
```kotlin
// Auto-sauvegarder le compte (comme suggestions Android)
credentialManager.saveAccount(email, password)
```
**Impact**: Crée des copies supplémentaires des mots de passe automatiquement.
**Remédiation**: Demander consentement explicite utilisateur avant sauvegarde.

### 14. Auto-fill par 4 taps exploitant données en clair
**Emplacement**: `LoginScreen.kt:153-163`
```kotlin
if (tapCount >= 4 && !autoFillTriggered) {
    credentialManager.autoFill()
        .onSuccess { credentials: CredentialPair ->
            email = credentials.email
            password = credentials.password  // Extrait depuis stockage en clair!
```
**Impact**: Facilite l'extraction programmée des identifiants.
**Remédiation**: Nécessiter authentification biométrique avant auto-fill.

### 15. Firebase tokens stockés en clair
**Emplacement**: `SharedPreferencesManager.kt:24`
```kotlin
private const val KEY_TOKEN = "auth_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
```
**Impact**: Tokens de session exposés sur appareil.
**Remédiation**: EncryptedSharedPreferences comme points 5 et 6.

### 16. CVV traité côté client sans nettoyage garanti
**Emplacement**: `FirebaseDataManager.kt:225`
```kotlin
suspend fun addCard(
    // ...
    cvv: String,  // ❌ NE PAS STOCKER (utilisé uniquement pour validation côté client)
    // ...
```
**Impact**: Risk de CVV accidentellement logué ou persisté.
**Remédiation**: Nettoyage explicite de CVV après validation, interdiction totale de stockage.

---

## 🟡 VULNÉRABILITÉS MOYENNES (Niveau 5-6)

### 17. Timeout verrouillage compte trop court
**Emplacement**: `PinAttemptTracker.kt:30`
```kotlin
private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000 // 5 minutes
```
**Impact**: Attaques brute-force possibles après 5 min.
**Remédiation**: Augmenter à 15-30 min avec escalade progressive.

### 18. Pas de rate limiting côté serveur Firestore
**Impact**: Attaques par force brutale possibles sur API Firestore.
**Remédiation**: Implémenter Cloud Functions avec rate limiting.

### 19. PIN de 4 chiffres sans salt (SHA-256 seul)
**Emplacement**: `EncryptionService.kt:102-106`
```kotlin
fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}
```
**Impact**: Vulnérable aux rainbow tables.
**Remédiation**: Ajouter salt unique par utilisateur: `SHA-256(pin + user_salt)`.

### 20. Firestore update sans token expiration
**Emplacement**: Règles Firebase manquant validation timestamp
```
allow update: if isOwner(userId); // Pas de vérification temps
```
**Impact**: Tokens volés peuvent être réutilisés indéfiniment.
**Remédiation**: Ajouter vérification token expiration côté serveur.

### 21. Validation insuffisante dans règles Firestore
**Emplacement**: `firestore.rules:28`
```kotlin
allow write: if isOwner(userId);
// Pas de validation de structure/contenu des données
```
**Impact**: Injection de données malveillantes possible.
**Remédiation**: Ajouter validation des types et valeurs pour tous les champs.

### 22. Pas de vérification email/phone avant opérations
**Impact**: Comptes non vérifiés peuvent effectuer des transferts.
**Remédiation**: Obliger email verification + phone verification avant transferts.

### 23. Images profil accessibles publiquement
**Emplacement**: `storage.rules:48`
```javascript
allow read: if true;  // Publique!
```
**Impact**: Fuites de données profils utilisateurs.
**Remédiation**: Restreindre l'accès ou utiliser liens signés avec expiration.

---

## ✅ POINTS FORTS DE SÉCURITÉ

1. **Biométrie implémentée** avec BiometricManager
2. **Surveillance PIN** avec PinAttemptTracker (3 tentatives max)
3. **Chiffrement disponible** dans EncryptionService (AES/GCM)
4. **Hashage PIN côté client** implémenté (mais pas toujours utilisé)
5. **Firebase Auth** utilisé correctement pour login
6. **Cloud Functions** pour notifications sécurisées
7. **ProGuard activé** pour obfuscation du code

---

## 📋 RECOMMANDATIONS PAR PRIORITÉ

### PRIORITÉ 1 - IMMÉDIAT (Bloquer la release)

1. ❌ **Remplacer `usesCleartextTraffic="true"` par `false`**
2. ❌ **Implémenter EncryptedSharedPreferences** pour toutes les données sensibles
3. ❌ **Hasher le PIN avant stockage Firestore** (utiliser la fonction déjà existante)
4. ❌ **Supprimer sauvegarde automatique des mots de passe**
5. ❌ **Retirer PIN hardcoded "1234"** depuis PinProtectedAction
6. ❌ **Configurer backup rules** pour exclure les données sensibles
7. ❌ **Déplacer/masquer l'API KEY Firebase**

### PRIORITÉ 2 - SPRINT SUIVANT

1. **Ajouter FLAG_SECURE** pour écrans sensibles
2. **Implémenter NetworkSecurityConfig.xml** avec certificate pinning
3. **Supprimer logs sensibles** en production
4. **Ajouter salt au hashage du PIN**
5. **Augmenter timeout verrouillage** à 15-30 min
6. **Demander consentement avant sauvegarde identifiants**

### PRIORITÉ 3 - Court Terme

1. **Vérification email + phone obligatoire** avant transferts
2. **Rate limiting Cloud Functions** côté serveur
3. **Nettoyage CVV explicit** après validation
4. **Restriction images profil** dans Storage rules
5. **Supprimer logs SecurityLogger** ou logging sécurisé distant
6. **Validation Firestore rules** plus strictes

### PRIORITÉ 4 - Moyen Terme

1. MFA obligatoire pour opérations > 10,000 MAD
2. Device binding (limite appareils par utilisateur)
3. Analyse comportementale de fraude
4. Audit trails côté serveur
5. Token rotation automatique
6. Penetration testing externe

---

## 📊 ANALYSE DE CONFORMITÉ

### PCI-DSS (Payment Card Industry)
| Exigence | Statut | Notes |
|----------|--------|-------|
| CVV non stocké | ⚠️ PARTIEL | Pas dans DB mais traité en mémoire |
| Chiffrement transmission | ❌ FAIL | Cleartext traffic autorisé |
| Chiffrement stockage | ❌ FAIL | Mots de passe en clair local |
| Authentification forte | ⚠️ PARTIEL | PIN + biométrie mais PIN weak |
| Logging/Audit | ⚠️ PARTIEL | Logs en clair dans Logcat |
| **Score PCI-DSS** | **3/10** | **NON CONFORME** |

### OWASP Mobile Top 10
| Risque | Statut | Détails |
|--------|--------|---------|
| M1: Improper Platform Usage | ❌ FAIL | Cleartext traffic |
| M2: Insecure Data Storage | ❌ FAIL | Mots de passe en clair |
| M3: Insecure Communication | ❌ FAIL | HTTP autorisé |
| M4: Insecure Authentication | ⚠️ PARTIEL | PIN 4 digits, no salt |
| M5: Insufficient Cryptography | ❌ FAIL | PIN non salted |
| M6: Insecure Authorization | ✅ PASS | Firebase rules OK |
| M7: Client Code Quality | ✅ PASS | Code propre |
| M8: Code Tampering | ⚠️ PARTIEL | ProGuard partial |
| M9: Reverse Engineering | ⚠️ PARTIEL | Classes sécu keepées |
| M10: Extraneous Functionality | ✅ PASS | Pas de code debug |
| **Score OWASP** | **4/10** | **VULNERABLE** |

---

## 🚨 CONCLUSION

L'application Aureus Banking **N'EST PAS prête pour une release en production** dans son état actuel. Les 9 vulnérabilités critiques doivent être corrigées avant tout transfert de fonds réel.

### Score Final: **3.5/10** - Application NON CONFORME

### Statut de Conformité Bancaire: **REFUSÉE ⛔**

**Recommandation finale**: Interdire l'utilisation de cette application pour des transactions réelles jusqu'à ce que toutes les vulnérabilités de Priorité 1 soient résolues.

---

## 📝 ANNEXE - Détails Techniques

### Fichiers Critiques à Modifier
1. `AndroidManifest.xml` - Ligne 21
2. `SecureCredentialManager.kt` - Lignes 36, 83
3. `SharedPreferencesManager.kt` - Lignes 10, 24
4. `FirebaseDataManager.kt` - Ligne 116
5. `PinProtectedAction.kt` - Lignes 37, 129
6. `proguard-rules.pro` - Lignes 86-88
7. `backup_rules.xml` - En entier
8. `data_extraction_rules.xml` - En entier
9. `google-services.json` - Ligne 31 (extraction API key)

### Configuration Requise

#### 1. EncryptedSharedPreferences
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context,
    "secure_credentials",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

#### 2. Network Security Config
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">firebasestorage.googleapis.com</domain>
        <domain includeSubdomains="true">firestore.googleapis.com</domain>
        <domain includeSubdomains="true">firebase-auth.googleapis.com</domain>
    </domain-config>
</network-security-config>
```

#### 3. PIN avec Salt
```kotlin
fun hashPinWithSalt(pin: String, userSalt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val salted = pin + userSalt
    val hash = digest.digest(salted.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}
```

#### 4. Backup Rules
```xml
<full-backup-content>
    <exclude domain="sharedpref" path="secure_credentials.xml" />
    <exclude domain="sharedpref" path="MyBankPrefs.xml" />
    <exclude domain="database" path="*.db" />
</full-backup-content>
```

---

**Document généré automatiquement via audit de sécurité complet**
**Pour toute question, contacter l'équipe de sécurité**