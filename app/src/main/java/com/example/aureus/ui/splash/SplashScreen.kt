package com.example.aureus.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.R
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash Screen avec animations élégantes
 * Affiche le logo et le nom de la banque avec des effets visuels attractifs
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // États d'animation
    var startAnimation by remember { mutableStateOf(false) }
    
    // Animations
    val alphaLogo by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha_logo"
    )
    
    val scaleLogo by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_logo"
    )
    
    val alphaText by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "alpha_text"
    )
    
    val slideText by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 30.dp,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "slide_text"
    )

    // Animation infinie pour l'effet de brillance
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Démarrer les animations et naviguer après délai
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Durée d'affichage du splash
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryNavyBlue,
                        PrimaryMediumBlue,
                        Color(0xFF3A7BC8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo avec animation
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scaleLogo)
                    .alpha(alphaLogo),
                contentAlignment = Alignment.Center
            ) {
                // Effet de glow derrière le logo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SecondaryGold.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Aureus Logo",
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Nom de la banque avec animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = slideText)
                    .alpha(alphaText)
            ) {
                // Nom principal "AUREUS"
                Text(
                    text = "AUREUS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralWhite,
                    letterSpacing = 4.sp,
                    modifier = Modifier.graphicsLayer {
                        // Effet de brillance
                        val shimmerAlpha = if (shimmerOffset > -0.2f && shimmerOffset < 0.2f) {
                            1f - kotlin.math.abs(shimmerOffset) * 5f
                        } else 0f
                        alpha = 1f + shimmerAlpha * 0.3f
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Séparateur doré
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    SecondaryGold,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Slogan
                Text(
                    text = "Votre Banque Digitale",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryGold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Sous-titre
                Text(
                    text = "Prestige & Confiance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = NeutralWhite.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Indicateur de chargement en bas
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(alphaText),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots()
        }
    }
}

/**
 * Composant d'animation de points de chargement
 */
@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 150)
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(
                        color = SecondaryGold,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
