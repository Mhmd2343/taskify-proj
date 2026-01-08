package com.example.taskify.ui.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.student.data.StudentAssignmentRow
import com.example.taskify.ui.student.data.StudentAssignmentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudentSubjectFeedUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val tasks: List<StudentAssignmentRow> = emptyList()
)

class StudentSubjectFeedViewModel(
    private val repo: StudentAssignmentsRepository = StudentAssignmentsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(StudentSubjectFeedUiState())
    val ui: StateFlow<StudentSubjectFeedUiState> = _ui

    fun load(subject: String) {
        _ui.value = StudentSubjectFeedUiState(loading = true)
        viewModelScope.launch {
            runCatching {
                repo.getAssignmentsForSubject(subject)
            }.onSuccess { rows ->
                _ui.value = StudentSubjectFeedUiState(
                    loading = false,
                    tasks = rows
                )
            }.onFailure { e ->
                _ui.value = StudentSubjectFeedUiState(
                    loading = false,
                    error = e.message ?: "Failed to load tasks"
                )
            }
        }
    }

    fun markOpened(taskId: String) {
        viewModelScope.launch { runCatching { repo.markOpened(taskId) } }
    }
}
