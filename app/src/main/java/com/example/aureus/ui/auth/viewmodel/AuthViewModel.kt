package com.example.aureus.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel
 * Handles authentication logic
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val loginState: StateFlow<Resource<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val registerState: StateFlow<Resource<User>> = _registerState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val logoutState: StateFlow<Resource<Unit>> = _logoutState.asStateFlow()

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    init {
        // Check if user is already logged in
        if (authRepository.isLoggedIn()) {
            _loginState.value = Resource.Loading
        } else {
            _loginState.value = Resource.Error("Not logged in")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(email, password)
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            _registerState.value = authRepository.register(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = Resource.Loading
            _logoutState.value = authRepository.logout()
        }
    }

    fun resetLoginState() {
        _loginState.value = Resource.Loading
    }

    fun resetRegisterState() {
        _registerState.value = Resource.Loading
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Loading
    }
}