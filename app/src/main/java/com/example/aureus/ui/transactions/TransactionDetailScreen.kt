package com.example.aureus.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun TransactionDetailScreen(
    transaction: Transaction = StaticTransactions.transactions.first(),
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { /* Download */ }) {
                        Icon(Icons.Default.Download, "Download")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (transaction.status) {
                        TransactionStatus.COMPLETED -> SemanticGreen.copy(alpha = 0.1f)
                        TransactionStatus.PENDING -> SemanticAmber.copy(alpha = 0.1f)
                        TransactionStatus.FAILED -> SemanticRed.copy(alpha = 0.1f)
                        TransactionStatus.CANCELLED -> NeutralMediumGray.copy(alpha = 0.1f)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                when (transaction.type) {
                                    TransactionType.INCOME -> SemanticGreen.copy(alpha = 0.2f)
                                    TransactionType.EXPENSE -> SemanticRed.copy(alpha = 0.2f)
                                    TransactionType.TRANSFER -> SecondaryGold.copy(alpha = 0.2f)
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
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${if (transaction.amount > 0) "+" else ""}${formatCurrency(transaction.amount)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.amount > 0) SemanticGreen else SemanticRed
                    )

                    Text(
                        text = transaction.status.name,
                        fontSize = 14.sp,
                        color = when (transaction.status) {
                            TransactionStatus.COMPLETED -> SemanticGreen
                            TransactionStatus.PENDING -> SemanticAmber
                            TransactionStatus.FAILED -> SemanticRed
                            TransactionStatus.CANCELLED -> NeutralMediumGray
                        }
                    )
                }
            }

            // Details
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Transaction ID", transaction.id)
                DetailRow("Title", transaction.title)
                DetailRow("Description", transaction.description)
                DetailRow("Date", formatDetailDate(transaction.date))
                DetailRow("Time", formatTime(transaction.date))
                DetailRow("Category", transaction.category.name)
                DetailRow("Type", transaction.type.name)
                transaction.recipientName?.let {
                    DetailRow("Recipient", it)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Download PDF */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt")
                    }

                    OutlinedButton(
                        onClick = { /* Share */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = NeutralMediumGray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun formatDetailDate(date: Date): String {
    return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(date)
}

private fun formatTime(date: Date): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
}
