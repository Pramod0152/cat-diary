package com.petwell

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
import com.petwell.notification.NotificationHelper
import com.petwell.ui.navigation.PetWellNavHost
import com.petwell.ui.viewmodel.DailyLogViewModel
import com.petwell.ui.viewmodel.HomeViewModel
import com.petwell.ui.viewmodel.PetProfileViewModel
import com.petwell.ui.viewmodel.PetReminderViewModel
import com.petwell.ui.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {

    private val petProfileViewModel: PetProfileViewModel by viewModels()
    private val dailyLogViewModel: DailyLogViewModel by viewModels()
    private val petReminderViewModel: PetReminderViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val reportViewModel: ReportViewModel by viewModels()

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
                    PetWellNavHost(
                        petProfileViewModel = petProfileViewModel,
                        dailyLogViewModel = dailyLogViewModel,
                        petReminderViewModel = petReminderViewModel,
                        homeViewModel = homeViewModel,
                        reportViewModel = reportViewModel
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
