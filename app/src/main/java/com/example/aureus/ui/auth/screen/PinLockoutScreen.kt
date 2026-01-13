package com.example.aureus.ui.auth.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*

@Composable
fun PinLockoutScreen(
    onLockoutExpired: () -> Unit = {}
) {
    var timeRemaining by remember { mutableIntStateOf(5 * 60) } // 5 minutes in seconds
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            timeRemaining--

            if (timeRemaining == 0) {
                onLockoutExpired()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SemanticRed,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Trop de tentatives",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = NeutralWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Vous avez tenté d'entrer le code PIN trop de fois.",
                style = MaterialTheme.typography.bodyLarge,
                color = NeutralWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pour des raisons de sécurité, votre compte est temporairement verrouillé.",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Countdown timer
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SemanticRed.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTime(timeRemaining),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        ),
                        color = SemanticRed
                    )

                    Text(
                        text = "avant de réessayer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralWhite.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = "Si vous n'êtes pas à l'origine de ces tentatives, veuillez contacter le support.",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}