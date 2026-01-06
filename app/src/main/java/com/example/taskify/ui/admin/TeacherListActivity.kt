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

class TeacherListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TeacherListScreen() } }
    }
}

@Composable
fun TeacherListScreen() {
    val context = LocalContext.current
    val storage = remember { AdminStorage(context) }

    var teachers by remember { mutableStateOf<List<TeacherRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        teachers = storage.getAllTeachers().sortedBy { it.lastName.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Teachers", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))

        if (teachers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No teachers created yet.")
            }
        } else {
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
                            Text(
                                "Subjects: ${t.subjectNames.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
