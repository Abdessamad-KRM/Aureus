package com.example.aureus.data.remote

import com.example.aureus.data.remote.api.AccountApiService
import com.example.aureus.data.remote.api.AuthApiService
import com.example.aureus.data.remote.api.TransactionApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client Configuration
 */
object RetrofitClient {

    // TODO: Replace with your actual base URL
    private const val BASE_URL = "https://api.mybank.test/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val accountApiService: AccountApiService = retrofit.create(AccountApiService::class.java)
    val transactionApiService: TransactionApiService = retrofit.create(TransactionApiService::class.java)
}