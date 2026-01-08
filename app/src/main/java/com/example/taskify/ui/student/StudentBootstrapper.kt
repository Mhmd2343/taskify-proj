package com.example.taskify.ui.student

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudentBootstrapper(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun ensureStudentDocs(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val email = auth.currentUser?.email?.trim()?.lowercase().orEmpty()

        val userRef = db.collection("users").document(uid)
        val userSnap = userRef.get().await()

        if (!userSnap.exists()) {
            userRef.set(
                mapOf(
                    "email" to email,
                    "role" to "student",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            val role = userSnap.getString("role")?.trim()?.lowercase()
            if (role.isNullOrBlank()) {
                userRef.update("role", "student").await()
            }
        }

        val studentRef = db.collection("studentProfiles").document(uid)
        val studentSnap = studentRef.get().await()

        if (!studentSnap.exists()) {
            studentRef.set(
                mapOf(
                    "email" to email,
                    "subjects" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        return true
    }
}
