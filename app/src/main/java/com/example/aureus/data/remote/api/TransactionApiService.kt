package com.example.aureus.data.remote.api

import com.example.aureus.data.remote.dto.TransactionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Transaction API Service
 * Handles all transaction-related API calls
 */
interface TransactionApiService {

    @GET("transactions")
    suspend fun getTransactions(@Header("Authorization") token: String): Response<List<TransactionResponse>>

    @GET("transactions/{transactionId}")
    suspend fun getTransactionById(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): Response<TransactionResponse>

    @GET("accounts/{accountId}/transactions")
    suspend fun getTransactionsByAccount(
        @Header("Authorization") token: String,
        @Path("accountId") accountId: String
    ): Response<List<TransactionResponse>>
}