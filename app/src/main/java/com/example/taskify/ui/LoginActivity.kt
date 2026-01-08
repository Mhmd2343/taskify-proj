package com.example.taskify.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.    compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.taskify.auth.GoogleAuthUiClient
import com.example.taskify.ui.admin.AdminMainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.taskify.ui.teacher.TeacherMainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.taskify.ui.student.StudentMainActivity
import com.example.taskify.ui.student.StudentBootstrapper





private const val ADMIN_EMAIL = "admin@ad.edu.lb"

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LoginScreen() } }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val googleAuthUiClient = remember { GoogleAuthUiClient(activity.applicationContext) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    fun goNext(normalizedEmail: String) {
        if (normalizedEmail == ADMIN_EMAIL || normalizedEmail.endsWith("@ad.edu.lb")) {
            val i = Intent(context, AdminMainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(i)
            return
        }

        if (normalizedEmail.endsWith("@tc.edu.lb")) {
            val i = Intent(context, TeacherMainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(i)
            return
        }

        val i = Intent(context, StudentMainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(i)


        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(context, "Missing user session", Toast.LENGTH_LONG).show()
            return
        }

        loading = true
        scope.launch {
            try {
                val ok = StudentBootstrapper(auth, FirebaseFirestore.getInstance()).ensureStudentDocs()
                loading = false

                if (!ok) {
                    Toast.makeText(context, "Failed to prepare student account", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val next = Intent(context, StudentMainActivity::class.java)
                next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(next)

            } catch (e: Exception) {
                loading = false
                Toast.makeText(context, e.message ?: "Student setup failed", Toast.LENGTH_LONG).show()
            }
        }
        return

    }






    val oneTapLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult

        loading = true
        scope.launch {
            val ok = googleAuthUiClient.signInWithIntent(data)
            loading = false
            if (ok) {
                val userEmail = auth.currentUser?.email?.trim()?.lowercase().orEmpty()
                Toast.makeText(context, "Google login successful!", Toast.LENGTH_SHORT).show()
                goNext(userEmail)
            } else {
                Toast.makeText(context, "Google login failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val normalizedEmail = email.trim().lowercase()
                val rawPassword = password

                if (normalizedEmail.isBlank() || rawPassword.isBlank()) {
                    Toast.makeText(context, "Enter email and password", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true
                auth.signInWithEmailAndPassword(normalizedEmail, rawPassword)
                    .addOnCompleteListener { t ->
                        loading = false
                        if (t.isSuccessful) {
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            goNext(normalizedEmail)
                        } else {
                            Toast.makeText(context, t.exception?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Loading..." else "Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                if (loading) return@OutlinedButton
                scope.launch {
                    googleAuthUiClient.signOut()
                    val intentSender = googleAuthUiClient.signIn()
                    if (intentSender == null) {
                        Toast.makeText(context, "Google sign-in not available. Check google-services.json + SHA-1.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    oneTapLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Continue with Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { context.startActivity(Intent(context, RegisterActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Don't have an account? Register")
        }
    }
}
