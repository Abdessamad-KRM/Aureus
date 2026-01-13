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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aureus.domain.model.CardColor
import com.example.aureus.domain.model.CardType
import com.example.aureus.ui.cards.viewmodel.CardsViewModel
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.navigation.Screen
import com.example.aureus.ui.components.SecureBackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    navController: NavHostController? = null,
    viewModel: CardsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onAddSuccess: () -> Unit = {}
) {
    var cardNumber by remember { mutableStateOf("") }
    var pinVerified by remember { mutableStateOf(false) }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    var showSecurityWarningDialog by remember { mutableStateOf(false) } // ✅ PHASE 5
    var showExitConfirmationDialog by remember { mutableStateOf(false) } // ✅ PHASE 6

    // Card type and color selection
    var selectedCardType by remember { mutableStateOf(CardType.VISA) }
    var selectedCardColor by remember { mutableStateOf(CardColor.NAVY) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Show error from ViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let { showErrorMessage = it }
    }

    // ✅ PHASE 6: Secure back handler - prevent accidental back when user has entered data
    SecureBackHandler(
        enabled = true,
        onBackRequest = {
            if (cardNumber.isNotEmpty() || cardHolder.isNotEmpty() || expiryDate.isNotEmpty() || cvv.isNotEmpty()) {
                // Show confirmation dialog if user has entered data
                showExitConfirmationDialog = true
            } else {
                onNavigateBack()
            }
        }
    )

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
                            getCardGradient(selectedCardColor)
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
                                      else formatCardNumberPreview(cardNumber),
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
                                Text(
                                    text = selectedCardType.name,
                                    color = SecondaryGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                // Expiry Date & CVV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            // ✅ PHASE 5: CVV validation - 3 chiffres pour Visa/MC, 4 pour Amex
                            val maxLength = if (selectedCardType == CardType.AMEX) 4 else 3
                            if (it.length <= maxLength && it.all { char -> char.isDigit() }) {
                                cvv = it
                            }
                        },
                        label = { Text(if (selectedCardType == CardType.AMEX) "CID" else "CVV") },
                        placeholder = { Text(if (selectedCardType == CardType.AMEX) "1234" else "123") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            focusedLabelColor = SecondaryGold,
                            focusedLeadingIconColor = SecondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )
                }

                // Card Type Selection
                Text(
                    text = "Card Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryNavyBlue
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardType.values().forEach { type ->
                        CardTypeButton(
                            type = type,
                            isSelected = selectedCardType == type,
                            onClick = { selectedCardType = type },
                            enabled = !isLoading
                        )
                    }
                }

                // Card Color Selection
                Text(
                    text = "Card Style",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryNavyBlue
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardColor.values().forEach { color ->
                        CardColorButton(
                            color = color,
                            isSelected = selectedCardColor == color,
                            onClick = { selectedCardColor = color },
                            enabled = !isLoading
                        )
                    }
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
                        // ✅ SÉCURITÉ PHASE 5: Afficher avertissement de sécurité avant PIN
                        showSecurityWarningDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    // ✅ PHASE 5: Validation CVV adaptée au type de carte
                    enabled = cardNumber.length == 19 &&
                              cardHolder.isNotEmpty() &&
                              expiryDate.length == 5 &&
                              cvv.length == (if (selectedCardType == CardType.AMEX) 4 else 3) &&
                              !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) NeutralMediumGray else SecondaryGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeutralWhite
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Card",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Handle PIN verified state for card addition
                if (pinVerified && !isLoading) {
                    LaunchedEffect(Unit) {
                        // ✅ SÉCURITÉ PHASE 5: CVV NEVER stored - sent as empty string
                        viewModel.addCard(
                            cardNumber = cardNumber,
                            cardHolder = cardHolder,
                            expiryDate = expiryDate,
                            cvv = "",  // ✅ CVV jamais stocké, vide systématiquement
                            cardType = selectedCardType,
                            cardColor = selectedCardColor,
                            onSuccess = {
                                showSuccessDialog = true
                                viewModel.clearError()
                            },
                            onError = { error ->
                                showErrorMessage = error
                            }
                        )
                        pinVerified = false
                    }
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

    // Error Dialog
    showErrorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { showErrorMessage = null },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = SemanticRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorMessage = null
                        viewModel.clearError()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticRed
                    )
                ) {
                    Text("OK")
                }
            },
            containerColor = NeutralWhite
        )
    }

    // ✅ PHASE 5: Security Warning Dialog before PIN verification
    if (showSecurityWarningDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityWarningDialog = false },
            icon = {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Sécurité de votre carte",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pour protéger votre sécurité :")
                    Text(
                        "• Le CVV ne sera pas stocké",
                        fontSize = 14.sp,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        "• Le numéro de carte sera tokenisé",
                        fontSize = 14.sp,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        "• Veuillez confirmer avec votre PIN",
                        fontSize = 14.sp,
                        color = PrimaryNavyBlue
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecurityWarningDialog = false
                        // ✅ SÉCURITÉ PHASE 2: Naviguer vers vérification PIN avant ajout carte
                        navController?.navigate(Screen.PinVerification.route.replace("{action}", "add_card"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                ) {
                    Text("Continuer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecurityWarningDialog = false }) {
                    Text("Annuler")
                }
            },
            containerColor = NeutralWhite
        )
    }

    // ✅ PHASE 6: Exit Confirmation Dialog - prevent accidental exit
    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Discard Card Details?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "You have entered card information. Are you sure you want to leave without adding the card?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmationDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmationDialog = false }) {
                    Text("Stay")
                }
            },
            containerColor = NeutralWhite
        )
    }
}

@Composable
private fun CardTypeButton(
    type: CardType,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                type.name,
                                fontSize = 12.sp
            )
        },
        modifier = Modifier.height(36.dp),
        enabled = enabled,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SecondaryGold,
            selectedLabelColor = NeutralWhite
        )
    )
}

@Composable
private fun CardColorButton(
    color: CardColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                color.name,
                fontSize = 12.sp
            )
        },
        modifier = Modifier.height(36.dp),
        enabled = enabled,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = getColorForCardColor(color),
            selectedLabelColor = NeutralWhite
        )
    )
}

private fun formatCardNumberInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return digitsOnly.chunked(4).joinToString(" ").take(19)
}

private fun formatCardNumberPreview(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length >= 4 -> "**** **** **** ${digitsOnly.takeLast(4)}"
        else -> "**** **** **** ****"
    }
}

private fun formatExpiryDate(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length <= 2 -> digitsOnly
        else -> "${digitsOnly.take(2)}/${digitsOnly.drop(2).take(2)}"
    }
}

private fun getCardGradient(color: CardColor): Brush {
    return when (color) {
        CardColor.NAVY -> Brush.horizontalGradient(
            listOf(PrimaryNavyBlue, PrimaryMediumBlue)
        )
        CardColor.GOLD -> Brush.horizontalGradient(
            listOf(SecondaryGold, SecondaryDarkGold)
        )
        CardColor.BLACK -> Brush.horizontalGradient(
            listOf(androidx.compose.ui.graphics.Color(0xFF212121), androidx.compose.ui.graphics.Color(0xFF424242))
        )
        CardColor.BLUE -> Brush.horizontalGradient(
            listOf(androidx.compose.ui.graphics.Color(0xFF1976D2), androidx.compose.ui.graphics.Color(0xFF42A5F5))
        )
        CardColor.PURPLE -> Brush.horizontalGradient(
            listOf(androidx.compose.ui.graphics.Color(0xFF7B1FA2), androidx.compose.ui.graphics.Color(0xFF9C27B0))
        )
        CardColor.GREEN -> Brush.horizontalGradient(
            listOf(androidx.compose.ui.graphics.Color(0xFF388E3C), androidx.compose.ui.graphics.Color(0xFF66BB6A))
        )
    }
}

private fun getColorForCardColor(color: CardColor): androidx.compose.ui.graphics.Color {
    return when (color) {
        CardColor.NAVY -> PrimaryNavyBlue
        CardColor.GOLD -> SecondaryGold
        CardColor.BLACK -> androidx.compose.ui.graphics.Color(0xFF212121)
        CardColor.BLUE -> androidx.compose.ui.graphics.Color(0xFF1976D2)
        CardColor.PURPLE -> androidx.compose.ui.graphics.Color(0xFF7B1FA2)
        CardColor.GREEN -> androidx.compose.ui.graphics.Color(0xFF388E3C)
    }
}

// ✅ PHASE 5: Fonction de validation CVV côté client
private fun validateCVV(cvv: String, cardType: CardType): Boolean {
    // Amex: 4 chiffres, Visa/MC: 3 chiffres
    val requiredLength = if (cardType == CardType.AMEX) 4 else 3
    return cvv.length == requiredLength && cvv.all { it.isDigit() }
}