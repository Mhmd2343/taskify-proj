// app/src/main/java/com/example/taskify/ui/RegisterActivity.kt
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.google.firebase.auth.FirebaseAuth
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException



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

@Composable
fun RegisterScreen() {
    val context = LocalContext.current

    val countries = remember { CountryUtils.loadCountries(context) }

    if (countries.isEmpty()) {
        Text("Failed to load countries")
        return
    }

    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var countryCodeText by remember { mutableStateOf(countries.first().phoneCode) }

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf("") }

    val passwordStrength = getPasswordStrength(password)

    fun validatePhoneLive(country: PhoneCountry, dialCodeText: String, nationalDigits: String): String {
        val normalizedCode = normalizeCountryCode(dialCodeText)
        if (normalizedCode.isBlank() || nationalDigits.isBlank()) return "Phone is required"

        val e164 = normalizedCode + nationalDigits.filter(Char::isDigit)

        val util = PhoneNumberUtil.getInstance()
        return try {
            val parsed = util.parse(e164, null)
            val isValid = util.isValidNumberForRegion(parsed, country.iso.uppercase())
            if (!isValid) "Invalid phone number for ${country.name}"
            else ""
        } catch (e: NumberParseException) {
            "Invalid phone number for ${country.name}"
        } catch (e: Exception) {
            "Invalid phone number"
        }
    }

    fun updatePhoneError() {
        phoneError = validatePhoneLive(selectedCountry, countryCodeText, phoneNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        InputField("First Name *", firstName) { firstName = it.trim() }
        InputField("Middle Name", middleName) { middleName = it.trim() }
        InputField("Last Name *", lastName) { lastName = it.trim() }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CountryCodePicker(
                selectedCountry = selectedCountry,
                countries = countries,
                codeText = countryCodeText,
                onCodeTextChange = {
                    countryCodeText = it
                    updatePhoneError()
                },
                onCountrySelected = {
                    selectedCountry = it
                    updatePhoneError()
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { newValue ->
                    val digits = newValue.filter { ch -> ch.isDigit() }
                    phoneNumber = digits
                    updatePhoneError()
                },
                label = { Text("Phone") },
                modifier = Modifier.weight(2f),
                singleLine = true,
                isError = phoneError.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        if (phoneError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phoneError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        InputField("Email *", email, KeyboardType.Email) { email = it.trim() }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = passwordStrength == PasswordStrength.STRONG && phoneError.isEmpty(),
            onClick = {
                updatePhoneError()
                if (phoneError.isNotEmpty()) return@Button

                val normalizedCode = normalizeCountryCode(countryCodeText)
                val fullPhone = normalizedCode + phoneNumber.trim()

                if (!validateAllInputs(
                        firstName,
                        lastName,
                        email,
                        fullPhone,
                        password,
                        confirmPassword,
                        passwordStrength
                    ) { errorMessage = it }
                ) return@Button

                saveRegistration(
                    context,
                    firstName,
                    middleName,
                    lastName,
                    fullPhone,
                    email,
                    password
                ) { msg ->
                    errorMessage = msg
                }
            }
        ) {
            Text("Register")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun CountryCodePicker(
    selectedCountry: PhoneCountry,
    countries: List<PhoneCountry>,
    codeText: String,
    onCodeTextChange: (String) -> Unit,
    onCountrySelected: (PhoneCountry) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    fun sanitize(input: String): String {
        val filtered = input.filter { it == '+' || it.isDigit() }
        return when {
            filtered.isEmpty() -> ""
            filtered.startsWith("+") -> "+" + filtered.drop(1).filter(Char::isDigit)
            else -> "+" + filtered.filter(Char::isDigit)
        }
    }

    fun cleaned(code: String): String = code.replace(" ", "").trim()

    fun findMatch(code: String): PhoneCountry? {
        val q = cleaned(code)
        if (q.isBlank()) return null
        return countries.firstOrNull { cleaned(it.phoneCode) == q }
    }

    val suggestions = remember(codeText, countries) {
        val q = cleaned(codeText)
        if (q.length < 2) emptyList()
        else countries
            .filter {
                cleaned(it.phoneCode).startsWith(q) ||
                        it.name.contains(q, ignoreCase = true)
            }
            .take(12)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = codeText,
            onValueChange = { newValue ->
                val v = sanitize(newValue)
                onCodeTextChange(v)
                val match = findMatch(v)
                if (match != null && match.iso != selectedCountry.iso) {
                    onCountrySelected(match)
                }
            },
            label = { Text("Code") },
            leadingIcon = { Text(selectedCountry.flag) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.KeyboardArrowDown, null)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val list = if (suggestions.isNotEmpty()) suggestions else countries.take(40)
            list.forEach { country ->
                DropdownMenuItem(
                    text = { Text("${country.flag} ${country.name} (${country.phoneCode})") },
                    onClick = {
                        onCountrySelected(country)
                        onCodeTextChange(cleaned(country.phoneCode))
                        expanded = false
                    }
                )
            }
        }
    }
}

fun normalizeCountryCode(input: String): String {
    val v = input.filter { it == '+' || it.isDigit() }
    if (v.isBlank()) return ""
    val digits = v.filter(Char::isDigit)
    return if (digits.isBlank()) "" else "+$digits"
}

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

    val normalizedEmail = email.trim().lowercase()

    if (firstName.isBlank() || lastName.isBlank()) {
        onError("First and last name are required")
        return false
    }

    if (normalizedEmail.isBlank()) {
        onError("Email is required")
        return false
    }

    if (!Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(normalizedEmail)) {
        onError("Invalid email format")
        return false
    }

    if (normalizedEmail.endsWith("@ad.edu.lb")) {
        onError("This email domain is reserved for admin accounts")
        return false
    }

    if (normalizedEmail.endsWith("@tc.edu.lb")) {
        onError("This email domain is reserved for teacher accounts")
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
    password: String,
    onError: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()

    auth.createUserWithEmailAndPassword(email.trim().lowercase(), password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("firstName", firstName)
                    putString("middleName", middleName)
                    putString("lastName", lastName)
                    putString("phone", phone)
                    putString("email", email.trim())
                    apply()
                }

                context.startActivity(Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                onError(task.exception?.message ?: "Registration failed")
            }
        }
}
