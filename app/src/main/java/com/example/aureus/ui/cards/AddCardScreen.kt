package com.example.aureus.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onNavigateBack: () -> Unit = {},
    onAddSuccess: () -> Unit = {}
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
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
                                text = if (cardNumber.isEmpty()) "**** **** **** ****" 
                                      else formatCardNumber(cardNumber),
                                color = NeutralWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "CARD HOLDER",
                                        color = NeutralWhite.copy(alpha = 0.7f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = if (cardHolder.isEmpty()) "YOUR NAME" else cardHolder.uppercase(),
                                        color = NeutralWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "EXPIRES",
                                        color = NeutralWhite.copy(alpha = 0.7f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = if (expiryDate.isEmpty()) "MM/YY" else expiryDate,
                                        color = NeutralWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Form
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Card Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )

                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        if (it.length <= 19) { // 16 digits + 3 spaces
                            cardNumber = formatCardNumberInput(it)
                        }
                    },
                    label = { Text("Card Number") },
                    placeholder = { Text("4562 1122 4945 9852") },
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryGold,
                        focusedLabelColor = SecondaryGold,
                        focusedLeadingIconColor = SecondaryGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Card Holder
                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("Card Holder Name") },
                    placeholder = { Text("Yassir Hamzaoui") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryGold,
                        focusedLabelColor = SecondaryGold,
                        focusedLeadingIconColor = SecondaryGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Expiry Date & CVV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Expiry Date
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {
                            if (it.length <= 5) {
                                expiryDate = formatExpiryDate(it)
                            }
                        },
                        label = { Text("Expiry") },
                        placeholder = { Text("MM/YY") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            focusedLabelColor = SecondaryGold,
                            focusedLeadingIconColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // CVV
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                cvv = it
                            }
                        },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            focusedLabelColor = SecondaryGold,
                            focusedLeadingIconColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryGold.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = SecondaryGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your card information is encrypted and secure",
                            fontSize = 12.sp,
                            color = PrimaryNavyBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Card Button
                Button(
                    onClick = {
                        if (cardNumber.length == 19 && cardHolder.isNotEmpty() && 
                            expiryDate.length == 5 && cvv.length == 3) {
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = cardNumber.length == 19 && cardHolder.isNotEmpty() && 
                              expiryDate.length == 5 && cvv.length == 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Card",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SemanticGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Card Added!") },
            text = { Text("Your new card has been added successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onAddSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticGreen
                    )
                ) {
                    Text("Done")
                }
            },
            containerColor = NeutralWhite
        )
    }
}

private fun formatCardNumberInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return digitsOnly.chunked(4).joinToString(" ").take(19)
}

private fun formatCardNumber(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return if (digitsOnly.length >= 4) {
        "**** **** **** ${digitsOnly.takeLast(4)}"
    } else {
        "**** **** **** ****"
    }
}

private fun formatExpiryDate(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length <= 2 -> digitsOnly
        else -> "${digitsOnly.take(2)}/${digitsOnly.drop(2).take(2)}"
    }
}
