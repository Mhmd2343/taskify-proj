package com.example.taskify.ui.admin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminDashboardScreen(
    onCreateTeacher: () -> Unit,
    onTeacherList: () -> Unit,
    onStudentGrades: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hello Admin 👋", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))

        Button(
            onClick = onCreateTeacher,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Add Teacher")
        }

        OutlinedButton(
            onClick = onTeacherList,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Teacher List")
        }

        OutlinedButton(
            onClick = onStudentGrades,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Student Grades (Soon)")
        }
    }
}
