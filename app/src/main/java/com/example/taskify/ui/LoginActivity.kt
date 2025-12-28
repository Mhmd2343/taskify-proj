package com.example.taskify.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Wrap with MaterialTheme
            MaterialTheme {
                LoginScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(
                    onClick = { showPassword = !showPassword }
                ) {
                    Icon(
                        imageVector = if (showPassword) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (showPassword) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                performLogin(context, email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

//        // ADD THIS TEST BUTTON
//        Spacer(modifier = Modifier.height(12.dp))
//        Button(
//            onClick = {
//                // Direct test navigation to Dashboard
//                println("DEBUG: Testing direct navigation to Dashboard")
//                val intent = Intent(context, DashboardActivity::class.java)
//                context.startActivity(intent)
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Green
//            )
//        ) {
//            Text("TEST: Go to Dashboard")
//        }

        Spacer(modifier = Modifier.height(12.dp))

        // Register Button
        TextButton(
            onClick = {
                context.startActivity(Intent(context, RegisterActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? Register")
        }
    }
}

private fun performLogin(context: android.content.Context, email: String, password: String) {
    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
        return
    }

    val prefs = context.getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
    val savedEmail = prefs.getString("email", "") ?: ""
    val savedPassword = prefs.getString("password", "") ?: ""

    // Debug logs
    println("DEBUG Login Check:")
    println("  Entered: $email, Saved: $savedEmail")
    println("  Pass Entered: $password, Pass Saved: $savedPassword")

    if (email == savedEmail && password == savedPassword) {
        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

        // THIS LINE SHOULD NAVIGATE TO DashboardActivity:
        val intent = Intent(context, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Wrong email or password", Toast.LENGTH_SHORT).show()
    }
}