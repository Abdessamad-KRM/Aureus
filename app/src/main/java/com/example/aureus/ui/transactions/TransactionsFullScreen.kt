package com.example.aureus.ui.transactions

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.data.*
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsFullScreen(
    onNavigateBack: () -> Unit = {},
    onSearch: () -> Unit = {}
) {
    val transactions = remember { StaticTransactions.transactions }
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Income", "Expense").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryGold,
                            selectedLabelColor = PrimaryNavyBlue
                        )
                    )
                }
            }

            // Transactions list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredTransactions = when (selectedFilter) {
                    "Income" -> transactions.filter { it.type == TransactionType.INCOME }
                    "Expense" -> transactions.filter { it.type == TransactionType.EXPENSE }
                    else -> transactions
                }

                items(filteredTransactions) { transaction ->
                    TransactionDetailItem(transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                        imageVector = when (transaction.category) {
                            TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
                            TransactionCategory.FOOD -> Icons.Default.Restaurant
                            TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
                            TransactionCategory.ENTERTAINMENT -> Icons.Default.Movie
                            TransactionCategory.BILLS -> Icons.Default.Receipt
                            TransactionCategory.SALARY -> Icons.Default.AccountBalance
                            else -> Icons.Default.Payment
                        },
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.INCOME -> SemanticGreen
                            TransactionType.EXPENSE -> SemanticRed
                            TransactionType.TRANSFER -> SecondaryGold
                        }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = formatDate(transaction.date),
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                    Text(
                        text = transaction.category.name,
                        fontSize = 11.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.amount > 0) "+" else ""}${formatCurrency(transaction.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount > 0) SemanticGreen else SemanticRed
                )
                Text(
                    text = formatTime(transaction.date),
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}

private fun formatTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}
