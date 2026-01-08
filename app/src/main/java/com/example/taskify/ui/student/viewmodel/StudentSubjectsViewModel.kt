package com.example.taskify.ui.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.student.data.StudentSubjectItem
import com.example.taskify.ui.student.data.StudentSubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentSubjectsUiState(
    val loading: Boolean = false,
    val allSubjects: List<StudentSubjectItem> = emptyList(),
    val selectedSubjects: Set<String> = emptySet(),
    val error: String? = null
)

class StudentSubjectsViewModel(
    private val repo: StudentSubjectRepository = StudentSubjectRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(StudentSubjectsUiState(loading = true))
    val state: StateFlow<StudentSubjectsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching {
                val all = repo.getAllSubjectsWithTeachers()
                val selected = repo.getSelectedSubjects().toSet()
                _state.value = StudentSubjectsUiState(
                    loading = false,
                    allSubjects = all,
                    selectedSubjects = selected,
                    error = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load subjects"
                )
            }
        }
    }

    fun toggleSubject(subjectName: String) {
        val current = _state.value.selectedSubjects.toMutableSet()
        if (current.contains(subjectName)) current.remove(subjectName) else current.add(subjectName)
        _state.value = _state.value.copy(selectedSubjects = current)
    }

    fun toggleSelectAll() {
        val all = _state.value.allSubjects.map { it.subjectName }.toSet()
        val current = _state.value.selectedSubjects

        _state.value = if (current.size == all.size && all.isNotEmpty()) {
            _state.value.copy(selectedSubjects = emptySet())
        } else {
            _state.value.copy(selectedSubjects = all)
        }
    }

    fun saveSelection(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val selectedList = _state.value.selectedSubjects.toList().sortedBy { it.lowercase() }
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching {
                repo.saveSelectedSubjects(selectedList)
            }.onSuccess {
                _state.value = _state.value.copy(loading = false)
                onDone?.invoke()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to save subjects"
                )
            }
        }
    }
}
