package com.example.aureus.ui.auth.screen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aureus.R
import com.example.aureus.domain.model.Resource
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.components.CompactQuickLoginButtons
import com.example.aureus.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

/**
 * Login Screen - Modern Prestige Design
 * Avec Google Auth et Quick Login Buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInSuccess: () -> Unit = {},
    onGoogleSignInError: (String) -> Unit = {},
    storedAccounts: List<Map<String, String>> = emptyList()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val loginState by viewModel.loginState.collectAsState()

    // Auto-fill from Quick Login Buttons
    val handleQuickLogin = { quickEmail: String, quickPassword: String ->
        email = quickEmail
        password = quickPassword
        emailError = null
        passwordError = null
    }

    // Navigate on successful login
    LaunchedEffect(loginState) {
        if (loginState is Resource.Success) {
            onLoginSuccess()
        }
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            // Ici normalement on appellerait Firebase pour authentifier avec le credential Google
            // Pour l'instant, on considère le login Google comme réussi
            Log.d("LoginScreen", "Google Sign-In Success with account: ${account.email}")
            onGoogleSignInSuccess()
        } catch (e: ApiException) {
            Log.w("LoginScreen", "Google Sign-In Failed", e)
            onGoogleSignInError("Google Sign-In Failed: ${e.message}")
        }
    }

    // Google Sign-In Click Handler
    val handleGoogleSignInClick = {
        try {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions
                .Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e("LoginScreen", "Error launching Google Sign-In", e)
            onGoogleSignInError("Error launching Google Sign-In: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
    ) {
        // Back button
        IconButton(
            onClick = { /* Navigate back if needed */ },
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
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenue sur Aureus",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sign In Button
            Button(
                onClick = {
                    val isValid = validateInput(email, password) { e, p ->
                        emailError = e
                        passwordError = p
                    }
                    if (isValid) {
                        viewModel.login(email, password)
                    }
                },
                enabled = loginState !is Resource.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavyBlue,
                    contentColor = NeutralWhite
                )
            ) {
                if (loginState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeutralWhite
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NeutralWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = NeutralMediumGray.copy(alpha = 0.3f)
                )
                Text(
                    text = " OU ",
                    color = NeutralMediumGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = NeutralMediumGray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = handleGoogleSignInClick,
                enabled = loginState !is Resource.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NeutralWhite,
                    contentColor = PrimaryNavyBlue
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = SecondaryGold.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google G logo - chargé depuis Internet
                    AsyncImage(
                        model = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/96px-Google_%22G%22_Logo.svg.png",
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuer avec Google",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "I'm a new user. ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralMediumGray
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PrimaryNavyBlue
                    )
                }
            }

            if (loginState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginState as Resource.Error).message,
                    color = SemanticRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun validateInput(
    email: String,
    password: String,
    onError: (String?, String?) -> Unit
): Boolean {
    val emailError = if (email.isBlank()) "Email is required" else null
    val passwordError = if (password.isBlank()) "Password is required" else null
    onError(emailError, passwordError)
    return emailError == null && passwordError == null
}

@Composable
fun LoginScreenContent(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToRegister: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    storedAccounts: List<Map<String, String>> = emptyList()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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
            onClick = { /* Navigate back if needed */ },
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
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenue sur Aureus",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMediumGray
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sign In Button
            Button(
                onClick = {
                    val isValid = validateInput(email, password) { e, p ->
                        emailError = e
                        passwordError = p
                    }
                    if (isValid) {
                        onLoginClick(email, password)
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
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NeutralWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = NeutralMediumGray.copy(alpha = 0.3f)
                )
                Text(
                    text = " OU ",
                    color = NeutralMediumGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = NeutralMediumGray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = onGoogleSignInClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NeutralWhite,
                    contentColor = PrimaryNavyBlue
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = SecondaryGold.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google G logo - chargé depuis Internet
                    AsyncImage(
                        model = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/96px-Google_%22G%22_Logo.svg.png",
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuer avec Google",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "I'm a new user. ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralMediumGray
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign Up",
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
fun LoginScreenPreview() {
    AureusTheme {
        LoginScreenContent(storedAccounts = emptyList())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login Screen - Dark")
@Composable
fun LoginScreenDarkPreview() {
    AureusTheme(darkTheme = true) {
        LoginScreenContent(storedAccounts = emptyList())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login Screen - With Quick Accounts")
@Composable
fun LoginScreenWithAccountsPreview() {
    AureusTheme {
        LoginScreenContent(
            storedAccounts = listOf(
                mapOf("email" to "user1@test.com", "password" to "pass123", "label" to "User 1"),
                mapOf("email" to "user2@test.com", "password" to "pass456", "label" to "User 2")
            )
        )
    }
}