package com.example.aureus.ui.auth.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.security.PinAttemptTracker
import com.example.aureus.security.PinSecurityManager
import com.example.aureus.ui.auth.viewmodel.PinViewModel
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Écran de vérification PIN pour accéder au Dashboard
 * Appelé après login si l'utilisateur a déjà configuré un PIN
 */
@Composable
fun UnlockWithPinScreen(
    pinSecurityManager: PinSecurityManager,
    pinAttemptTracker: PinAttemptTracker,
    onUnlockSuccess: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false)}

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Animated shake when error occurs
    val shakeAnimation = remember {
        Animatable(0f)
    }

    LaunchedEffect(isError) {
        if (isError) {
            shakeAnimation.animateTo(
                targetValue = 1f,
                animationSpec = repeatable(
                    iterations = 3,
                    animation = tween(80, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            shakeAnimation.snapTo(0f)
        }
    }

    // Check for lockout first
    LaunchedEffect(Unit) {
        if (pinAttemptTracker.isLocked()) {
            // Account is locked - stay on screen but show lockout message
            delay(100)
        }
    }

    // Wait for lockout to expire and try again
    LaunchedEffect(pinAttemptTracker.getAttemptsRemaining()) {
        if (pinAttemptTracker.isLocked()) {
            // Lockout timer is managed by PinAttemptTracker
            // User will be unlocked automatically after timer expires
        }
    }

    // Check PIN when 4 digits entered
    LaunchedEffect(pin) {
        if (pin.length == 4 && !isChecking && !pinAttemptTracker.isLocked()) {
            isChecking = true
            delay(300)

            val isValid = try {
                viewModel.verifyPin(pin)
            } catch (e: Exception) {
                false
            }

            if (isValid) {
                // PIN correct - reset attempts
                pinAttemptTracker.resetAttempts()
                isChecking = false
                delay(300)
                onUnlockSuccess()
            } else {
                // PIN incorrect
                pinAttemptTracker.recordFailedAttempt()
                isError = true
                isChecking = false
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                delay(1500)
                pin = ""
                isError = false
            }
        }
    }

    val shakeOffset by shakeAnimation.asState()
    val maxOffset = 10f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
                .padding(24.dp)
                .graphicsLayer {
                    translationX = maxOffset * shakeOffset
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Lock Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isError) SemanticRed.copy(alpha = 0.2f)
                        else SecondaryGold.copy(alpha = 0.2f)
                    )
                    .border(
                        width = 3.dp,
                        color = if (isError) SemanticRed else SecondaryGold,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Lock",
                    tint = if (isError) SemanticRed else SecondaryGold,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Bienvenue",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = NeutralWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            if (pinAttemptTracker.isLocked()) {
                Text(
                    text = "Compte temporairement verrouillé",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SemanticRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Réessayez dans quelques minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralLightGray
                )
            } else {
                Text(
                    text = if (isError) "PIN incorrect" else "Entrez votre code PIN",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isError) SemanticRed else NeutralLightGray
                )

                if (pinAttemptTracker.getAttemptsRemaining() < 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${pinAttemptTracker.getAttemptsRemaining()} essais restants",
                        style = MaterialTheme.typography.bodySmall,
                        color = SemanticRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) {
                                    if (isError) SemanticRed else SecondaryGold
                                } else {
                                    NeutralMediumGray.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Number Keypad
            NumberPad(
                onNumberClick = { num ->
                    if (pin.length < 4) {
                        pin = pin + num.toString()
                    }
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                },
                isChecking = isChecking
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Déconnexion",
                    color = NeutralLightGray.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    isChecking: Boolean = false
) {
    val numbers = (1..9).toList()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rows 1-3 (1-9)
        repeat(3) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                repeat(3) { col ->
                    val num = numbers[row * 3 + col]
                    NumberButton(
                        number = num.toString(),
                        onClick = { onNumberClick(num) },
                        enabled = !isChecking
                    )
                }
            }
        }

        // Row 4 (0 + Delete)
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(80.dp)) // Empty space for alignment

            NumberButton(
                number = "0",
                onClick = { onNumberClick(0) },
                enabled = !isChecking
            )

            // Delete button
            IconButton(
                onClick = onDeleteClick,
                enabled = !isChecking && !isChecking,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = if (isChecking) NeutralMediumGray.copy(alpha = 0.5f) else SecondaryGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                if (enabled) NeutralWhite.copy(alpha = 0.1f)
                else NeutralMediumGray.copy(alpha = 0.2f)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp
            ),
            color = if (enabled) NeutralWhite else NeutralMediumGray
        )
    }
}