package com.example.taskify.ui.teacher

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TeacherProfileEditable(
    val firstName: String,
    val middleName: String,
    val lastName: String
)

class TeacherProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun loadMyProfile(): TeacherProfileEditable {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val doc = db.collection("teacherProfiles").document(uid).get().await()
        return TeacherProfileEditable(
            firstName = doc.getString("firstName").orEmpty(),
            middleName = doc.getString("middleName").orEmpty(),
            lastName = doc.getString("lastName").orEmpty()
        )
    }

    suspend fun saveMyProfile(firstName: String, middleName: String, lastName: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        db.collection("teacherProfiles").document(uid)
            .set(
                mapOf(
                    "firstName" to firstName.trim(),
                    "middleName" to middleName.trim(),
                    "lastName" to lastName.trim()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    suspend fun changePassword(oldPass: String, newPass: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        val email = user.email ?: throw IllegalStateException("No email found")
        val credential = EmailAuthProvider.getCredential(email, oldPass)
        user.reauthenticate(credential).await()
        user.updatePassword(newPass).await()
    }

    suspend fun deleteAccount(password: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        val email = user.email ?: throw IllegalStateException("No email found")
        val uid = user.uid

        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()

        db.collection("teacherProfiles").document(uid).delete().await()
        user.delete().await()
    }
}
