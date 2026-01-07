package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskify.ui.teacher.StudentRow
import com.example.taskify.ui.teacher.TeacherStudentsRepository
import com.example.taskify.viewmodel.TeacherHomeViewModel
import kotlinx.coroutines.launch

@Composable
fun TeacherStudentsScreen(vm: TeacherHomeViewModel) {
    val state by vm.uiState.collectAsState()
    val repo = remember { TeacherStudentsRepository() }
    val scope = rememberCoroutineScope()

    var students by remember { mutableStateOf<List<StudentRow>>(emptyList()) }
    var error by remember { mutableStateOf("") }

    val subject = state.subjects.firstOrNull().orEmpty()

    LaunchedEffect(subject) {
        if (subject.isBlank()) return@LaunchedEffect
        error = ""
        scope.launch {
            try {
                students = repo.getStudentsForSubject(subject)
            } catch (e: Exception) {
                students = emptyList()
                error = e.message ?: "Failed to load students"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (subject.isBlank()) "No subject assigned" else "Students for $subject",
            style = MaterialTheme.typography.titleLarge
        )

        if (error.isNotBlank()) {
            Text(error, modifier = Modifier.padding(top = 8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp)
        ) {
            items(students) { s ->
                Text("${s.fullName}  •  ${s.email}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
