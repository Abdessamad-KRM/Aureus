package com.example.aureus.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.transform.RoundedCornersTransformation
import com.example.aureus.ui.theme.SecondaryGold
import com.example.aureus.ui.theme.NeutralLightGray

// Default placeholder painters
private val DEFAULT_PLACEHOLDER: Painter = ColorPainter(NeutralLightGray)
private val DEFAULT_ERROR: Painter = ColorPainter(Color.Transparent)

/**
 * Optimized Profile Image Component - Phase 15: Performance Optimization
 * Uses Coil with memory/disk caching and proper sizing
 */
@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter = DEFAULT_PLACEHOLDER,
    error: Painter = DEFAULT_ERROR,
    contentDescription: String? = null
) {
    AsyncImage(
        model = CoilImageLoader.buildProfileImageRequest(
            context = androidx.compose.ui.platform.LocalContext.current,
            imageUrl = imageUrl
        ),
        placeholder = placeholder,
        error = error,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}

/**
 * Optimized Card Image Component - Phase 15: Performance Optimization
 */
@Composable
fun CardImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter = DEFAULT_PLACEHOLDER,
    error: Painter = DEFAULT_ERROR,
    contentDescription: String? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    AsyncImage(
        model = CoilImageLoader.buildCardImageRequest(
            context = androidx.compose.ui.platform.LocalContext.current,
            imageUrl = imageUrl
        ),
        placeholder = placeholder,
        error = error,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape)
    )
}

/**
 * Optimized General Image Component - Phase 15: Performance Optimization
 * With loading state support
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    width: Int = 1024,
    height: Int = 1024
) {
    SubcomposeAsyncImage(
        model = CoilImageLoader.buildGeneralImageRequest(
            context = androidx.compose.ui.platform.LocalContext.current,
            imageUrl = imageUrl,
            width = width,
            height = height
        ),
        loading = {
            placeholder?.invoke() ?: DefaultImagePlaceholder()
        },
        error = {
            error?.invoke() ?: DefaultImageError()
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * Default loading placeholder
 */
@Composable
private fun DefaultImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = SecondaryGold,
            strokeWidth = 2.dp
        )
    }
}

/**
 * Default error placeholder
 */
@Composable
private fun DefaultImageError() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Error indicator could be added here
    }
}

/**
 * Optimized Rounded Image Component - Phase 15: Performance Optimization
 * Applies rounded corners via modifier
 */
@Composable
fun RoundedCornerImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Float = 16f,
    placeholder: Painter = DEFAULT_PLACEHOLDER,
    error: Painter = DEFAULT_ERROR,
    contentDescription: String? = null
) {
    AsyncImage(
        model = CoilImageLoader.buildCardImageRequest(
            context = androidx.compose.ui.platform.LocalContext.current,
            imageUrl = imageUrl
        ),
        placeholder = placeholder,
        error = error,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius.dp))
    )
}