package com.example.aureus.ui.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SMS Verification Screen - Premium Design
 * 6 individual boxes for SMS code verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsVerificationScreen(
    phoneNumber: String = "+212 6XX XXX XXX", // Phone number to display
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Countdown timer for resend
    LaunchedEffect(Unit) {
        while (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        }
        canResend = true
    }
    
    // Auto-verify when 6 digits are entered
    LaunchedEffect(code) {
        if (code.length == 6) {
            isVerifying = true
            delay(1500) // Simulate API call
            
            // Static validation - accept "123456" as correct code
            if (code == "123456") {
                isSuccess = true
                delay(1000)
                onVerificationSuccess()
            } else {
                isError = true
                delay(2000)
                isError = false
                code = ""
            }
            isVerifying = false
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
            onClick = onNavigateBack,
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
            verticalArrangement = Arrangement.Center
        ) {
            // Animated icon
            AnimatedVerificationIcon(isSuccess = isSuccess, isError = isError)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title
            Text(
                text = "Vérification SMS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = NeutralWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = "Nous avons envoyé un code à 6 chiffres au",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Phone number
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SecondaryGold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 6 OTP boxes
            OtpInputBoxes(
                code = code,
                onCodeChange = { newCode ->
                    if (newCode.length <= 6 && newCode.all { it.isDigit() }) {
                        code = newCode
                    }
                },
                isError = isError,
                isSuccess = isSuccess
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status message
            AnimatedVisibility(
                visible = isVerifying,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SecondaryGold,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vérification en cours...",
                        color = NeutralWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SemanticGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Code vérifié avec succès !",
                        color = SemanticGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isError,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = "Code incorrect. Veuillez réessayer.",
                    color = SemanticRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Resend code
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Vous n'avez pas reçu le code ? ",
                    color = NeutralWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                if (canResend) {
                    TextButton(
                        onClick = {
                            // Resend logic here
                            canResend = false
                            resendCountdown = 60
                            code = ""
                            scope.launch {
                                while (resendCountdown > 0) {
                                    delay(1000)
                                    resendCountdown--
                                }
                                canResend = true
                            }
                        }
                    ) {
                        Text(
                            text = "Renvoyer",
                            color = SecondaryGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Renvoyer ($resendCountdown)",
                        color = NeutralWhite.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Helper text
            Text(
                text = "Code de test : 123456",
                color = SecondaryGold.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

/**
 * 6 OTP Input Boxes Component
 */
@Composable
private fun OtpInputBoxes(
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
    isSuccess: Boolean
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var focusedIndex by remember { mutableStateOf(-1) }
    
    // Focus first box on initial composition
    LaunchedEffect(Unit) {
        delay(300)
        focusRequesters[0].requestFocus()
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Hidden text field for actual input
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .size(0.dp)
                .focusRequester(focusRequesters[0])
        )
        
        // Visual boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                OtpBox(
                    digit = code.getOrNull(index)?.toString() ?: "",
                    isFocused = index == code.length && !isSuccess && !isError,
                    isError = isError,
                    isSuccess = isSuccess,
                    onClick = {
                        focusRequesters[0].requestFocus()
                    }
                )
            }
        }
    }
}

/**
 * Individual OTP Box
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpBox(
    digit: String,
    isFocused: Boolean,
    isError: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val borderColor = when {
        isSuccess -> SemanticGreen
        isError -> SemanticRed
        isFocused -> SecondaryGold
        digit.isNotEmpty() -> SecondaryGold.copy(alpha = 0.5f)
        else -> NeutralWhite.copy(alpha = 0.3f)
    }
    
    val backgroundColor = when {
        isSuccess -> SemanticGreen.copy(alpha = 0.1f)
        isError -> SemanticRed.copy(alpha = 0.1f)
        digit.isNotEmpty() -> SecondaryGold.copy(alpha = 0.05f)
        else -> NeutralWhite.copy(alpha = 0.05f)
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(50.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isFocused) 2.dp else 1.5.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Cursor animation when focused
            if (isFocused && digit.isEmpty()) {
                val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cursor_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .alpha(alpha)
                        .background(SecondaryGold)
                )
            }
            
            // Digit display
            if (digit.isNotEmpty()) {
                Text(
                    text = digit,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isSuccess -> SemanticGreen
                        isError -> SemanticRed
                        else -> NeutralWhite
                    }
                )
            }
        }
    }
}

/**
 * Animated Verification Icon
 */
@Composable
private fun AnimatedVerificationIcon(
    isSuccess: Boolean,
    isError: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
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
                .alpha(if (isSuccess || isError) 0f else 0.5f)
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
        
        // Icon background
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
            Text(
                text = when {
                    isSuccess -> "✓"
                    isError -> "✗"
                    else -> "📱"
                },
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
