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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.petwell.data.entity.enums.LitterUrination
import com.petwell.data.entity.enums.WaterIntake
import com.petwell.ui.viewmodel.DailyLogViewModel
import com.petwell.ui.viewmodel.SaveState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    viewModel: DailyLogViewModel,
    onSaved: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        val currentState = saveState
        when (currentState) {
            is SaveState.Saved -> {
                snackbarHostState.showSnackbar("Daily log saved")
                viewModel.resetSaveState()
                onSaved()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(currentState.message)
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formState.selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChanged(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Log") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                        .format(Date(formState.selectedDate)),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select date"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.weight,
                onValueChange = { viewModel.updateWeight(it) },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Appetite",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { viewModel.updateAppetiteScore(i) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Appetite $i",
                            tint = if (i <= formState.appetiteScore)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Water Intake",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WaterIntake.entries.forEachIndexed { index, intake ->
                    SegmentedButton(
                        selected = formState.waterIntake == intake,
                        onClick = { viewModel.updateWaterIntake(intake) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = WaterIntake.entries.size
                        )
                    ) {
                        Text(
                            text = intake.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Litter - Stool Score",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = formState.litterStoolScore.toFloat(),
                onValueChange = { viewModel.updateLitterStoolScore(it.toInt()) },
                valueRange = 1f..7f,
                steps = 5,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1 - Hard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "4 - Normal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "7 - Liquid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Score: ${formState.litterStoolScore}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Urination",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LitterUrination.entries.forEachIndexed { index, level ->
                    SegmentedButton(
                        selected = formState.litterUrination == level,
                        onClick = { viewModel.updateLitterUrination(level) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = LitterUrination.entries.size
                        )
                    ) {
                        Text(
                            text = level.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = formState.customNotes,
                onValueChange = { viewModel.updateCustomNotes(it) },
                label = { Text("Notes") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            val isSaving = saveState is SaveState.Saving
            Button(
                onClick = { viewModel.save() },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Log",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
