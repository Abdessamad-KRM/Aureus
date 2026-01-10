package com.example.aureus.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.aureus.data.*
import com.example.aureus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMoneyScreen(
    onNavigateBack: () -> Unit = {},
    onRequestClick: (Contact, Double, String) -> Unit = { _, _, _ -> }
) {
    var amount by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var reason by remember { mutableStateOf("") }
    val contacts = remember { StaticContacts.contacts }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Money", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                        Text("Amount to Request", fontSize = 14.sp, color = NeutralMediumGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
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
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MAD", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SecondaryGold)
                        }
                    }
                }
            }

            item {
                Text("Request From", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryNavyBlue)
            }

            items(contacts) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedContact = contact },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedContact?.id == contact.id) 
                            SecondaryGold.copy(alpha = 0.1f) else NeutralWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SecondaryGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.split(" ").map { it.first() }.joinToString(""),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavyBlue)
                            Text(contact.phone, fontSize = 12.sp, color = NeutralMediumGray)
                        }
                        if (selectedContact?.id == contact.id) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryGold)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (Optional)") },
                    placeholder = { Text("What is this request for?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        selectedContact?.let { contact ->
                            amount.toDoubleOrNull()?.let { amt ->
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = amount.isNotEmpty() && selectedContact != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Request Sent!") },
            text = { Text("Your payment request has been sent successfully.") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onNavigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SemanticGreen)) {
                    Text("Done")
                }
            },
            containerColor = NeutralWhite
        )
    }
}
