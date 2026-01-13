package com.example.aureus.ui.contact.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.domain.model.ContactCategory
import com.example.aureus.ui.contact.viewmodel.ContactViewModel
import com.example.aureus.ui.theme.*

/**
 * Contact Management Screen - Firebase-based
 * Full CRUD interface for managing user contacts
 * Supports add, edit, delete, search, and filter operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactManagementScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onContactSelected: (Contact) -> Unit = {},
    onEditContact: (String) -> Unit = {},
    onAddContact: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Show success/error messages as snackbars
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddContact) {
                        Icon(Icons.Default.Add, "Add Contact")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = SecondaryGold
            ) {
                Icon(Icons.Default.PersonAdd, "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchContacts(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search", tint = NeutralMediumGray)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.resetFilters() }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralLightGray
                )
            )

            // Category Filter Chips
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.filterByCategory(it) }
            )

            // Favorites Toggle
            FilterChip(
                selected = uiState.filteredContacts.size < uiState.contacts.size && uiState.filteredContacts.all { it.isFavorite },
                onClick = { viewModel.toggleFavoritesOnly() },
                label = { Text("Favorites Only") },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                leadingIcon = {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = if (uiState.filteredContacts.all { it.isFavorite }) SemanticRed else NeutralMediumGray
                    )
                }
            )

            // Contacts List
            when {
                isLoading && uiState.contacts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SecondaryGold)
                    }
                }
                uiState.error != null -> {
                    ErrorMessageState(
                        message = uiState.error,
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.filteredContacts.isEmpty() -> {
                    EmptyContactsState(searchQuery.isNotEmpty())
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredContacts) { contact ->
                            ContactManagementItem(
                                contact = contact,
                                onClick = { onContactSelected(contact) },
                                onEdit = { onEditContact(contact.id) },
                                onDelete = { viewModel.deleteContact(contact.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(contact.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onConfirm = { name, phone, email, account, category ->
                viewModel.addContact(name, phone, email, account, category)
                showAddContactDialog = false
            }
        )
    }
}

// ==================== UI COMPONENTS ====================

@Composable
private fun CategoryFilterRow(
    selectedCategory: ContactCategory?,
    onCategorySelected: (ContactCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All") }
        )
        ContactCategory.values().forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
private fun ContactManagementItem(
    contact: Contact,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = contact.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = contact.phone,
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                    contact.email?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = NeutralMediumGray
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        tint = if (contact.isFavorite) SemanticRed else NeutralMediumGray
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More", tint = NeutralMediumGray)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { onEdit(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = SemanticRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = SemanticRed) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
        }

        // Additional info (account number, category)
        if (!contact.accountNumber.isNullOrBlank() || contact.category != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (contact.isBankContact) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Bank Contact", fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = PrimaryNavyBlue.copy(alpha = 0.1f)
                        )
                    )
                }
                contact.category?.let {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(it.displayName, fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SecondaryGold.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContactsState(isSearching: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ContactPhone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NeutralMediumGray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearching) "No contacts found" else "No contacts yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearching) "Try a different search term" else "Tap + to add your first contact",
                fontSize = 14.sp,
                color = NeutralMediumGray
            )
        }
    }
}

@Composable
private fun ErrorMessageState(
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
                modifier = Modifier.size(48.dp),
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
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryGold)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

// ==================== ADD CONTACT DIALOG ====================

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?, ContactCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ContactCategory.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add New Contact",
                fontWeight = FontWeight.Bold,
                color = PrimaryNavyBlue
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone *") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("XXX XXX XXXXXXXXXXXX XX") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", fontSize = 12.sp, color = NeutralMediumGray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ContactCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(
                            name,
                            phone,
                            email.takeIf { it.isNotBlank() },
                            accountNumber.takeIf { it.isNotBlank() },
                            category
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Contact", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = NeutralWhite
    )
}