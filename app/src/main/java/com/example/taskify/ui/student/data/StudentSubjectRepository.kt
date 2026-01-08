package com.example.taskify.ui.student.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class StudentSubjectItem(
    val subjectName: String,
    val teacherName: String?
)

class StudentSubjectRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun uid(): String? = auth.currentUser?.uid
    private fun email(): String = auth.currentUser?.email?.trim()?.lowercase().orEmpty()

    suspend fun getSelectedSubjects(): List<String> {
        val u = uid() ?: return emptyList()
        val doc = db.collection("studentProfiles").document(u).get().await()
        val arr = doc.get("subjects") as? List<*>
        return arr?.mapNotNull { it?.toString()?.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun saveSelectedSubjects(subjects: List<String>) {
        val u = uid() ?: return
        val payload = mapOf(
            "email" to email(),
            "subjects" to subjects.distinct(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("studentProfiles").document(u).set(payload).await()
    }

    suspend fun getAllSubjectsWithTeachers(): List<StudentSubjectItem> {
        val snap = db.collection("teacherProfiles").get().await()

        val map = linkedMapOf<String, String?>()

        snap.documents.forEach { d ->
            val firstName = d.getString("firstName").orEmpty()
            val middleName = d.getString("middleName").orEmpty()
            val lastName = d.getString("lastName").orEmpty()

            val teacherFullName = listOf(firstName, middleName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { null }

            val subjects = (d.get("subjects") as? List<*>)?.mapNotNull { it?.toString()?.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            subjects.forEach { s ->
                if (!map.containsKey(s)) map[s] = teacherFullName
            }
        }

        return map.keys
            .sortedBy { it.lowercase() }
            .map { s -> StudentSubjectItem(subjectName = s, teacherName = map[s]) }
    }
}
