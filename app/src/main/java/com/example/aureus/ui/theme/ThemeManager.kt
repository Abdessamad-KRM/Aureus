package com.example.aureus.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

/**
 * Gestionnaire du thème (Dark/Light) avec persistance
 * Utilise DataStore pour sauvegarder la préférence utilisateur
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Obtenir le flux du mode sombre
     * Retourne true si le mode sombre est activé, false sinon
     */
    val darkMode: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    /**
     * Changer le mode sombre
     * @param isDark true pour activer le mode sombre, false pour le mode clair
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }

        // Appliquer le thème système
        applyThemeMode(isDark)
    }

    /**
     * Appliquer le thème en fonction du préférence
     * @param isDark true pour mode sombre, false pour mode clair
     */
    private fun applyThemeMode(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Appliquer le thème système actuel
     * Détecte automatiquement si le système est en mode sombre ou clair
     */
    suspend fun applySystemTheme() {
        val isSystemDark = isSystemInDarkTheme()
        context.themeDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isSystemDark
        }
        applyThemeMode(isSystemDark)
    }

    /**
     * Vérifier si le système est en mode sombre
     * @return true si le système est en mode sombre
     */
    fun isSystemInDarkTheme(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_AUTO, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                val systemTheme = android.content.res.Configuration.UI_MODE_NIGHT_MASK
                val nightMode = context.resources.configuration.uiMode and systemTheme
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
}