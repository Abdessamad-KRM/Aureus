package com.example.aureus.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile and Settings management
 * Handles user profile data, image uploads, and settings updates
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    // State for user profile
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    // Current user data as Flow
    val currentUser: StateFlow<User?> = firebaseAuthManager.currentUser?.let { user ->
        userRepository.getUserProfile(user.uid)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } ?: MutableStateFlow(null)

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message for user feedback
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Load user profile from Firebase
     */
    fun loadUserProfile() {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                userRepository.getUserProfile(userId).collect { user ->
                    if (user != null) {
                        _userState.value = UiState.Success(user)
                    } else {
                        _userState.value = UiState.Error("User profile not found")
                    }
                }
            }
        } else {
            _userState.value = UiState.Error("No user logged in")
        }
    }

    /**
     * Update user profile基本信息
     */
    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        address: String?,
        city: String?,
        country: String
    ) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val userData = mutableMapOf<String, Any>()
            userData["firstName"] = firstName
            userData["lastName"] = lastName
            userData["email"] = email
            userData["phone"] = phone
            userData["country"] = country
            address?.let { userData["address"] = it }
            city?.let { userData["city"] = it }

            val result = userRepository.updateProfile(userId, userData)
            if (result.isSuccess) {
                _successMessage.value = "Profile updated successfully"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update profile"
            }

            _isLoading.value = false
        }
    }

    /**
     * Upload profile image
     */
    fun uploadProfileImage(imageUri: String) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository.uploadProfileImage(userId, imageUri)
            if (result.isSuccess) {
                // Update user data with new image URL
                result.getOrNull()?.let { imageUrl ->
                    userRepository.updateProfile(
                        userId,
                        mapOf<String, Any>("profileImage" to imageUrl)
                    )
                }
                _successMessage.value = "Profile image uploaded successfully"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to upload image"
            }

            _isLoading.value = false
        }
    }

    /**
     * Update notification settings
     */
    fun updateNotificationSettings(
        pushNotifications: Boolean,
        emailNotifications: Boolean,
        transactionAlerts: Boolean,
        lowBalanceAlerts: Boolean,
        promotionalEmails: Boolean
    ) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val settings = mapOf(
                "pushNotifications" to pushNotifications,
                "emailNotifications" to emailNotifications,
                "transactionAlerts" to transactionAlerts,
                "lowBalanceAlerts" to lowBalanceAlerts,
                "promotionalEmails" to promotionalEmails
            )

            val result = userRepository.updateNotificationSettings(userId, settings)
            if (result.isSuccess) {
                _successMessage.value = "Notification settings updated"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update settings"
            }

            _isLoading.value = false
        }
    }

    /**
     * Update security settings
     */
    fun updateSecuritySettings(
        biometricEnabled: Boolean,
        twoFactorAuth: Boolean,
        sessionTimeout: Int
    ) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val settings = mapOf(
                "biometricEnabled" to biometricEnabled,
                "twoFactorAuth" to twoFactorAuth,
                "sessionTimeout" to sessionTimeout
            )

            val result = userRepository.updateSecuritySettings(userId, settings)
            if (result.isSuccess) {
                _successMessage.value = "Security settings updated"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update settings"
            }

            _isLoading.value = false
        }
    }

    /**
     * Change password
     */
    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository.changePassword(newPassword)
            if (result.isSuccess) {
                _successMessage.value = "Password changed successfully"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to change password"
            }

            _isLoading.value = false
        }
    }

    /**
     * Request password reset
     */
    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository.resetPassword(email)
            if (result.isSuccess) {
                _successMessage.value = "Password reset email sent"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
            }

            _isLoading.value = false
        }
    }

    /**
     * Update preferred language
     */
    fun updatePreferredLanguage(language: String) {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = userRepository.updatePreferredLanguage(userId, language)
            if (result.isSuccess) {
                _successMessage.value = "Language updated successfully"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update language"
            }

            _isLoading.value = false
        }
    }

    /**
     * Delete account
     */
    fun deleteAccount() {
        val userId = firebaseAuthManager.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = userRepository.deleteAccount(userId)
            if (result.isSuccess) {
                _successMessage.value = "Account deleted successfully"
                firebaseAuthManager.signOut()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete account"
            }

            _isLoading.value = false
        }
    }

    /**
     * Clear success message after display
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Clear error message after display
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * UI State sealed class
     */
    sealed class UiState<out T> {
        data object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }
}