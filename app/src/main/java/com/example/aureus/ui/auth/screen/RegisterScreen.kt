package com.example.aureus.ui.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.domain.model.Resource
import com.example.aureus.ui.auth.model.Country
import com.example.aureus.ui.auth.model.countries
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.components.CompactQuickLoginButtons
import com.example.aureus.ui.theme.*

/**
 * Register Screen - Modern Prestige Design
 * Avec Quick Login Buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: (phoneNumber: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    storedAccounts: List<Map<String, String>> = emptyList()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()

    // Navigate on successful registration
    LaunchedEffect(registerState) {
        if (registerState is Resource.Success) {
            val fullPhone = if (phone.isNotBlank()) {
                "${selectedCountry.dialCode}${phone.filter { it.isDigit() }}"
            } else ""
            onRegisterSuccess(fullPhone)
        }
    }

    // Auto-fill from Quick Login Buttons
    val handleQuickLogin = { quickEmail: String, quickPassword: String ->
        email = quickEmail
        password = quickPassword
        emailError = null
        passwordError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
    ) {
        // Back button
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryNavyBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Créez votre compte Aureus",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name
            Text(
                text = "Full Name",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = "$firstName $lastName".trim(),
                onValueChange = {
                    val parts = it.split(" ", limit = 2)
                    firstName = parts.getOrNull(0) ?: ""
                    lastName = parts.getOrNull(1) ?: ""
                    firstNameError = null
                    lastNameError = null
                },
                placeholder = { Text("Tanya Myroniuk", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = firstNameError != null || lastNameError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Phone Number
            Text(
                text = "Phone Number",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Country Selector + Phone Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country Selector Dropdown
                Box(
                    modifier = Modifier.width(120.dp)
                ) {
                    OutlinedTextField(
                        value = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Country", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = NeutralMediumGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Country",
                                tint = NeutralMediumGray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                            focusedContainerColor = NeutralWhite,
                            unfocusedContainerColor = NeutralWhite
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = isCountryDropdownExpanded,
                        onDismissRequest = { isCountryDropdownExpanded = false },
                        modifier = Modifier
                            .height(300.dp)
                            .background(NeutralWhite)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${country.flag} ${country.name} (${country.dialCode})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedCountry = country
                                    isCountryDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Invisible click area for dropdown
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable { isCountryDropdownExpanded = !isCountryDropdownExpanded }
                    )
                }

                // Phone Number Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("6XX XXX XXX", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryGold,
                        unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                        focusedContainerColor = NeutralWhite,
                        unfocusedContainerColor = NeutralWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                placeholder = { Text("your@email.com", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            Text(
                text = "Password",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                placeholder = { Text("••••••••", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = passwordError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Login Buttons
            if (storedAccounts.isNotEmpty()) {
                CompactQuickLoginButtons(
                    accounts = storedAccounts,
                    onAccountClick = { e, p -> handleQuickLogin(e, p) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Button
            Button(
                onClick = {
                    val isValid = validateInput(
                        firstName,
                        lastName,
                        email,
                        password
                    ) { fn, ln, e, p ->
                        firstNameError = fn
                        lastNameError = ln
                        emailError = e
                        passwordError = p
                    }
                    if (isValid) {
                        val fullPhone = if (phone.isNotBlank()) {
                            "${selectedCountry.dialCode}$phone"
                        } else null
                        viewModel.register(
                            email = email,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            phone = fullPhone
                        )
                    }
                },
                enabled = registerState !is Resource.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavyBlue,
                    contentColor = NeutralWhite
                )
            ) {
                if (registerState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeutralWhite
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NeutralWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Link
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account. ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralMediumGray
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PrimaryNavyBlue
                    )
                }
            }

            if (registerState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (registerState as Resource.Error).message,
                    color = SemanticRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun validateInput(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    onError: (String?, String?, String?, String?) -> Unit
): Boolean {
    val firstNameError = if (firstName.isBlank()) "First name is required" else null
    val lastNameError = if (lastName.isBlank()) "Last name is required" else null
    val emailError = if (email.isBlank()) "Email is required" else null
    val passwordError = if (password.isBlank()) "Password is required" else null
    onError(firstNameError, lastNameError, emailError, passwordError)
    return firstNameError == null && lastNameError == null && emailError == null && passwordError == null
}

@Composable
fun RegisterScreenContent(
    onRegisterClick: (String, String, String, String, String?) -> Unit = { _, _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: (phoneNumber: String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    storedAccounts: List<Map<String, String>> = emptyList()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Auto-fill from Quick Login Buttons
    val handleQuickLogin = { quickEmail: String, quickPassword: String ->
        email = quickEmail
        password = quickPassword
        emailError = null
        passwordError = null
    }

    // On register success callback
    LaunchedEffect(Unit) {
        // On trigger the success callback when needed after registration
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
    ) {
        // Back button
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryNavyBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Créez votre compte Aureus",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name
            Text(
                text = "Full Name",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = "$firstName $lastName".trim(),
                onValueChange = {
                    val parts = it.split(" ", limit = 2)
                    firstName = parts.getOrNull(0) ?: ""
                    lastName = parts.getOrNull(1) ?: ""
                    firstNameError = null
                    lastNameError = null
                },
                placeholder = { Text("Tanya Myroniuk", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = firstNameError != null || lastNameError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Phone Number
            Text(
                text = "Phone Number",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Country Selector + Phone Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country Selector Dropdown
                Box(
                    modifier = Modifier.width(120.dp)
                ) {
                    OutlinedTextField(
                        value = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Country", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = NeutralMediumGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Country",
                                tint = NeutralMediumGray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                            focusedContainerColor = NeutralWhite,
                            unfocusedContainerColor = NeutralWhite
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = isCountryDropdownExpanded,
                        onDismissRequest = { isCountryDropdownExpanded = false },
                        modifier = Modifier
                            .height(300.dp)
                            .background(NeutralWhite)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${country.flag} ${country.name} (${country.dialCode})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedCountry = country
                                    isCountryDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Invisible click area for dropdown
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable { isCountryDropdownExpanded = !isCountryDropdownExpanded }
                    )
                }

                // Phone Number Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("6XX XXX XXX", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryGold,
                        unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                        focusedContainerColor = NeutralWhite,
                        unfocusedContainerColor = NeutralWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                placeholder = { Text("your@email.com", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            Text(
                text = "Password",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                placeholder = { Text("••••••••", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeutralMediumGray
                    )
                },
                isError = passwordError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryGold,
                    unfocusedBorderColor = NeutralMediumGray.copy(alpha = 0.5f),
                    focusedContainerColor = NeutralWhite,
                    unfocusedContainerColor = NeutralWhite
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Login Buttons
            if (storedAccounts.isNotEmpty()) {
                CompactQuickLoginButtons(
                    accounts = storedAccounts,
                    onAccountClick = { e, p -> handleQuickLogin(e, p) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Button
            Button(
                onClick = {
                    val isValid = validateInput(
                        firstName,
                        lastName,
                        email,
                        password
                    ) { fn, ln, e, p ->
                        firstNameError = fn
                        lastNameError = ln
                        emailError = e
                        passwordError = p
                    }
                    if (isValid) {
                        val fullPhone = if (phone.isNotBlank()) {
                            "${selectedCountry.dialCode}$phone"
                        } else null
                        onRegisterClick(email, password, firstName, lastName, fullPhone)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavyBlue,
                    contentColor = NeutralWhite
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeutralWhite
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NeutralWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Link
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account. ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralMediumGray
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PrimaryNavyBlue
                    )
                }
            }
        }

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = errorMessage,
                    color = SemanticRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    AureusTheme {
        RegisterScreenContent(storedAccounts = emptyList())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register Screen - Dark")
@Composable
fun RegisterScreenDarkPreview() {
    AureusTheme(darkTheme = true) {
        RegisterScreenContent(storedAccounts = emptyList())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register Screen - With Quick Accounts")
@Composable
fun RegisterScreenWithAccountsPreview() {
    AureusTheme {
        RegisterScreenContent(
            storedAccounts = listOf(
                mapOf("email" to "user1@test.com", "password" to "pass123", "label" to "User 1"),
                mapOf("email" to "user2@test.com", "password" to "pass456", "label" to "User 2")
            )
        )
    }
}