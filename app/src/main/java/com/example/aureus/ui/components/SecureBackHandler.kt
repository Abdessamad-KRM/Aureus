package com.example.aureus.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Secure Back Handler
 * 
 * This component provides secure back navigation by:
 * 1. Preventing accidental back on critical screens
 * 2. Confirming user intent before exiting
 * 3. Supporting configurable enable/disable logic
 * 
 * @param enabled Whether the back handler is active
 * @param onBackRequest Callback when user attempts to go back
 */
@Composable
fun SecureBackHandler(
    enabled: Boolean = true,
    onBackRequest: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = {
        onBackRequest()
    })
}