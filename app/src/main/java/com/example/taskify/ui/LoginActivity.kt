package com.example.taskify.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskify.ui.DashboardActivity
import com.example.taskify.UserStorage

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen(
                onLoginSuccess = {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                },
                onNavigateToRegister = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    fun validateLogin(): Boolean {
        val storedUser = UserStorage.getUser(context)

        if (storedUser == null) {
            errorMsg = "No account found. Please register first."
            return false
        }

        if (email != storedUser.email || password != storedUser.password) {
            errorMsg = "Invalid email or password. Please try again."
            return false
        }

        return true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", fontSize = 32.sp)

            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = 20.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontSize = 20.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    if (validateLogin()) onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", fontSize = 22.sp)
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = { onNavigateToRegister() }) {
                Text("Don't have an account? Register", fontSize = 18.sp)
            }
        }
    }
}
