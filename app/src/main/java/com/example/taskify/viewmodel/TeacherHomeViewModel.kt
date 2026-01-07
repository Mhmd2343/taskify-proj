package com.example.taskify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TeacherHomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val subjects: List<String> = emptyList(),
    val error: String = ""
)

class TeacherHomeViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(TeacherHomeUiState())
    val uiState: StateFlow<TeacherHomeUiState> = _uiState



    fun loadTeacher() {
        val user = auth.currentUser
        val uid = user?.uid
        val email = user?.email?.trim()?.lowercase()

        if (uid.isNullOrBlank() || email.isNullOrBlank()) {
            _uiState.value = TeacherHomeUiState(isLoading = false, error = "No user logged in")
            return
        }

        _uiState.value = TeacherHomeUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val profileRef = db.collection("teacherProfiles").document(uid)
                val doc = profileRef.get().await()

                if (doc.exists()) {
                    val firstName = doc.getString("firstName").orEmpty()
                    val middleName = doc.getString("middleName").orEmpty()
                    val lastName = doc.getString("lastName").orEmpty()
                    val subjects = (doc.get("subjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    val fullName = listOf(firstName, middleName, lastName).filter { it.isNotBlank() }.joinToString(" ")
                    val subjectsText = if (subjects.isEmpty()) "subject" else subjects.joinToString(", ")

                    _uiState.value = TeacherHomeUiState(
                        isLoading = false,
                        greeting = "Hello Mr. $fullName, our $subjectsText teacher",
                        subjects = subjects
                    )
                    return@launch
                }

                val indexDoc = db.collection("teachersIndex").document(email).get().await()
                if (!indexDoc.exists()) {
                    _uiState.value = TeacherHomeUiState(isLoading = false, error = "Teacher profile not found")
                    return@launch
                }

                val firstName = indexDoc.getString("firstName").orEmpty()
                val middleName = indexDoc.getString("middleName").orEmpty()
                val lastName = indexDoc.getString("lastName").orEmpty()
                val subjects = (indexDoc.get("subjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                profileRef.set(
                    mapOf(
                        "firstName" to firstName,
                        "middleName" to middleName.ifBlank { null },
                        "lastName" to lastName,
                        "email" to email,
                        "subjects" to subjects,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                val fullName = listOf(firstName, middleName, lastName).filter { it.isNotBlank() }.joinToString(" ")
                val subjectsText = if (subjects.isEmpty()) "subject" else subjects.joinToString(", ")

                _uiState.value = TeacherHomeUiState(
                    isLoading = false,
                    greeting = "Hello Mr. $fullName, our $subjectsText teacher",
                    subjects = subjects
                )
            } catch (e: Exception) {
                _uiState.value = TeacherHomeUiState(isLoading = false, error = e.message ?: "Failed to load teacher")
            }
        }
    }
}
