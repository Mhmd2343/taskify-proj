package com.example.taskify.ui.teacher.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.viewmodel.TeacherProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TeacherProfileScreen(
    onLogout: () -> Unit
) {
    val vm: TeacherProfileViewModel = viewModel()
    val state by vm.ui.collectAsState()

    val auth = remember { FirebaseAuth.getInstance() }
    val email = remember { auth.currentUser?.email.orEmpty() }

    LaunchedEffect(Unit) { vm.load(email) }

    val scroll = rememberScrollState()

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConf by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeletePass by remember { mutableStateOf(false) }

    val frOld = remember { FocusRequester() }
    val frNew = remember { FocusRequester() }
    val frConf = remember { FocusRequester() }

    val passMatch = remember(state.newPass, state.confirmPass) {
        state.confirmPass.isNotBlank() && state.newPass == state.confirmPass
    }
    val passMismatch = remember(state.newPass, state.confirmPass) {
        state.confirmPass.isNotBlank() && state.newPass != state.confirmPass
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Profile", style = MaterialTheme.typography.headlineLarge)

            IconButton(onClick = {
                auth.signOut()
            }) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
            }
        }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            return
        }

        if (state.error.isNotBlank()) {
            Text(state.error)
        }
        if (state.success.isNotBlank()) {
            Text(state.success)
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Username (read-only)", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.username,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Personal info", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = vm::setFirstName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("First name *") },
                    singleLine = true,
                    isError = state.firstName.trim().isBlank()
                )

                OutlinedTextField(
                    value = state.middleName,
                    onValueChange = vm::setMiddleName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Middle name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = vm::setLastName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Last name *") },
                    singleLine = true,
                    isError = state.lastName.trim().isBlank()
                )

                Button(
                    onClick = vm::saveProfile,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.saving) CircularProgressIndicator() else Text("Save changes")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Change password", style = MaterialTheme.typography.titleMedium)

                val oldReq = state.oldPass.isBlank()
                val newReq = state.newPass.isBlank()
                val confReq = state.confirmPass.isBlank()

                OutlinedTextField(
                    value = state.oldPass,
                    onValueChange = vm::setOldPass,
                    modifier = Modifier.fillMaxWidth().focusRequester(frOld),
                    label = { Text("Old password *") },
                    singleLine = true,
                    isError = oldReq && (state.newPass.isNotBlank() || state.confirmPass.isNotBlank()),
                    visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOld = !showOld }) {
                            Icon(if (showOld) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { frNew.requestFocus() })
                )

                OutlinedTextField(
                    value = state.newPass,
                    onValueChange = vm::setNewPass,
                    modifier = Modifier.fillMaxWidth().focusRequester(frNew),
                    label = { Text("New password *") },
                    singleLine = true,
                    isError = (state.newPass.isNotBlank() && state.newPass.length < 6) || (newReq && state.confirmPass.isNotBlank()),
                    supportingText = {
                        when {
                            state.newPass.isNotBlank() && state.newPass.length < 6 -> Text("Minimum 6 characters")
                            else -> {}
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { frConf.requestFocus() })
                )

                OutlinedTextField(
                    value = state.confirmPass,
                    onValueChange = vm::setConfirmPass,
                    modifier = Modifier.fillMaxWidth().focusRequester(frConf),
                    label = { Text("Confirm new password *") },
                    singleLine = true,
                    isError = passMismatch || (confReq && state.newPass.isNotBlank()),
                    supportingText = {
                        when {
                            passMatch -> Text("Passwords match ✅")
                            passMismatch -> Text("Passwords do not match ❌")
                            else -> {}
                        }
                    },
                    visualTransformation = if (showConf) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConf = !showConf }) {
                            Icon(if (showConf) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { vm.changePassword() })
                )

                Button(
                    onClick = vm::changePassword,
                    enabled = !state.changingPass,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.changingPass) CircularProgressIndicator() else Text("Change password")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium)

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !state.deletingAcc,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.deletingAcc) CircularProgressIndicator() else Text("Disable account (Delete)")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                vm.setDeletePassword("")
            },
            title = { Text("Delete account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Are you sure you want to permanently delete your account? This cannot be undone.")

                    OutlinedTextField(
                        value = state.deletePassword,
                        onValueChange = vm::setDeletePassword,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password required") },
                        singleLine = true,
                        visualTransformation = if (showDeletePass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showDeletePass = !showDeletePass }) {
                                Icon(if (showDeletePass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.deleteAccount(
                        onDone = {
                            auth.signOut()
                            showDeleteDialog = false
                        }
                    )
                }) { Text("Yes, delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showDeleteDialog = false
                    vm.setDeletePassword("")
                }) { Text("Cancel") }
            }
        )
    }
}
