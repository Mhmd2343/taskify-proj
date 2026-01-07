package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.viewmodel.TeacherTasksListViewModel

@Composable
fun TeacherTasksListScreen(
    onCreateTask: () -> Unit,
    onOpenTask: (String) -> Unit
) {
    val vm: TeacherTasksListViewModel = viewModel()
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Tasks", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Create Task")
            }
        }

        Divider()

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            return
        }

        if (state.tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTask(task.taskId) }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(task.subject, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(6.dp))
                            Text("Over ${task.points} points")
                        }
                    }
                }
            }
        }
    }
}
