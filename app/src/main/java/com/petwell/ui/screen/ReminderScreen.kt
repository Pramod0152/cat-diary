package com.petwell.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.enums.ReminderType
import com.petwell.ui.viewmodel.PetReminderViewModel
import com.petwell.ui.viewmodel.RemSaveState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val typeIconMap: Map<ReminderType, ImageVector> = mapOf(
    ReminderType.MEDICATION to Icons.Filled.Medication,
    ReminderType.FEEDING to Icons.Filled.Fastfood,
    ReminderType.WALK to Icons.Filled.DirectionsWalk,
    ReminderType.VET_VISIT to Icons.Filled.LocalHospital,
    ReminderType.GROOMING to Icons.Outlined.ContentCut,
    ReminderType.CUSTOM to Icons.Filled.MoreHoriz
)

private val typeColorMap: Map<ReminderType, Color> = mapOf(
    ReminderType.MEDICATION to Color(0xFF26A69A),
    ReminderType.FEEDING to Color(0xFFFFA726),
    ReminderType.WALK to Color(0xFF42A5F5),
    ReminderType.VET_VISIT to Color(0xFFEF5350),
    ReminderType.GROOMING to Color(0xFFAB47BC),
    ReminderType.CUSTOM to Color(0xFF78909C)
)

private val typeLabelMap: Map<ReminderType, String> = mapOf(
    ReminderType.MEDICATION to "Medication",
    ReminderType.FEEDING to "Feeding",
    ReminderType.WALK to "Walking",
    ReminderType.VET_VISIT to "Vet Visits",
    ReminderType.GROOMING to "Grooming",
    ReminderType.CUSTOM to "Other"
)

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
            onConfirm = { title, dosage, reminderType, freqHours, alarmHour, alarmMinute, enabled, nextDate ->
                viewModel.addReminder(title, dosage, reminderType, freqHours, alarmHour, alarmMinute, enabled, nextDate)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "No reminders yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap + to add medication, feeding, walks,\nvet visits, grooming, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            GroupedReminderList(
                reminders = reminders,
                contentPadding = padding,
                onToggle = { viewModel.toggleEnabled(it) },
                onDelete = { viewModel.deleteReminder(it) }
            )
        }
    }
}

@Composable
private fun GroupedReminderList(
    reminders: List<PetReminder>,
    contentPadding: PaddingValues,
    onToggle: (PetReminder) -> Unit,
    onDelete: (PetReminder) -> Unit
) {
    val grouped = reminders.groupBy { it.reminderType }
    val displayOrder = listOf(
        ReminderType.MEDICATION,
        ReminderType.FEEDING,
        ReminderType.WALK,
        ReminderType.VET_VISIT,
        ReminderType.GROOMING,
        ReminderType.CUSTOM
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        displayOrder.forEach { type ->
            val typeReminders = grouped[type] ?: emptyList()
            if (typeReminders.isNotEmpty()) {
                item(key = "header_${type.name}") {
                    ReminderGroupHeader(type, typeReminders.size)
                }
                items(typeReminders, key = { "reminder_${it.id}" }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { onToggle(reminder) },
                        onDelete = { onDelete(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderGroupHeader(type: ReminderType, count: Int) {
    val icon = typeIconMap[type] ?: Icons.Filled.MoreHoriz
    val color = typeColorMap[type] ?: MaterialTheme.colorScheme.primary
    val label = typeLabelMap[type] ?: type.displayName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: PetReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = typeColorMap[reminder.reminderType] ?: MaterialTheme.colorScheme.primary
    val typeIcon = typeIconMap[reminder.reminderType] ?: Icons.Filled.MoreHoriz
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = typeColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = typeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.dosage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reminder.dosage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%02d:%02d".format(reminder.alarmHour, reminder.alarmMinute),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reminder.frequencyHours > 0) {
                        Text(
                            text = "  every ${reminder.frequencyHours}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                if (reminder.nextReminderDate != null) {
                    Text(
                        text = dateFormat.format(Date(reminder.nextReminderDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dosage: String, reminderType: ReminderType, freqHours: Int, alarmHour: Int, alarmMinute: Int, enabled: Boolean, nextReminderDate: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var reminderType by remember { mutableStateOf(ReminderType.MEDICATION) }
    var freqHours by remember { mutableIntStateOf(8) }
    var enabled by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(0L) }

    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Alarm Time") },
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("Set Date")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedDateMillis = 0L
                    showDatePicker = false
                }) {
                    Text("Clear")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val typeIcon = typeIconMap[reminderType] ?: Icons.Filled.MoreHoriz
                val typeColor = typeColorMap[reminderType] ?: MaterialTheme.colorScheme.primary
                Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("New Reminder")
            }
        },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = (typeLabelMap[reminderType] ?: reminderType.displayName),
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
                        val order = listOf(
                            ReminderType.MEDICATION,
                            ReminderType.FEEDING,
                            ReminderType.WALK,
                            ReminderType.VET_VISIT,
                            ReminderType.GROOMING,
                            ReminderType.CUSTOM
                        )
                        order.forEach { type ->
                            val typeIcon = typeIconMap[type] ?: Icons.Filled.MoreHoriz
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(typeIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(typeLabelMap[type] ?: type.displayName)
                                    }
                                },
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
                    Text("Reminder Date", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { showDatePicker = true }) {
                        Text(
                            if (selectedDateMillis > 0L) dateFormat.format(Date(selectedDateMillis))
                            else "Select Date"
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
                            enabled,
                            if (selectedDateMillis > 0L) selectedDateMillis else null
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
