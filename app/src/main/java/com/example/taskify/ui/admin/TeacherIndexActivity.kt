package com.example.taskify.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeacherIndexActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TeacherIndexScreen() } }
    }
}

@Composable
fun TeacherIndexScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var subjectsRaw by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Link Existing Teacher (Auth → Firestore)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Teacher Email (must exist in Auth)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Middle Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = subjectsRaw,
            onValueChange = { subjectsRaw = it },
            label = { Text("Subjects (comma separated) *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val e = email.trim().lowercase()
                val fn = firstName.trim()
                val mn = middleName.trim()
                val ln = lastName.trim()
                val subjects = subjectsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }

                if (e.isBlank() || fn.isBlank() || ln.isBlank() || subjects.isEmpty()) {
                    Toast.makeText(context, "Fill email, first, last, subjects", Toast.LENGTH_LONG).show()
                    return@Button
                }

                loading = true
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            Tasks.await(
                                db.collection("teachersIndex").document(e).set(
                                    mapOf(
                                        "email" to e,
                                        "firstName" to fn,
                                        "middleName" to mn.ifBlank { null },
                                        "lastName" to ln,
                                        "subjects" to subjects
                                    )
                                )
                            )
                        }
                        Toast.makeText(context, "Saved teachersIndex/$e", Toast.LENGTH_LONG).show()
                        email = ""
                        firstName = ""
                        middleName = ""
                        lastName = ""
                        subjectsRaw = ""
                    } catch (ex: Exception) {
                        Toast.makeText(context, ex.message ?: "Failed", Toast.LENGTH_LONG).show()
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Saving..." else "Save Link")
        }
    }
}
