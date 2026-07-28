package com.petwell.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.enums.ReminderType
import com.petwell.ui.viewmodel.PetReminderViewModel
import com.petwell.ui.viewmodel.RemSaveState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: PetReminderViewModel
) {
    val reminders by viewModel.reminders.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        val currentState = saveState
        when (currentState) {
            is RemSaveState.Saved -> {
                snackbarHostState.showSnackbar("Reminder saved")
                viewModel.resetSaveState()
            }
            is RemSaveState.Error -> {
                snackbarHostState.showSnackbar(currentState.message)
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, dosage, reminderType, freqHours, alarmHour, alarmMinute, enabled ->
                viewModel.addReminder(title, dosage, reminderType, freqHours, alarmHour, alarmMinute, enabled)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reminders") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No reminders yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap + to add a reminder for medications, feeding, walks, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { viewModel.toggleEnabled(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: PetReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reminder.reminderType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (reminder.dosage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reminder.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Every ${reminder.frequencyHours}h at %02d:%02d".format(
                        Locale.getDefault(), reminder.alarmHour, reminder.alarmMinute
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle() }
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete reminder",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dosage: String, reminderType: ReminderType, freqHours: Int, alarmHour: Int, alarmMinute: Int, enabled: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var reminderType by remember { mutableStateOf(ReminderType.MEDICATION) }
    var freqHours by remember { mutableIntStateOf(8) }
    var enabled by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Alarm Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = reminderType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        ReminderType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    reminderType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage / Details") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = if (freqHours == 0) "" else freqHours.toString(),
                    onValueChange = { freqHours = it.toIntOrNull() ?: 0 },
                    label = { Text("Frequency (hours)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Alarm Time", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { showTimePicker = true }) {
                        Text(
                            "%02d:%02d".format(
                                Locale.getDefault(),
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Reminders", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && freqHours > 0) {
                        onConfirm(
                            title.trim(),
                            dosage.trim(),
                            reminderType,
                            freqHours,
                            timePickerState.hour,
                            timePickerState.minute,
                            enabled
                        )
                    }
                },
                enabled = title.isNotBlank() && freqHours > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
