package com.example.taskify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.teacher.TeacherTaskRow
import com.example.taskify.ui.teacher.TeacherTasksFirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherTasksListUiState(
    val loading: Boolean = true,
    val tasks: List<TeacherTaskRow> = emptyList(),
    val error: String = ""
)

class TeacherTasksListViewModel(
    private val repo: TeacherTasksFirestoreRepository = TeacherTasksFirestoreRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(TeacherTasksListUiState())
    val ui: StateFlow<TeacherTasksListUiState> = _ui

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            try {
                val tasks = repo.getMyTasks()
                _ui.value = TeacherTasksListUiState(loading = false, tasks = tasks, error = "")
            } catch (e: Exception) {
                _ui.value = TeacherTasksListUiState(
                    loading = false,
                    tasks = emptyList(),
                    error = e.message ?: "Failed to load tasks"
                )
            }
        }
    }
}
