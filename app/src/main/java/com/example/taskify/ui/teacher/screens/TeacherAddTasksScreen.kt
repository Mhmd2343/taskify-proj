package com.example.taskify.ui.teacher.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.teacher.AttachmentType
import com.example.taskify.ui.teacher.SubmissionState
import com.example.taskify.ui.teacher.TaskPriority
import com.example.taskify.viewmodel.TeacherTasksViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAddTasksScreen(
    onBack: () -> Unit
) {
    val vm: TeacherTasksViewModel = viewModel()
    val state by vm.ui.collectAsState()

    val ctx = LocalContext.current
    val fmt = remember { SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault()) }

    var insertMenuOpen by remember { mutableStateOf(false) }

    var showUrlDialog by remember { mutableStateOf(false) }
    var urlValue by remember { mutableStateOf("") }

    var showTxtDialog by remember { mutableStateOf(false) }
    var txtTitle by remember { mutableStateOf("") }
    var txtBody by remember { mutableStateOf("") }

    fun pickDateTime(
        initialMillis: Long?,
        onPicked: (Long) -> Unit
    ) {
        val cal = Calendar.getInstance()
        if (initialMillis != null) cal.timeInMillis = initialMillis

        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                val cal2 = Calendar.getInstance()
                cal2.set(Calendar.YEAR, y)
                cal2.set(Calendar.MONTH, m)
                cal2.set(Calendar.DAY_OF_MONTH, d)

                val hInit = cal.get(Calendar.HOUR_OF_DAY)
                val minInit = cal.get(Calendar.MINUTE)

                TimePickerDialog(
                    ctx,
                    { _, hh, mm ->
                        cal2.set(Calendar.HOUR_OF_DAY, hh)
                        cal2.set(Calendar.MINUTE, mm)
                        cal2.set(Calendar.SECOND, 0)
                        cal2.set(Calendar.MILLISECOND, 0)
                        onPicked(cal2.timeInMillis)
                    },
                    hInit,
                    minInit,
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(Unit) { vm.load() }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Insert URL link") },
            text = {
                OutlinedTextField(
                    value = urlValue,
                    onValueChange = { urlValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val v = urlValue.trim()
                    if (v.isNotBlank()) vm.addAttachment(AttachmentType.URL, "URL link", v)
                    urlValue = ""
                    showUrlDialog = false
                }) { Text("Insert") }
            },
            dismissButton = {
                OutlinedButton(onClick = { urlValue = ""; showUrlDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showTxtDialog) {
        AlertDialog(
            onDismissRequest = { showTxtDialog = false },
            title = { Text("Insert TXT") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = txtTitle,
                        onValueChange = { txtTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = txtBody,
                        onValueChange = { txtBody = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Text") },
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val label = txtTitle.trim().ifBlank { "TXT" }
                    val body = txtBody.trim()
                    if (body.isNotBlank()) vm.addAttachment(AttachmentType.TXT, label, body)
                    txtTitle = ""
                    txtBody = ""
                    showTxtDialog = false
                }) { Text("Insert") }
            },
            dismissButton = {
                OutlinedButton(onClick = { txtTitle = ""; txtBody = ""; showTxtDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add Task", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Over:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.pointsText,
                    onValueChange = vm::setPointsText,
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Points") }
                )
                Spacer(Modifier.width(8.dp))
                Text("points", style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (state.error.isNotBlank()) Text(state.error)
        if (state.success.isNotBlank()) Text(state.success)

        if (state.subjects.isEmpty()) {
            Text("No subjects assigned yet.")
            return
        }

        var subjExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = subjExpanded,
            onExpandedChange = { subjExpanded = !subjExpanded }
        ) {
            OutlinedTextField(
                value = state.selectedSubject,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text("Subject") }
            )
            ExposedDropdownMenu(
                expanded = subjExpanded,
                onDismissRequest = { subjExpanded = false }
            ) {
                state.subjects.forEach { subj ->
                    DropdownMenuItem(
                        text = { Text(subj) },
                        onClick = { vm.setSubject(subj); subjExpanded = false }
                    )
                }
            }
        }

        var prExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = prExpanded,
            onExpandedChange = { prExpanded = !prExpanded }
        ) {
            OutlinedTextField(
                value = state.priority.name,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text("Priority") }
            )
            ExposedDropdownMenu(
                expanded = prExpanded,
                onDismissRequest = { prExpanded = false }
            ) {
                TaskPriority.entries.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.name) },
                        onClick = { vm.setPriority(p); prExpanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.title,
            onValueChange = vm::setTitle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title *") },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val dueText = state.dueAtMillis?.let { fmt.format(Date(it)) }.orEmpty()

            OutlinedTextField(
                value = dueText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                label = { Text("Due date & time *") },
                trailingIcon = {
                    IconButton(onClick = {
                        pickDateTime(state.dueAtMillis) { picked ->
                            vm.setDueAtMillis(picked)
                        }
                    }) { Icon(Icons.Filled.Schedule, contentDescription = null) }
                }
            )

            OutlinedButton(
                onClick = { vm.setDueAtMillis(null) },
                enabled = state.dueAtMillis != null,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) { Text("Clear") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Open scheduling", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = state.scheduleEnabled,
                onCheckedChange = vm::setScheduleEnabled
            )
        }

        if (state.scheduleEnabled) {
            val openFromText = state.openFromMillis?.let { fmt.format(Date(it)) }.orEmpty()

            OutlinedTextField(
                value = openFromText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Unlock date & time *") },
                trailingIcon = {
                    IconButton(onClick = {
                        pickDateTime(state.openFromMillis) { picked ->
                            vm.setOpenFromMillis(picked)
                        }
                    }) { Icon(Icons.Filled.Schedule, contentDescription = null) }
                }
            )

            OutlinedTextField(
                value = state.availableHoursText,
                onValueChange = vm::setAvailableHoursText,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Available hours *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Text("Content", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = state.content,
                onValueChange = vm::setContent,
                modifier = Modifier.weight(1f),
                label = { Text("Type content here *") },
                minLines = 5
            )

            Box {
                Button(onClick = { insertMenuOpen = true }) { Text("Insert") }

                DropdownMenu(
                    expanded = insertMenuOpen,
                    onDismissRequest = { insertMenuOpen = false }
                ) {
                    DropdownMenuItem(text = { Text("PPT") }, onClick = { insertMenuOpen = false; vm.addAttachment(AttachmentType.PPT, "PPT", "pending") })
                    DropdownMenuItem(text = { Text("Excel") }, onClick = { insertMenuOpen = false; vm.addAttachment(AttachmentType.EXCEL, "Excel", "pending") })
                    DropdownMenuItem(text = { Text("Word") }, onClick = { insertMenuOpen = false; vm.addAttachment(AttachmentType.WORD, "Word", "pending") })
                    DropdownMenuItem(text = { Text("URL link") }, onClick = { insertMenuOpen = false; showUrlDialog = true })
                    DropdownMenuItem(text = { Text("Image") }, onClick = { insertMenuOpen = false; vm.addAttachment(AttachmentType.IMAGE, "Image", "pending") })
                    DropdownMenuItem(text = { Text("TXT") }, onClick = { insertMenuOpen = false; showTxtDialog = true })
                    DropdownMenuItem(text = { Text("PDF") }, onClick = { insertMenuOpen = false; vm.addAttachment(AttachmentType.PDF, "PDF", "pending") })
                }
            }
        }

        if (state.attachments.isNotEmpty()) {
            Text("Attached", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.attachments.forEach { a ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("${a.type.name}: ${a.label}", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                if (a.isLoading) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                } else {
                                    Text("Ready", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { vm.removeAttachment(a.id) }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { vm.submitTaskToFirestore() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.saving
        ) {
            if (state.saving) CircularProgressIndicator() else Text("Create Task")
        }

        Spacer(Modifier.height(10.dp))

        Text("Registered Students", style = MaterialTheme.typography.headlineSmall)

        val points = state.pointsText.toIntOrNull() ?: 0

        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text("Full name", modifier = Modifier.weight(0.34f))
            Text("Email", modifier = Modifier.weight(0.34f))
            Text("State", modifier = Modifier.weight(0.16f))
            Text("Grade", modifier = Modifier.weight(0.16f))
        }

        Divider()

        state.students.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.fullName, modifier = Modifier.weight(0.34f))
                Text(s.email, modifier = Modifier.weight(0.34f))

                Box(modifier = Modifier.weight(0.16f), contentAlignment = Alignment.CenterStart) {
                    when (s.state) {
                        SubmissionState.DELIVERED -> Icon(Icons.Filled.Schedule, contentDescription = null)
                        SubmissionState.OPENED -> Icon(Icons.Filled.RemoveRedEye, contentDescription = null)
                        SubmissionState.SUBMITTED -> Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }

                Row(
                    modifier = Modifier.weight(0.16f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = state.grades[s.uid].orEmpty(),
                        onValueChange = { vm.setGrade(s.uid, it) },
                        modifier = Modifier.width(70.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text("/$points")
                }
            }
            Divider()
        }
    }
}
