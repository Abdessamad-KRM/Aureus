package com.example.aureus.data.remote.api

import com.example.aureus.data.remote.dto.AccountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Account API Service
 * Handles all account-related API calls
 */
interface AccountApiService {

    @GET("accounts")
    suspend fun getAccounts(@Header("Authorization") token: String): Response<List<AccountResponse>>

    @GET("accounts/{accountId}")
    suspend fun getAccountById(
        @Header("Authorization") token: String,
        @Path("accountId") accountId: String
    ): Response<AccountResponse>
}