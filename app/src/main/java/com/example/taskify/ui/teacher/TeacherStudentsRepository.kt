package com.example.taskify.ui.teacher

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeacherStudentsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getStudentsForSubject(subject: String): List<StudentRow> {
        val snap = db.collection("studentProfiles")
            .whereArrayContains("subjects", subject)
            .get()
            .await()

        return snap.documents.map { d ->
            val firstName = d.getString("firstName").orEmpty()
            val middleName = d.getString("middleName").orEmpty()
            val lastName = d.getString("lastName").orEmpty()
            val email = d.getString("email").orEmpty()

            val fullName = listOf(firstName, middleName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Student" }

            StudentRow(
                uid = d.id,
                fullName = fullName,
                email = email,
                state = SubmissionState.DELIVERED
            )
        }.sortedBy { it.fullName.lowercase() }
    }
}
