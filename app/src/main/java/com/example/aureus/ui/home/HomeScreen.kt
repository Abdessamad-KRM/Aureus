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
import com.example.aureus.data.*
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home/Dashboard Screen - Main screen of the app
 */
@Composable
fun HomeScreen(
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val defaultCard = remember { StaticCards.cards.first() }
    val recentTransactions = remember { StaticTransactions.transactions.take(5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            HomeHeader(
                user = TestAccount.user,
                onProfileClick = onNavigateToProfile
            )
        }

        // Balance Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            BankCardDisplay(
                card = defaultCard,
                onClick = onNavigateToCards
            )
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(24.dp))
            QuickActionsRow()
        }

        // Mini Chart
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MiniChartCard(onClick = onNavigateToStatistics)
        }

        // Recent Transactions
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(recentTransactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = { /* Navigate to transaction detail */ }
            )
        }

        item {
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
    user: com.example.aureus.data.User,
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
                    text = "${user.firstName.first()}${user.lastName.first()}",
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
                    text = user.firstName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }
        }

        IconButton(onClick = { /* Notifications */ }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = PrimaryNavyBlue
            )
        }
    }
}

@Composable
private fun BankCardDisplay(
    card: BankCard,
    onClick: () -> Unit
) {
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
                        text = "Total Balance",
                        color = NeutralWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SecondaryGold
                    )
                }

                // Balance
                Text(
                    text = formatCurrency(card.balance),
                    color = NeutralWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                // Card number and details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = maskCardNumber(card.cardNumber),
                            color = NeutralWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.cardHolder,
                            color = NeutralWhite.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = card.cardType.name,
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
private fun QuickActionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            onClick = { /* Send money */ }
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            label = "Request",
            onClick = { /* Request money */ }
        )
        QuickActionButton(
            icon = Icons.Default.QrCodeScanner,
            label = "Scan",
            onClick = { /* Scan QR */ }
        )
        QuickActionButton(
            icon = Icons.Default.MoreHoriz,
            label = "More",
            onClick = { /* More options */ }
        )
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
                tint = PrimaryNavyBlue,
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

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
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
                            when (transaction.type) {
                                TransactionType.INCOME -> SemanticGreen.copy(alpha = 0.1f)
                                TransactionType.EXPENSE -> SemanticRed.copy(alpha = 0.1f)
                                TransactionType.TRANSFER -> SecondaryGold.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.INCOME -> Icons.Default.TrendingUp
                            TransactionType.EXPENSE -> Icons.Default.TrendingDown
                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.INCOME -> SemanticGreen
                            TransactionType.EXPENSE -> SemanticRed
                            TransactionType.TRANSFER -> SecondaryGold
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = formatTransactionDate(transaction.date),
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            Text(
                text = "${if (transaction.amount > 0) "+" else ""}${formatCurrency(transaction.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) SemanticGreen else SemanticRed
            )
        }
    }
}

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

private fun formatTransactionDate(date: Date): String {
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}
