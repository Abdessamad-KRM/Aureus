package com.example.aureus.i18n

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

private val LANGUAGE_KEY = stringPreferencesKey("language")

/**
 * Langues supportées
 */
enum class Language(val code: String, val displayName: String, val flag: String) {
    FRENCH("fr", "Français", "🇫🇷"),
    ENGLISH("en", "English", "🇬🇧"),
    ARABIC("ar", "العربية", "🇲🇦"),
    SPANISH("es", "Español", "🇪🇸"),
    GERMAN("de", "Deutsch", "🇩🇪");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: FRENCH
        }
    }
}

/**
 * Gestionnaire de langue
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Obtenir la langue actuelle
     */
    val currentLanguage: Flow<Language> = context.languageDataStore.data
        .map { preferences ->
            Language.fromCode(preferences[LANGUAGE_KEY] ?: "fr")
        }

    /**
     * Obtenir le code de langue actuel
     */
    fun getCurrentLanguageCode(): String {
        return Locale.getDefault().language
    }

    /**
     * Changer la langue
     */
    suspend fun setLanguage(languageCode: String) {
        val language = Language.fromCode(languageCode)
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }

        applyLanguage(language)
    }

    /**
     * Appliquer la langue
     */
    fun applyLanguage(language: Language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config = android.content.res.Configuration(resources.configuration)

        // Update resources configuration
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Localiser une chaîne
     */
    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Localiser une chaîne avec formatage
     */
    fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }

    /**
     * Vérifier si la langue sera RTL
     */
    fun isRTL(): Boolean {
        val direction = context.resources.configuration.layoutDirection
        return direction == View.LAYOUT_DIRECTION_RTL
    }

    /**
     * Obtenir toutes les langues disponibles
     */
    fun getAvailableLanguages(): List<Language> {
        return Language.values().toList()
    }
}