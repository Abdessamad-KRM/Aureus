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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Resource
import com.example.aureus.ui.auth.viewmodel.PinViewModel
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PIN Setup Screen - Set 4-digit security PIN
 * Used for confirming transactions and sensitive actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onPinSetupComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(PinSetupStep.CREATE) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pinState by viewModel.pinState.collectAsState()

    // Get current PIN based on step
    val currentPin = when (currentStep) {
        PinSetupStep.CREATE -> firstPin
        PinSetupStep.CONFIRM -> confirmPin
    }

    // Navigate sur succès de sauvegarde
    LaunchedEffect(pinState) {
        if (pinState is Resource.Success) {
            isSaving = false
            isSuccess = true
            delay(500)
            onPinSetupComplete()
        }
    }

    // Afficher erreur de sauvegarde
    LaunchedEffect(pinState) {
        if (pinState is Resource.Error) {
            isError = true
            isSaving = false
            delay(1500)
            isError = false
        }
    }

    // Handle PIN input
    fun onPinChange(newPin: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        when (currentStep) {
            PinSetupStep.CREATE -> {
                firstPin = newPin
                if (newPin.length == 4) {
                    scope.launch {
                        delay(300)
                        currentStep = PinSetupStep.CONFIRM
                    }
                }
            }
            PinSetupStep.CONFIRM -> {
                confirmPin = newPin
                if (newPin.length == 4) {
                    scope.launch {
                        delay(300)
                        // Validate PINs match
                        if (firstPin == confirmPin) {
                            // Sauvegarder le PIN dans Firestore
                            isSaving = true
                            viewModel.savePin(firstPin)
                        } else {
                            isError = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            delay(1500)
                            isError = false
                            confirmPin = ""
                        }
                    }
                }
            }
        }
    }
    
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
        // Back button
        IconButton(
            onClick = {
                if (currentStep == PinSetupStep.CONFIRM && !isSuccess) {
                    currentStep = PinSetupStep.CREATE
                    confirmPin = ""
                } else {
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NeutralWhite
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Lock Icon
                AnimatedLockIcon(
                    isSuccess = isSuccess,
                    isError = isError
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Title
                Text(
                    text = when (currentStep) {
                        PinSetupStep.CREATE -> "Créer votre Code PIN"
                        PinSetupStep.CONFIRM -> "Confirmer votre Code PIN"
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = NeutralWhite,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Subtitle
                Text(
                    text = when {
                        isSuccess -> "Code PIN créé avec succès !"
                        isError -> "Les codes ne correspondent pas"
                        currentStep == PinSetupStep.CREATE -> "Ce code sécurisera vos transactions"
                        else -> "Entrez à nouveau votre code"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSuccess -> SemanticGreen
                        isError -> SemanticRed
                        else -> NeutralWhite.copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // PIN Dots Display
                PinDotsDisplay(
                    pinLength = currentPin.length,
                    isError = isError || (pinState is Resource.Error),
                    isSuccess = isSuccess || (pinState is Resource.Success)
                )

                // Saving indicator
                if (pinState is Resource.Loading) {
                    Row(
                        modifier = Modifier.padding(top = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = SecondaryGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sauvegarde en cours...",
                            color = NeutralWhite.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }

                // Success message
                if (isSuccess) {
                    Row(
                        modifier = Modifier.padding(top = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SemanticGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Code enregistré !",
                            color = SemanticGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Error message
                if (pinState is Resource.Error && !isError) {
                    Text(
                        text = (pinState as Resource.Error).message,
                        color = SemanticRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Numeric Keypad
            Column(
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                NumericKeypad(
                    onNumberClick = { number ->
                        if (currentPin.length < 4) {
                            onPinChange(currentPin + number)
                        }
                    },
                    onBackspaceClick = {
                        if (currentPin.isNotEmpty()) {
                            onPinChange(currentPin.dropLast(1))
                        }
                    },
                    enabled = !isSuccess && !isError
                )
            }
        }
    }
}

/**
 * PIN Setup Steps
 */
private enum class PinSetupStep {
    CREATE,
    CONFIRM
}

/**
 * PIN Dots Display (4 dots)
 */
@Composable
private fun PinDotsDisplay(
    pinLength: Int,
    isError: Boolean,
    isSuccess: Boolean
) {
    val shakeOffset = remember(isError) {
        if (isError) {
            Animatable(0f).also { animatable ->
                kotlinx.coroutines.GlobalScope.launch {
                    repeat(3) {
                        animatable.animateTo(20f, tween(50))
                        animatable.animateTo(-20f, tween(50))
                    }
                    animatable.animateTo(0f, tween(50))
                }
            }
        } else {
            Animatable(0f)
        }
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer {
            translationX = shakeOffset.value
        }
    ) {
        repeat(4) { index ->
            PinDot(
                isFilled = index < pinLength,
                isError = isError,
                isSuccess = isSuccess,
                animationDelay = index * 50
            )
        }
    }
}

/**
 * Individual PIN Dot
 */
@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    isSuccess: Boolean,
    animationDelay: Int
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
                if (isFilled) color else Color.Transparent
            )
            .border(
                width = 2.dp,
                color = color,
                shape = CircleShape
            )
    )
}

/**
 * Numeric Keypad (0-9 + Backspace)
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rows 1-3 (1-9)
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { number ->
                    NumberButton(
                        number = number,
                        onClick = { onNumberClick(number) },
                        enabled = enabled
                    )
                }
            }
        }
        
        // Row 4 (empty, 0, backspace)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Empty spacer
            Spacer(modifier = Modifier.size(70.dp))
            
            // 0 button
            NumberButton(
                number = "0",
                onClick = { onNumberClick("0") },
                enabled = enabled
            )
            
            // Backspace button
            BackspaceButton(
                onClick = onBackspaceClick,
                enabled = enabled
            )
        }
    }
}

/**
 * Number Button
 */
@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
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
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isPressed = true
                onClick()
                // Reset pressed state
                kotlinx.coroutines.GlobalScope.launch {
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

/**
 * Backspace Button
 */
@Composable
private fun BackspaceButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
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
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Animated Lock Icon
 */
@Composable
private fun AnimatedLockIcon(
    isSuccess: Boolean,
    isError: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lock")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSuccess) 1.2f else if (isError) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating ring
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isSuccess || isError) 0f else 0.4f)
                .graphicsLayer { rotationZ = rotation }
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        SecondaryGold.copy(alpha = 0.8f),
                        Color.Transparent,
                        SecondaryGold.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
        
        // Lock icon background
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = when {
                        isSuccess -> SemanticGreen.copy(alpha = 0.2f)
                        isError -> SemanticRed.copy(alpha = 0.2f)
                        else -> SecondaryGold.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    color = when {
                        isSuccess -> SemanticGreen
                        isError -> SemanticRed
                        else -> SecondaryGold
                    },
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = "Lock",
                tint = when {
                    isSuccess -> SemanticGreen
                    isError -> SemanticRed
                    else -> SecondaryGold
                },
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
