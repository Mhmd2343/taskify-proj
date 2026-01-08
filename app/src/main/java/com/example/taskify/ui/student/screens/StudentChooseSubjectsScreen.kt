package com.example.taskify.ui.student.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.student.viewmodel.StudentSubjectsViewModel

@Composable
fun StudentChooseSubjectsScreen(
    onContinue: () -> Unit,
    vm: StudentSubjectsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    val allCount = state.allSubjects.size
    val selectedCount = state.selectedSubjects.size
    val allSelected = allCount > 0 && selectedCount == allCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Choose your subjects", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text("Select the subjects you will participate in this semester.", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = allSelected,
                onCheckedChange = { vm.toggleSelectAll() }
            )
            Spacer(Modifier.width(8.dp))
            Text("Select All")
        }

        Spacer(Modifier.height(8.dp))

        if (state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (state.loading && state.allSubjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(state.allSubjects, key = { it.subjectName }) { item ->
                        val checked = state.selectedSubjects.contains(item.subjectName)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            onClick = { vm.toggleSubject(item.subjectName) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { vm.toggleSubject(item.subjectName) }
                                )

                                Spacer(Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.subjectName, style = MaterialTheme.typography.titleMedium)
                                    val t = item.teacherName?.trim().orEmpty()
                                    if (t.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = t,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.saveSelection(onDone = onContinue) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading && state.selectedSubjects.isNotEmpty()
        ) {
            Text(if (state.loading) "Saving..." else "Continue")
        }
    }
}
