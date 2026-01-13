package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Phase 14: Tests - AuthRepositoryImpl Unit Tests
 *
 * Tests complets pour AuthRepositoryImpl vérifiant:
 * - Login avec credentials valides/invalides
 * - Register avec success et rollback
 * - Logout
 * - getCurrentUser
 * - isLoggedIn
 * - Méthodes additionnelles (resetPassword, etc.)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class AuthRepositoryImplTest {

    private lateinit var authRepository: AuthRepositoryImpl

    @Mock
    private lateinit var authManager: FirebaseAuthManager

    @Mock
    private lateinit var dataManager: FirebaseDataManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        authRepository = AuthRepositoryImpl(authManager, dataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login should return success with valid credentials`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(
            uid = "user_123",
            email = "test@example.com",
            displayName = "Test User"
        )

        whenever(authManager.loginWithEmail("test@example.com", "password"))
            .thenReturn(Result.success(mockUser))

        // When
        val result = authRepository.login("test@example.com", "password")

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("user_123", (result as Resource.Success).data?.id)
        assertEquals("test@example.com", (result as Resource.Success).data?.email)
        assertEquals("Test", (result as Resource.Success).data?.firstName)
        assertEquals("User", (result as Resource.Success).data?.lastName)

        verify(authManager, times(1)).loginWithEmail("test@example.com", "password")
    }

    @Test
    fun `login should return error with invalid credentials`() = runTest {
        // Given
        whenever(authManager.loginWithEmail(any(), any()))
            .thenReturn(Result.failure(
                com.google.firebase.auth.FirebaseAuthInvalidCredentialsException(
                    "test",
                    "Invalid credentials"
                )
            ))

        // When
        val result = authRepository.login("wrong@example.com", "wrongpassword")

        // Then
        assertTrue(result is Resource.Error)
        assertTrue(
            (result as Resource.Error).message?.contains("Email ou mot de passe incorrect") == true ||
            (result as Resource.Error).message?.contains("Erreur de connexion") == true
        )

        verify(authManager, times(1)).loginWithEmail("wrong@example.com", "wrongpassword")
    }

    @Test
    fun `login should handle FirebaseAuthInvalidUserException`() = runTest {
        // Given
        whenever(authManager.loginWithEmail(any(), any()))
            .thenReturn(Result.failure(
                com.google.firebase.auth.FirebaseAuthInvalidUserException("test", "User not found")
            ))

        // When
        val result = authRepository.login("nonexistent@example.com", "password")

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Utilisateur introuvable", (result as Resource.Error).message)
    }

    // ==================== REGISTER TESTS ====================

    @Test
    fun `register should create user in Firebase`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(
            uid = "new_user_456",
            email = "newuser@example.com"
        )

        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.success(mockUser))

        whenever(dataManager.createUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.success(Unit))

        whenever(dataManager.createDefaultCards(any()))
            .thenReturn(Result.success(Unit))

        whenever(dataManager.createDefaultTransactions(any()))
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.register(
            email = "newuser@example.com",
            password = "password123",
            firstName = "New",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("new_user_456", (result as Resource.Success).data?.id)
        assertEquals("newuser@example.com", (result as Resource.Success).data?.email)
        assertEquals("New", (result as Resource.Success).data?.firstName)
        assertEquals("User", (result as Resource.Success).data?.lastName)

        // Verify Firebase operations
        verify(authManager, times(1)).registerWithEmail(
            "newuser@example.com", "password123", "New", "User", "+212600000000"
        )
        verify(dataManager, times(1)).createUser(any(), any(), any(), any(), any(), any())
        verify(dataManager, times(1)).createDefaultCards("new_user_456")
        verify(dataManager, times(1)).createDefaultTransactions("new_user_456")
    }

    @Test
    fun `register should rollback auth if Firestore fails`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(uid = "temp_user", email = "temp@example.com")

        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.success(mockUser))

        whenever(dataManager.createUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("Firestore error")))

        whenever(mockUser.delete()).thenReturn(mockk {
            on { await() } returns Unit
        })

        // When
        val result = authRepository.register(
            email = "temp@example.com",
            password = "password123",
            firstName = "Temp",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("Erreur lors de la création du profil") == true)

        // Verify rollback was called
        verify(mockUser, times(1)).delete()
    }

    @Test
    fun `register should handle email already in use`() = runTest {
        // Given
        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("The email address is already in use")))

        // When
        val result = authRepository.register(
            email = "existing@example.com",
            password = "password123",
            firstName = "Existing",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("Email déjà utilisé") == true)
    }

    @Test
    fun `register should handle weak password`() = runTest {
        // Given
        whenever(authManager.registerWithEmail(any(), any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("Password should be at least 6 characters")))

        // When
        val result = authRepository.register(
            email = "new@example.com",
            password = "123", // Weak password
            firstName = "New",
            lastName = "User",
            phone = "+212600000000"
        )

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("mot de passe") == true)
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    fun `logout should sign out from Firebase`() = runTest {
        // Given
        whenever(authManager.signOut()).thenReturn(Unit)

        // When
        val result = authRepository.logout()

        // Then
        assertTrue(result is Resource.Success)
        verify(authManager, times(1)).signOut()
    }

    @Test
    fun `logout should handle sign out error`() = runTest {
        // Given
        whenever(authManager.signOut()).thenThrow(RuntimeException("Sign out failed"))

        // When
        val result = authRepository.logout()

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("Erreur lors de la déconnexion") == true)
    }

    // ==================== GET CURRENT USER TESTS ====================

    @Test
    fun `getCurrentUser should return user when logged in`() = runTest {
        // Given
        val mockUser = mockFirebaseUser(
            uid = "user_789",
            email = "logged@example.com",
            displayName = "Logged User"
        )

        whenever(authManager.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("user_789", (result as Resource.Success).data?.id)
        assertEquals("logged@example.com", (result as Resource.Success).data?.email)
        assertEquals("Logged", (result as Resource.Success).data?.firstName)
        assertEquals("User", (result as Resource.Success).data?.lastName)
    }

    @Test
    fun `getCurrentUser should return error when not logged in`() = runTest {
        // Given
        whenever(authManager.currentUser).thenReturn(null)

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Aucun utilisateur connecté", (result as Resource.Error).message)
    }

    // ==================== IS LOGGED IN TESTS ====================

    @Test
    fun `isLoggedIn should return true when user is logged in`() {
        // Given
        whenever(authManager.isUserLoggedIn()).thenReturn(true)

        // When
        val result = authRepository.isLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isLoggedIn should return false when user is not logged in`() {
        // Given
        whenever(authManager.isUserLoggedIn()).thenReturn(false)

        // When
        val result = authRepository.isLoggedIn()

        // Then
        assertFalse(result)
    }

    // ==================== TOKEN & USER ID TESTS ====================

    @Test
    fun `getToken should return UID as token`() {
        // Given
        val mockUser = mockFirebaseUser(uid = "user_token_123")
        whenever(authManager.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.getToken()

        // Then
        assertEquals("user_token_123", result)
    }

    @Test
    fun `getToken should return null when no user`() {
        // Given
        whenever(authManager.currentUser).thenReturn(null)

        // When
        val result = authRepository.getToken()

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `getUserId should return UID`() {
        // Given
        val mockUser = mockFirebaseUser(uid = "user_id_456")
        whenever(authManager.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.getUserId()

        // Then
        assertEquals("user_id_456", result)
    }

    @Test
    fun `getUserId should return null when no user`() {
        // Given
        whenever(authManager.currentUser).thenReturn(null)

        // When
        val result = authRepository.getUserId()

        // Then
        assertEquals(null, result)
    }

    // ==================== ADDITIONAL METHODS TESTS ====================

    @Test
    fun `resetPassword should succeed with valid email`() = runTest {
        // Given
        whenever(authManager.resetPassword("test@example.com"))
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.resetPassword("test@example.com")

        // Then
        assertTrue(result is Resource.Success)
        verify(authManager, times(1)).resetPassword("test@example.com")
    }

    @Test
    fun `resetPassword should handle error`() = runTest {
        // Given
        whenever(authManager.resetPassword(any()))
            .thenReturn(Result.failure(Exception("Invalid email")))

        // When
        val result = authRepository.resetPassword("invalid@example.com")

        // Then
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `sendEmailVerification should succeed`() = runTest {
        // Given
        whenever(authManager.sendEmailVerification())
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.sendEmailVerification()

        // Then
        assertTrue(result is Resource.Success)
        verify(authManager, times(1)).sendEmailVerification()
    }

    @Test
    fun `updatePassword should succeed`() = runTest {
        // Given
        whenever(authManager.updatePassword("newPassword123"))
            .thenReturn(Result.success(Unit))

        // When
        val result = authRepository.updatePassword("newPassword123")

        // Then
        assertTrue(result is Resource.Success)
        verify(authManager, times(1)).updatePassword("newPassword123")
    }

    @Test
    fun `isEmailVerified should return true when email verified`() {
        // Given
        val mockUser = mockFirebaseUser(uid = "user_123")
        whenever(mockUser.isEmailVerified).thenReturn(true)
        whenever(authManager.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.isEmailVerified()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isEmailVerified should return false when email not verified`() {
        // Given
        val mockUser = mockFirebaseUser(uid = "user_123")
        whenever(mockUser.isEmailVerified).thenReturn(false)
        whenever(authManager.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.isEmailVerified()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isEmailVerified should return false when no user`() {
        // Given
        whenever(authManager.currentUser).thenReturn(null)

        // When
        val result = authRepository.isEmailVerified()

        // Then
        assertFalse(result)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun mockFirebaseUser(
        uid: String,
        email: String = "",
        displayName: String = ""
    ): com.google.firebase.auth.FirebaseUser {
        val mock = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true)
        every { mock.uid } returns uid
        every { mock.email } returns email
        every { mock.displayName } returns displayName
        every { mock.phoneNumber } returns null
        every { mock.isEmailVerified } returns false
        every { mock.delete() } returns mockk {
            on { await() } returns Unit
        }
        return mock
    }
}