package com.example.aureus

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Phase 14: Tests - Custom Test Runner for Hilt
 *
 * Ce runner utilise HiltTestApplication au lieu de l'application principale
 * pour permettre l'injection de dépendances dans les tests instrumentés.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: android.content.Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}