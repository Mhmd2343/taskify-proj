package com.example.taskify.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

class CreateTeacherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { CreateTeacherScreen() } }
    }
}

@Composable
fun CreateTeacherScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storage = remember { AdminStorage(context) }

    val subjects = remember {
        listOf(
            "Math", "English", "Arabic", "French", "History", "Geography",
            "Physics", "Chemistry", "Biology", "Computer Science", "Civics", "Philosophy"
        )
    }

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var selected by remember { mutableStateOf(setOf<String>()) }
    var loading by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var createdEmail by remember { mutableStateOf("") }
    var createdPass by remember { mutableStateOf("") }
    var createdId by remember { mutableStateOf(0L) }

    fun sanitizeFirstName(s: String): String {
        val cleaned = s.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        return if (cleaned.isBlank()) "teacher" else cleaned
    }

    fun genPassword(len: Int = 10): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#\$%&*_-"
        val rnd = SecureRandom()
        return buildString {
            repeat(len) { append(chars[rnd.nextInt(chars.length)]) }
        }
    }

    if (showDialog) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        fun copy(label: String, text: String) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
        }

        fun share(text: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Share credentials via"))
        }

        val credentialsText = """
    Teacher Account Created

    Email: $createdEmail
    Password: $createdPass
  """.trimIndent()

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Teacher Created") },
            text = {
                Column {
                    Text("ID: $createdId")
                    Spacer(Modifier.height(8.dp))

                    Text("Email:")
                    Text(createdEmail)

                    Spacer(Modifier.height(8.dp))

                    Text("Password:")
                    Text(createdPass)

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { copy("Email", createdEmail) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Copy Email") }

                        OutlinedButton(
                            onClick = { copy("Password", createdPass) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Copy Password") }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { copy("Credentials", credentialsText) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Both")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { share(credentialsText) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share")
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("⚠️ Save these now. The password is shown once.")
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Create Teacher", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(18.dp))
        Text("Teacher of (select 1+ subjects)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        subjects.forEach { subj ->
            val checked = selected.contains(subj)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selected = if (checked) selected - subj else selected + subj
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        selected = if (checked) selected - subj else selected + subj
                    }
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(subj)
                    val assignedId = storage.getAssignedTeacherIdForSubject(subj)
                    if (assignedId != null) {
                        Text("Currently assigned to teacher ID: $assignedId", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Divider()
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                val fn = firstName.trim()
                val ln = lastName.trim()
                val mn = middleName.trim().ifBlank { "" }
                val subjectsChosen = selected.toList()

                if (fn.isBlank() || ln.isBlank()) {
                    Toast.makeText(context, "First and last name are required", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (subjectsChosen.isEmpty()) {
                    Toast.makeText(context, "Select at least 1 subject", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                scope.launch {
                    try {
                        val teacherId = storage.nextTeacherId()
                        val email = "${sanitizeFirstName(fn)}$teacherId@tc.edu.lb"
                        val pass = genPassword(10)

                        withContext(Dispatchers.IO) {
                            FirebaseSecondaryAuth.createUserWithoutAffectingAdmin(context, email, pass)
                            storage.saveTeacher(
                                teacherId = teacherId,
                                firstName = fn,
                                middleName = mn.ifBlank { null },
                                lastName = ln,
                                email = email,
                                subjectNames = subjectsChosen
                            )
                            storage.assignSubjectsToTeacher(teacherId, subjectsChosen)
                        }

                        createdId = teacherId
                        createdEmail = email
                        createdPass = pass
                        showDialog = true

                        firstName = ""
                        middleName = ""
                        lastName = ""
                        selected = emptySet()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Failed to create teacher", Toast.LENGTH_LONG).show()
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Creating..." else "Create Teacher")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { (context as? ComponentActivity)?.finish() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Back")
        }
    }
}
