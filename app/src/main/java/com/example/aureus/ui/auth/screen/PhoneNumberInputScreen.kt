package com.example.aureus.ui.auth.screen

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.auth.model.Country
import com.example.aureus.ui.auth.model.countries
import com.example.aureus.ui.auth.viewmodel.PhoneAuthState
import com.example.aureus.ui.auth.viewmodel.PhoneAuthViewModel
import com.example.aureus.ui.components.withSecurity
import com.example.aureus.ui.components.SecureScreenType
import com.example.aureus.ui.theme.*

/**
 * Phone Number Input Screen
 * Écran demandant le numéro de téléphone après authentification Google
 * L'utilisateur DOIT entrer son numéro pour continuer
 *
 * Design: Gradient background, premium colors, centered layout
 * Sécurité: Pas de retour en arrière possible (action critique)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInputScreen(
    userEmail: String = "",
    onPhoneVerified: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToSmsVerification: (String) -> Unit = {},
    viewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var phoneNumber by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }

    val phoneAuthState by viewModel.phoneAuthState.collectAsState()

    // Naviguer vers l'écran SMS quand le code est envoyé
    LaunchedEffect(phoneAuthState) {
        when (val state = phoneAuthState) {
            is PhoneAuthState.CodeSent -> {
                val fullPhone = "${selectedCountry.dialCode}${phoneNumber.filter { it.isDigit() }}"
                onNavigateToSmsVerification(fullPhone)
            }
            is PhoneAuthState.Success -> {
                onPhoneVerified()
            }
            is PhoneAuthState.Error -> {
                phoneError = state.message
            }
            else -> {}
        }
    }

    // Validation et soumission
    fun handleSubmit() {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            phoneError = "Numéro de téléphone requis"
        } else {
            // Validation basique du format
            val digits = cleanPhone.filter { it.isDigit() }
            if (digits.length < 8) {
                phoneError = "Numéro invalide (min. 8 chiffres)"
            } else {
                val fullPhone = "${selectedCountry.dialCode}$digits"
                activity?.let {
                    viewModel.sendVerificationCode(fullPhone, it)
                }
            }
        }
    }

    // Auto-focus sur le champ au chargement
    LaunchedEffect(Unit) {
        // Focus peut être géré via FocusRequester si nécessaire
    }

    // Écran sécurisé - Empêcher retour arrière
    withSecurity(screenType = SecureScreenType.PHONE_NUMBER_INPUT) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A1628),
                            PrimaryNavyBlue,
                            PrimaryMediumBlue
                        )
                    )
                )
        ) {
        // Back button disabled (visuel seulement)
        IconButton(
            onClick = { /* Désactivé - action critique */ },
            enabled = false,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = NeutralWhite.copy(alpha = 0.3f) // Désactivé visuellement
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Icon
            PhoneInputIcon()

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Numéro de Téléphone",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = NeutralWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Pour votre sécurité, veuillez entrer",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "votre numéro de téléphone",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email display (from Google Sign-In)
            if (userEmail.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryGold.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = SecondaryGold.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = userEmail,
                            color = SecondaryGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // Phone Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Country Selector Dropdown
                    Box(
                        modifier = Modifier.width(130.dp)
                    ) {
                        OutlinedTextField(
                            value = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                            onValueChange = { },
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Country",
                                    tint = NeutralWhite.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryGold,
                                unfocusedBorderColor = NeutralWhite.copy(alpha = 0.3f),
                                focusedContainerColor = NeutralWhite.copy(alpha = 0.1f),
                                unfocusedContainerColor = NeutralWhite.copy(alpha = 0.05f),
                                focusedTextColor = NeutralWhite,
                                unfocusedTextColor = NeutralWhite,
                                cursorColor = SecondaryGold
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = NeutralWhite,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = isCountryDropdownExpanded,
                            onDismissRequest = { isCountryDropdownExpanded = false },
                            modifier = Modifier
                                .height(300.dp)
                                .background(Color(0xFF0A1628))
                        ) {
                            countries.forEach { country ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${country.flag} ${country.name} (${country.dialCode})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = NeutralWhite
                                        )
                                    },
                                    onClick = {
                                        selectedCountry = country
                                        isCountryDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Text(text = country.flag, fontSize = 20.sp)
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
                        value = phoneNumber,
                        onValueChange = {
                            phoneError = null
                            phoneNumber = it.filter { char -> char.isDigit() || char == ' ' }
                        },
                        placeholder = {
                            Text(
                                text = "6 61 23 45 67",
                                color = NeutralWhite.copy(alpha = 0.5f)
                            )
                        },
                        isError = phoneError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGold,
                            unfocusedBorderColor = NeutralWhite.copy(alpha = 0.3f),
                            focusedContainerColor = NeutralWhite.copy(alpha = 0.1f),
                            unfocusedContainerColor = NeutralWhite.copy(alpha = 0.05f),
                            focusedTextColor = NeutralWhite,
                            unfocusedTextColor = NeutralWhite,
                            focusedPlaceholderColor = NeutralWhite.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = NeutralWhite.copy(alpha = 0.3f),
                            errorBorderColor = SemanticRed,
                            errorTextColor = SemanticRed
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            color = NeutralWhite
                        )
                    )
                }
            }

            // Error message
            if (phoneError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = phoneError!!,
                    color = SemanticRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Info text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralWhite.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Un code de vérification sera envoyé",
                            color = NeutralWhite.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "à ce numéro par SMS",
                        color = NeutralWhite.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Submit Button
            Button(
                onClick = { handleSubmit() },
                enabled = phoneNumber.isNotBlank() && phoneAuthState !is PhoneAuthState.SendingCode && phoneError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGold,
                    contentColor = PrimaryNavyBlue,
                    disabledContainerColor = NeutralWhite.copy(alpha = 0.2f),
                    disabledContentColor = NeutralWhite.copy(alpha = 0.5f)
                )
            ) {
                if (phoneAuthState is PhoneAuthState.SendingCode) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PrimaryNavyBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continuer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Safety note
            Text(
                text = "Cette étape est obligatoire pour sécuriser votre compte",
                color = NeutralWhite.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
    }
}

/**
 * Animated Phone Icon
 */
@Composable
private fun PhoneInputIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "phone_animation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating ring
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f)
                .graphicsLayer { rotationZ = rotation }
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        SecondaryGold.copy(alpha = 0.8f),
                        Color.Transparent,
                        SecondaryGold.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }

        // Icon background
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = SecondaryGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = SecondaryGold,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📱",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}