package com.example.aureus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aureus.ui.theme.NavyBlue

/**
 * Example screens showing how to use Lottie animations in different contexts
 * These are reference implementations that can be copied to actual screens
 */

/**
 * Example: Empty beneficiaries list
 */
@Composable
fun EmptyBeneficiariesExample() {
    EmptyStateView(
        message = "Aucun bénéficiaire enregistré",
        actionText = "Ajouter un bénéficiaire",
        onActionClick = { 
            // Navigate to add beneficiary screen
        },
        animationUrl = LottieUrls.EMPTY_LIST_FALLBACK
    )
}

/**
 * Example: Empty transactions list
 */
@Composable
fun EmptyTransactionsExample() {
    EmptyStateView(
        message = "Aucune transaction pour le moment",
        actionText = "Effectuer une transaction",
        onActionClick = { 
            // Navigate to transaction screen
        },
        animationUrl = LottieUrls.NO_TRANSACTIONS
    )
}

/**
 * Example: Empty cards list
 */
@Composable
fun EmptyCardsExample() {
    EmptyStateView(
        message = "Aucune carte enregistrée",
        actionText = "Ajouter une carte",
        onActionClick = { 
            // Navigate to add card screen
        },
        animationUrl = LottieUrls.NO_CARDS
    )
}

/**
 * Example: Loading screen while fetching data
 */
@Composable
fun LoadingDataExample() {
    LoadingView(
        message = "Chargement de vos données..."
    )
}

/**
 * Example: Transaction processing
 */
@Composable
fun ProcessingPaymentExample() {
    ProcessingTransactionView(
        message = "Traitement de votre paiement en cours..."
    )
}

/**
 * Example: Success dialog after transaction
 */
@Composable
fun TransactionSuccessExample() {
    var showDialog by remember { mutableStateOf(true) }
    
    if (showDialog) {
        SuccessView(
            message = "Transaction effectuée avec succès !",
            onDismiss = { 
                showDialog = false
                // Navigate back or update UI
            }
        )
    }
}

/**
 * Example: Error dialog after failed transaction
 */
@Composable
fun TransactionErrorExample() {
    var showDialog by remember { mutableStateOf(true) }
    
    if (showDialog) {
        ErrorView(
            message = "La transaction a échoué. Veuillez réessayer.",
            onDismiss = { 
                showDialog = false
                // Retry transaction
            }
        )
    }
}

/**
 * Example: Feature showcase with animation
 */
@Composable
fun WalletFeatureExample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleLottieAnimation(
            url = LottieUrls.WALLET,
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Votre Portefeuille Numérique",
            style = MaterialTheme.typography.titleLarge,
            color = NavyBlue
        )
        
        Text(
            text = "Gérez toutes vos cartes en un seul endroit",
            style = MaterialTheme.typography.bodyMedium,
            color = NavyBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * Example: Transfer feature with animation
 */
@Composable
fun TransferFeatureExample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleLottieAnimation(
            url = LottieUrls.TRANSFER,
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Transferts Instantanés",
            style = MaterialTheme.typography.titleLarge,
            color = NavyBlue
        )
        
        Text(
            text = "Envoyez de l'argent en quelques secondes",
            style = MaterialTheme.typography.bodyMedium,
            color = NavyBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * Example: Analytics dashboard header
 */
@Composable
fun AnalyticsDashboardExample() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Analyses Financières",
                style = MaterialTheme.typography.titleLarge,
                color = NavyBlue
            )
            Text(
                text = "Vue d'ensemble de vos finances",
                style = MaterialTheme.typography.bodyMedium,
                color = NavyBlue.copy(alpha = 0.7f)
            )
        }
        
        SimpleLottieAnimation(
            url = LottieUrls.ANALYTICS,
            modifier = Modifier.size(80.dp)
        )
    }
}

/**
 * Example: Biometric authentication prompt
 */
@Composable
fun BiometricAuthExample(useFaceId: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleLottieAnimation(
            url = if (useFaceId) LottieUrls.FACE_ID else LottieUrls.FINGERPRINT,
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (useFaceId) "Scannez votre visage" else "Placez votre doigt",
            style = MaterialTheme.typography.titleMedium,
            color = NavyBlue
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Pour confirmer votre identité",
            style = MaterialTheme.typography.bodyMedium,
            color = NavyBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * Example: Complete transaction flow with animations
 */
@Composable
fun TransactionFlowExample() {
    var state by remember { mutableStateOf(TransactionState.IDLE) }
    
    when (state) {
        TransactionState.IDLE -> {
            // Normal transaction form
            Button(
                onClick = { state = TransactionState.PROCESSING },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Effectuer le transfert")
            }
        }
        
        TransactionState.PROCESSING -> {
            ProcessingTransactionView(
                message = "Traitement en cours..."
            )
            
            // Simulate processing
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                state = TransactionState.SUCCESS // or ERROR
            }
        }
        
        TransactionState.SUCCESS -> {
            SuccessView(
                message = "Transfert effectué avec succès !",
                onDismiss = { 
                    state = TransactionState.IDLE
                    // Navigate or reset
                }
            )
        }
        
        TransactionState.ERROR -> {
            ErrorView(
                message = "Le transfert a échoué",
                onDismiss = { 
                    state = TransactionState.IDLE
                    // Retry
                }
            )
        }
    }
}

enum class TransactionState {
    IDLE,
    PROCESSING,
    SUCCESS,
    ERROR
}
