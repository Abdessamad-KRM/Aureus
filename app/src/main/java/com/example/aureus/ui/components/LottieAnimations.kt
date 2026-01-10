package com.example.aureus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.aureus.ui.theme.Gold
import com.example.aureus.ui.theme.NavyBlue

/**
 * Lottie animation URLs repository
 * Using verified URLs from GitHub repositories (LottieFiles & Airbnb)
 */
object LottieUrls {
    // Onboarding - GitHub verified URLs
    const val SECURITY = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/Watermelon.json"
    const val ACCOUNTS = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/TwitterHeart.json"
    const val NOTIFICATIONS = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/PinJump.json"

    // Empty States - GitHub verified URLs
    const val NO_BENEFICIARIES = "https://raw.githubusercontent.com/airbnb/lottie-web/master/demo/json/data.json"
    const val NO_TRANSACTIONS = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/EmptyState.json"
    const val NO_CARDS = "https://raw.githubusercontent.com/airbnb/lottie-web/master/demo/json/data.json"
    const val NO_DATA = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/HamburgerArrow.json"

    // Fallback URLs
    const val EMPTY_LIST_FALLBACK = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/EmptyState.json"
    const val EMPTY_DATA_FALLBACK = "https://raw.githubusercontent.com/airbnb/lottie-web/master/demo/json/data.json"

    // Feedback - GitHub verified URLs
    const val SUCCESS = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/TwitterHeart.json"
    const val ERROR = "https://raw.githubusercontent.com/airbnb/lottie-web/master/demo/json/data.json"
    const val WARNING = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/PinJump.json"

    // Loading - GitHub verified URLs
    const val LOADING = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/LottieLogo1.json"
    const val PROCESSING = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/LottieLogo2.json"

    // Authentication - Using available animations
    const val FINGERPRINT = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/PinJump.json"
    const val FACE_ID = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/Watermelon.json"

    // Features - GitHub verified URLs
    const val WALLET = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/Watermelon.json"
    const val TRANSFER = "https://raw.githubusercontent.com/airbnb/lottie-web/master/demo/json/data.json"
    const val ANALYTICS = "https://raw.githubusercontent.com/LottieFiles/lottie-react-native/master/example/src/assets/TwitterHeart.json"
}

/**
 * Simple Lottie animation component
 */
@Composable
fun SimpleLottieAnimation(
    url: String,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1f
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(url)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

/**
 * Empty state component with Lottie animation
 */
@Composable
fun EmptyStateView(
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    animationUrl: String = LottieUrls.EMPTY_DATA_FALLBACK,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lottie animation
        SimpleLottieAnimation(
            url = animationUrl,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Message
        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = NavyBlue.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        // Action button
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue
                )
            ) {
                Text(
                    text = actionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Loading component with Lottie animation
 */
@Composable
fun LoadingView(
    message: String = "Chargement...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SimpleLottieAnimation(
            url = LottieUrls.LOADING,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = NavyBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * Success feedback component
 */
@Composable
fun SuccessView(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    FeedbackView(
        message = message,
        animationUrl = LottieUrls.SUCCESS,
        buttonText = "Continuer",
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Error feedback component
 */
@Composable
fun ErrorView(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    FeedbackView(
        message = message,
        animationUrl = LottieUrls.ERROR,
        buttonText = "Réessayer",
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Generic feedback component
 */
@Composable
private fun FeedbackView(
    message: String,
    animationUrl: String,
    buttonText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(NavyBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                SimpleLottieAnimation(
                    url = animationUrl,
                    modifier = Modifier.size(140.dp),
                    iterations = 1
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = NavyBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Processing transaction component
 */
@Composable
fun ProcessingTransactionView(
    message: String = "Traitement en cours...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            SimpleLottieAnimation(
                url = LottieUrls.PROCESSING,
                modifier = Modifier.size(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = NavyBlue
        )
    }
}
