package com.example.aureus.ui.auth.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.aureus.security.BiometricAvailability
import com.example.aureus.security.BiometricManager
import com.example.aureus.security.BiometricResult
import com.example.aureus.ui.theme.*

/**
 * Écran de verrouillage biométrique
 */
@Composable
fun BiometricLockScreen(
    biometricManager: BiometricManager,
    onUnlockSuccess: () -> Unit = {},
    onUsePin: () -> Unit = {},
    onEnableBiometric: () -> Unit = {}
) {
    val activity = LocalContext.current as? FragmentActivity
    val authResult by biometricManager.authResult.collectAsState()

    var showEnablePrompt by remember { mutableStateOf(false) }
    var autoAuthenticateTriggered by remember { mutableStateOf(false) }

    // Auto-authentifier quand écran ouvre
    LaunchedEffect(Unit) {
        if (!autoAuthenticateTriggered && activity != null) {
            autoAuthenticateTriggered = true
            kotlinx.coroutines.delay(500)

            biometricManager.authenticate(
                activity = activity,
                title = "Unlock Aureus Banking",
                subtitle = "Use your fingerprint to continue",
                description = "Touch the sensor to verify your identity",
                negativeButtonText = "Use PIN",
                onSuccess = { onUnlockSuccess() },
                onError = { error ->
                    if (error.contains("NoCredentials") || error.contains("none_enrolled")) {
                        showEnablePrompt = true
                    }
                }
            )
        }
    }

    // Handle result
    LaunchedEffect(authResult) {
        when (authResult) {
            is BiometricResult.Success -> {
                // Success handled in onSuccess callback
            }
            is BiometricResult.Failed -> {
                // User cancelled or biometric failed, stay on screen
                biometricManager.resetAuthResult()
            }
            is BiometricResult.Error -> {
                // Error handled in onError callback, stay on screen
                kotlinx.coroutines.delay(2000)
                biometricManager.resetAuthResult()
            }
            BiometricResult.Idle -> {}
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Fingerprint Icon
            AnimatedFingerprintIcon(
                isScanning = authResult == BiometricResult.Idle || authResult is BiometricResult.Failed
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Unlock to Continue",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = NeutralWhite
            )

            // Subtitle
            Text(
                text = when (authResult) {
                    is BiometricResult.Success -> "Identity Verified!"
                    is BiometricResult.Failed -> "Authentication Failed. Try again."
                    is BiometricResult.Error -> (authResult as BiometricResult.Error).message
                    BiometricResult.Idle -> "Touch the fingerprint sensor"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = NeutralWhite.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Use PIN Button
            Button(
                onClick = {
                    biometricManager.resetAuthResult()
                    onUsePin()
                    autoAuthenticateTriggered = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeutralWhite.copy(alpha = 0.1f),
                    contentColor = NeutralWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Use PIN Instead", fontWeight = FontWeight.Medium)
            }
        }
    }

    // Enable Biometric Prompt Dialog
    if (showEnablePrompt) {
        AlertDialog(
            onDismissRequest = { showEnablePrompt = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Enable Biometric Authentication",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            },
            text = {
                Text(
                    "No fingerprint registered on this device. Would you like to set up biometric authentication for faster access?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEnablePrompt = false
                        activity?.let { biometricManager.promptEnableBiometric(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                ) {
                    Text("Setup Biometric")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEnablePrompt = false
                    onUsePin()
                }) {
                    Text("Skip")
                }
            }
        )
    }
}

@Composable
private fun AnimatedFingerprintIcon(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint")

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isScanning) 1.0f else 1.1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    // Alpha animation for scanning effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Rotation for outer ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring with rotation
        if (isScanning) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
                    .alpha(alpha),
            ) {
                val strokeWidth = 4.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                drawCircle(
                    color = SecondaryGold,
                    radius = radius,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(20f, 20f),
                            phase = 0f
                        )
                    )
                )
            }
        }

        // Inner ring
        Canvas(
            modifier = Modifier
                .size(90.dp)
                .alpha(if (isScanning) alpha else 1f),
        ) {
            val strokeWidth = 2.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            drawCircle(
                color = SecondaryGold.copy(alpha = 0.5f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
        }

        // Fingerprint icon container
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SecondaryGold.copy(alpha = 0.15f))
                .border(
                    width = 3.dp,
                    color = if (isScanning) SecondaryGold.copy(alpha = alpha) else SecondaryGold,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            if (isScanning) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawCircle(
                        color = SecondaryGold.copy(alpha = alpha * 0.3f),
                        radius = size.minDimension / 2
                    )
                }
            }

            // Fingerprint icon
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Fingerprint",
                tint = SecondaryGold,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}