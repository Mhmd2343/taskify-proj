package com.example.taskify.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RegisterScreen()
            }
        }
    }
}

/* ---------------- MAIN SCREEN ---------------- */

@Composable
fun RegisterScreen() {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val countries = remember {
        CountryUtils.loadCountries(context)
    }

    var selectedCountry by remember {
        mutableStateOf(countries.first())
    }

    var phoneNumber by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    val passwordStrength = getPasswordStrength(password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        InputField("First Name *", firstName) { firstName = it }
        InputField("Middle Name", middleName) { middleName = it }
        InputField("Last Name *", lastName) { lastName = it }

        Spacer(modifier = Modifier.height(8.dp))

        // PHONE ROW
        Row(verticalAlignment = Alignment.CenterVertically) {
            CountryPicker(
                selectedCountry = selectedCountry,
                countries = countries,
                onCountrySelected = { selectedCountry = it },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it.filter { ch -> ch.isDigit() }
                },
                label = { Text("Phone") },
                modifier = Modifier.weight(2f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        InputField("Email", email, KeyboardType.Email) { email = it }

        Spacer(modifier = Modifier.height(8.dp))

        // PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.trim() },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null
                    )
                }
            }
        )

        PasswordStrengthLabel(passwordStrength)

        TextButton(onClick = {
            password = generateStrongPassword()
            confirmPassword = password
        }) {
            Text("Generate strong password")
        }

        // CONFIRM PASSWORD
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it.trim() },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val fullPhone = selectedCountry.phoneCode + phoneNumber

                if (
                    !validateAllInputs(
                        firstName.trim(),
                        lastName.trim(),
                        email.trim(),
                        fullPhone,
                        password,
                        confirmPassword,
                        passwordStrength
                    ) { errorMessage = it }
                ) return@Button

                saveRegistration(
                    context,
                    firstName.trim(),
                    middleName.trim(),
                    lastName.trim(),
                    fullPhone,
                    email.trim(),
                    password
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

/* ---------------- REUSABLE INPUT ---------------- */

@Composable
fun InputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.trimStart()) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

/* ---------------- COUNTRY PICKER ---------------- */


@Composable
fun CountryPicker(
    selectedCountry: PhoneCountry,
    onCountrySelected: (PhoneCountry) -> Unit,
    modifier: Modifier = Modifier,
    countries: List<PhoneCountry>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("${selectedCountry.flag} ${selectedCountry.phoneCode}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Text("${country.flag} ${country.name} (${country.phoneCode})")
                    },
                    onClick = {
                        onCountrySelected(country)
                        expanded = false
                    }
                )
            }
        }
    }
}



/* ---------------- PASSWORD ---------------- */

enum class PasswordStrength(val label: String, val color: Color) {
    EMPTY("", Color.Transparent),
    WEAK("✗ Weak password", Color.Red),
    MEDIUM("– Medium password", Color(0xFFFF9800)),
    STRONG("✓ Strong password", Color.Green)
}

@Composable
fun PasswordStrengthLabel(strength: PasswordStrength) {
    if (strength == PasswordStrength.EMPTY) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (strength == PasswordStrength.STRONG) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.Green)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(strength.label, color = strength.color)
    }
}

fun getPasswordStrength(password: String): PasswordStrength {
    val score = listOf(
        password.length >= 8,
        password.any { it.isUpperCase() },
        password.any { it.isLowerCase() },
        password.any { it.isDigit() },
        password.any { "!@#\$%^&*+=".contains(it) }
    ).count { it }

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score <= 4 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

fun generateStrongPassword(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*+="
    return (1..12).map { chars.random() }.joinToString("")
}

/* ---------------- VALIDATION + SAVE ---------------- */

fun validateAllInputs(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    password: String,
    confirmPassword: String,
    strength: PasswordStrength,
    onError: (String) -> Unit
): Boolean {

    if (firstName.isBlank() || lastName.isBlank()) {
        onError("First and last name are required")
        return false
    }

    if (email.isBlank()) {
        onError("Email is required")
        return false
    }

    if (!Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$").matches(email)) {
        onError("Invalid email format")
        return false
    }

    if (!Regex("^\\+\\d+$").matches(phone)) {
        onError("Invalid phone number")
        return false
    }

    if (strength != PasswordStrength.STRONG) {
        onError("Password must be strong")
        return false
    }

    if (password != confirmPassword) {
        onError("Passwords do not match")
        return false
    }

    onError("")
    return true
}


fun saveRegistration(
    context: Context,
    firstName: String,
    middleName: String,
    lastName: String,
    phone: String,
    email: String,
    password: String
) {
    val prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    with(prefs.edit()) {
        putString("firstName", firstName)
        putString("middleName", middleName)
        putString("lastName", lastName)
        putString("phone", phone)
        putString("email", email)
        putString("password", password)
        apply()
    }

    context.startActivity(Intent(context, LoginActivity::class.java))
}
