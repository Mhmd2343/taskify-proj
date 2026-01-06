package com.example.taskify.ui.admin.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminProfileScreen(
    onPasswordUpdated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    val newFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    fun validate(): String? {
        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) return "Fill all fields"
        if (newPass.length < 6) return "New password must be at least 6 characters"
        if (newPass != confirmPass) return "Passwords do not match"
        if (user == null) return "Not logged in"
        if (user.email.isNullOrBlank()) return "No email on account"
        return null
    }

    fun submit() {
        val msg = validate()
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            return
        }

        loading = true

        scope.launch {
            try {
                val email = user!!.email!!
                val cred = EmailAuthProvider.getCredential(email, oldPass)

                user.reauthenticate(cred).await()
                user.updatePassword(newPass).await()

                Toast.makeText(context, "Password updated successfully ✅", Toast.LENGTH_SHORT).show()

                oldPass = ""
                newPass = ""
                confirmPass = ""

                onPasswordUpdated()
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Failed to update password", Toast.LENGTH_LONG).show()
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Admin Profile", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(6.dp))

        Text("Change Password", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = oldPass,
            onValueChange = { oldPass = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Old Password") },
            singleLine = true,
            visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showOld = !showOld }) {
                    Icon(if (showOld) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { newFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(newFocus),
            label = { Text("New Password") },
            singleLine = true,
            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showNew = !showNew }) {
                    Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { confirmFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmFocus),
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            )
        )

        Button(
            onClick = { submit() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Update Password")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (user == null) {
            Toast.makeText(context, "Admin not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
