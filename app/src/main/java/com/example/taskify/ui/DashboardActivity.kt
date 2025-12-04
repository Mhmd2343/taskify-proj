package com.example.taskify.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskify.viewmodel.AuthViewModel
import com.example.taskify.ui.ui.theme.TaskifyTheme

class DashboardActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskifyTheme {
                DashboardScreen(
                    onLogout = {
                        viewModel.signOut()
                        finish()   // go back to login
                    },
                    email = viewModel.currentUserEmail()
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(onLogout: () -> Unit, email: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, ${email ?: "User"}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}
