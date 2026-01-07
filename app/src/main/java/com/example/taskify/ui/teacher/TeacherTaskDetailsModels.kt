package com.example.taskify.ui.teacher

data class TeacherTaskDetails(
    val taskId: String,
    val teacherUid: String,
    val teacherName: String,
    val subject: String,
    val title: String,
    val content: String,
    val points: Int,
    val priority: TaskPriority,
    val createdAt: Long,
    val dueAt: Long?,
    val openFrom: Long?,
    val availableHours: Int?,
    val attachments: List<TaskAttachment>
)

data class TeacherTaskStudentRow(
    val uid: String,
    val fullName: String,
    val email: String,
    val state: SubmissionState,
    val grade: Int?
)
