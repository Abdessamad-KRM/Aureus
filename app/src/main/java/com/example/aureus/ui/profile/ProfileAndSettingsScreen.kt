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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import com.example.aureus.di.LanguageManagerEntryPoint
import com.example.aureus.di.ThemeManagerEntryPoint
import com.example.aureus.domain.model.User
import androidx.compose.ui.platform.LocalContext
import com.example.aureus.ui.profile.viewmodel.ProfileViewModel
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.components.ThemeToggle
import com.example.aureus.ui.theme.ThemeManager
import com.example.aureus.i18n.LanguageManager
import com.example.aureus.ui.components.LanguageSelector
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch

// Profile Screen - Migrated to use dynamic Firebase data (Phase 5)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Show error snackbar if present
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearErrorMessage()
        }
    }

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
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = SecondaryGold
                        )
                    } else if (currentUser == null) {
                        // User not loaded or logged out - Add navigation back to login
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Please log in to view your profile",
                                    fontSize = 16.sp,
                                    color = NeutralMediumGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onLogout,
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
                                ) {
                                    Text("Go to Login")
                                }
                            }
                        }
                    } else {
                        UserAvatar(currentUser!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${currentUser!!.firstName} ${currentUser!!.lastName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavyBlue
                        )
                        Text(
                            text = currentUser!!.email,
                            fontSize = 14.sp,
                            color = NeutralMediumGray
                        )
                    }
                }
            }

            if (currentUser != null) {
                item { ProfileInfoCard("Email", currentUser!!.email, Icons.Default.Email) }
                item { ProfileInfoCard("Phone", currentUser!!.phone ?: "Not set", Icons.Default.Phone) }
                item { ProfileInfoCard("Address", currentUser!!.address ?: "Not set", Icons.Default.LocationOn) }
                item { ProfileInfoCard("City", currentUser!!.city ?: "Not set", Icons.Default.LocationCity) }
                item { ProfileInfoCard("Country", currentUser!!.country, Icons.Default.Public) }
            }

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
private fun UserAvatar(user: User) {
    // TODO: Load actual profile image from Firebase Storage using Coil or Glide
    // For now, display initials
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

// Settings Screen - Migrated to use dynamic Firebase settings (Phase 5)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onTerms: () -> Unit = {},
    onCategories: () -> Unit = {},
    onContacts: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ThemeManagerEntryPoint::class.java
        ).themeManager()
    }
    val languageManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            LanguageManagerEntryPoint::class.java
        ).languageManager()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isDark by themeManager.darkMode.collectAsState(initial = false)
    var showLanguageDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Show success snackbar if present
    successMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearSuccessMessage()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearSuccessMessage() }) {
                    Text("OK")
                }
            }
        ) {
            Text(message)
        }
    }
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
            item { SettingsItem("Language", Icons.Default.Language, onClick = { showLanguageDialog = true }) }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("Management") }
            item { SettingsItem("Manage Categories", Icons.Default.Category, onClick = onCategories) }
            item { SettingsItem("Manage Contacts", Icons.Default.ContactPhone, onClick = onContacts) }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("Appearance") }
            // Theme toggle (Phase 12: Dark Mode)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ThemeToggle(
                        isDark = isDark,
                        onThemeChanged = { newIsDark ->
                            scope.launch {
                                themeManager.setDarkMode(newIsDark)
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("Preferences") }
            // Notifications toggle - uses dynamic settings
            item {
                val notificationsEnabled = currentUser?.notificationSettings?.pushNotifications ?: true
                SettingsToggleItem("Notifications", Icons.Default.Notifications, notificationsEnabled, isLoading) {
                    if (currentUser != null) {
                        viewModel.updateNotificationSettings(
                            pushNotifications = it,
                            emailNotifications = currentUser!!.notificationSettings.emailNotifications,
                            transactionAlerts = currentUser!!.notificationSettings.transactionAlerts,
                            lowBalanceAlerts = currentUser!!.notificationSettings.lowBalanceAlerts,
                            promotionalEmails = currentUser!!.notificationSettings.promotionalEmails
                        )
                    }
                }
            }
            // Biometric toggle - uses dynamic settings
            item {
                val biometricEnabled = currentUser?.securitySettings?.biometricEnabled ?: false
                SettingsToggleItem("Biometric Auth", Icons.Default.Fingerprint, biometricEnabled, isLoading) {
                    if (currentUser != null) {
                        viewModel.updateSecuritySettings(
                            biometricEnabled = it,
                            twoFactorAuth = currentUser!!.securitySettings.twoFactorAuth,
                            sessionTimeout = currentUser!!.securitySettings.sessionTimeout
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SectionHeader("About") }
            item { SettingsItem("Terms & Conditions", Icons.Default.Description, onClick = onTerms) }
            item { SettingsItem("Privacy Policy", Icons.Default.PrivacyTip, onClick = {}) }
            item { SettingsItem("About Aureus", Icons.Default.Info, onClick = {}) }

            item { SettingsItem("About Aureus", Icons.Default.Info, onClick = {}) }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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

    // Language Selector Dialog (outside LazyColumn)
    LanguageSelector(
        languageManager = languageManager,
        showDialog = showLanguageDialog,
        onDismiss = { showLanguageDialog = false }
    )
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
    isLoading: Boolean = false,
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
                enabled = !isLoading,
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
