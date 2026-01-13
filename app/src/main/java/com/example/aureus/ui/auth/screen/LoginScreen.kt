package com.example.aureus.ui.auth.screen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.aureus.R
import com.example.aureus.domain.model.Resource
import com.example.aureus.security.CredentialPair
import com.example.aureus.security.SecureCredentialManager
import com.example.aureus.ui.auth.viewmodel.AuthViewModel
import com.example.aureus.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Login Screen - Modern Prestige Design
 * With Google Authentication
 * PHASE 8: Secure Quick Login - Auto-fill sur 4 taps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    credentialManager: SecureCredentialManager = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInSuccess: () -> Unit = {},
    onGoogleSignInError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val loginState by viewModel.loginState.collectAsState()
    val googleSignInState by viewModel.googleSignInState.collectAsState()

    // PHASE 8: Detection 4 taps + Auto-fill (comme suggestions Android)
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0) }
    var autoFillTriggered by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Auto-sauvegarde après login réussi
    LaunchedEffect(loginState) {
        if (loginState is Resource.Success && email.isNotBlank() && password.isNotBlank()) {
            // Auto-sauvegarder le compte (comme suggestions Android)
            delay(500) // Petit délai pour s'assurer que le login est complété
            credentialManager.saveAccount(email, password)
                .onSuccess {
                    Log.d("LoginScreen", "Account auto-saved: $email")
                }
                .onFailure {
                    Log.w("LoginScreen", "Failed to auto-save account", it)
                }
        }
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            // CRITICAL: Create FirebaseCredential and authenticate with Firebase
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.signInWithGoogleCredential(credential)
            Log.d("LoginScreen", "Google Sign-In Success with Firebase credential")
        } catch (e: ApiException) {
            Log.w("LoginScreen", "Google Sign-In Failed", e)
            onGoogleSignInError("Google Sign-In Failed: ${e.message}")
        }
    }

    // Navigate on successful Google sign-in
    LaunchedEffect(googleSignInState) {
        when (val state = googleSignInState) {
            is Resource.Success -> {
                // Google Sign-In successful - navigate to phone verification
                onGoogleSignInSuccess()
            }
            is Resource.Error -> {
                onGoogleSignInError(state.message)
            }
            else -> {}
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

    // Auto-fill sur 4 taps
    fun handleScreenTap() {
        val currentTime = System.currentTimeMillis()

        // Reset tap count si trop de temps entre taps (2 sec)
        if (currentTime - lastTapTime > 2000) {
            tapCount = 0
        }

        tapCount++
        lastTapTime = currentTime

        // Auto-fill après 4 taps (mais pas déjà déclenché récemment)
        if (tapCount >= 4 && !autoFillTriggered) {
            autoFillTriggered = true
            coroutineScope.launch {
                credentialManager.autoFill()
                    .onSuccess { credentials: CredentialPair ->
                        email = credentials.email
                        password = credentials.password
                        emailError = null
                        passwordError = null
                        // Trigger haptic feedback
                        Log.d("LoginScreen", "Auto-filled: ${credentials.email}")
                    }
                    .onFailure {
                        Log.d("LoginScreen", "No saved account for auto-fill")
                    }

                // Reset after 5 seconds
                delay(5000)
                autoFillTriggered = false
                tapCount = 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { _ -> handleScreenTap() }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Back button
            IconButton(
                onClick = { },
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

            Spacer(modifier = Modifier.height(32.dp))

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    AureusTheme {
        // Preview would need mock data
    }
}