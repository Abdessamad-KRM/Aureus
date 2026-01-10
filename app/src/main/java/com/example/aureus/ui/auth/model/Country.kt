package com.example.aureus.ui.auth.model

/**
 * Data class pour représenter un pays
 */
data class Country(
    val code: String,          // Code ISO (e.g., "MA")
    val name: String,          // Nom du pays (e.g., "Morocco")
    val dialCode: String,      // Indicatif (e.g., "+212")
    val flag: String           // Emoji du drapeau
)

// Liste des pays avec leurs indicateurs
val countries = listOf(
    Country("MA", "Morocco", "+212", "🇲🇦"),
    Country("FR", "France", "+33", "🇫🇷"),
    Country("US", "United States", "+1", "🇺🇸"),
    Country("GB", "United Kingdom", "+44", "🇬🇧"),
    Country("DZ", "Algeria", "+213", "🇩🇿"),
    Country("TN", "Tunisia", "+216", "🇹🇳"),
    Country("ES", "Spain", "+34", "🇪🇸"),
    Country("DE", "Germany", "+49", "🇩🇪"),
    Country("IT", "Italy", "+39", "🇮🇹"),
    Country("CA", "Canada", "+1", "🇨🇦"),
    Country("AE", "UAE", "+971", "🇦🇪"),
    Country("SA", "Saudi Arabia", "+966", "🇸🇦"),
    Country("EG", "Egypt", "+20", "🇪🇬"),
    Country("TR", "Turkey", "+90", "🇹🇷"),
    Country("JP", "Japan", "+81", "🇯🇵"),
    Country("CN", "China", "+86", "🇨🇳"),
    Country("IN", "India", "+91", "🇮🇳"),
    Country("BR", "Brazil", "+55", "🇧🇷"),
    Country("RU", "Russia", "+7", "🇷🇺"),
    Country("MX", "Mexico", "+52", "🇲🇽")
)