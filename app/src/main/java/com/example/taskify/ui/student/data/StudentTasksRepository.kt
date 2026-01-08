package com.example.taskify.ui.student.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class StudentTaskFeedItem(
    val taskId: String,
    val teacherName: String,
    val subject: String,
    val title: String,
    val createdAt: Long,
    val dueAt: Long?,
    val priority: String
)

class StudentTasksRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getMyTasksForSubject(subject: String): List<StudentTaskFeedItem> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val studentSubs = db.collectionGroup("students")
            .whereEqualTo("__name__", uid)
            .get()
            .await()

        if (studentSubs.isEmpty) return emptyList()

        val taskIds = studentSubs.documents.mapNotNull { it.reference.parent.parent?.id }.distinct()
        if (taskIds.isEmpty()) return emptyList()

        val tasks = mutableListOf<StudentTaskFeedItem>()

        val chunks = taskIds.chunked(10)
        for (chunk in chunks) {
            val snap = db.collection("tasks")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                .get()
                .await()

            snap.documents.forEach { doc ->
                val subj = doc.getString("subject").orEmpty()
                if (subj != subject) return@forEach

                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                val dueAt = doc.getTimestamp("dueAt")?.toDate()?.time
                val teacherName = doc.getString("teacherName").orEmpty().ifBlank { "Teacher" }
                val title = doc.getString("title").orEmpty()
                val priority = doc.getString("priority").orEmpty()

                tasks.add(
                    StudentTaskFeedItem(
                        taskId = doc.id,
                        teacherName = teacherName,
                        subject = subj,
                        title = title,
                        createdAt = createdAt,
                        dueAt = dueAt,
                        priority = priority
                    )
                )
            }
        }

        return tasks.sortedByDescending { it.createdAt }
    }
}
