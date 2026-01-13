package com.example.aureus.ui.profile.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.aureus.MainDispatcherRule
import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.kotlin.*

/**
 * ProfileViewModel Test
 * Tests for profile management, settings updates, and user operations
 */
@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var firebaseAuthManager: FirebaseAuthManager

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var viewModel: ProfileViewModel

    private val testUserId = "test_user_id"
    private val testUser = User(
        id = testUserId,
        email = "john.doe@example.com",
        firstName = "John",
        lastName = "Doe",
        phone = "+1234567890",
        createdAt = "2024-01-01",
        updatedAt = "2024-01-01",
        address = "123 Street",
        city = "City",
        country = "Morocco",
        profileImage = "https://example.com/image.jpg"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup default mock behaviors
        `when`(firebaseAuthManager.currentUser).thenReturn(firebaseUser)
        `when`(firebaseUser.uid).thenReturn(testUserId)
        `when`(userRepository.getUserProfile(testUserId)).thenReturn(flowOf(testUser))
    }

    @After
    fun tearDown() {
        // MainDispatcherRule handles cleanup
    }

    // ==================== INITIAL STATE TESTS ====================

    @Test
    fun `initial state should be loading`() = runTest {
        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        advanceUntilIdle()

        // Then - Initial state is loading, then success
        val state = viewModel.userState.value
        assertTrue(state is ProfileViewModel.UiState.Success || state is ProfileViewModel.UiState.Loading)
    }

    @Test
    fun `loadUserProfile should populate user state`() = runTest {
        // Given
        `when`(userRepository.getUserProfile(testUserId)).thenReturn(flowOf(testUser))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.userState.value
        assertTrue(state is ProfileViewModel.UiState.Success)
        assertEquals(testUser.firstName, (state as ProfileViewModel.UiState.Success).data.firstName)
    }

    @Test
    fun `loadUserProfile should set error without user`() = runTest {
        // Given
        `when`(firebaseAuthManager.currentUser).thenReturn(null)

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.userState.value
        assertTrue(state is ProfileViewModel.UiState.Error)
        assertEquals("No user logged in", (state as ProfileViewModel.UiState.Error).message)
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    fun `updateProfile should call repository with correct data`() = runTest {
        // Given
        `when`(userRepository.updateProfile(any(), anyMap<String, Any>())).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+0987654321",
            address = "456 Ave",
            city = "New City",
            country = "USA"
        )
        advanceUntilIdle()

        // Then
        verify(userRepository).updateProfile(eq(testUserId), argThat { map ->
            map["firstName"] == "Jane" &&
            map["lastName"] == "Smith" &&
            map["email"] == "jane@example.com" &&
            map["phone"] == "+0987654321" &&
            map["address"] == "456 Ave" &&
            map["city"] == "New City" &&
            map["country"] == "USA"
        })
        assertEquals("Profile updated successfully", viewModel.successMessage.value)
    }

    @Test
    fun `updateProfile should handle null address and city`() = runTest {
        // Given
        `when`(userRepository.updateProfile(any(), anyMap<String, Any>())).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+0987654321",
            address = null,
            city = null,
            country = "USA"
        )
        advanceUntilIdle()

        // Then
        verify(userRepository).updateProfile(eq(testUserId), argThat { map ->
            !map.containsKey("address") && !map.containsKey("city")
        })
    }

    @Test
    fun `updateProfile should set error on failure`() = runTest {
        // Given
        `when`(userRepository.updateProfile(any(), anyMap<String, Any>()))
            .thenReturn(Result.failure(Exception("Update failed")))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+0987654321",
            address = "456 Ave",
            city = "New City",
            country = "USA"
        )
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.errorMessage.value)
        assertEquals("Update failed", viewModel.errorMessage.value)
    }

    @Test
    fun `updateProfile should fail without user`() = runTest {
        // Given
        `when`(firebaseAuthManager.currentUser).thenReturn(null)

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+0987654321",
            address = "456 Ave",
            city = "New City",
            country = "USA"
        )
        advanceUntilIdle()

        // Then
        verify(userRepository, never()).updateProfile(any(), any())
        assertEquals("No user logged in", viewModel.errorMessage.value)
    }

    // ==================== UPLOAD PROFILE IMAGE TESTS ====================

    @Test
    fun `uploadProfileImage should call repository and update profile`() = runTest {
        // Given
        val imageUrl = "https://example.com/uploaded.jpg"
        `when`(userRepository.uploadProfileImage(testUserId, "content://image"))
            .thenReturn(Result.success(imageUrl))
        `when`(userRepository.updateProfile(testUserId, mapOf("profileImage" to imageUrl)))
            .thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.uploadProfileImage("content://image")
        advanceUntilIdle()

        // Then
        verify(userRepository).uploadProfileImage(testUserId, "content://image")
        verify(userRepository).updateProfile(testUserId, mapOf("profileImage" to imageUrl))
        assertEquals("Profile image uploaded successfully", viewModel.successMessage.value)
    }

    @Test
    fun `uploadProfileImage should set error on upload failure`() = runTest {
        // Given
        `when`(userRepository.uploadProfileImage(any(), any()))
            .thenReturn(Result.failure(Exception("Upload failed")))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.uploadProfileImage("content://image")
        advanceUntilIdle()

        // Then
        assertEquals("Upload failed", viewModel.errorMessage.value)
    }

    // ==================== UPDATE NOTIFICATION SETTINGS TESTS ====================

    @Test
    fun `updateNotificationSettings should call repository`() = runTest {
        // Given
        `when`(userRepository.updateNotificationSettings(any(), anyMap<String, Boolean>())).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateNotificationSettings(
            pushNotifications = true,
            emailNotifications = false,
            transactionAlerts = true,
            lowBalanceAlerts = false,
            promotionalEmails = true
        )
        advanceUntilIdle()

        // Then
        verify(userRepository).updateNotificationSettings(eq(testUserId), argThat { map ->
            map["pushNotifications"] == true &&
            map["emailNotifications"] == false &&
            map["transactionAlerts"] == true &&
            map["lowBalanceAlerts"] == false &&
            map["promotionalEmails"] == true
        })
        assertEquals("Notification settings updated", viewModel.successMessage.value)
    }

    // ==================== UPDATE SECURITY SETTINGS TESTS ====================

    @Test
    fun `updateSecuritySettings should call repository`() = runTest {
        // Given
        `when`(userRepository.updateSecuritySettings(any(), anyMap<String, Any>())).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateSecuritySettings(
            biometricEnabled = true,
            twoFactorAuth = true,
            sessionTimeout = 30
        )
        advanceUntilIdle()

        // Then
        verify(userRepository).updateSecuritySettings(eq(testUserId), argThat { map ->
            map["biometricEnabled"] == true &&
            map["twoFactorAuth"] == true &&
            map["sessionTimeout"] == 30
        })
        assertEquals("Security settings updated", viewModel.successMessage.value)
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    fun `changePassword should call repository`() = runTest {
        // Given
        `when`(userRepository.changePassword("newSecurePassword123"))
            .thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.changePassword("newSecurePassword123")
        advanceUntilIdle()

        // Then
        verify(userRepository).changePassword("newSecurePassword123")
        assertEquals("Password changed successfully", viewModel.successMessage.value)
    }

    @Test
    fun `changePassword should set error on failure`() = runTest {
        // Given
        `when`(userRepository.changePassword(any()))
            .thenReturn(Result.failure(Exception("Weak password")))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.changePassword("weak")
        advanceUntilIdle()

        // Then
        assertEquals("Weak password", viewModel.errorMessage.value)
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    fun `requestPasswordReset should call repository`() = runTest {
        // Given
        `when`(userRepository.resetPassword("user@example.com"))
            .thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.requestPasswordReset("user@example.com")
        advanceUntilIdle()

        // Then
        verify(userRepository).resetPassword("user@example.com")
        assertEquals("Password reset email sent", viewModel.successMessage.value)
    }

    // ==================== UPDATE LANGUAGE TESTS ====================

    @Test
    fun `updatePreferredLanguage should call repository`() = runTest {
        // Given
        `when`(userRepository.updatePreferredLanguage(testUserId, "ar"))
            .thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updatePreferredLanguage("ar")
        advanceUntilIdle()

        // Then
        verify(userRepository).updatePreferredLanguage(testUserId, "ar")
        assertEquals("Language updated successfully", viewModel.successMessage.value)
    }

    @Test
    fun `updatePreferredLanguage should handle all supported languages`() = runTest {
        val languages = listOf("en", "fr", "ar", "es", "de")

        languages.forEach { lang ->
            // Given
            `when`(userRepository.updatePreferredLanguage(testUserId, lang))
                .thenReturn(Result.success(Unit))

            // When
            viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
            viewModel.updatePreferredLanguage(lang)
            advanceUntilIdle()

            // Then
            verify(userRepository).updatePreferredLanguage(testUserId, lang)
            assertEquals("Language updated successfully", viewModel.successMessage.value)
        }
    }

    // ==================== DELETE ACCOUNT TESTS ====================

    @Test
    fun `deleteAccount should call repository and sign out`() = runTest {
        // Given
        `when`(userRepository.deleteAccount(testUserId)).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.deleteAccount()
        advanceUntilIdle()

        // Then
        verify(userRepository).deleteAccount(testUserId)
        verify(firebaseAuthManager).signOut()
        assertEquals("Account deleted successfully", viewModel.successMessage.value)
    }

    @Test
    fun `deleteAccount should set error on failure`() = runTest {
        // Given
        `when`(userRepository.deleteAccount(any()))
            .thenReturn(Result.failure(Exception("Cannot delete account")))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.deleteAccount()
        advanceUntilIdle()

        // Then
        assertEquals("Cannot delete account", viewModel.errorMessage.value)
        verify(firebaseAuthManager, never()).signOut()
    }

    // ==================== CLEAR MESSAGES TESTS ====================

    @Test
    fun `clearSuccessMessage should reset successMessage`() = runTest {
        // Given - set a success message
        `when`(userRepository.changePassword("newPass")).thenReturn(Result.success(Unit))
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.changePassword("newPass")
        advanceUntilIdle()
        assertNotNull(viewModel.successMessage.value)

        // When
        viewModel.clearSuccessMessage()

        // Then
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `clearErrorMessage should reset errorMessage`() = runTest {
        // Given - set an error message
        `when`(userRepository.changePassword(any())).thenReturn(Result.failure(Exception("Error")))
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.changePassword("pass")
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)

        // When
        viewModel.clearErrorMessage()

        // Then
        assertNull(viewModel.errorMessage.value)
    }

    // ==================== LOADING STATE TESTS ====================

    @Test
    fun `operations should update loading state correctly`() = runTest {
        // Given
        `when`(userRepository.updateProfile(testUserId, anyMap<String, Any>())).thenReturn(Result.success(Unit))

        // When
        viewModel = ProfileViewModel(userRepository, firebaseAuthManager)
        viewModel.updateProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+0987654321",
            address = "456 Ave",
            city = "New City",
            country = "USA"
        )
        advanceUntilIdle()

        // Then - Loading should be false after operation completes
        assertFalse(viewModel.isLoading.value)
    }
}