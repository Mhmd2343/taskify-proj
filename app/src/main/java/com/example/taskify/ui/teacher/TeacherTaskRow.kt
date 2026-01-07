package com.example.taskify.ui.teacher


data class TeacherTaskRow(
    val taskId: String,
    val title: String,
    val subject: String,
    val points: Int,
    val priority: TaskPriority,
    val createdAt: Long,
    val dueAt: Long?,
    val openFrom: Long?,
    val availableHours: Int?
)