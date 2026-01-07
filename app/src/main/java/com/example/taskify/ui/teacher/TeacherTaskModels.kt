package com.example.taskify.ui.teacher

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class AttachmentType { PPT, EXCEL, WORD, URL, IMAGE, TXT, PDF }

data class TaskAttachment(
    val id: String,
    val type: AttachmentType,
    val label: String,
    val value: String,
    val isLoading: Boolean = false
)

enum class SubmissionState { DELIVERED, OPENED, SUBMITTED }

data class StudentRow(
    val uid: String,
    val fullName: String,
    val email: String,
    val state: SubmissionState
)