package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User

/**
 * Authentication Repository Interface
 */
interface AuthRepository {

    suspend fun login(email: String, password: String): Resource<User>

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Resource<User>

    suspend fun logout(): Resource<Unit>

    suspend fun getCurrentUser(): Resource<User>

    fun isLoggedIn(): Boolean

    fun getToken(): String?

    fun getUserId(): String?
}