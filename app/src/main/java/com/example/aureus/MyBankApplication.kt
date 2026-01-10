package com.example.aureus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for MyBank
 * Annotated with @HiltAndroidApp for Hilt dependency injection
 */
@HiltAndroidApp
class MyBankApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-level components here
    }
}