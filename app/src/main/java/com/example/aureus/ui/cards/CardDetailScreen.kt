package com.example.aureus.ui.cards

import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardStatus
import com.example.aureus.ui.cards.viewmodel.CardDetailViewModel
import com.example.aureus.ui.cards.viewmodel.CardDetailUiState
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    LaunchedEffect(cardId) {
        viewModel.loadCardDetail(cardId)
    }

    val uiState by viewModel.cardDetailState.collectAsState()
    val cardTransactions by viewModel.cardTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CardDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                }
            }
            is CardDetailUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadCardDetail(cardId) }
                )
            }
            is CardDetailUiState.Success -> {
                CardDetailContent(
                    card = state.card,
                    transactions = cardTransactions,
                    onFreezeCard = { viewModel.freezeCard(cardId) },
                    onBlockCard = { viewModel.blockCard(cardId) },
                    onSetDefault = { viewModel.setAsDefault(cardId) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: com.example.aureus.domain.model.CardDetail,
    transactions: List<Map<String, Any>>,
    onFreezeCard: () -> Unit,
    onBlockCard: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralLightGray),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card Display
        item {
            CardDisplay(card = card)
        }

        // Balance and Limits
        item {
            BalanceAndLimitsCard(card = card)
        }

        // Status Badge
        item {
            StatusCard(
                status = card.status,
                isActive = card.isActive,
                onFreezeCard = onFreezeCard,
                onBlockCard = onBlockCard,
                onSetDefault = onSetDefault,
                isDefault = card.isDefault,
                cardId = card.id
            )
        }

        // Recent Transactions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = NeutralMediumGray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavyBlue
                        )
                    }
                }
            }
        } else {
            items(transactions.take(10)) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
private fun CardDisplay(card: com.example.aureus.domain.model.CardDetail) {
    val gradient = when (card.cardColor) {
        CardColor.NAVY -> Brush.horizontalGradient(
            listOf(PrimaryNavyBlue, PrimaryMediumBlue)
        )
        CardColor.GOLD -> Brush.horizontalGradient(
            listOf(SecondaryGold, SecondaryDarkGold)
        )
        CardColor.BLACK -> Brush.horizontalGradient(
            listOf(Color.Black, Color.DarkGray)
        )
        CardColor.BLUE -> Brush.horizontalGradient(
            listOf(PrimaryMediumBlue, PrimaryNavyBlue)
        )
        CardColor.PURPLE -> Brush.horizontalGradient(
            listOf(Color(0xFF6B21A8), Color(0xFF9333EA))
        )
        CardColor.GREEN -> Brush.horizontalGradient(
            listOf(Color(0xFF059669), Color(0xFF10B981))
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = NeutralWhite.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column {
                    Text(
                        text = maskCardNumber(card.cardNumber),
                        color = NeutralWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CARD HOLDER",
                                color = NeutralWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = card.cardHolder.uppercase(),
                                color = NeutralWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXPIRES",
                                color = NeutralWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = card.expiryDate,
                                color = NeutralWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = when (card.cardType) {
                                com.example.aureus.domain.model.CardType.VISA -> "VISA"
                                com.example.aureus.domain.model.CardType.MASTERCARD -> "MASTERCARD"
                                com.example.aureus.domain.model.CardType.AMEX -> "AMEX"
                                com.example.aureus.domain.model.CardType.DISCOVER -> "DISCOVER"
                            },
                            color = SecondaryGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceAndLimitsCard(card: com.example.aureus.domain.model.CardDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Balance & Limits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance
            StatRow("Available Balance", formatCurrency(card.balance), SecondaryGold)
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Daily Limit
            val dailyPercentage = if (card.dailyLimit > 0) {
                ((card.spendingToday / card.dailyLimit) * 100).toInt()
            } else 0
            StatRow(
                "Daily Limit",
                "${formatCurrency(card.spendingToday)} / ${formatCurrency(card.dailyLimit)}",
                color = if (dailyPercentage > 80) SemanticRed else PrimaryNavyBlue
            )

            // Progress bar for daily limit
            LinearProgressIndicator(
                progress = { (dailyPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = if (dailyPercentage > 80) SemanticRed else SecondaryGold,
                trackColor = NeutralLightGray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Monthly Limit
            MonthlyLimitRow(card)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MonthlyLimitRow(card: com.example.aureus.domain.model.CardDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Monthly Limit",
            fontSize = 14.sp,
            color = NeutralMediumGray
        )
        Text(
            text = formatCurrency(card.monthlyLimit),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavyBlue
        )
    }
}

@Composable
private fun StatusCard(
    status: CardStatus,
    isActive: Boolean,
    onFreezeCard: () -> Unit,
    onBlockCard: () -> Unit,
    onSetDefault: () -> Unit,
    isDefault: Boolean,
    cardId: String
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = when (status) {
            CardStatus.ACTIVE -> SemanticGreen.copy(alpha = 0.1f)
            CardStatus.FROZEN -> SemanticRed.copy(alpha = 0.1f)
            CardStatus.BLOCKED -> NeutralMediumGray.copy(alpha = 0.1f)
            CardStatus.EXPIRED -> NeutralMediumGray.copy(alpha = 0.1f)
            CardStatus.PENDING -> SemanticAmber.copy(alpha = 0.1f)
        }),
        shape = RoundedCornerShape(16.dp)
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
                        .background(when (status) {
                            CardStatus.ACTIVE -> SemanticGreen
                            CardStatus.FROZEN -> SemanticRed
                            CardStatus.BLOCKED -> NeutralMediumGray
                            CardStatus.EXPIRED -> NeutralMediumGray
                            CardStatus.PENDING -> SemanticAmber
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (status) {
                            CardStatus.ACTIVE -> Icons.Default.CheckCircle
                            CardStatus.FROZEN -> Icons.Default.AcUnit
                            CardStatus.BLOCKED -> Icons.Default.Block
                            CardStatus.EXPIRED -> Icons.Default.Warning
                            CardStatus.PENDING -> Icons.Default.Pending
                        },
                        contentDescription = null,
                        tint = NeutralWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = status.name.replace("_", " "),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            CardStatus.ACTIVE -> SemanticGreen
                            CardStatus.FROZEN -> SemanticRed
                            CardStatus.BLOCKED -> NeutralMediumGray
                            CardStatus.EXPIRED -> NeutralMediumGray
                            CardStatus.PENDING -> SemanticAmber
                        }
                    )
                    Text(
                        text = when (status) {
                            CardStatus.ACTIVE -> "Card is active and ready to use"
                            CardStatus.FROZEN -> "Card is temporarily frozen"
                            CardStatus.BLOCKED -> "Card is permanently blocked"
                            CardStatus.EXPIRED -> "Card has expired"
                            CardStatus.PENDING -> "Card is pending activation"
                        },
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Row {
                if (isDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("DEFAULT", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SecondaryGold.copy(alpha = 0.2f),
                            labelColor = SecondaryGold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box {
                    IconButton(onClick = { showActionMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions"
                        )
                    }

                    DropdownMenu(
                        expanded = showActionMenu,
                        onDismissRequest = { showActionMenu = false }
                    ) {
                        if (status == CardStatus.ACTIVE && !isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as Default") },
                                leadingIcon = { Icon(Icons.Default.Star, null) },
                                onClick = {
                                    showActionMenu = false
                                    onSetDefault()
                                }
                            )
                        }
                        if (status == CardStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text("Freeze Card") },
                                leadingIcon = { Icon(Icons.Default.AcUnit, null) },
                                onClick = {
                                    showActionMenu = false
                                    onFreezeCard()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Block Card") },
                            leadingIcon = { Icon(Icons.Default.Block, null) },
                            onClick = {
                                showActionMenu = false
                                onBlockCard()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Map<String, Any>) {
    val amount = (transaction["amount"] as? Number)?.toDouble() ?: 0.0
    val type = transaction["type"] as? String ?: "EXPENSE"
    val title = transaction["title"] as? String ?: "Transaction"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavyBlue
                )
                Text(
                    text = formatTransactionDate(transaction["createdAt"]),
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
            Text(
                text = if (type == "INCOME") "+${formatCurrency(amount)}" else formatCurrency(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (type == "INCOME") SemanticGreen else SemanticRed
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(NeutralLightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = SemanticRed.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = SemanticRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
            ) {
                Text("Retry")
            }
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
    return "**** **** **** ${cardNumber.takeLast(4)}"
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