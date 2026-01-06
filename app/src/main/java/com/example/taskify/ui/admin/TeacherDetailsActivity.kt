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

class TeacherDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teacherId = intent.getLongExtra("teacherId", -1L)

        setContent {
            MaterialTheme {
                TeacherDetailsScreen(teacherId = teacherId)
            }
        }
    }
}

@Composable
fun TeacherDetailsScreen(teacherId: Long) {
    val context = LocalContext.current
    val storage = remember { AdminStorage(context) }

    var teacher by remember { mutableStateOf<TeacherRecord?>(null) }

    LaunchedEffect(teacherId) {
        teacher = storage.getTeacherById(teacherId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Teacher Info", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        val t = teacher
        if (t == null) {
            Text("Teacher not found.")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                Text("Back")
            }
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
