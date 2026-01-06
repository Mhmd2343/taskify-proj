package com.example.taskify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.admin.AdminStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherHomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val error: String = ""
)

class TeacherHomeViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val storage = AdminStorage(app.applicationContext)

    private val _uiState = MutableStateFlow(TeacherHomeUiState())
    val uiState: StateFlow<TeacherHomeUiState> = _uiState

    fun loadTeacher() {
        val email = auth.currentUser?.email?.trim()?.lowercase()
        if (email.isNullOrBlank()) {
            _uiState.value = TeacherHomeUiState(isLoading = false, error = "No user logged in")
            return
        }

        _uiState.value = TeacherHomeUiState(isLoading = true)

        viewModelScope.launch {
            val teacher = storage.getAllTeachers()
                .firstOrNull { it.email.trim().lowercase() == email }

            if (teacher == null) {
                _uiState.value = TeacherHomeUiState(isLoading = false, error = "Teacher record not found")
                return@launch
            }

            val fullName = buildString {
                append(teacher.firstName)
                if (!teacher.middleName.isNullOrBlank()) {
                    append(" ")
                    append(teacher.middleName)
                }
                append(" ")
                append(teacher.lastName)
            }.trim()

            val subjects = teacher.subjectNames.joinToString(", ")

            _uiState.value = TeacherHomeUiState(
                isLoading = false,
                greeting = "Hello Mr. $fullName, our $subjects teacher"
            )
        }
    }
}
