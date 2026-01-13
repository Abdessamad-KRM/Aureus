package com.example.aureus.ui.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.aureus.domain.model.Resource
import com.example.aureus.security.RegisterCredentials
import com.example.aureus.security.SecureCredentialManager
import com.example.aureus.ui.auth.model.Country
import com.example.aureus.ui.auth.model.countries
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.theme.*
import dagger.hilt.android.EntryPointAccessors
import com.example.aureus.di.SecureCredentialManagerEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Register Screen - Modern Prestige Design
 * PHASE 8: Secure Quick Login - Auto-fill sur 4 taps (TOUTES les infos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: (phoneNumber: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    // Properly inject SecureCredentialManager singleton through Hilt EntryPoint
    val credentialManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecureCredentialManagerEntryPoint::class.java
        ).secureCredentialManager()
    }

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
    val coroutineScope = rememberCoroutineScope()

    // PHASE 8: Detection 4 taps + Auto-fill complet (tous les champs)
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0) }
    var autoFillTriggered by remember { mutableStateOf(false) }

    // Auto-sauvegarde et navigation après register réussi
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is Resource.Success -> {
                // Auto-sauvegarde des crédentials
                if (email.isNotBlank() && password.isNotBlank()) {
                    delay(500)
                    credentialManager.saveRegisterCredentials(
                        email = email,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        phone = if (phone.isNotBlank()) "${selectedCountry.dialCode}$phone" else ""
                    )
                }
                // Navigate vers SMS verification
                val fullPhone = if (phone.isNotBlank()) "${selectedCountry.dialCode}$phone" else ""
                onRegisterSuccess(fullPhone)
            }
            else -> {}
        }
    }

    // Auto-fill sur 4 taps
    fun handleScreenTap() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTapTime > 2000) {
            tapCount = 0
        }

        tapCount++
        lastTapTime = currentTime

        if (tapCount >= 4 && !autoFillTriggered) {
            autoFillTriggered = true
            coroutineScope.launch {
                credentialManager.autoFillRegister()
                    .onSuccess { credentials: RegisterCredentials ->
                        // Auto-remplir tous les champs
                        email = credentials.email
                        password = credentials.password
                        firstName = credentials.firstName
                        lastName = credentials.lastName
                        
                        // Extraire le téléphone (format: dialCode + phone)
                        val fullPhone = credentials.phone
                        if (fullPhone.isNotBlank()) {
                            // Trouver le pays correspondant via dialCode
                            val matchedCountry = countries.find { fullPhone.startsWith(it.dialCode) }
                            if (matchedCountry != null) {
                                selectedCountry = matchedCountry
                                phone = fullPhone.substring(matchedCountry.dialCode.length)
                            } else {
                                phone = fullPhone  // Fallback
                            }
                        }
                        
                        // Clear erreurs
                        emailError = null
                        passwordError = null
                        firstNameError = null
                        lastNameError = null
                    }

                delay(5000)
                autoFillTriggered = false
                tapCount = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { _ -> handleScreenTap() }
                    )
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Back button
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryNavyBlue
                )
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country Selector
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .pointerInput(Unit) {
                            // Intercepter taps sur dropdown pour ne pas déclencher auto-fill
                            detectTapGestures(
                                onTap = { isCountryDropdownExpanded = !isCountryDropdownExpanded }
                            )
                        }
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
                        modifier = Modifier.height(56.dp),
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

                    DropdownMenu(
                        expanded = isCountryDropdownExpanded,
                        onDismissRequest = { isCountryDropdownExpanded = false }
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text("${country.flag} ${country.name} (${country.dialCode})")
                                },
                                onClick = {
                                    selectedCountry = country
                                    isCountryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("6XX XXX XXX", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f).height(56.dp),
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

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {
                    val isValid = validateInput(firstName, lastName, email, password) { fn, ln, e, p ->
                        firstNameError = fn
                        lastNameError = ln
                        emailError = e
                        passwordError = p
                    }
                    if (isValid) {
                        val fullPhone = if (phone.isNotBlank()) "${selectedCountry.dialCode}$phone" else null
                        viewModel.register(email, password, firstName, lastName, fullPhone)
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