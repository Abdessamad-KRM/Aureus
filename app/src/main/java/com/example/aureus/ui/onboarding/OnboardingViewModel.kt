package com.example.aureus.ui.onboarding

import androidx.lifecycle.ViewModel
import com.example.aureus.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: SharedPreferencesManager
) : ViewModel() {

    fun completeOnboarding() {
        preferencesManager.setOnboardingCompleted(true)
    }

    fun isOnboardingCompleted(): Boolean {
        return preferencesManager.isOnboardingCompleted()
    }
}
