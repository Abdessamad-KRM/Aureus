package com.example.aureus.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreenFirebase(
    transactionId: String = "",
    onNavigateBack: () -> Unit = {},
    firebaseDataManager: com.example.aureus.data.remote.firebase.FirebaseDataManager? = null
) {
    var transaction by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load transaction details from Firebase
    LaunchedEffect(transactionId) {
        if (transactionId.isNotBlank()) {
            isLoading = true
            try {
                val result = firebaseDataManager?.getTransactionById(transactionId)
                if (result?.isSuccess == true) {
                    transaction = result.getOrNull()
                } else {
                    errorMessage = result?.exceptionOrNull()?.message ?: "Transaction not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load transaction: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Transaction Details", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SemanticRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            fontSize = 16.sp,
                            color = NeutralMediumGray
                        )
                    }
                }
            }
            transaction != null -> {
                val trx = transaction!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralLightGray)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Amount Card
                    val type = trx["type"] as? String ?: "EXPENSE"
                    val amount = trx["amount"] as? Double ?: 0.0
                    val amountColor = if (type == "INCOME") SemanticGreen else SemanticRed

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            if (type == "INCOME") 
                                SemanticGreen.copy(alpha = 0.1f) 
                            else SemanticRed.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (type == "INCOME") "Received" else "Paid",
                                fontSize = 14.sp,
                                color = if (type == "INCOME") SemanticGreen else SemanticRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${if (type == "INCOME") "+" else ""}${formatCurrency(amount)}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            )
                            val status = trx["status"] as? String ?: "UNKNOWN"
                            Spacer(modifier = Modifier.height(8.dp))
                            StatusBadge(status)
                        }
                    }

                    // Transaction Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = NeutralWhite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Transaction Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavyBlue
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            TransactionInfoRow(
                                label = "Title",
                                value = trx["title"] as? String ?: ""
                            )
                            TransactionInfoRow(
                                label = "Category",
                                value = trx["category"] as? String ?: ""
                            )
                            TransactionInfoRow(
                                label = "Merchant",
                                value = trx["merchant"] as? String ?: ""
                            )
                            TransactionInfoRow(
                                label = "Status",
                                value = formatStatus(trx["status"] as? String ?: "")
                            )
                        }
                    }

                    // Date & Time
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = NeutralWhite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            val createdAt = trx["createdAt"]
                            val dateStr = when (createdAt) {
                                is Timestamp -> formatDate(createdAt.toDate())
                                is Date -> formatDate(createdAt)
                                else -> "Unknown"
                            }
                            TransactionInfoRow(
                                label = "Date & Time",
                                value = dateStr
                            )
                        }
                    }

                    // Description
                    val description = trx["description"] as? String
                    if (!description.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = NeutralWhite
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    fontSize = 14.sp,
                                    color = NeutralMediumGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = description,
                                    fontSize = 16.sp,
                                    color = PrimaryNavyBlue
                                )
                            }
                        }
                    }

                    // Transaction ID
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = NeutralWhite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            TransactionInfoRow(
                                label = "Transaction ID",
                                value = trx["transactionId"] as? String ?: transactionId
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Share Receipt TODO */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share")
                        }
                        Button(
                            onClick = { /* Download Receipt TODO */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNavyBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.Download,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Receipt")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (statusText, statusColor) = when (status.uppercase()) {
        "COMPLETED" -> "Completed" to SemanticGreen
        "PENDING" -> "Pending" to SemanticAmber
        "FAILED" -> "Failed" to SemanticRed
        "CANCELLED" -> "Cancelled" to NeutralMediumGray
        else -> status to NeutralMediumGray
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
                        )
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(
                horizontal = 12.dp, 
                vertical = 6.dp
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

@Composable
private fun TransactionInfoRow(label: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = NeutralMediumGray
        )
        Text(
            text = value.ifBlank { "N/A" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryNavyBlue
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun formatStatus(status: String): String {
    return status.replace("_", " ")
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}