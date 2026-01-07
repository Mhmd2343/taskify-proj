package com.example.taskify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.teacher.TeacherTasksFirestoreRepository
import com.example.taskify.ui.teacher.TaskStudentsBundle
import com.example.taskify.ui.teacher.SubmissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherTaskDetails(
    val taskId: String,
    val title: String,
    val subject: String,
    val content: String,
    val points: Int,
    val priority: String,
    val createdAtText: String,
    val attachments: List<AttachmentMini>
)

data class AttachmentMini(
    val type: String,
    val label: String,
    val value: String
)

data class StudentTaskRow(
    val uid: String,
    val fullName: String,
    val email: String,
    val state: SubmissionState
)

data class TeacherTaskDetailsUiState(
    val loading: Boolean = true,
    val deleting: Boolean = false,
    val savingGrades: Boolean = false,
    val task: TeacherTaskDetails? = null,
    val students: List<StudentTaskRow> = emptyList(),
    val grades: Map<String, String> = emptyMap(),
    val error: String = ""
)

class TeacherTaskDetailsViewModel(
    private val taskId: String,
    private val repo: TeacherTasksFirestoreRepository = TeacherTasksFirestoreRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(TeacherTaskDetailsUiState())
    val ui: StateFlow<TeacherTaskDetailsUiState> = _ui

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = "")
        viewModelScope.launch {
            try {
                val task = repo.getTaskDetails(taskId)
                val bundle: TaskStudentsBundle = repo.getTaskStudents(taskId)

                _ui.value = _ui.value.copy(
                    loading = false,
                    task = task,
                    students = bundle.students,
                    grades = bundle.grades,
                    error = ""
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load task")
            }
        }
    }

    fun setGrade(studentUid: String, gradeText: String) {
        val onlyDigits = gradeText.filter { it.isDigit() }
        _ui.value = _ui.value.copy(
            grades = _ui.value.grades.toMutableMap().apply { put(studentUid, onlyDigits) }
        )
    }

    fun saveGrades() {
        val st = _ui.value
        if (st.task == null) return
        _ui.value = st.copy(savingGrades = true, error = "")
        viewModelScope.launch {
            try {
                repo.updateTaskGrades(taskId = taskId, grades = st.grades)
                _ui.value = _ui.value.copy(savingGrades = false)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(savingGrades = false, error = e.message ?: "Failed to save grades")
            }
        }
    }

    fun deleteTask(onDone: () -> Unit) {
        _ui.value = _ui.value.copy(deleting = true, error = "")
        viewModelScope.launch {
            try {
                repo.deleteTask(taskId)
                _ui.value = _ui.value.copy(deleting = false)
                onDone()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(deleting = false, error = e.message ?: "Failed to delete task")
            }
        }
    }
}

class TeacherTaskDetailsViewModelFactory(
    private val taskId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TeacherTaskDetailsViewModel(taskId) as T
    }
}
