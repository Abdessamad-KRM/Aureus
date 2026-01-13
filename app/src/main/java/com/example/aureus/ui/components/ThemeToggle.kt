package com.example.aureus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Toggle Theme Switcher - Component pour changer le thème
 * Utilise une animation fluide pour le changement de thème
 */
@Composable
fun ThemeToggle(
    isDark: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E293B) else Color(0xFFF3F4F6),
        animationSpec = tween(durationMillis = 300),
        label = "background"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (isDark) SecondaryGold else PrimaryNavyBlue,
        animationSpec = tween(durationMillis = 300),
        label = "thumb"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDark) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    val icon: ImageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onThemeChanged(!isDark) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isDark) "Dark" else "Light",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .width(52.dp)
                .height(30.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(15.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(15.dp)
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .fillMaxHeight()
                    .width(24.dp)
                    .align(if (isDark) Alignment.CenterEnd else Alignment.CenterStart)
                    .scale(scale)
                    .background(
                        color = thumbColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Theme Settings Dialog - Dialog complet pour les préférences de thème
 */
@Composable
fun ThemeSettingsDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onThemeChanged: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Theme Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose your preferred theme mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Light Theme Option
                ThemeOption(
                    title = "Light Mode",
                    description = "Bright and clean interface",
                    icon = Icons.Default.LightMode,
                    isSelected = !isDark,
                    onClick = { onThemeChanged(false) }
                )

                // Dark Theme Option
                ThemeOption(
                    title = "Dark Mode",
                    description = "Easy on the eyes",
                    icon = Icons.Default.DarkMode,
                    isSelected = isDark,
                    onClick = { onThemeChanged(true) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Theme Option - Component pour sélectionner une option de thème
 */
@Composable
private fun ThemeOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Theme Settings Screen - Écran complet pour les paramètres de thème
 */
@Composable
fun ThemeSettingsScreen(
    themeManager: ThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkFlow = themeManager.darkMode.collectAsState(initial = false)
    val isDark = isDarkFlow.value

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ThemeToggle(
                        isDark = isDark,
                        onThemeChanged = { newIsDark ->
                            scope.launch {
                                themeManager.setDarkMode(newIsDark)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isDark) "Dark mode is currently enabled" else "Light mode is currently enabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // System Theme Preference
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                themeManager.applySystemTheme()
                            }
                        }
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Use System Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automatically match your device theme settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}