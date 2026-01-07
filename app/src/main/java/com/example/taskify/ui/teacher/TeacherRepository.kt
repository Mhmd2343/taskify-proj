package com.example.taskify.ui.teacher

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TeacherProfile(
    val uid: String,
    val teacherId: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val email: String,
    val subjects: List<String>
)

class TeacherRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMyProfile(): TeacherProfile {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val snap = db.collection("teacherProfiles").document(uid).get().await()
        if (!snap.exists()) throw IllegalStateException("Teacher profile not found")

        val teacherId = snap.getLong("teacherId") ?: -1L
        val firstName = snap.getString("firstName") ?: ""
        val middleName = snap.getString("middleName")
        val lastName = snap.getString("lastName") ?: ""
        val email = snap.getString("email") ?: (auth.currentUser?.email ?: "")
        val subjects = (snap.get("subjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        return TeacherProfile(
            uid = uid,
            teacherId = teacherId,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            email = email,
            subjects = subjects
        )
    }
}
