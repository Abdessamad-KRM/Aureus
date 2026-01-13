package com.example.aureus.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aureus.domain.model.Contact
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.navigation.Screen
import com.example.aureus.ui.components.SecureBackHandler
import com.example.aureus.ui.transfer.viewmodel.TransferViewModel

/**
 * Send Money Screen - Firebase-based
 * ✅ PHASE 5: Intégration avec TransferViewModel pour transferts RÉELS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreenFirebase(
    navController: NavHostController? = null,
    viewModel: ContactViewModel = hiltViewModel(),
    transferViewModel: TransferViewModel = hiltViewModel(),  // ✅ NOUVEAU
    onNavigateBack: () -> Unit = {},
    onSendClick: (Contact, Double) -> Unit = { _, _ -> },
    onAddContactClick: () -> Unit = {}
) {
    val transferUiState by transferViewModel.uiState.collectAsState()
    val contactUiState by viewModel.uiState.collectAsState()
    val amount by transferViewModel.amount.collectAsState()
    val description by transferViewModel.description.collectAsState()
    val selectedContact by transferViewModel.selectedContact.collectAsState()

    // Success/Error Dialogs
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Load contacts when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
        viewModel.loadFavoriteContacts()
        transferViewModel.checkTransferLimits()
    }

    // Watch for successful transfer
    LaunchedEffect(transferUiState.transferSuccess) {
        if (transferUiState.transferSuccess) {
            showSuccessDialog = true
        }
    }

    // ✅ PHASE 6: Secure back handler - prevent accidental back when user has entered data
    SecureBackHandler(
        enabled = true,
        onBackRequest = {
            if (amount.isNotEmpty() || selectedContact != null) {
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
                title = { Text("Send Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddContactClick) {
                        Icon(Icons.Default.PersonAdd, "Add Contact")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ✅ NEW: Show validation status for selected contact
            item {
                if (selectedContact != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (transferUiState.isContactAppUser)
                                SemanticGreen.copy(alpha = 0.1f)
                            else if (transferUiState.contactValidationError != null)
                                SemanticRed.copy(alpha = 0.1f)
                            else NeutralLightGray
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (transferUiState.isValidatingContact) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SecondaryGold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Validation du contact...", style = MaterialTheme.typography.bodyMedium)
                            } else if (transferUiState.isContactAppUser) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SemanticGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${selectedContact!!.getDisplayNameForTransfer()} ✓",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (transferUiState.contactUserInfo != null) {
                                        Text(
                                            "User ID: ${transferUiState.contactUserInfo!!.userId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeutralMediumGray
                                        )
                                    }
                                }
                            } else if (transferUiState.contactValidationError != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = SemanticRed
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    transferUiState.contactValidationError ?: "Contact invalide",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SemanticRed
                                )
                            }
                        }
                    }
                }
            }

            // Amount Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount",
                            fontSize = 14.sp,
                            color = NeutralMediumGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { transferViewModel.setAmount(it) },
                                placeholder = { Text("0.00", fontSize = 40.sp) },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryGold,
                                    unfocusedBorderColor = NeutralLightGray
                                ),
                                isError = transferUiState.amountValidationError != null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MAD",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGold
                            )
                        }

                        // ✅ NEW: Show amount validation error
                        if (transferUiState.amountValidationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = transferUiState.amountValidationError!!,
                                color = SemanticRed,
                                fontSize = 12.sp
                            )
                        }

                        // ✅ NEW: Show transfer limits if available
                        if (transferUiState.transferLimits != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = NeutralLightGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        "Limites disponibles:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Journalier: ${transferUiState.transferLimits!!.dailyRemaining.toInt()} MAD",
                                        fontSize = 11.sp,
                                        color = NeutralMediumGray
                                    )
                                    Text(
                                        "Mensuel: ${transferUiState.transferLimits!!.monthlyRemaining.toInt()} MAD",
                                        fontSize = 11.sp,
                                        color = NeutralMediumGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Favorites
            if (contactUiState.favoriteContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorites",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(contactUiState.favoriteContacts) { contact ->
                            FavoriteContactItem(
                                contact = contact,
                                isSelected = selectedContact?.id == contact.id,
                                onClick = { transferViewModel.selectContact(contact) }
                            )
                        }
                    }
                }
            }

            // All Contacts
            item {
                Text(
                    text = "All Contacts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }

            if (contactUiState.isLoading && contactUiState.contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SecondaryGold)
                    }
                }
            } else {
                items(contactUiState.contacts) { contact ->
                    // ✅ MARKER: Indiquer si le contact est un utilisateur de l'app
                    val isAppUser = contact.isAppUser()

                    ContactListItem(
                        contact = contact,
                        isSelected = selectedContact?.id == contact.id,
                        isUserBadge = isAppUser,
                        onClick = { transferViewModel.selectContact(contact) }
                    )
                }
            }

            if (contactUiState.contacts.isEmpty() && !contactUiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ContactPhone,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = NeutralMediumGray.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No contacts yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryNavyBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your first contact to send money",
                                fontSize = 14.sp,
                                color = NeutralMediumGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddContactClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                            ) {
                                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Contact")
                            }
                        }
                    }
                }
            }

            // Note
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { transferViewModel.setDescription(it) },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Send Button
            item {
                Button(
                    onClick = {
                        // ✅ PHASE 5: Navigate vers PIN verification AVANT transfert
                        when {
                            selectedContact == null -> {
                                transferViewModel.clearMessages()
                            }
                            amount.isBlank() -> {
                                transferViewModel.setAmount("")
                            }
                            transferUiState.contactValidationError != null -> {
                                // Show error alert
                            }
                            transferUiState.amountValidationError != null -> {
                                // Show error alert
                            }
                            else -> {
                                // ✅ PIN verification avant transfert
                                navController?.navigate(
                                    Screen.PinVerification.route.replace("{action}", "send_money")
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = amount.isNotEmpty() &&
                             selectedContact != null &&
                             transferUiState.isContactAppUser &&
                             transferUiState.contactValidationError == null &&
                             transferUiState.amountValidationError == null &&
                             !transferUiState.isTransferring,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (transferUiState.isTransferring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeutralWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Money", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ✅ NEW: Error Display
            if (transferUiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SemanticRed.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = SemanticRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                transferUiState.error!!,
                                color = SemanticRed,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Success Dialog
            item {
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessDialog = false
                            transferViewModel.clearMessages()
                        },
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SemanticGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = { Text("Transfer Successful!", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("Your money has been sent successfully.")
                                selectedContact?.let { contact ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("To: ${contact.name}", fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Amount: ${amount} MAD", fontWeight = FontWeight.SemiBold)

                                    if (transferUiState.transferResultData != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Your new balance: ${transferUiState.transferResultData!!.senderBalance} MAD",
                                            fontSize = 13.sp,
                                            color = NeutralMediumGray
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    transferViewModel.clearMessages()
                                    // Navigate to Dashboard
                                    navController?.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.SendMoney.route) { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)
                            ) {
                                Text("Done")
                            }
                        },
                        containerColor = NeutralWhite
                    )
                }
            }

            // ✅ NEW: Exit Confirmation Dialog
            item {
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
                        title = { Text("Discard Changes?", fontWeight = FontWeight.Bold) },
                        text = {
                            Text(
                                "You have entered some information. Are you sure you want to leave without sending money?"
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showExitConfirmationDialog = false
                                    transferViewModel.resetForm()
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
        }
    }
}

@Composable
private fun FavoriteContactItem(
    contact: Contact,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) SecondaryGold else NeutralLightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.getInitials(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryNavyBlue else NeutralMediumGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.getFirstName(),
            fontSize = 12.sp,
            color = PrimaryNavyBlue
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    isSelected: Boolean,
    isUserBadge: Boolean = false,  // ✅ NOUVEAU PARAMÈTRE
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SecondaryGold.copy(alpha = 0.1f) else NeutralWhite
        ),
        shape = RoundedCornerShape(12.dp)
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
                        .background(SecondaryGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.getInitials(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavyBlue
                        )

                        // ✅ NOUVEAU: Badge utilisateur de l'app
                        if (isUserBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SemanticGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "App User",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = contact.phone,
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = SemanticRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}