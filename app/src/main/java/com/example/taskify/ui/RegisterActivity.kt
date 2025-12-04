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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskify.UserStorage

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegisterScreen(
                onRegisterSuccess = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    fun validateRegister(): Boolean {

        if (!email.contains("@") || !email.contains(".com")) {
            errorMsg = "Please enter a valid email format."
            return false
        }

        if (password.length < 6) {
            errorMsg = "Password must be at least 6 characters long."
            return false
        }
        if (!password.any { it.isUpperCase() }) {
            errorMsg = "Password must include at least one uppercase letter."
            return false
        }
        if (!password.any { it.isDigit() }) {
            errorMsg = "Password must include at least one number."
            return false
        }
        if (!password.any { "!@#$%^&*()_+=-".contains(it) }) {
            errorMsg = "Password must include at least one special character."
            return false
        }

        UserStorage.saveUser(context, email, password)
        return true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Register", fontSize = 32.sp)

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
                    if (validateRegister()) onRegisterSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create account", fontSize = 22.sp)
            }
        }
    }
}
