package com.example.taskify.ui.student.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.material3.HorizontalDivider


private const val THRESHOLD_10 = 70.0
private const val THRESHOLD_15 = 75.0
private const val THRESHOLD_20 = 80.0

@Composable
fun StudentAidCalculatorScreen() {
    val context = LocalContext.current

    var currentAvgText by remember { mutableStateOf("") }
    var remainingWeightText by remember { mutableStateOf("") }

    val currentAvg = currentAvgText.toDoubleOrNull()
    val remainingWeight = remainingWeightText.toDoubleOrNull()
    val w = (remainingWeight ?: 0.0) / 100.0

    val need10 = computeNeeded(currentAvg, w, THRESHOLD_10)
    val need15 = computeNeeded(currentAvg, w, THRESHOLD_15)
    val need20 = computeNeeded(currentAvg, w, THRESHOLD_20)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aide Social Calculator", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = currentAvgText,
                    onValueChange = { currentAvgText = it },
                    label = { Text("Current average (0-100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = remainingWeightText,
                    onValueChange = { remainingWeightText = it },
                    label = { Text("Remaining weight % (ex: 40 for final exam)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                HorizontalDivider()


                Text("Result", style = MaterialTheme.typography.titleMedium)

                AidLine("To get 10% financial aid", THRESHOLD_10, need10)
                AidLine("To get 15% financial aid", THRESHOLD_15, need15)
                AidLine("To get 20% financial aid", THRESHOLD_20, need20)

                Spacer(Modifier.height(2.dp))

                if (currentAvg == null || remainingWeight == null) {
                    Text(
                        "Enter your current average and remaining weight to calculate.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (remainingWeight <= 0.0) {
                    Text(
                        "Remaining weight must be > 0%.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text("External Support", style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("USAID Support", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Financial support programs that may not depend on your grades. Check eligibility and apply online.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.usaid.gov/"))
                        )
                    }
                ) {
                    Text("Press to participate")
                }
            }
        }
    }
}

@Composable
private fun AidLine(title: String, target: Double, needed: NeededResult) {
    Text(
        text = when (needed.type) {
            NeededType.OK -> "$title: Already eligible (≥ ${target.format0()})"
            NeededType.NEED_SCORE -> "$title: Need ${(needed.value ?: 0.0).format0()} on remaining"
            NeededType.IMPOSSIBLE -> "$title: Impossible (need > 100)"
            NeededType.INVALID -> "$title: --"
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun computeNeeded(currentAvg: Double?, w: Double, target: Double): NeededResult {
    if (currentAvg == null || w <= 0.0) return NeededResult(NeededType.INVALID, null)

    val cw = 1.0 - w
    val currentContribution = currentAvg * cw
    val needed = (target - currentContribution) / w

    return when {
        needed <= 0.0 -> NeededResult(NeededType.OK, null)
        needed > 100.0 -> NeededResult(NeededType.IMPOSSIBLE, null)
        else -> NeededResult(NeededType.NEED_SCORE, needed.coerceIn(0.0, 100.0))
    }
}

private enum class NeededType { INVALID, OK, NEED_SCORE, IMPOSSIBLE }
private data class NeededResult(val type: NeededType, val value: Double?)

private fun Double.format0(): String = this.roundToInt().toString()
