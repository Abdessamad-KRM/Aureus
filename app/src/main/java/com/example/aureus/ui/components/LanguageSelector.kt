package com.example.aureus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.i18n.Language
import com.example.aureus.i18n.LanguageManager
import com.example.aureus.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

/**
 * Sélecteur de langue
 */
@Composable
fun LanguageSelector(
    languageManager: LanguageManager,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    val currentLanguage by languageManager.currentLanguage.collectAsState(
        initial = Language.FRENCH
    )

    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val colors = LocalAppColors.current

    LaunchedEffect(currentLanguage) {
        selectedLanguage = currentLanguage
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (DarkThemeManager.isDark()) {
                    colors.primaryNavyBlue
                } else {
                    colors.neutralWhite
                }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Language / Langue / اللغة / Idioma / Sprache",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentLanguage == Language.ARABIC) {
                            if (colors.isDark) colors.primaryNavyBlue else colors.neutralWhite
                        } else {
                            colors.primaryNavyBlue
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(
                        color = if (colors.isDark) {
                            colors.neutralLightGray.copy(alpha = 0.2f)
                        } else {
                            colors.neutralMediumGray.copy(alpha = 0.2f)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Language.values()) { language ->
                            LanguageItem(
                                language = language,
                                isSelected = language == selectedLanguage,
                                colors = colors,
                                currentLanguage = currentLanguage,
                                onClick = {
                                    selectedLanguage = language
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                languageManager.setLanguage(selectedLanguage.code)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Confirm / Confirmer / تأكيد / Confirmar / Bestätigen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primaryNavyBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    colors: com.example.aureus.ui.theme.AppColorScheme,
    currentLanguage: Language,
    onClick: () -> Unit
) {
    val isRTL = currentLanguage == Language.ARABIC

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                colors.secondaryGold.copy(alpha = 0.2f)
            } else if (colors.isDark) {
                colors.neutralBlack
            } else {
                colors.neutralWhite
            }
        ),
        border = if (isSelected) {
            BorderStroke(
                1.dp,
                colors.secondaryGold
            )
        } else {
            BorderStroke(
                1.dp,
                if (colors.isDark) {
                    colors.neutralMediumGray.copy(alpha = 0.5f)
                } else {
                    colors.neutralMediumGray.copy(alpha = 0.3f)
                }
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isRTL) {
                    Arrangement.spacedBy(12.dp, Alignment.End)
                } else {
                    Arrangement.spacedBy(12.dp, Alignment.Start)
                }
            ) {
                Text(
                    text = language.flag,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = language.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (colors.isDark) {
                            colors.neutralWhite
                        } else {
                            colors.primaryNavyBlue
                        }
                    )
                    Text(
                        text = language.code.uppercase(),
                        fontSize = 12.sp,
                        color = if (colors.isDark) {
                            colors.neutralLightGray
                        } else {
                            colors.neutralMediumGray
                        }
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.secondaryGold
                )
            }
        }
    }
}

/**
 * Helper pour déterminer si l'application est en mode sombre
 */
object DarkThemeManager {
    @Composable
    fun isDark(): Boolean {
        val colors = LocalAppColors.current
        return colors.isDark
    }
}