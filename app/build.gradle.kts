plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.crashlytics)
    id("com.google.gms.google-services")  // ← REMOVED: version "4.4.2" apply false
}

android {
    namespace = "com.example.aureus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aureus"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Phase 14: Tests - Hilt Test Runner for Android tests
        testInstrumentationRunner = "com.example.aureus.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    // Compiler options
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
        // Enable Compose compiler reports for performance analysis
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Room schema export directory
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    // Disable KSP incremental processing to avoid cache corruption
    arg("incremental", "false")
    arg("incremental.module", "false")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.javapoet)
    ksp(libs.hilt.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Coil for images
    implementation(libs.coil.compose)

    // Lottie for animations
    implementation(libs.lottie.compose)

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Firebase - BOM et tous les services
    implementation(platform(libs.firebase.bom))

    // Firebase Core & Authentication
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-analytics")

    // WorkManager for background sync (Phase 7 - Offline-First)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Firebase Database (Temps Réel!)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Storage (pour images de profil, reçus)
    implementation("com.google.firebase:firebase-storage-ktx")

    // Firebase Messaging (Notifications Push)
    implementation(libs.firebase.messaging)

    // Firebase Functions (for wallet transfers)
    implementation("com.google.firebase:firebase-functions-ktx")

    // ✅ PHASE 1: Firebase App Check - Protection API Key
    implementation("com.google.firebase:firebase-appcheck:18.0.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")
    debugImplementation("com.google.firebase:firebase-appcheck-debug:18.0.0")

    // Firebase Crashlytics (Crash Reporting) - PHASE 11: Analytics & Monitoring
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")

    // Firebase Performance Monitoring - PHASE 11: Analytics & Monitoring
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-perf")

    // Coroutines Firebase pour Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Google Play Services Auth (pour Google Sign-In)
    implementation(libs.play.services.auth)

    // Biometric Authentication (Phase 9 - Fingerprint/FaceID)
    implementation("androidx.biometric:biometric:1.1.0")

    // AndroidX Security - PHASE 8: Secure Quick Login (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha07")

    // DataStore (Phase 12: Dark Mode) - Theme & Language persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // AppCompat (Phase 12: Dark Mode) - Theme management
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Splash Screen API (Phase 15: Performance Optimization)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // VICO Charts (Phase 10 - Professional Charts)
    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.17")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.17")
    implementation("com.patrykandpatrick.vico:core:2.0.0-alpha.17")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Phase 14: Tests - Unit Tests Dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("com.google.truth:truth:1.1.5")

    // Phase 14: Tests - Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.47")
    kspTest("com.google.dagger:hilt-android-compiler:2.47")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.47")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.47")

    // Phase 14: Tests - InstantTaskExecutorRule for LiveData testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Phase 15: Performance Optimization - Profiler Integration
    // Memory leak detection (debug only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // Firebase Performance Monitoring (already added in Phase 11)
    implementation("com.google.firebase:firebase-perf-ktx")

    // Compose compiler metrics for profiling
    debugImplementation("androidx.compose.compiler:compiler:1.5.4")
}