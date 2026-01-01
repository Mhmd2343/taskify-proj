// app/src/main/java/com/example/taskify/ui/VerifyPhoneActivity.kt
package com.example.taskify.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private const val DEV_OTP = "123456"

class VerifyPhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra("phone") ?: ""

        setContent {
            MaterialTheme {
                VerifyPhoneScreen(phone = phone)
            }
        }
    }
}

@Composable
fun VerifyPhoneScreen(phone: String) {
    val context = LocalContext.current

    var otpInput by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val maskedPhone = remember(phone) { maskPhoneKeepLast3(phone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Confirm your phone", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Enter the verification code sent to $maskedPhone")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "DEV MODE: use $DEV_OTP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(18.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                infoMessage = "Code ready"
                errorMessage = ""
                Toast.makeText(context, "DEV OTP: $DEV_OTP", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Send Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = otpInput,
            onValueChange = { otpInput = it.filter(Char::isDigit).take(6) },
            label = { Text("Enter 6-digit code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                //                val otp = Random.nextInt(100000, 999999).toString()
//                prefs.edit().putString("otp_code", otp).apply()

                if (otpInput == DEV_OTP) {
                    Toast.makeText(context, "Verified!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                } else {
                    errorMessage = "Invalid code. Use $DEV_OTP"
                    infoMessage = ""
                }
            }
        ) {
            Text("Verify")
        }

        if (infoMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(infoMessage)
        }

        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun maskPhoneKeepLast3(phone: String): String {
    val trimmed = phone.trim()
    val digits = trimmed.filter(Char::isDigit)
    if (digits.length <= 3) return "***"

    val last3 = digits.takeLast(3)
    val maskedDigits = "*".repeat(digits.length - 3) + last3

    val hasPlus = trimmed.startsWith("+")
    return (if (hasPlus) "+" else "") + maskedDigits
}
