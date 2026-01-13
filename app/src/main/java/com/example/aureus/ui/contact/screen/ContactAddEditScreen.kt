package com.example.aureus.ui.contact.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactAddEditScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    contactId: String? = null, // If null, adding new contact
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ContactCategory.OTHER) }
    var isFavorite by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    val isEditMode = contactId != null

    // Load existing contact data
    LaunchedEffect(contactId) {
        if (isEditMode && contactId != null) {
            val contact = uiState.contacts.find { it.id == contactId }
            contact?.let {
                name = it.name
                phone = it.phone
                email = it.email ?: ""
                accountNumber = it.accountNumber ?: ""
                category = it.category ?: ContactCategory.OTHER
                isFavorite = it.isFavorite
            }
        }
    }

    // Show success message
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            showSuccessDialog = true
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Edit Contact" else "Add Contact",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showConfirmDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            Text(
                text = "Name *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contact Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Phone
            Text(
                text = "Phone Number *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("06 12 34 56 78") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Email
            Text(
                text = "Email (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("contact@email.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Account Number
            Text(
                text = "Account Number (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bank Account Number") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category
            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryNavyBlue
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ContactCategory.values().forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.displayName, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mark as Favorite",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryNavyBlue
                )
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SecondaryGold,
                        checkedTrackColor = SecondaryGold.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val contact = Contact(
                        id = contactId ?: "",
                        name = name,
                        phone = phone,
                        email = email.ifBlank { null },
                        accountNumber = accountNumber.ifBlank { null },
                        isFavorite = isFavorite,
                        category = category,
                        isBankContact = !accountNumber.isNullOrBlank()
                    )

                    if (isEditMode && contactId != null) {
                        viewModel.updateContact(contact)
                    } else {
                        viewModel.addContact(name, phone, email.ifBlank { null }, accountNumber.ifBlank { null }, category)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && phone.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGold
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = NeutralWhite,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Update Contact" else "Add Contact",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
            title = { 
                Text(
                    if (isEditMode) "Contact Updated!" else "Contact Added!",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { Text("Your contact has been saved successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)
                ) {
                    Text("Done")
                }
            },
            containerColor = NeutralWhite
        )
    }

    // Delete Confirmation Dialog
    if (showConfirmDeleteDialog && isEditMode) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = SemanticRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "Delete Contact",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text("Are you sure you want to delete this contact? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        contactId?.let { viewModel.deleteContact(it) }
                        showConfirmDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = NeutralWhite
        )
    }
}