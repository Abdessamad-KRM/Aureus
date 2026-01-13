package com.example.aureus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.i18n.Language
import com.example.aureus.ui.theme.*

// Language Selection Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    onNavigateBack: () -> Unit = {},
    onLanguageSelected: (Language) -> Unit = {}
) {
    var selectedLanguage by remember { mutableStateOf(Language.FRENCH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Select Your Language",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }

            items(Language.values()) { language ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedLanguage = language
                            onLanguageSelected(language)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage.code == language.code)
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
                        Text(
                            text = language.flag,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                            text = language.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavyBlue
                        )
                        Text(
                            text = language.code.uppercase(),
                            fontSize = 13.sp,
                            color = NeutralMediumGray
                        )
                        }
                        if (selectedLanguage.code == language.code) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = SecondaryGold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Terms & Conditions Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Section("1. Introduction", """
                        Welcome to Aureus Banking Application. By using our services, you agree to comply with and be bound by the following terms and conditions.
                    """)

                    Section("2. Account Registration", """
                        - You must provide accurate and complete information
                        - You are responsible for maintaining account security
                        - You must be at least 18 years old to create an account
                    """)

                    Section("3. Services", """
                        Aureus provides digital banking services including:
                        - Account management
                        - Money transfers
                        - Transaction history
                        - Card management
                    """)

                    Section("4. Security", """
                        - Keep your PIN and passwords confidential
                        - Report unauthorized access immediately
                        - Use biometric authentication when available
                    """)

                    Section("5. Privacy", """
                        Your data is protected according to our Privacy Policy. We use industry-standard encryption and security measures.
                    """)

                    Section("6. Fees and Charges", """
                        Certain services may incur fees. All applicable charges will be clearly displayed before you confirm any transaction.
                    """)

                    Section("7. Liability", """
                        Aureus is not liable for:
                        - Losses due to unauthorized access
                        - Service interruptions beyond our control
                        - Third-party service failures
                    """)

                    Section("8. Changes to Terms", """
                        We reserve the right to modify these terms at any time. Users will be notified of significant changes.
                    """)

                    Section("9. Contact", """
                        For questions about these terms, contact us at:
                        Email: support@aureus.com
                        Phone: +212 5XX XXX XXX
                    """)

                    Divider()

                    Text(
                        text = "Last Updated: January 9, 2026",
                        fontSize = 12.sp,
                        color = NeutralMediumGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "© 2026 Aureus Bank. All rights reserved.",
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavyBlue
        )
        Text(
            text = content.trimIndent(),
            fontSize = 14.sp,
            color = NeutralMediumGray,
            lineHeight = 20.sp
        )
    }
}
