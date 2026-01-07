package com.example.taskify.ui.teacher

import com.example.taskify.viewmodel.AttachmentMini
import com.example.taskify.viewmodel.StudentTaskRow
import com.example.taskify.viewmodel.TeacherTaskDetails
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TaskStudentsBundle(
    val students: List<StudentTaskRow>,
    val grades: Map<String, String>
)

class TeacherTasksFirestoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getMyTasks(): List<TeacherTaskRow> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val snap = db.collection("tasks")
            .whereEqualTo("teacherUid", uid)
            .get()
            .await()

        return snap.documents.map { doc ->
            val priorityStr = doc.getString("priority") ?: TaskPriority.MEDIUM.name
            val priority = runCatching { TaskPriority.valueOf(priorityStr) }.getOrDefault(TaskPriority.MEDIUM)

            val createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
            val dueAtMillis = doc.getTimestamp("dueAt")?.toDate()?.time
            val openFromMillis = doc.getTimestamp("openFrom")?.toDate()?.time
            val availableHours = (doc.getLong("availableHours") ?: 0L).toInt().takeIf { it > 0 }

            TeacherTaskRow(
                taskId = doc.id,
                title = doc.getString("title") ?: "",
                subject = doc.getString("subject") ?: "",
                points = (doc.getLong("points") ?: 0L).toInt(),
                priority = priority,
                createdAt = createdAtMillis,
                dueAt = dueAtMillis,
                openFrom = openFromMillis,
                availableHours = availableHours
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun createTaskAndAssignStudents(
        teacherUid: String,
        teacherName: String,
        subject: String,
        title: String,
        content: String,
        points: Int,
        priority: TaskPriority,
        attachments: List<TaskAttachment>,
        studentUids: List<String>,
        initialGrades: Map<String, String>,
        dueAtMillis: Long?,
        openFromMillis: Long?,
        availableHours: Int?
    ): String {
        val taskRef = db.collection("tasks").document()
        val batch = db.batch()

        val attachmentsPayload = attachments.map {
            mapOf(
                "id" to it.id,
                "type" to it.type.name,
                "label" to it.label,
                "value" to it.value,
                "isLoading" to false
            )
        }

        val taskData = mutableMapOf<String, Any?>(
            "teacherUid" to teacherUid,
            "teacherName" to teacherName,
            "subject" to subject,
            "title" to title.trim(),
            "content" to content.trim(),
            "points" to points,
            "priority" to priority.name,
            "attachments" to attachmentsPayload,
            "createdAt" to FieldValue.serverTimestamp(),
            "dueAt" to (dueAtMillis?.let { Timestamp(Date(it)) })
        )

        if (openFromMillis != null) taskData["openFrom"] = Timestamp(Date(openFromMillis))
        if (availableHours != null) taskData["availableHours"] = availableHours

        batch.set(taskRef, taskData)

        studentUids.forEach { studentUid ->
            val stRef = taskRef.collection("students").document(studentUid)

            val gradeText = initialGrades[studentUid]?.trim()?.takeIf { it.isNotBlank() }
            val gradeNumber = gradeText?.toIntOrNull()

            val data = mutableMapOf<String, Any?>(
                "state" to SubmissionState.DELIVERED.name,
                "grade" to gradeNumber,
                "deliveredAt" to FieldValue.serverTimestamp(),
                "openedAt" to null,
                "submittedAt" to null,
                "gradedAt" to if (gradeNumber != null) FieldValue.serverTimestamp() else null
            )

            batch.set(stRef, data)
        }

        batch.commit().await()
        return taskRef.id
    }

    suspend fun getTaskDetails(taskId: String): TeacherTaskDetails {
        val doc = db.collection("tasks").document(taskId).get().await()

        val title = doc.getString("title") ?: ""
        val subject = doc.getString("subject") ?: ""
        val content = doc.getString("content") ?: ""
        val points = (doc.getLong("points") ?: 0L).toInt()
        val priority = doc.getString("priority") ?: TaskPriority.MEDIUM.name

        val createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
        val createdAtText = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
            .format(Date(createdAtMillis))

        val attachments = (doc.get("attachments") as? List<*>)?.mapNotNull { any ->
            val m = any as? Map<*, *> ?: return@mapNotNull null
            val type = m["type"]?.toString().orEmpty()
            val label = m["label"]?.toString().orEmpty()
            val value = m["value"]?.toString().orEmpty()
            AttachmentMini(type = type, label = label, value = value)
        } ?: emptyList()

        return TeacherTaskDetails(
            taskId = doc.id,
            title = title,
            subject = subject,
            content = content,
            points = points,
            priority = priority,
            createdAtText = createdAtText,
            attachments = attachments
        )
    }

    suspend fun getTaskStudents(taskId: String): TaskStudentsBundle {
        val studentsSnap = db.collection("tasks")
            .document(taskId)
            .collection("students")
            .get()
            .await()

        val studentIds = studentsSnap.documents.map { it.id }
        val grades = studentsSnap.documents.associate { d ->
            val g = (d.getLong("grade")?.toInt())?.toString() ?: ""
            d.id to g
        }

        if (studentIds.isEmpty()) {
            return TaskStudentsBundle(students = emptyList(), grades = grades)
        }

        val studentsProfiles = mutableMapOf<String, StudentRow>()

        val chunks = studentIds.chunked(10)
        for (chunk in chunks) {
            val profSnap = db.collection("studentProfiles")
                .whereIn("__name__", chunk)
                .get()
                .await()

            profSnap.documents.forEach { d ->
                val firstName = d.getString("firstName").orEmpty()
                val middleName = d.getString("middleName").orEmpty()
                val lastName = d.getString("lastName").orEmpty()
                val email = d.getString("email").orEmpty()

                val fullName = listOf(firstName, middleName, lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Student" }

                studentsProfiles[d.id] = StudentRow(
                    uid = d.id,
                    fullName = fullName,
                    email = email,
                    state = SubmissionState.DELIVERED
                )
            }
        }

        val rows = studentsSnap.documents.map { stDoc ->
            val stateStr = stDoc.getString("state") ?: SubmissionState.DELIVERED.name
            val state = runCatching { SubmissionState.valueOf(stateStr) }.getOrDefault(SubmissionState.DELIVERED)

            val p = studentsProfiles[stDoc.id]
            StudentTaskRow(
                uid = stDoc.id,
                fullName = p?.fullName ?: "Student",
                email = p?.email ?: "",
                state = state
            )
        }.sortedBy { it.fullName.lowercase() }

        return TaskStudentsBundle(students = rows, grades = grades)
    }

    suspend fun updateTaskGrades(taskId: String, grades: Map<String, String>) {
        val taskRef = db.collection("tasks").document(taskId)
        val batch = db.batch()

        grades.forEach { (studentUid, gradeText) ->
            val gradeInt = gradeText.trim().toIntOrNull()
            val stRef = taskRef.collection("students").document(studentUid)

            val update = mutableMapOf<String, Any?>(
                "grade" to gradeInt
            )
            if (gradeInt != null) update["gradedAt"] = FieldValue.serverTimestamp()

            batch.update(stRef, update)
        }

        batch.commit().await()
    }

    suspend fun deleteTask(taskId: String) {
        val taskRef = db.collection("tasks").document(taskId)

        val studentsSnap = taskRef.collection("students").get().await()
        val batch = db.batch()

        studentsSnap.documents.forEach { d ->
            batch.delete(taskRef.collection("students").document(d.id))
        }

        batch.delete(taskRef)
        batch.commit().await()
    }
}
