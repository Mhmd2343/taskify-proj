package com.example.taskify.ui.student.data

import com.example.taskify.ui.teacher.AttachmentType
import com.example.taskify.ui.teacher.SubmissionState
import com.example.taskify.ui.teacher.TaskAttachment
import com.example.taskify.ui.teacher.TaskPriority
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

data class StudentAssignmentRow(
    val taskId: String,
    val subject: String,
    val teacherName: String,
    val title: String,
    val content: String,
    val points: Int,
    val priority: TaskPriority,
    val createdAt: Long,
    val dueAt: Long?,
    val openFrom: Long?,
    val availableHours: Int?,
    val attachments: List<TaskAttachment>,
    val state: SubmissionState,
    val grade: Int?
)

class StudentAssignmentsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun toMillis(ts: Timestamp?): Long? = ts?.toDate()?.time

    suspend fun getAssignmentsForSubject(subject: String): List<StudentAssignmentRow> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val tasksSnap = db.collection("tasks")
            .whereEqualTo("subject", subject)
            .get()
            .await()

        if (tasksSnap.isEmpty) return emptyList()

        val rows = mutableListOf<StudentAssignmentRow>()

        for (taskDoc in tasksSnap.documents) {
            val studentDoc = db.collection("tasks")
                .document(taskDoc.id)
                .collection("students")
                .document(uid)
                .get()
                .await()

            if (!studentDoc.exists()) continue

            val priorityStr = taskDoc.getString("priority") ?: TaskPriority.MEDIUM.name
            val priority = runCatching { TaskPriority.valueOf(priorityStr) }.getOrDefault(TaskPriority.MEDIUM)

            val createdAt = toMillis(taskDoc.getTimestamp("createdAt")) ?: 0L
            val dueAt = toMillis(taskDoc.getTimestamp("dueAt"))
            val openFrom = toMillis(taskDoc.getTimestamp("openFrom"))
            val availableHours = (taskDoc.getLong("availableHours") ?: 0L).toInt().takeIf { it > 0 }

            val attachments = (taskDoc.get("attachments") as? List<*>)?.mapNotNull { any ->
                val m = any as? Map<*, *> ?: return@mapNotNull null
                val id = m["id"]?.toString().orEmpty()
                val typeStr = m["type"]?.toString().orEmpty()
                val type = runCatching { AttachmentType.valueOf(typeStr) }.getOrDefault(AttachmentType.PDF)
                val label = m["label"]?.toString().orEmpty()
                val value = m["value"]?.toString().orEmpty()
                TaskAttachment(id = id, type = type, label = label, value = value, isLoading = false)
            } ?: emptyList()

            val stateStr = studentDoc.getString("state") ?: SubmissionState.DELIVERED.name
            val state = runCatching { SubmissionState.valueOf(stateStr) }.getOrDefault(SubmissionState.DELIVERED)
            val grade = studentDoc.getLong("grade")?.toInt()

            rows.add(
                StudentAssignmentRow(
                    taskId = taskDoc.id,
                    subject = taskDoc.getString("subject").orEmpty(),
                    teacherName = taskDoc.getString("teacherName").orEmpty(),
                    title = taskDoc.getString("title").orEmpty(),
                    content = taskDoc.getString("content").orEmpty(),
                    points = (taskDoc.getLong("points") ?: 0L).toInt(),
                    priority = priority,
                    createdAt = createdAt,
                    dueAt = dueAt,
                    openFrom = openFrom,
                    availableHours = availableHours,
                    attachments = attachments,
                    state = state,
                    grade = grade
                )
            )
        }

        return rows.sortedByDescending { it.createdAt }
    }

    suspend fun markOpened(taskId: String) {
        val uid = auth.currentUser?.uid ?: return
        val stRef = db.collection("tasks").document(taskId).collection("students").document(uid)
        stRef.update(
            mapOf(
                "state" to SubmissionState.OPENED.name,
                "openedAt" to Timestamp(Date())
            )
        ).await()
    }
}
