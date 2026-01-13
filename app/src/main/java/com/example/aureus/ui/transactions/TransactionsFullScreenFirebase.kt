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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.transaction.viewmodel.TransactionViewModelFirebase
import com.example.aureus.ui.transaction.viewmodel.DatePeriod
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Transactions Full Screen - Firebase-based
 * Version 100% dynamique avec TransactionViewModelFirebase
 * Remplace StaticTransactions par des données Firestore en temps réel
 * ✅ PHASE 2: FLAG_SECURE enabled to prevent screenshots
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsFullScreenFirebase(
    viewModel: TransactionViewModelFirebase = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onTransactionClick: (String) -> Unit = {}
) {
    // ✅ PHASE 2: Prevent screenshots on sensitive transaction screens
    com.example.aureus.ui.components.SecureScreenFlag(enabled = true) {
        val uiState by viewModel.transactionsState.collectAsState()
        val filteredTransactions by viewModel.filteredTransactionsState.collectAsState()
        val selectedFilter by viewModel.selectedFilter.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDateFilterDialog by remember { mutableStateOf(false) }
        var selectedDatePeriod by remember { mutableStateOf(DatePeriod.All) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transactions", fontWeight = FontWeight.Bold)
                        if (selectedDatePeriod != DatePeriod.All) {
                            Text(
                                text = selectedDatePeriod.name.lowercase().replace("_", " "),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDateFilterDialog = true }) {
                        Icon(Icons.Default.DateRange, "Filter by date")
                    }
                    IconButton(onClick = onSearchClick) {
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
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchTransactions(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search transactions...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedPlaceholderColor = NeutralMediumGray,
                            unfocusedPlaceholderColor = NeutralMediumGray
                        )
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.resetFilters() }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                }
            }

            // Statistics Summary Card
            if (uiState.totalIncome > 0 || uiState.totalExpense > 0) {
                StatisticsSummaryCard(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense
                )
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Income", "Expense").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.filterByType(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryGold,
                            selectedLabelColor = PrimaryNavyBlue
                        )
                    )
                }
            }

            // Transactions list
            if (uiState.isLoading && filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                }
            } else if (uiState.error != null) {
                ErrorState(
                    message = uiState.error,
                    onRetry = { viewModel.refreshTransactions() }
                )
            } else if (filteredTransactions.isEmpty()) {
                EmptyState(
                    message = "No transactions found",
                    description = "Your transactions will appear here"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { transactionMap ->
                        DynamicTransactionItem(
                            transaction = transactionMap as Map<String, Any>,
                            onClick = {
                                val transactionId = transactionMap["transactionId"] as? String ?: ""
                                if (transactionId.isNotBlank()) {
                                    onTransactionClick(transactionId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Date Filter Dialog
    if (showDateFilterDialog) {
        DateFilterDialog(
            selectedPeriod = selectedDatePeriod,
            onPeriodSelected = { period ->
                selectedDatePeriod = period
                viewModel.filterByDatePeriod(period)
                showDateFilterDialog = false
            },
            onDismiss = { showDateFilterDialog = false }
        )
    }
    }
}

/**
 * Transaction Item Dynamic - Utilise les données Firestore
 */
@Composable
private fun DynamicTransactionItem(
    transaction: Map<String, Any>,
    onClick: () -> Unit
) {
    val title = transaction["title"] as? String ?: "Transaction"
    val amount = transaction["amount"] as? Double ?: 0.0
    val type = transaction["type"] as? String ?: "EXPENSE"
    val category = transaction["category"] as? String ?: "Other"
    val createdAt = transaction["createdAt"]

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                        .size(48.dp)
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
                        imageVector = getIconForCategory(category),
                        contentDescription = null,
                        tint = when (type) {
                            "INCOME" -> SemanticGreen
                            "EXPENSE" -> SemanticRed
                            else -> SecondaryGold
                        }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = formatTransactionDate(createdAt),
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (amount > 0 || type == "INCOME") "+" else ""}${formatCurrency(amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amount > 0 || type == "INCOME") SemanticGreen else SemanticRed
                )
            }
        }
    }
}

/**
 * Statistics Summary Card
 */
@Composable
private fun StatisticsSummaryCard(
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryNavyBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Income",
                    fontSize = 12.sp,
                    color = NeutralWhite.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(totalIncome),
                    fontSize = 18.sp,
                    color = SemanticGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(NeutralWhite.copy(alpha = 0.3f))
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Expense",
                    fontSize = 12.sp,
                    color = NeutralWhite.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(totalExpense),
                    fontSize = 18.sp,
                    color = SemanticRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Empty State
 */
@Composable
private fun EmptyState(
    message: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NeutralMediumGray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = NeutralMediumGray
            )
        }
    }
}

/**
 * Error State
 */
@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = SemanticRed.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message ?: "An error occurred",
                fontSize = 16.sp,
                color = SemanticRed,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

/**
 * Date Filter Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterDialog(
    selectedPeriod: DatePeriod,
    onPeriodSelected: (DatePeriod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Filter by Date",
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = {
                            Text(
                                when (period) {
                                    DatePeriod.All -> "All Time"
                                    DatePeriod.Today -> "Today"
                                    DatePeriod.ThisWeek -> "This Week"
                                    DatePeriod.ThisMonth -> "This Month"
                                    DatePeriod.ThisYear -> "This Year"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SecondaryGold)
            }
        }
    )
}

// ==================== HELPER FUNCTIONS ====================

private fun getIconForCategory(category: String) = when (category.lowercase()) {
    "shopping" -> Icons.Default.ShoppingBag
    "food", "groceries" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "entertainment" -> Icons.Default.Movie
    "bills", "utilities" -> Icons.Default.Receipt
    "salary" -> Icons.Default.AccountBalance
    else -> Icons.Default.Payment
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun formatTransactionDate(timestamp: Any?): String {
    if (timestamp == null) return "Just now"
    val date = when (timestamp) {
        is com.google.firebase.Timestamp -> timestamp.toDate()
        is Date -> timestamp
        else -> return "Just now"
    }
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}