package com.example.aureus.ui.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PIN Verification Dialog - Verify 4-digit PIN for sensitive operations
 * Can be used as a Dialog or Full Screen
 */
@Composable
fun PinVerificationDialog(
    onPinVerified: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Confirmer l'opération",
    subtitle: String = "Entrez votre code PIN de sécurité",
    correctPin: String = "1234" // Static PIN for demo - should come from secure storage
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            PinVerificationContent(
                title = title,
                subtitle = subtitle,
                correctPin = correctPin,
                onPinVerified = onPinVerified,
                onDismiss = onDismiss,
                isDialog = true
            )
        }
    }
}

/**
 * PIN Verification Full Screen
 */
@Composable
fun PinVerificationScreen(
    onPinVerified: () -> Unit,
    onCancel: () -> Unit,
    title: String = "Vérification",
    subtitle: String = "Entrez votre code PIN",
    correctPin: String = "1234"
) {
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
        PinVerificationContent(
            title = title,
            subtitle = subtitle,
            correctPin = correctPin,
            onPinVerified = onPinVerified,
            onDismiss = onCancel,
            isDialog = false
        )
    }
}

/**
 * Shared PIN Verification Content
 */
@Composable
private fun PinVerificationContent(
    title: String,
    subtitle: String,
    correctPin: String,
    onPinVerified: () -> Unit,
    onDismiss: () -> Unit,
    isDialog: Boolean
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    // Handle PIN validation
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(200)
            if (pin == correctPin) {
                isSuccess = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(800)
                onPinVerified()
            } else {
                isError = true
                attemptCount++
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(1000)
                isError = false
                pin = ""
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isDialog) 24.dp else 0.dp),
        shape = RoundedCornerShape(if (isDialog) 24.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDialog) 
                PrimaryNavyBlue.copy(alpha = 0.98f) 
            else 
                Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 32.dp,
                    vertical = if (isDialog) 40.dp else 80.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button (for dialog)
            if (isDialog) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NeutralWhite
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Lock Icon
            VerificationLockIcon(isSuccess = isSuccess, isError = isError)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = NeutralWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtitle with error state
            Text(
                text = when {
                    isSuccess -> "Code correct !"
                    isError -> "Code incorrect (${attemptCount}/3)"
                    else -> subtitle
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSuccess -> SemanticGreen
                    isError -> SemanticRed
                    else -> NeutralWhite.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // PIN Dots
            PinVerificationDots(
                pinLength = pin.length,
                isError = isError,
                isSuccess = isSuccess
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Numeric Keypad
            NumericKeypadCompact(
                onNumberClick = { number ->
                    if (pin.length < 4 && !isSuccess) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pin += number
                    }
                },
                onBackspaceClick = {
                    if (pin.isNotEmpty() && !isSuccess) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pin = pin.dropLast(1)
                    }
                },
                enabled = !isSuccess && !isError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Forgot PIN link
            TextButton(
                onClick = { /* TODO: Handle forgot PIN */ },
                enabled = !isSuccess
            ) {
                Text(
                    text = "Code PIN oublié ?",
                    color = SecondaryGold.copy(alpha = if (isSuccess) 0.3f else 1f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * PIN Verification Dots (4 dots)
 */
@Composable
private fun PinVerificationDots(
    pinLength: Int,
    isError: Boolean,
    isSuccess: Boolean
) {
    val shakeOffset = remember(isError) {
        if (isError) {
            Animatable(0f).also { animatable ->
                kotlinx.coroutines.GlobalScope.launch {
                    repeat(3) {
                        animatable.animateTo(15f, tween(50))
                        animatable.animateTo(-15f, tween(50))
                    }
                    animatable.animateTo(0f, tween(50))
                }
            }
        } else {
            Animatable(0f)
        }
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer {
            translationX = shakeOffset.value
        }
    ) {
        repeat(4) { index ->
            VerificationDot(
                isFilled = index < pinLength,
                isError = isError,
                isSuccess = isSuccess
            )
        }
    }
}

/**
 * Individual Verification Dot
 */
@Composable
private fun VerificationDot(
    isFilled: Boolean,
    isError: Boolean,
    isSuccess: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
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
            .size(18.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isFilled) color else Color.Transparent)
            .border(
                width = 2.dp,
                color = color,
                shape = CircleShape
            )
    )
}

/**
 * Compact Numeric Keypad for Dialog
 */
@Composable
private fun NumericKeypadCompact(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rows 1-3
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { number ->
                    CompactNumberButton(
                        number = number,
                        onClick = { onNumberClick(number) },
                        enabled = enabled
                    )
                }
            }
        }
        
        // Row 4
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(modifier = Modifier.size(60.dp))
            CompactNumberButton(
                number = "0",
                onClick = { onNumberClick("0") },
                enabled = enabled
            )
            CompactBackspaceButton(
                onClick = onBackspaceClick,
                enabled = enabled
            )
        }
    }
}

/**
 * Compact Number Button
 */
@Composable
private fun CompactNumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = NeutralWhite.copy(alpha = if (enabled) 0.1f else 0.05f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = NeutralWhite.copy(alpha = if (enabled) 0.25f else 0.1f),
                shape = CircleShape
            )
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
                kotlinx.coroutines.GlobalScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) NeutralWhite else NeutralWhite.copy(alpha = 0.3f)
        )
    }
}

/**
 * Compact Backspace Button
 */
@Composable
private fun CompactBackspaceButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = NeutralWhite.copy(alpha = if (enabled) 0.1f else 0.05f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = NeutralWhite.copy(alpha = if (enabled) 0.25f else 0.1f),
                shape = CircleShape
            )
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
                kotlinx.coroutines.GlobalScope.launch {
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
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Verification Lock Icon
 */
@Composable
private fun VerificationLockIcon(
    isSuccess: Boolean,
    isError: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lock")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSuccess) 1.15f else if (isError) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale * (if (!isSuccess && !isError) pulse else 1f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    color = when {
                        isSuccess -> SemanticGreen.copy(alpha = 0.2f)
                        isError -> SemanticRed.copy(alpha = 0.2f)
                        else -> SecondaryGold.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    color = when {
                        isSuccess -> SemanticGreen
                        isError -> SemanticRed
                        else -> SecondaryGold
                    },
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isSuccess -> Icons.Default.CheckCircle
                    else -> Icons.Default.Lock
                },
                contentDescription = "Lock",
                tint = when {
                    isSuccess -> SemanticGreen
                    isError -> SemanticRed
                    else -> SecondaryGold
                },
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
