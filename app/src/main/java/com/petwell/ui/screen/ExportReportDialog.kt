package com.petwell.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petwell.data.entity.PetProfile
import com.petwell.ui.viewmodel.ReportViewModel

@Composable
fun ExportReportDialog(
    petProfile: PetProfile,
    viewModel: ReportViewModel,
    onDismiss: () -> Unit,
    onShare: (android.content.Intent) -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state is ReportViewModel.ReportState.Ready) {
        val ready = state as ReportViewModel.ReportState.Ready
        onShare(ready.shareIntent)
        viewModel.resetState()
        onDismiss()
        return
    }

    var selectedDays by remember { mutableStateOf(7) }

    AlertDialog(
        onDismissRequest = {
            if (state !is ReportViewModel.ReportState.Generating) {
                viewModel.resetState()
                onDismiss()
            }
        },
        title = { Text("Export Vet Report") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (state) {
                    is ReportViewModel.ReportState.Generating -> {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Generating PDF...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is ReportViewModel.ReportState.Error -> {
                        val error = state as ReportViewModel.ReportState.Error
                        Text(
                            "Error: ${error.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        Text(
                            "Generate a PDF report for ${petProfile.name} with weight history, daily logs, and reminder adherence.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select date range:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { selectedDays = 7 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (selectedDays == 7) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Last 7 Days")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { selectedDays = 30 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (selectedDays == 30) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Last 30 Days")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (state !is ReportViewModel.ReportState.Generating) {
                Button(
                    onClick = { viewModel.generateReport(petProfile, selectedDays) },
                    enabled = state !is ReportViewModel.ReportState.Generating
                ) {
                    Text("Generate")
                }
            }
        },
        dismissButton = {
            if (state !is ReportViewModel.ReportState.Generating) {
                TextButton(onClick = {
                    viewModel.resetState()
                    onDismiss()
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
