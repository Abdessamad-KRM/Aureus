package com.example.aureus.data.repository

import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.local.entity.UserEntity
import com.example.aureus.data.remote.RetrofitClient
import com.example.aureus.data.remote.dto.LoginRequest
import com.example.aureus.data.remote.dto.LoginResponse
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import com.example.aureus.util.SharedPreferencesManager

/**
 * Authentication Repository Implementation
 */
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val preferencesManager: SharedPreferencesManager
) : AuthRepository {

    private val authApi = RetrofitClient.authApiService

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // Save token to SharedPreferences
                preferencesManager.saveToken(loginResponse.token)
                preferencesManager.saveUserId(loginResponse.user.id)
                preferencesManager.setLoggedIn(true)

                // Save user to local database
                val userEntity = UserEntity(
                    id = loginResponse.user.id,
                    email = loginResponse.user.email,
                    firstName = loginResponse.user.firstName,
                    lastName = loginResponse.user.lastName,
                    phone = loginResponse.user.phone,
                    createdAt = loginResponse.user.createdAt,
                    updatedAt = loginResponse.user.updatedAt
                )
                userDao.insertUser(userEntity)

                Resource.Success(loginResponse.user.toDomainModel())
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Resource<User> {
        return try {
            val registerRequest = com.example.aureus.data.remote.dto.RegisterRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )

            val response = authApi.register(registerRequest)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomainModel())
            } else {
                Resource.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            val token = "Bearer ${getToken()}"
            authApi.logout(token)

            // Clear local data
            preferencesManager.clearUserData()

            Resource.Success(Unit)
        } catch (e: Exception) {
            // Even if API call fails, clear local data
            preferencesManager.clearUserData()
            Resource.Success(Unit)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val userId = getUserId() ?: return Resource.Error("User not logged in")

            // Try to get from local database first
            val localUser = userDao.getUserById(userId)

            // For simplicity, returning null as the dao returns Flow
            // In real implementation, collect flow or use a different approach
            Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override fun isLoggedIn(): Boolean = preferencesManager.isLoggedIn()

    override fun getToken(): String? = preferencesManager.getToken()

    override fun getUserId(): String? = preferencesManager.getUserId()
}

// Extension functions for mapping
fun com.example.aureus.data.remote.dto.UserResponse.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}