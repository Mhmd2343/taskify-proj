package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskify.viewmodel.TeacherHomeViewModel

@Composable
fun TeacherHomeScreen(vm: TeacherHomeViewModel) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadTeacher()
    }

    when {
        state.isLoading -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        state.error.isNotBlank() -> {
            Text(
                text = state.error,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        else -> {
            Text(
                text = state.greeting,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
