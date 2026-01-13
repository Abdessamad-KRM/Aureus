package com.example.aureus.ui.auth.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import kotlinx.coroutines.launch
import java.lang.Integer.max

/**
 * Écran de vérification PIN pour actions critiques
 * Utilisé avant: transferts, ajout carte, modification profil
 */
@Composable
fun PinVerificationScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    title: String = "Confirmer l'action",
    message: String = "Entrez votre code PIN pour continuer",
    pinSecurityManager: PinSecurityManager,
    pinAttemptTracker: PinAttemptTracker,
    viewModel: PinViewModel = hiltViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableIntStateOf(pinAttemptTracker.getCurrentAttemptCount()) }

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
            // Account is locked, navigate away
            delay(100)
            onCancel()
        }
    }

    // Check PIN when 4 digits entered
    LaunchedEffect(pin) {
        if (pin.length == 4 && !isChecking && !pinAttemptTracker.isLocked()) {
            isChecking = true
            delay(300) // Small pause for UX

            // Verify the PIN using ViewModel's suspend function
            val isValid = try {
                viewModel.verifyPin(pin)
            } catch (e: Exception) {
                false
            }

            if (isValid) {
                // PIN correct
                pinAttemptTracker.resetAttempts()
                pinSecurityManager.resetAttempts()
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                delay(500)
                onSuccess()
            } else {
                // PIN incorrect
                val remainingAttempts = pinAttemptTracker.recordFailedAttempt()
                attemptCount = remainingAttempts
                pinSecurityManager.recordFailedAttempt()
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                isError = true
                delay(1000)
                isError = false
                pin = ""

                // Check if too many attempts
                if (pinAttemptTracker.isLocked()) {
                    // Lock or redirect
                    delay(100)
                    onCancel()
                }
            }
            isChecking = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF0A1628),
                        PrimaryNavyBlue,
                        PrimaryMediumBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = NeutralWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = if (isError) {
                                shakeAnimation.value * 10f * if ((System.currentTimeMillis() / 80) % 2 == 0L) 1f else -1f
                            } else 0f
                        }
                ) {
                    repeat(4) { index ->
                        PinDot(
                            isFilled = index < pin.length,
                            isError = isError,
                            isSuccess = false
                        )
                    }
                }

                // Tentatives restantes
                if (pinAttemptTracker.getCurrentAttemptCount() > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tentatives restantes: ${pinAttemptTracker.getAttemptsRemaining()}",
                        color = if (pinAttemptTracker.getAttemptsRemaining() <= 1) SemanticRed else NeutralWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Cancel button
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Annuler",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Medium
                )
            }

            // Clavier numérique
            Column(
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                NumericKeypad(
                    onNumberClick = { number ->
                        if (pin.length < 4) {
                            pin += number
                        }
                    },
                    onBackspaceClick = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    },
                    enabled = !isChecking
                )
            }
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    isSuccess: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val color = when {
        isSuccess -> SemanticGreen
        isError -> SemanticRed
        isFilled -> SecondaryGold
        else -> NeutralWhite.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isFilled) color else androidx.compose.ui.graphics.Color.Transparent
            )
            .border(
                width = 2.dp,
                color = color,
                shape = CircleShape
            )
    )
}

@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    enabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { number ->
                    NumberButton(number, { onNumberClick(number) }, enabled, coroutineScope)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(modifier = Modifier.size(70.dp))
            NumberButton("0", { onNumberClick("0") }, enabled, coroutineScope)
            BackspaceButton(onBackspaceClick, enabled, coroutineScope)
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = NeutralWhite.copy(alpha = if (enabled) 0.1f else 0.05f),
                shape = CircleShape
            )
            .border(
                width = 1.5.dp,
                color = NeutralWhite.copy(alpha = if (enabled) 0.3f else 0.1f),
                shape = CircleShape
            )
            .noRippleClickable(enabled, coroutineScope) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                isPressed = true
                onClick()
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) NeutralWhite else NeutralWhite.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun BackspaceButton(
    onClick: () -> Unit,
    enabled: Boolean,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = NeutralWhite.copy(alpha = if (enabled) 0.1f else 0.05f),
                shape = CircleShape
            )
            .border(
                width = 1.5.dp,
                color = NeutralWhite.copy(alpha = if (enabled) 0.3f else 0.1f),
                shape = CircleShape
            )
            .noRippleClickable(enabled, coroutineScope) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                isPressed = true
                onClick()
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Backspace,
            contentDescription = "Backspace",
            tint = if (enabled) SecondaryGold else SecondaryGold.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
    }
}

// Helper extension for clickable without ripple
@Composable
private fun Modifier.noRippleClickable(
    enabled: Boolean,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onClick: () -> Unit
) = then(
    if (enabled) {
        this.then(
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        )
    } else {
        this
    }
)