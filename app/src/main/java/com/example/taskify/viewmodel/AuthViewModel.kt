package com.example.taskify.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    val authState = MutableLiveData<AuthState>(AuthState.Idle)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authState.postValue(AuthState.Loading)
            val result = repo.login(email.trim(), password)
            if (result.isSuccess) {
                authState.postValue(AuthState.Success)
            } else {
                authState.postValue(AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed"))
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            authState.postValue(AuthState.Loading)
            val result = repo.register(email.trim(), password)
            if (result.isSuccess) {
                authState.postValue(AuthState.Success)
            } else {
                authState.postValue(AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed"))
            }
        }
    }

    fun signOut() {
        repo.signOut()
        authState.postValue(AuthState.Idle)
    }

    fun currentUserEmail(): String? = repo.currentUserEmail()
}
