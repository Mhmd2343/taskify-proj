package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskify.viewmodel.TeacherHomeViewModel

@Composable
fun TeacherHomeScreen(
    vm: TeacherHomeViewModel,
    onShowStudents: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadTeacher()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            return
        }

        if (state.error.isNotBlank()) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
            return
        }

        Text(
            text = state.greeting.ifBlank { "Hello Teacher" },
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onShowStudents,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show My Students")
        }
    }
}
