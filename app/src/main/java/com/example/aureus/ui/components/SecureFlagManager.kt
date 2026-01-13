package com.example.aureus.ui.components

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * FLAG_SECURE Manager - Phase 2 Sécurité
 *
 * Prevents screenshots and screen recording on sensitive screens
 * Protects card numbers, transactions, and other sensitive financial data
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun SensitiveScreen() {
 *     SecureScreenFlag(enabled = true) {
 *         YourSensitiveContent()
 *     }
 * }
 * ```
 */
@Composable
fun SecureScreenFlag(enabled: Boolean = true, content: @Composable () -> Unit) {
    val view = LocalView.current
    val context = view.context
    val activity = context as? android.app.Activity
    val window = activity?.window

    if (window == null) {
        content()
        return
    }

    DisposableEffect(enabled) {
        if (enabled) {
            // Disable screenshots and screen recording
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        onDispose {
            // Re-enable screenshots when leaving the screen
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    content()
}