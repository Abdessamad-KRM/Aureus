package com.example.aureus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.*

/**
 * Composant de prévisualisation de la palette de couleurs Aureus
 * Utile pour le développement et la documentation
 */
@Composable
fun ColorPalettePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLightGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🎨 Palette de Couleurs Aureus",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralDarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Primaires
        ColorSection(
            title = "Couleurs Primaires",
            subtitle = "Actions principales et branding"
        ) {
            ColorItem(
                name = "Primary Navy Blue",
                hex = "#1E3A5F",
                color = PrimaryNavyBlue,
                textColor = NeutralWhite
            )
            ColorItem(
                name = "Primary Medium Blue",
                hex = "#2C5F8D",
                color = PrimaryMediumBlue,
                textColor = NeutralWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secondaires
        ColorSection(
            title = "Couleurs Secondaires",
            subtitle = "Accents premium et éléments spéciaux"
        ) {
            ColorItem(
                name = "Secondary Gold",
                hex = "#D4AF37",
                color = SecondaryGold,
                textColor = PrimaryNavyBlue
            )
            ColorItem(
                name = "Secondary Dark Gold",
                hex = "#C89F3C",
                color = SecondaryDarkGold,
                textColor = NeutralWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sémantiques
        ColorSection(
            title = "Couleurs Sémantiques",
            subtitle = "Feedback utilisateur et états"
        ) {
            ColorItem(
                name = "Semantic Green (Succès)",
                hex = "#10B981",
                color = SemanticGreen,
                textColor = NeutralWhite
            )
            ColorItem(
                name = "Semantic Red (Erreur)",
                hex = "#EF4444",
                color = SemanticRed,
                textColor = NeutralWhite
            )
            ColorItem(
                name = "Semantic Amber (Avertissement)",
                hex = "#F59E0B",
                color = SemanticAmber,
                textColor = NeutralWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Neutres
        ColorSection(
            title = "Couleurs Neutres",
            subtitle = "Textes, backgrounds et UI"
        ) {
            ColorItem(
                name = "Neutral White",
                hex = "#FFFFFF",
                color = NeutralWhite,
                textColor = NeutralDarkGray,
                hasBorder = true
            )
            ColorItem(
                name = "Neutral Light Gray",
                hex = "#F8FAFC",
                color = NeutralLightGray,
                textColor = NeutralDarkGray,
                hasBorder = true
            )
            ColorItem(
                name = "Neutral Medium Gray",
                hex = "#64748B",
                color = NeutralMediumGray,
                textColor = NeutralWhite
            )
            ColorItem(
                name = "Neutral Dark Gray",
                hex = "#1E293B",
                color = NeutralDarkGray,
                textColor = NeutralWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exemples d'utilisation
        Text(
            text = "Exemples d'Utilisation",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralDarkGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        UsageExampleCard()
    }
}

@Composable
private fun ColorSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NeutralWhite
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralDarkGray
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = NeutralMediumGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun ColorItem(
    name: String,
    hex: String,
    color: Color,
    textColor: Color,
    hasBorder: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 60.dp)
                .background(color, RoundedCornerShape(8.dp))
                .then(
                    if (hasBorder) Modifier.border(
                        1.dp,
                        NeutralMediumGray.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hex,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NeutralDarkGray
            )
            Text(
                text = hex,
                fontSize = 12.sp,
                color = NeutralMediumGray
            )
        }
    }
}

@Composable
private fun UsageExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NeutralWhite
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Boutons
            Text(
                text = "Boutons",
                fontWeight = FontWeight.Bold,
                color = NeutralDarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNavyBlue
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Primaire")
                }
                
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryGold
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Secondaire")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transactions
            Text(
                text = "Transactions",
                fontWeight = FontWeight.Bold,
                color = NeutralDarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "+500 €",
                    color = SemanticGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "-150 €",
                    color = SemanticRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "2,500 €",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges
            Text(
                text = "Badges",
                fontWeight = FontWeight.Bold,
                color = NeutralDarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(containerColor = SecondaryGold) {
                    Text("Premium", color = NeutralWhite)
                }
                Badge(containerColor = SemanticGreen) {
                    Text("Actif", color = NeutralWhite)
                }
                Badge(containerColor = SemanticAmber) {
                    Text("En attente", color = NeutralWhite)
                }
                Badge(containerColor = SemanticRed) {
                    Text("Inactif", color = NeutralWhite)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Textes
            Text(
                text = "Hiérarchie de Textes",
                fontWeight = FontWeight.Bold,
                color = NeutralDarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                Text(
                    text = "Texte Principal",
                    color = NeutralDarkGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Texte Secondaire",
                    color = NeutralMediumGray
                )
                Text(
                    text = "Texte Subtil",
                    color = NeutralMediumGray.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Preview pour Android Studio
 */
@Preview(showBackground = true)
@Composable
fun ColorPalettePreviewScreen() {
    AureusTheme {
        ColorPalettePreview()
    }
}
