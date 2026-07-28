package com.purrcare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.purrcare.notification.NotificationHelper
import com.purrcare.ui.navigation.PurrCareNavHost
import com.purrcare.ui.viewmodel.CatProfileViewModel
import com.purrcare.ui.viewmodel.DailyLogViewModel
import com.purrcare.ui.viewmodel.MedicationViewModel

class MainActivity : ComponentActivity() {

    private val catProfileViewModel: CatProfileViewModel by viewModels()
    private val dailyLogViewModel: DailyLogViewModel by viewModels()
    private val medicationViewModel: MedicationViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        requestNotificationPermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PurrCareNavHost(
                        catProfileViewModel = catProfileViewModel,
                        dailyLogViewModel = dailyLogViewModel,
                        medicationViewModel = medicationViewModel
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
