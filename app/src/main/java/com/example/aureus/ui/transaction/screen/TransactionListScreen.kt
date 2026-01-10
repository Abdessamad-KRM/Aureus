package com.example.aureus.ui.transaction.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.model.TransactionType
import com.example.aureus.ui.transaction.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Transaction List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    accountId: String,
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val transactionsState by viewModel.accountTransactionsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadTransactionsByAccount(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshTransactions(accountId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (transactionsState) {
            Resource.Idle,
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val transactions = (transactionsState as Resource.Success<List<Transaction>>).data
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(transactions) { transaction ->
                            TransactionItem(transaction = transaction)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (transactionsState as Resource.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshTransactions(accountId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction type icon
            Icon(
                imageVector = if (transaction.type == TransactionType.CREDIT)
                    Icons.Default.ArrowDownward
                else
                    Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (transaction.type == TransactionType.CREDIT)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                transaction.merchant?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                transaction.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.CREDIT) "+" else "-"}${formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.CREDIT)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Balance: ${formatCurrency(transaction.balanceAfter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}