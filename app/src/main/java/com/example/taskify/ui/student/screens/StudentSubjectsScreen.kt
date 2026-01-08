package com.example.taskify.ui.student.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.student.FocusModeActivity
import com.example.taskify.ui.student.viewmodel.StudentSubjectsViewModel

@Composable
fun StudentSubjectsScreen(
    onOpenSubject: (subjectName: String) -> Unit,
    onOpenAidCalculator: () -> Unit,
    vm: StudentSubjectsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val mySubjects = state.allSubjects.filter { state.selectedSubjects.contains(it.subjectName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Subjects", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                context.startActivity(Intent(context, FocusModeActivity::class.java))
            }
        ) {
            Text("Focus Mode")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenAidCalculator
        ) {
            Text("Aide Social calculator")
        }

        Spacer(Modifier.height(12.dp))

        if (state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading && mySubjects.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                mySubjects.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No subjects selected yet.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(mySubjects, key = { it.subjectName }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                onClick = { onOpenSubject(item.subjectName) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Text(item.subjectName, style = MaterialTheme.typography.titleMedium)
                                    val t = item.teacherName?.trim().orEmpty()
                                    if (t.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Mr/Ms. $t will be assisting you",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic
                                        )
                                    } else {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "A teacher will be assigned soon.",
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
    }
}
