package com.example.taskify.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeacherDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teacherUid = intent.getStringExtra("teacherUid") ?: ""
        val teacherId = intent.getLongExtra("teacherId", -1L)

        setContent {
            MaterialTheme {
                TeacherDetailsScreen(teacherUid = teacherUid, teacherId = teacherId)
            }
        }
    }
}

@Composable
fun TeacherDetailsScreen(teacherUid: String, teacherId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { AdminStorage(context) }

    var teacher by remember { mutableStateOf<TeacherRecord?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                if (teacherUid.isNotBlank()) {
                    val d = db.collection("teacherProfiles").document(teacherUid).get().await()
                    if (!d.exists()) {
                        teacher = null
                        return@launch
                    }

                    val tId = d.getLong("teacherId") ?: -1L
                    val firstName = d.getString("firstName") ?: ""
                    val middleName = d.getString("middleName")
                    val lastName = d.getString("lastName") ?: ""
                    val email = d.getString("email") ?: ""
                    val subjects = (d.get("subjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    teacher = TeacherRecord(
                        uid = teacherUid,
                        teacherId = tId,
                        firstName = firstName,
                        middleName = middleName,
                        lastName = lastName,
                        email = email,
                        subjectNames = subjects
                    )
                } else {
                    val local = storage.getTeacherById(teacherId)
                    teacher = local?.let {
                        TeacherRecord(
                            uid = "",
                            teacherId = it.teacherId,
                            firstName = it.firstName,
                            middleName = it.middleName,
                            lastName = it.lastName,
                            email = it.email,
                            subjectNames = it.subjectNames
                        )
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load teacher"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(teacherUid, teacherId) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Teacher Info", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { load() }, enabled = !loading) { Text("Refresh") }
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
            return
        }

        val err = error
        if (err != null) {
            Text(err)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { (context as? ComponentActivity)?.finish() }) { Text("Back") }
            return
        }

        val t = teacher
        if (t == null) {
            Text("Teacher not found.")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { (context as? ComponentActivity)?.finish() }) { Text("Back") }
            return
        }

        Text("ID: ${t.teacherId}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("First Name: ${t.firstName}")
        Spacer(Modifier.height(6.dp))
        Text("Middle Name: ${t.middleName ?: "-"}")
        Spacer(Modifier.height(6.dp))
        Text("Last Name: ${t.lastName}")
        Spacer(Modifier.height(6.dp))
        Text("Email: ${t.email}")
        Spacer(Modifier.height(10.dp))
        Text("Subjects: ${t.subjectNames.joinToString(", ")}")

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(t.email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.message ?: "Failed to send reset email", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Password (Send Email)")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { (context as? ComponentActivity)?.finish() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
