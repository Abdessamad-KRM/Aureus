package com.example.aureus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*

/**
 * Quick Login Buttons Component
 * Affiche jusqu'à 4 touches rapides pour se connecter avec des email/password fréquents
 *
 * Design: Circle buttons avec les lettres initiales, couleurs Aureus
 */
@Composable
fun QuickLoginButtons(
    accounts: List<Map<String, String>>,
    onAccountClick: (email: String, password: String) -> Unit,
    onAccountRemove: (email: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Comptes fréquents",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            accounts.forEach { account ->
                QuickLoginButton(
                    label = account["label"] ?: "U",
                    email = account["email"] ?: "",
                    onClick = { onAccountClick(account["email"] ?: "", account["password"] ?: "") },
                    onLongPress = { onAccountRemove(account["email"] ?: "") }
                )
            }

            // Placeholder pour atteindre 4 boutons
            repeat(4 - accounts.size) {
                EmptyQuickLoginButton()
            }
        }
    }
}

/**
 * Bouton Quick Login individuel
 */
@Composable
private fun QuickLoginButton(
    label: String,
    email: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val initials = label.take(1).uppercase()

    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(
                if (isPressed)
                    PrimaryNavyBlue
                else
                    SecondaryGold.copy(alpha = 0.1f)
            )
            .border(
                width = if (isPressed) 0.dp else 1.5.dp,
                color = if (isPressed) Color.Transparent else SecondaryGold.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = if (isPressed) SecondaryGold else PrimaryNavyBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Bouton vide (placeholder)
 */
@Composable
private fun EmptyQuickLoginButton() {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(
                Color.Transparent
            )
            .border(
                width = 1.dp,
                color = NeutralMediumGray.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = NeutralMediumGray.copy(alpha = 0.4f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Quick Login Cards (Alternative design - Square cards)
 * Pour un design plus moderne avec plus d'information
 */
@Composable
fun QuickLoginCards(
    accounts: List<Map<String, String>>,
    onAccountClick: (email: String, password: String) -> Unit,
    onAccountRemove: (email: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Comptes fréquents",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            accounts.forEach { account ->
                QuickLoginCard(
                    email = account["email"] ?: "",
                    label = account["label"] ?: "User",
                    onClick = { onAccountClick(account["email"] ?: "", account["password"] ?: "") },
                    onRemove = { onAccountRemove(account["email"] ?: "") }
                )
            }

            // Placeholder
            repeat(4 - accounts.size) {
                EmptyQuickLoginCard()
            }
        }
    }
}

/**
 * Quick Login Card (Square design)
 */
@Composable
private fun QuickLoginCard(
    email: String,
    label: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveButton by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(90.dp)
            .clickable {
                onClick()
                isPressed = false
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed)
                PrimaryNavyBlue
            else
                SecondaryGold.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPressed) 0.dp else 1.dp,
            color = if (isPressed) Color.Transparent else SecondaryGold.copy(alpha = 0.2f)
        )
    ) {
        Box {
            // Icon/Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        if (isPressed)
                            PrimaryMediumBlue.copy(alpha = 0.5f)
                        else
                            SecondaryGold.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.take(1).uppercase(),
                    color = if (isPressed) SecondaryGold else PrimaryNavyBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Remove button
            if (showRemoveButton) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = SemanticRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Email display
            Text(
                text = email.split("@")[0],
                color = if (isPressed) NeutralWhite else PrimaryNavyBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Empty Quick Login Card
 */
@Composable
private fun EmptyQuickLoginCard() {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = NeutralMediumGray.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = NeutralMediumGray.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Compact Quick Login Buttons (Horizontal row in login form)
 */
@Composable
fun CompactQuickLoginButtons(
    accounts: List<Map<String, String>>,
    onAccountClick: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Comptes:",
            color = Color.Gray,
            fontSize = 12.sp
        )

        accounts.forEach { account ->
            CompactQuickButton(
                label = account["label"] ?: "User",
                onClick = { onAccountClick(account["email"] ?: "", account["password"] ?: "") }
            )
        }
    }
}

@Composable
private fun CompactQuickButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = SecondaryGold.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = SecondaryGold.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.take(1).uppercase(),
                color = PrimaryNavyBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = PrimaryNavyBlue.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}