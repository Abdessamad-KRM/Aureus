package com.example.aureus.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.R
import com.example.aureus.ui.components.NotificationPermissionRequest
import com.example.aureus.ui.theme.*
import com.example.aureus.security.BiometricManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Version avancée du Splash Screen avec effets visuels premium
 * Inclut des particules animées, cercles concentriques et effets de lumière
 */
@Composable
fun SplashScreenAdvanced(
    biometricManager: BiometricManager,
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showPermissionRequest by remember { mutableStateOf(true) }

    // Animations principales
    val alphaLogo by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "alpha_logo"
    )
    
    val scaleLogo by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "scale_logo"
    )
    
    val rotationLogo by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -180f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "rotation_logo"
    )

    val alphaText by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, delayMillis = 800),
        label = "alpha_text"
    )

    // Animation infinie pour les effets
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500)
        onSplashFinished()
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
        // Cercles concentriques animés en arrière-plan
        AnimatedConcentricCircles(
            rotation = rotation,
            alpha = if (startAnimation) 0.3f else 0f
        )

        // Particules flottantes
        FloatingParticles(
            alpha = if (startAnimation) 1f else 0f
        )

        // Contenu principal
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Container du logo avec effets
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow radial
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .alpha(alphaLogo * 0.6f)
                        .scale(pulse)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SecondaryGold.copy(alpha = 0.4f),
                                SecondaryGold.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }

                // Cercle doré tournant
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .rotate(rotation)
                        .alpha(alphaLogo * 0.5f)
                ) {
                    val path = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                0f, 0f, size.width, size.height
                            )
                        )
                    }
                    drawPath(
                        path = path,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                SecondaryGold.copy(alpha = 0.8f),
                                Color.Transparent,
                                SecondaryGold.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Aureus Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scaleLogo)
                        .rotate(rotationLogo)
                        .alpha(alphaLogo)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Texte avec animations
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alphaText)
            ) {
                // Nom AUREUS avec effet shimmer
                ShimmerText(
                    text = "AUREUS",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ligne dorée animée
                AnimatedGoldenLine(width = 140.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Slogan
                Text(
                    text = "Votre Banque Digitale",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryGold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Prestige & Confiance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    color = NeutralWhite.copy(alpha = 0.8f),
                    letterSpacing = 3.sp
                )
            }
        }

        // Indicateur de progression en bas
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .alpha(alphaText)
        ) {
            ProgressIndicatorGold()
        }

        // Permission request dialog for Android 13+
        if (showPermissionRequest) {
            NotificationPermissionRequest(
                onPermissionGranted = {
                    showPermissionRequest = false
                },
                onDismiss = {
                    showPermissionRequest = false
                    // Continuer sans permission
                }
            )
        }
    }
}

/**
 * Cercles concentriques animés
 */
@Composable
private fun AnimatedConcentricCircles(
    rotation: Float,
    alpha: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .rotate(rotation)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        listOf(0.3f, 0.5f, 0.7f, 0.9f).forEach { scale ->
            val radius = size.minDimension * scale
            drawCircle(
                color = SecondaryGold.copy(alpha = 0.05f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

/**
 * Particules flottantes
 */
@Composable
private fun FloatingParticles(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    // Pré-calculer les animations pour chaque particule
    val angles = List(5) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 10000 + index * 2000,
                    easing = LinearEasing
                )
            ),
            label = "particle_$index"
        )
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha * 0.4f)
    ) {
        val particles = listOf(
            Offset(size.width * 0.2f, size.height * 0.3f),
            Offset(size.width * 0.8f, size.height * 0.4f),
            Offset(size.width * 0.15f, size.height * 0.7f),
            Offset(size.width * 0.85f, size.height * 0.6f),
            Offset(size.width * 0.5f, size.height * 0.2f),
        )

        particles.forEachIndexed { index, offset ->
            val angle = (angles[index].value * Math.PI / 180).toFloat()
            
            val radius = 20f + index * 10f
            val x = offset.x + cos(angle) * radius
            val y = offset.y + sin(angle) * radius
            
            drawCircle(
                color = SecondaryGold.copy(alpha = 0.3f),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Texte avec effet shimmer
 */
@Composable
private fun ShimmerText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    letterSpacing: androidx.compose.ui.unit.TextUnit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_text")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = NeutralWhite,
        letterSpacing = letterSpacing,
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            val shimmerAlpha = kotlin.math.abs(kotlin.math.sin(shimmer * Math.PI)).toFloat()
            alpha = 0.85f + shimmerAlpha * 0.15f
        }
    )
}

/**
 * Ligne dorée animée
 */
@Composable
private fun AnimatedGoldenLine(width: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "golden_line")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_offset"
    )

    Canvas(
        modifier = Modifier
            .width(width)
            .height(3.dp)
    ) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    SecondaryGold.copy(alpha = 0.3f),
                    SecondaryGold,
                    SecondaryGold.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                startX = size.width * (offset - 0.5f),
                endX = size.width * (offset + 0.5f)
            )
        )
    }
}

/**
 * Indicateur de progression doré
 */
@Composable
private fun ProgressIndicatorGold() {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "progress_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SecondaryGold,
                                SecondaryDarkGold
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
