package com.example.taskify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.teacher.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherTaskUiState(
    val loading: Boolean = true,
    val subjects: List<String> = emptyList(),
    val selectedSubject: String = "",
    val title: String = "",
    val pointsText: String = "",
    val content: String = "",
    val attachments: List<TaskAttachment> = emptyList(),
    val students: List<StudentRow> = emptyList(),
    val grades: Map<String, String> = emptyMap(),

    val priority: TaskPriority = TaskPriority.MEDIUM,

    val dueAtMillis: Long? = null,

    val scheduleEnabled: Boolean = false,
    val openFromMillis: Long? = null,
    val availableHoursText: String = "",

    val saving: Boolean = false,
    val error: String = "",
    val success: String = ""
)

class TeacherTasksViewModel(
    private val teacherRepo: TeacherRepository = TeacherRepository(),
    private val studentsRepo: TeacherStudentsRepository = TeacherStudentsRepository(),
    private val tasksRepo: TeacherTasksFirestoreRepository = TeacherTasksFirestoreRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(TeacherTaskUiState())
    val ui: StateFlow<TeacherTaskUiState> = _ui

    private var teacherUid: String = ""
    private var teacherName: String = ""

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = "", success = "")
        viewModelScope.launch {
            try {
                val profile = teacherRepo.getMyProfile()
                teacherUid = profile.uid
                teacherName = listOf(profile.firstName, profile.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Teacher" }

                val subjects = profile.subjects
                val selected = subjects.firstOrNull().orEmpty()

                _ui.value = _ui.value.copy(
                    loading = false,
                    subjects = subjects,
                    selectedSubject = selected
                )

                if (selected.isNotBlank()) {
                    val students = studentsRepo.getStudentsForSubject(selected)
                    _ui.value = _ui.value.copy(students = students)
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load")
            }
        }
    }

    fun setSubject(v: String) {
        _ui.value = _ui.value.copy(selectedSubject = v, error = "", success = "", students = emptyList())
        viewModelScope.launch {
            if (v.isBlank()) return@launch
            try {
                val students = studentsRepo.getStudentsForSubject(v)
                _ui.value = _ui.value.copy(students = students)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message ?: "Failed to load students")
            }
        }
    }

    fun setTitle(v: String) { _ui.value = _ui.value.copy(title = v, error = "", success = "") }
    fun setPointsText(v: String) { _ui.value = _ui.value.copy(pointsText = v.filter { it.isDigit() }, error = "", success = "") }
    fun setContent(v: String) { _ui.value = _ui.value.copy(content = v, error = "", success = "") }
    fun setPriority(v: TaskPriority) { _ui.value = _ui.value.copy(priority = v, error = "", success = "") }

    fun setDueAtMillis(v: Long?) { _ui.value = _ui.value.copy(dueAtMillis = v, error = "", success = "") }

    fun setScheduleEnabled(v: Boolean) {
        _ui.value = _ui.value.copy(
            scheduleEnabled = v,
            openFromMillis = if (v) _ui.value.openFromMillis else null,
            availableHoursText = if (v) _ui.value.availableHoursText else ""
        )
    }

    fun setOpenFromMillis(v: Long?) { _ui.value = _ui.value.copy(openFromMillis = v, error = "", success = "") }
    fun setAvailableHoursText(v: String) { _ui.value = _ui.value.copy(availableHoursText = v.filter { it.isDigit() }, error = "", success = "") }

    fun setGrade(studentUid: String, gradeText: String) {
        val onlyDigits = gradeText.filter { it.isDigit() }
        _ui.value = _ui.value.copy(
            grades = _ui.value.grades.toMutableMap().apply { put(studentUid, onlyDigits) },
            error = "",
            success = ""
        )
    }

    fun addAttachment(type: AttachmentType, label: String, value: String) {
        val att = TaskAttachment(
            id = java.util.UUID.randomUUID().toString(),
            type = type,
            label = label,
            value = value,
            isLoading = true
        )
        _ui.value = _ui.value.copy(attachments = _ui.value.attachments + att, error = "", success = "")
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                attachments = _ui.value.attachments.map { if (it.id == att.id) it.copy(isLoading = false) else it }
            )
        }
    }

    fun removeAttachment(id: String) {
        _ui.value = _ui.value.copy(attachments = _ui.value.attachments.filterNot { it.id == id })
    }

    private fun validate(): String? {
        val s = _ui.value
        if (teacherUid.isBlank()) return "Not logged in"
        if (s.selectedSubject.isBlank()) return "Select a subject"
        if (s.title.trim().isBlank()) return "Title is required"
        if (s.content.trim().isBlank()) return "Content is required"

        val pts = s.pointsText.toIntOrNull()
        if (pts == null || pts <= 0) return "Enter points (e.g. 20, 100)"
        if (s.attachments.any { it.isLoading }) return "Wait for attachments to finish"

        if (s.dueAtMillis == null) return "Select a due date"

        if (s.scheduleEnabled) {
            if (s.openFromMillis == null) return "Select the unlock date/time"
            val hrs = s.availableHoursText.toIntOrNull()
            if (hrs == null || hrs <= 0) return "Enter available hours (e.g. 2, 24)"
        }

        return null
    }

    fun submitTaskToFirestore() {
        val msg = validate()
        if (msg != null) {
            _ui.value = _ui.value.copy(error = msg, success = "")
            return
        }

        val s = _ui.value
        val points = s.pointsText.toInt()
        val availableHours = if (s.scheduleEnabled) s.availableHoursText.toInt() else null
        val openFromMillis = if (s.scheduleEnabled) s.openFromMillis else null

        _ui.value = s.copy(saving = true, error = "", success = "")

        viewModelScope.launch {
            try {
                val taskId = tasksRepo.createTaskAndAssignStudents(
                    teacherUid = teacherUid,
                    teacherName = teacherName,
                    subject = s.selectedSubject,
                    title = s.title,
                    content = s.content,
                    points = points,
                    priority = s.priority,
                    attachments = s.attachments,
                    studentUids = s.students.map { it.uid },
                    initialGrades = s.grades,
                    dueAtMillis = s.dueAtMillis,
                    openFromMillis = openFromMillis,
                    availableHours = availableHours
                )

                _ui.value = s.copy(
                    title = "",
                    content = "",
                    pointsText = "",
                    attachments = emptyList(),
                    grades = emptyMap(),
                    dueAtMillis = null,
                    scheduleEnabled = false,
                    openFromMillis = null,
                    availableHoursText = "",
                    saving = false,
                    success = "Task saved ✅ (id: $taskId)"
                )
            } catch (e: Exception) {
                _ui.value = s.copy(saving = false, error = e.message ?: "Failed to save task")
            }
        }
    }
}
