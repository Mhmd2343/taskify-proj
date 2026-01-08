package com.example.taskify.ui.student.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.student.data.StudentAssignmentRow
import com.example.taskify.ui.student.viewmodel.StudentSubjectFeedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentSubjectFeedScreen(
    subjectName: String,
    onOpenTask: (String) -> Unit,
    vm: StudentSubjectFeedViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(subjectName) {
        vm.load(subjectName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = subjectName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        when {
            ui.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading tasks...")
                }
            }

            ui.error != null -> {
                Text(
                    text = ui.error ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            ui.tasks.isEmpty() -> {
                Text(
                    text = "No activity yet.\nYour teacher hasn’t posted tasks for this subject.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ui.tasks) { task ->
                        StudentTaskCard(
                            task = task,
                            onClick = {
                                vm.markOpened(task.taskId)
                                onOpenTask(task.taskId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentTaskCard(
    task: StudentAssignmentRow,
    onClick: () -> Unit
) {
    val fmt = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    val createdText = fmt.format(Date(task.createdAt))
    val dueText = task.dueAt?.let { fmt.format(Date(it)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "By: ${task.teacherName.ifBlank { "Teacher" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = task.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Priority: ${task.priority.name} • Points: ${task.points}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Created: $createdText",
                style = MaterialTheme.typography.bodySmall
            )

            if (dueText != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Due: $dueText",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            val gradeText = task.grade?.toString() ?: "-"
            Text(
                text = "Status: ${task.state.name} • Grade: $gradeText",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
