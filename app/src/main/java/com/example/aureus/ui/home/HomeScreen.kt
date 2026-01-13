package com.example.aureus.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.home.viewmodel.HomeViewModel
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home/Dashboard Screen - Main screen of the app
 * Version démo statique avec données fictives
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToRequestMoney: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Phase 15: Performance Optimization - Remember expensive computations
    val userName = remember { viewModel.getCurrentUserName() }
    val defaultCard = remember(uiState.defaultCard) { uiState.defaultCard }
    val recentTransactions = remember(uiState.recentTransactions) { uiState.recentTransactions }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SecondaryGold)
        }
        return
    }

    // Phase 15: Performance Optimization - Optimized LazyColumn with keys
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray),
        contentPadding = PaddingValues(bottom = 80.dp), // Extra padding for bottom nav
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item(key = "header") {
            HomeHeaderDemo(
                userName = userName,
                onProfileClick = onNavigateToProfile,
                onNotificationsClick = onNavigateToNotifications,
                viewModel = viewModel
            )
        }

        // Balance Card
        item(key = "balance_card") {
            DynamicBalanceCard(
                balance = uiState.totalBalance,
                defaultCard = defaultCard,
                onClick = onNavigateToCards
            )
        }

        // Quick Actions
        item(key = "quick_actions") {
            QuickActionsRow(
                onNavigateToSendMoney = onNavigateToSendMoney,
                onNavigateToRequestMoney = onNavigateToRequestMoney
            )
        }

        // Mini Chart
        item(key = "mini_chart") {
            MiniChartCard(onClick = onNavigateToStatistics)
        }

        // Recent Transactions Header
        item(key = "transactions_header") {
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Phase 15: Performance Optimization - Items with keys for stable caching
        items(
            items = recentTransactions,
            key = { transaction ->
                // Use transactionId for stable key, fallback to random for stability
                (transaction as? Map<String, Any>)?.get("transactionId")?.toString()
                    ?: transaction.hashCode().toString()
            }
        ) { transaction ->
            DynamicTransactionItem(
                transaction = transaction as Map<String, Any>,
                onClick = { /* Navigate to transaction detail */ }
            )
        }

        // View All Button
        item(key = "view_all_button") {
            TextButton(
                onClick = onNavigateToTransactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text("View All Transactions", color = SecondaryGold)
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold.copy(alpha = 0.2f))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.first().uppercaseChar().toString() else "U",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Bienvenue,",
                    fontSize = 14.sp,
                    color = NeutralMediumGray
                )
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryNavyBlue
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderDemo(
    userName: String,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    viewModel: HomeViewModel
) {
    val unreadCount by viewModel.unreadCount.collectAsState(initial = 0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold.copy(alpha = 0.2f))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.first().uppercaseChar().toString() else "U",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Bienvenue,",
                    fontSize = 14.sp,
                    color = NeutralMediumGray
                )
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp)) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = PrimaryNavyBlue
                    )
                }
                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp),
                        containerColor = SemanticRed
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicHomeHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold.copy(alpha = 0.2f))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.first().uppercaseChar().toString() else "U",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = NeutralMediumGray
                )
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryNavyBlue
                )
            }
        }
    }
}

@Composable
private fun DynamicBalanceCard(
    balance: Double,
    defaultCard: Map<String, Any>?,
    onClick: () -> Unit
) {
    val cardNumber = defaultCard?.get("cardNumber") as? String ?: "4242"
    val cardHolder = defaultCard?.get("cardHolder") as? String ?: "User"
    val cardType = defaultCard?.get("cardType") as? String ?: "VISA"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Solde Total",
                        color = NeutralWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Balance
                Text(
                    text = formatCurrency(balance),
                    color = NeutralWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                // Card number and details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "**** **** **** $cardNumber",
                            color = NeutralWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cardHolder,
                            color = NeutralWhite.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = cardType,
                        color = SecondaryGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicTransactionItem(
    transaction: Map<String, Any>,
    onClick: () -> Unit
) {
    val title = transaction["title"] as? String ?: "Transaction"
    val amount = transaction["amount"] as? Double ?: 0.0
    val type = transaction["type"] as? String ?: "EXPENSE"
    val createdAt = transaction["createdAt"] as? com.google.firebase.Timestamp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (type) {
                                "INCOME" -> SemanticGreen.copy(alpha = 0.1f)
                                "EXPENSE" -> SemanticRed.copy(alpha = 0.1f)
                                else -> SecondaryGold.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            "INCOME" -> Icons.Default.TrendingUp
                            "EXPENSE" -> Icons.Default.TrendingDown
                            else -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        tint = when (type) {
                            "INCOME" -> SemanticGreen
                            "EXPENSE" -> SemanticRed
                            else -> SecondaryGold
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = formatTransactionDate(createdAt),
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            Text(
                text = "${if (amount > 0) "+" else ""}${formatCurrency(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (amount > 0 || type == "INCOME") SemanticGreen else SemanticRed
            )
        }
    }
}



@Composable
private fun QuickActionsRow(
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToRequestMoney: () -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            onClick = onNavigateToSendMoney
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            label = "Request",
            onClick = onNavigateToRequestMoney
        )
        QuickActionButton(
            icon = Icons.Default.QrCodeScanner,
            label = "Scan",
            onClick = { /* Scan QR - Future feature */ }
        )

        // More button with DropdownMenu
        Box {
            QuickActionButton(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                onClick = { showMoreMenu = true }
            )

            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                    onClick = {
                        showMoreMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Help") },
                    leadingIcon = { Icon(Icons.Default.Help, null) },
                    onClick = {
                        showMoreMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    onClick = {
                        showMoreMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(NeutralWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SecondaryGold,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = PrimaryNavyBlue
        )
    }
}

@Composable
private fun MiniChartCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = "This month",
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "View Stats",
                    tint = SemanticGreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SimplifiedChart()
        }
    }
}

@Composable
private fun SimplifiedChart() {
    // Simplified wave chart visualization
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val values = listOf(0.3f, 0.6f, 0.4f, 0.8f, 0.5f, 0.9f, 0.7f)
        values.forEach { value ->
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight(value)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SecondaryGold, SecondaryGold.copy(alpha = 0.3f))
                        )
                    )
            )
        }
    }
}

// Obsolete - Use DynamicTransactionItem instead (Firebase-based)
/*
@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    // Legacy component - no longer used
}
*/

@Composable
private fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home, "Home", selectedIndex == 0) { onItemSelected(0) }
            BottomNavItem(Icons.Default.BarChart, "Stats", selectedIndex == 1) { onItemSelected(1) }
            BottomNavItem(Icons.Default.CreditCard, "Cards", selectedIndex == 2) { onItemSelected(2) }
            BottomNavItem(Icons.Default.Settings, "Settings", selectedIndex == 3) { onItemSelected(3) }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) SecondaryGold else NeutralMediumGray,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold)
            )
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun maskCardNumber(cardNumber: String): String {
    return "**** **** **** " + cardNumber.takeLast(4)
}

private fun formatTransactionDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Just now"
    val date = timestamp.toDate()
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}

// Keep original for compatibility with Transaction data class
private fun formatTransactionDate(date: Date): String {
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}
