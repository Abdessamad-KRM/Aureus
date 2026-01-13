package com.example.aureus.ui.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.aureus.domain.model.Notification as NotificationModel
import com.example.aureus.domain.model.NotificationType
import com.example.aureus.ui.notifications.viewmodel.NotificationViewModel
import com.example.aureus.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                unreadCount = unreadCount,
                onNavigateBack = onNavigateBack,
                onMarkAllAsRead = {
                    if (unreadCount > 0) {
                        viewModel.markAllAsRead()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
        ) {
            when (uiState) {
                is NotificationViewModel.NotificationUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SecondaryGold
                    )
                }
                is NotificationViewModel.NotificationUiState.Error -> {
                    ErrorState((uiState as NotificationViewModel.NotificationUiState.Error).message) {
                        viewModel.refreshNotifications()
                    }
                }
                else -> {
                    if (notifications.isEmpty()) {
                        EmptyState()
                    } else {
                        NotificationList(
                            notifications = notifications,
                            onNotificationClick = { notification ->
                                viewModel.markAsRead(notification.id)
                            },
                            onNotificationDelete = { notificationId ->
                                viewModel.deleteNotification(notificationId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onNavigateBack: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (unreadCount > 0) {
                    Badge(
                        containerColor = SemanticRed,
                        modifier = Modifier.offset(y = (-4).dp)
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            if (unreadCount > 0) {
                TextButton(onClick = onMarkAllAsRead) {
                    Text(
                        text = "Tout marquer comme lu",
                        fontSize = 14.sp,
                        color = SecondaryGold
                    )
                }
            }
            IconButton(onClick = { /* Filtres */ }) {
                Icon(Icons.Default.FilterList, "Filter")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
    )
}

@Composable
private fun NotificationList(
    notifications: List<NotificationModel>,
    onNotificationClick: (NotificationModel) -> Unit,
    onNotificationDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onDelete = { onNotificationDelete(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItem(
    notification: NotificationModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val backgroundColor = if (!notification.isRead) {
        SecondaryGold.copy(alpha = 0.1f)
    } else {
        NeutralWhite
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title + Timestamp + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Notification Icon
                    NotificationTypeIcon(
                        type = notification.type,
                        isRead = notification.isRead
                    )

                    // Title and Body
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = PrimaryNavyBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notification.body,
                            fontSize = 14.sp,
                            color = if (notification.isRead) {
                                NeutralMediumGray
                            } else {
                                PrimaryNavyBlue.copy(alpha = 0.8f)
                            },
                            lineHeight = 18.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = NeutralMediumGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Timestamp and Expand button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(notification.timestamp),
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )

                if (notification.body.length > 100) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Voir moins" else "Voir plus",
                            fontSize = 12.sp,
                            color = SecondaryGold
                        )
                    }
                }
            }

            // Image if available
            notification.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Notification image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la notification") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette notification ?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Supprimer", color = SemanticRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    isRead: Boolean
) {
    val (icon, color) = when (type) {
        NotificationType.TRANSACTION -> Icons.Default.ShoppingCart to SecondaryGold
        NotificationType.TRANSFER_RECEIVED -> Icons.Default.ArrowDownward to Color(android.graphics.Color.parseColor("#4CAF50"))
        NotificationType.TRANSFER_SENT -> Icons.Default.ArrowUpward to SemanticRed
        NotificationType.BALANCE_ALERT -> Icons.Default.AccountBalance to Color(android.graphics.Color.parseColor("#FF9800"))
        NotificationType.SECURITY_ALERT -> Icons.Default.Security to Color(android.graphics.Color.parseColor("#9C27B0"))
        NotificationType.PROMOTION -> Icons.Default.LocalOffer to Color(android.graphics.Color.parseColor("#2196F3"))
        NotificationType.INFO -> Icons.Default.Info to SecondaryGold
        NotificationType.SYSTEM -> Icons.Default.Settings to NeutralMediumGray
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isRead) color.copy(alpha = 0.2f) else color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = NeutralMediumGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucune notification",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralMediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vous serez notifié ici des nouvelles transactions et alertes",
            fontSize = 14.sp,
            color = NeutralMediumGray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SemanticRed.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Erreur de chargement",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralMediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = NeutralMediumGray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
        ) {
            Text("Réessayer")
        }
    }
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "À l'instant"
        minutes < 60 -> "Il y a $minutes min"
        hours < 24 -> "Il y a $hours h"
        days < 2 -> "Hier"
        days < 7 -> "Il y a $days jours"
        else -> SimpleDateFormat("dd MMM", Locale.FRENCH).format(timestamp)
    }
}