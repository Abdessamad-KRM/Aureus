package com.example.aureus.ui.cards

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.data.*
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.util.*

/**
 * All Cards Screen - Display all user cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCardsScreen(
    onNavigateBack: () -> Unit = {},
    onAddCard: () -> Unit = {}
) {
    val cards = remember { StaticCards.cards }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddCard) {
                        Icon(Icons.Default.Add, "Add Card")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cards) { card ->
                DetailedCardItem(
                    card = card,
                    onClick = { /* Navigate to card detail */ }
                )
            }

            item {
                AddCardButton(onClick = onAddCard)
            }
        }
    }
}

/**
 * My Cards Screen - Compact view with card management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardsScreen(
    onNavigateBack: () -> Unit = {},
    onAddCard: () -> Unit = {}
) {
    val cards = remember { StaticCards.cards }
    var selectedCard by remember { mutableStateOf(cards.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Card Carousel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                FullCardDisplay(
                    card = selectedCard,
                    onCardClick = { /* Show card details */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cards.forEach { card ->
                        CardSelectorDot(
                            isSelected = card.id == selectedCard.id,
                            onClick = { selectedCard = card }
                        )
                    }
                }
            }

            // Card Info Section
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Card Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                }

                item {
                    CardInfoRow("Card Number", maskFullCard(selectedCard.cardNumber))
                }
                item {
                    CardInfoRow("Card Holder", selectedCard.cardHolder)
                }
                item {
                    CardInfoRow("Expiry Date", selectedCard.expiryDate)
                }
                item {
                    CardInfoRow("Card Type", selectedCard.cardType.name)
                }
                item {
                    CardInfoRow("Balance", formatCurrency(selectedCard.balance))
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Button(
                        onClick = { /* Set as default */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCard.isDefault) SemanticGreen else PrimaryNavyBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedCard.isDefault) Icons.Default.Check else Icons.Default.Star,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedCard.isDefault) "Default Card" else "Set as Default")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onAddCard,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecondaryGold
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Card")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedCardItem(
    card: BankCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = when (card.cardColor) {
                        CardColor.NAVY -> Brush.horizontalGradient(
                            listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                        )
                        CardColor.GOLD -> Brush.horizontalGradient(
                            listOf(SecondaryGold, SecondaryDarkGold)
                        )
                        CardColor.BLACK -> Brush.horizontalGradient(
                            listOf(Color(0xFF212121), Color(0xFF424242))
                        )
                        CardColor.BLUE -> Brush.horizontalGradient(
                            listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                        )
                        CardColor.PURPLE -> Brush.horizontalGradient(
                            listOf(Color(0xFF7B1FA2), Color(0xFF9C27B0))
                        )
                        CardColor.GREEN -> Brush.horizontalGradient(
                            listOf(Color(0xFF388E3C), Color(0xFF66BB6A))
                        )
                    }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Balance",
                            color = NeutralWhite.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatCurrency(card.balance),
                            color = NeutralWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (card.isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SemanticGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                color = SemanticGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = maskCardNumber(card.cardNumber),
                    color = NeutralWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARD HOLDER",
                            color = NeutralWhite.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = card.cardHolder,
                            color = NeutralWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRES",
                            color = NeutralWhite.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = card.expiryDate,
                            color = NeutralWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
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

            // Decorative chip icon
            Icon(
                imageVector = Icons.Default.Contactless,
                contentDescription = null,
                tint = SecondaryGold.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(64.dp)
                    .rotate(-15f)
            )
        }
    }
}

@Composable
private fun FullCardDisplay(
    card: BankCard,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = when (card.cardColor) {
                        CardColor.NAVY -> Brush.linearGradient(
                            colors = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                        )
                        CardColor.GOLD -> Brush.linearGradient(
                            colors = listOf(SecondaryGold, SecondaryDarkGold)
                        )
                        else -> Brush.linearGradient(
                            colors = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                        )
                    }
                )
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
                        imageVector = Icons.Default.Contactless,
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CARD HOLDER",
                                color = NeutralWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = card.cardHolder,
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
                            text = card.cardType.name,
                            color = SecondaryGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardSelectorDot(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 32.dp else 8.dp)
            .clip(RoundedCornerShape(if (isSelected) 16.dp else 4.dp))
            .background(if (isSelected) SecondaryGold else NeutralMediumGray.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
    )
}

@Composable
private fun CardInfoRow(label: String, value: String) {
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

@Composable
private fun AddCardButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = SecondaryGold.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    tint = SecondaryGold,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Add New Card",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )
            Text(
                text = "Link your credit or debit card",
                fontSize = 12.sp,
                color = NeutralMediumGray
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun maskCardNumber(cardNumber: String): String {
    return "**** " + cardNumber.takeLast(4)
}

private fun maskFullCard(cardNumber: String): String {
    val parts = cardNumber.split(" ")
    return "**** **** **** ${parts.last()}"
}
