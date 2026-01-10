package com.example.aureus.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.data.TestAccount
import com.example.aureus.ui.theme.*

// Profile Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val user = TestAccount.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
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
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SecondaryGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${user.firstName.first()}${user.lastName.first()}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            item { ProfileInfoCard("Email", user.email, Icons.Default.Email) }
            item { ProfileInfoCard("Phone", user.phone, Icons.Default.Phone) }
            item { ProfileInfoCard("Address", user.address ?: "Not set", Icons.Default.LocationOn) }
            item { ProfileInfoCard("City", user.city ?: "Not set", Icons.Default.LocationCity) }
            item { ProfileInfoCard("Country", user.country, Icons.Default.Public) }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = NeutralMediumGray)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavyBlue)
            }
        }
    }
}

// Settings Screen - Version démo statique
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onTerms: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
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
            item { SectionHeader("Account") }
            item { SettingsItem("Change Password", Icons.Default.Lock, onClick = onChangePassword) }
            item { SettingsItem("Language", Icons.Default.Language, onClick = onLanguage) }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("Preferences") }
            item {
                var notifications by remember { mutableStateOf(true) }
                SettingsToggleItem("Notifications", Icons.Default.Notifications, notifications) {
                    notifications = it
                }
            }
            item {
                var biometric by remember { mutableStateOf(false) }
                SettingsToggleItem("Biometric Auth", Icons.Default.Fingerprint, biometric) {
                    biometric = it
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("About") }
            item { SettingsItem("Terms & Conditions", Icons.Default.Description, onClick = onTerms) }
            item { SettingsItem("Privacy Policy", Icons.Default.PrivacyTip, onClick = {}) }
            item { SettingsItem("About Aureus", Icons.Default.Info, onClick = {}) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Version 1.0.0", fontSize = 12.sp, color = NeutralMediumGray)
                        Text("© 2026 Aureus Bank", fontSize = 10.sp, color = NeutralMediumGray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryNavyBlue,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SecondaryGold)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = NeutralMediumGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SecondaryGold)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = SecondaryGold)
            )
        }
    }
}

// Search Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onNavigateBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search transactions...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralWhite)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = NeutralMediumGray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Search for transactions", color = NeutralMediumGray)
            }
        }
    }
}
