package com.example.aureus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*

/**
 * Example implementation of PIN-protected actions
 * Demonstrates how to use the PIN verification in real scenarios
 */

/**
 * Example 1: Simple Transfer with PIN Protection
 */
@Composable
fun TransferWithPinExample() {
    val pinState = rememberPinProtectedActionState()
    var transferComplete by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("5000") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        PrimaryNavyBlue,
                        PrimaryMediumBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Transaction Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralWhite.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Transfer",
                        tint = SecondaryGold,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Transfert d'argent",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeutralWhite
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Vers: Mohammed ALAMI",
                        fontSize = 14.sp,
                        color = NeutralWhite.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "$amount MAD",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGold
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (transferComplete) {
                        Text(
                            text = "✓ Transfert effectué avec succès !",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SemanticGreen
                        )
                    } else {
                        Button(
                            onClick = {
                                pinState.requestPin(
                                    title = "Confirmer le transfert",
                                    subtitle = "Transférer $amount MAD à Mohammed ALAMI"
                                ) {
                                    // This code executes after successful PIN verification
                                    transferComplete = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryGold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmer avec PIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavyBlue
                            )
                        }
                    }
                }
            }
            
            if (transferComplete) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        transferComplete = false
                    }
                ) {
                    Text(
                        text = "Nouveau transfert",
                        color = SecondaryGold
                    )
                }
            }
        }
        
        // PIN Protection Handler
        PinProtectedActionHandler(state = pinState)
    }
}

/**
 * Example 2: Multiple PIN-protected actions
 */
@Composable
fun MultipleActionsWithPinExample() {
    val pinState = rememberPinProtectedActionState()
    var lastAction by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Actions sécurisées",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavyBlue
        )
        
        lastAction?.let { action ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SemanticGreen.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "✓ Action effectuée: $action",
                    modifier = Modifier.padding(16.dp),
                    color = SemanticGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Action 1: Change Settings
        SecureActionButton(
            title = "Modifier les paramètres",
            description = "Changer les informations sensibles",
            icon = "⚙️"
        ) {
            pinState.requestPin(
                title = "Modifier les paramètres",
                subtitle = "Confirmez votre identité"
            ) {
                lastAction = "Paramètres modifiés"
            }
        }
        
        // Action 2: Delete Account
        SecureActionButton(
            title = "Supprimer le compte",
            description = "Action irréversible",
            icon = "🗑️",
            isDangerous = true
        ) {
            pinState.requestPin(
                title = "Supprimer le compte",
                subtitle = "Cette action est irréversible"
            ) {
                lastAction = "Compte supprimé"
            }
        }
        
        // Action 3: Export Data
        SecureActionButton(
            title = "Exporter les données",
            description = "Télécharger toutes vos données",
            icon = "📥"
        ) {
            pinState.requestPin(
                title = "Exporter les données",
                subtitle = "Confirmez pour télécharger"
            ) {
                lastAction = "Données exportées"
            }
        }
        
        // PIN Protection Handler
        PinProtectedActionHandler(state = pinState)
    }
}

@Composable
private fun SecureActionButton(
    title: String,
    description: String,
    icon: String,
    isDangerous: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isDangerous) 
                SemanticRed.copy(alpha = 0.05f) 
            else 
                NeutralWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDangerous) SemanticRed else PrimaryNavyBlue
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
            
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secured",
                tint = if (isDangerous) SemanticRed else SecondaryGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true, showSystemUi = true, name = "Transfer Example")
@Composable
fun TransferWithPinExamplePreview() {
    AureusTheme {
        TransferWithPinExample()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Multiple Actions")
@Composable
fun MultipleActionsWithPinExamplePreview() {
    AureusTheme {
        MultipleActionsWithPinExample()
    }
}
