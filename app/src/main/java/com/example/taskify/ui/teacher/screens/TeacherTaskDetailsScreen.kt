package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.teacher.SubmissionState
import com.example.taskify.viewmodel.TeacherTaskDetailsViewModel
import com.example.taskify.viewmodel.TeacherTaskDetailsViewModelFactory

@Composable
fun TeacherTaskDetailsScreen(
    taskId: String,
    onBack: () -> Unit
) {
    val vm: TeacherTaskDetailsViewModel = viewModel(factory = TeacherTaskDetailsViewModelFactory(taskId))
    val state by vm.ui.collectAsState()

    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) { vm.load() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = { Text("Are you sure you want to delete this task? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    confirmDelete = false
                    vm.deleteTask(onDone = onBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Task Details", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { confirmDelete = true }, enabled = !state.loading && !state.deleting) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            return
        }

        if (state.error.isNotBlank()) {
            Text(state.error)
            return
        }

        state.task?.let { t ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t.title, style = MaterialTheme.typography.titleLarge)
                    Text(t.subject, style = MaterialTheme.typography.bodyMedium)
                    Text("Over ${t.points} points", style = MaterialTheme.typography.bodyMedium)
                    Text("Priority: ${t.priority}", style = MaterialTheme.typography.bodyMedium)
                    if (t.createdAtText.isNotBlank()) Text("Created: ${t.createdAtText}", style = MaterialTheme.typography.bodySmall)
                    Divider()
                    Text(t.content, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (t.attachments.isNotEmpty()) {
                Text("Attachments", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    t.attachments.forEach { a ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${a.type}: ${a.label}", style = MaterialTheme.typography.titleSmall)
                                if (a.value.isNotBlank()) Text(a.value, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Registered Students", style = MaterialTheme.typography.headlineSmall)

        val points = state.task?.points ?: 0

        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text("Full name", modifier = Modifier.weight(0.34f))
            Text("Email", modifier = Modifier.weight(0.34f))
            Text("State", modifier = Modifier.weight(0.16f))
            Text("Grade", modifier = Modifier.weight(0.16f))
        }

        Divider()

        state.students.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.fullName, modifier = Modifier.weight(0.34f))
                Text(s.email, modifier = Modifier.weight(0.34f))

                Box(modifier = Modifier.weight(0.16f), contentAlignment = Alignment.CenterStart) {
                    when (s.state) {
                        SubmissionState.DELIVERED -> Icon(Icons.Filled.Schedule, contentDescription = null)
                        SubmissionState.OPENED -> Icon(Icons.Filled.RemoveRedEye, contentDescription = null)
                        SubmissionState.SUBMITTED -> Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }

                Row(
                    modifier = Modifier.weight(0.16f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = state.grades[s.uid].orEmpty(),
                        onValueChange = { vm.setGrade(s.uid, it) },
                        modifier = Modifier.width(70.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !state.savingGrades
                    )
                    Text("/$points")
                }
            }
            Divider()
        }

        Button(
            onClick = { vm.saveGrades() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.savingGrades && state.students.isNotEmpty()
        ) {
            if (state.savingGrades) {
                CircularProgressIndicator()
            } else {
                Text("Save Grades")
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
