package com.purrcare.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.purrcare.data.entity.CatProfile
import com.purrcare.ui.viewmodel.CatProfileViewModel
import com.purrcare.ui.viewmodel.ProfileSaveState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CatProfileViewModel,
    catProfile: CatProfile?,
    onSaved: () -> Unit
) {
    val saveState by viewModel.profileSaveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf(catProfile?.name ?: "") }
    var birthYear by remember { mutableIntStateOf(catProfile?.birthYear ?: 2024) }
    var targetWeight by remember { mutableFloatStateOf(catProfile?.targetWeight ?: 0f) }
    var conditionNotes by remember { mutableStateOf(catProfile?.conditionNotes ?: "") }
    var avatarUri by remember { mutableStateOf(catProfile?.avatarUri ?: "") }

    LaunchedEffect(catProfile) {
        catProfile?.let {
            name = it.name
            birthYear = it.birthYear
            targetWeight = it.targetWeight
            conditionNotes = it.conditionNotes
            avatarUri = it.avatarUri
        }
    }

    LaunchedEffect(saveState) {
        val currentSaveState = saveState
        when (currentSaveState) {
            is ProfileSaveState.Saved -> {
                snackbarHostState.showSnackbar("Profile saved")
                viewModel.resetSaveState()
                onSaved()
            }
            is ProfileSaveState.Error -> {
                snackbarHostState.showSnackbar("Error: ${currentSaveState.message}")
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (catProfile == null) "Create Profile" else "Edit Profile") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AsyncImage(
                model = avatarUri,
                contentDescription = "Cat avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(onClick = { /* photo picker stub */ }) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = if (birthYear == 0) "" else birthYear.toString(),
                onValueChange = { birthYear = it.toIntOrNull() ?: 0 },
                label = { Text("Birth Year") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = if (targetWeight == 0f) "" else targetWeight.toString(),
                onValueChange = { targetWeight = it.toFloatOrNull() ?: 0f },
                label = { Text("Target Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = conditionNotes,
                onValueChange = { conditionNotes = it },
                label = { Text("Condition Notes") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val profile = CatProfile(
                        id = catProfile?.id ?: 0,
                        name = name,
                        birthYear = birthYear,
                        targetWeight = targetWeight,
                        conditionNotes = conditionNotes,
                        avatarUri = avatarUri
                    )
                    viewModel.createOrUpdateProfile(profile)
                },
                enabled = saveState !is ProfileSaveState.Saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saveState is ProfileSaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (catProfile == null) "Create Profile" else "Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
