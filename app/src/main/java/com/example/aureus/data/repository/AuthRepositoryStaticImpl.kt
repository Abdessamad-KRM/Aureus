package com.example.aureus.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.aureus.data.TestAccount
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Static implementation of AuthRepository for demo purposes
 * Uses test account credentials
 */
class AuthRepositoryStaticImpl(context: Context) : AuthRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "aureus_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        // Simulate network delay
        delay(1000)

        return if (email == TestAccount.EMAIL && password == TestAccount.PASSWORD) {
            // Save login state
            prefs.edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_USER_ID, TestAccount.USER_ID)
                putString(KEY_TOKEN, "static_token_${System.currentTimeMillis()}")
                apply()
            }

            Resource.Success(
                User(
                    id = TestAccount.USER_ID,
                    email = TestAccount.EMAIL,
                    firstName = TestAccount.FIRST_NAME,
                    lastName = TestAccount.LAST_NAME,
                    phone = TestAccount.PHONE,
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = System.currentTimeMillis().toString()
                )
            )
        } else {
            Resource.Error("Email ou mot de passe incorrect")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Resource<User> {
        // Simulate network delay
        delay(1500)

        // For demo, always succeed but don't log in yet
        return Resource.Success(
            User(
                id = "new_user_${System.currentTimeMillis()}",
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString()
            )
        )
    }

    override suspend fun logout(): Resource<Unit> {
        delay(500)

        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_TOKEN)
            apply()
        }

        return Resource.Success(Unit)
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return if (isLoggedIn()) {
            Resource.Success(
                User(
                    id = TestAccount.USER_ID,
                    email = TestAccount.EMAIL,
                    firstName = TestAccount.FIRST_NAME,
                    lastName = TestAccount.LAST_NAME,
                    phone = TestAccount.PHONE,
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = System.currentTimeMillis().toString()
                )
            )
        } else {
            Resource.Error("Not logged in")
        }
    }

    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    override fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    override fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
}
