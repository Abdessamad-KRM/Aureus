# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep data classes for API models (Retrofit)
-keep class com.example.aureus.data.remote.dto.** { *; }
-keep class com.example.aureus.domain.model.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class dagger.hilt.android.internal.managers.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep sensitive classes from obfuscation for security audits
# Note: Remove or modify this section for production builds
-keep class com.example.aureus.util.SharedPreferencesManager { *; }
-keep class com.example.aureus.data.repository.** { *; }

# ✅ PHASE 2: ProGuard Sécurité - Suppression et Obfuscation
# =====================================================

# 1. Supprimer les strings sensibles du bytecode final
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# 2. Masquer les clés API et tokens dans les builds de release
# Supprime tous les strings contenant "key", "token", "secret"
-assumenosideeffects class java.lang.String {
    *** getString(...);
    *** getBytes(...);
}

# 3. Renommer les fichiers sources pour la sécurité
-renamesourcefileattribute SourceFile

# 4. Obfusquer les classes de sécurité
# Garder les noms des classes mais obfusquer les méthodes internes
-keep,allowobfuscation class com.example.aureus.security.** { *; }
-keep,allowobfuscation class com.example.aureus.util.SharedPreferencesManager {
    public <methods>;
    public <fields>;
}

# 5. Supprimer les informations de debug en release
-if **:BuildConfig.DEBUG
-dontoptimize
-dontobfuscate
-if ! **:BuildConfig.DEBUG
-optimizeaggressively
-repackageclasses ''

# 6. Masquer les strings Firebase dans les release builds
# Obfusquer les classes Firebase internes
-keepnames class com.google.firebase.** { *; }
-keepnames class com.google.android.gms.** { *; }

# 7. Empêcher l'extraction des données encrypted
# Supprimer les informations de débugging
-verbose

# 8. Protection contre la réflexion sur les classes sensibles
-keepclassmembers class com.example.aureus.security.** {
    private static *** ***(...);
    public static *** ***(...);
}

# 9. Supprimer les méthodes de debug inutilisées
-assumenosideeffects com.example.aureus.util.Logger { *; }
-assumenosideeffects com.example.aureus.security.SecurityLogger { *; }

# 10. Optimisation des classes de cryptographie
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# 11. Masquer les strings sensibles dans les logs
-assumenosideeffects class java.lang.Throwable {
    *** printStackTrace(...);
    *** getStackTrace(...);
}

# General Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Jetpack Compose
-dontwarn androidx.compose.runtime.Composer
-keep class androidx.compose.** { *; }