package com.example.taskify.ui.admin

data class TeacherRecord(
    val uid: String,
    val teacherId: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val email: String,
    val subjectNames: List<String>
)
