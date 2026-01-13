package com.example.aureus.ui.auth.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.aureus.data.firestore.PinFirestoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PIN Checkpoint Screen - Vérifie si l'utilisateur a déjà configuré un PIN
 * Redirige automatiquement vers PinSetup si pas de PIN, vers UnlockWithPin sinon
 */
@Composable
fun PinCheckpointScreen(
    pinFirestoreManager: PinFirestoreManager,
    onPinSetupRequired: () -> Unit,
    onPinAlreadyConfigured: () -> Unit  // Redirige vers UnlockWithPin
) {
    LaunchedEffect(Unit) {
        // Vérifier si PIN est configuré
        val hasPin = withContext(Dispatchers.IO) {
            pinFirestoreManager.hasPinConfigured()
        }

        if (hasPin) {
            onPinAlreadyConfigured()
        } else {
            onPinSetupRequired()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}