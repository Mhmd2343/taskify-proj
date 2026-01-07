package com.example.taskify.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeacherListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TeacherListScreen() } }
    }
}

@Composable
fun TeacherListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { AdminStorage(context) }

    var teachers by remember { mutableStateOf<List<TeacherRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                val firestoreSnap = db.collection("teacherProfiles").get().await()
                val firestoreList = firestoreSnap.documents.mapNotNull { d ->
                    val teacherId = d.getLong("teacherId") ?: return@mapNotNull null
                    val firstName = d.getString("firstName") ?: ""
                    val middleName = d.getString("middleName")
                    val lastName = d.getString("lastName") ?: ""
                    val email = d.getString("email") ?: ""
                    val subjects = (d.get("subjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    TeacherRecord(
                        uid = d.id,
                        teacherId = teacherId,
                        firstName = firstName,
                        middleName = middleName,
                        lastName = lastName,
                        email = email,
                        subjectNames = subjects
                    )
                }

                val localList = storage.getAllTeachers().map {
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

                val merged = (firestoreList + localList)
                    .distinctBy { it.email.trim().lowercase() }
                    .sortedWith(compareBy({ it.lastName.lowercase() }, { it.firstName.lowercase() }))

                teachers = merged
            } catch (e: Exception) {
                error = e.message ?: "Failed to load teachers"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Teachers", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { load() }, enabled = !loading) { Text("Refresh") }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        val err = error
        if (err != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(err)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { load() }) { Text("Try Again") }
                }
            }
            return
        }

        if (teachers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No teachers yet.")
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(teachers) { t ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(
                                Intent(context, TeacherDetailsActivity::class.java).apply {
                                    if (t.uid.isNotBlank()) putExtra("teacherUid", t.uid)
                                    putExtra("teacherId", t.teacherId)
                                }
                            )
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "${t.firstName} ${t.middleName?.let { "$it " } ?: ""}${t.lastName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(t.email, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("Subjects: ${t.subjectNames.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                        if (t.uid.isBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Source: Local (old data)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
