package com.petwell.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.petwell.data.entity.enums.Species
import com.petwell.ui.screen.DailyLogScreen
import com.petwell.ui.screen.ExportReportDialog
import com.petwell.ui.screen.HomeScreen
import com.petwell.ui.screen.JournalScreen
import com.petwell.ui.screen.ProfileScreen
import com.petwell.ui.screen.ReminderScreen
import com.petwell.ui.viewmodel.DailyLogViewModel
import com.petwell.ui.viewmodel.HomeViewModel
import com.petwell.ui.viewmodel.JournalViewModel
import com.petwell.ui.viewmodel.PetProfileViewModel
import com.petwell.ui.viewmodel.PetReminderViewModel
import com.petwell.ui.viewmodel.ReportViewModel

@Composable
fun PetWellNavHost(
    petProfileViewModel: PetProfileViewModel,
    dailyLogViewModel: DailyLogViewModel,
    petReminderViewModel: PetReminderViewModel,
    journalViewModel: JournalViewModel,
    homeViewModel: HomeViewModel,
    reportViewModel: ReportViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedPet by petProfileViewModel.selectedPet.collectAsStateWithLifecycle()
    val recentLogs by homeViewModel.recentLogs.collectAsState()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog && selectedPet != null) {
        ExportReportDialog(
            petProfile = selectedPet!!,
            viewModel = reportViewModel,
            onDismiss = { showExportDialog = false },
            onShare = { intent ->
                context.startActivity(android.content.Intent.createChooser(intent, "Share Report"))
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300)) { -it / 4 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300)) { it / 4 }
            }
        ) {
            composable(Screen.Home.route) {
                LaunchedEffect(selectedPet?.id) {
                    selectedPet?.id?.let { homeViewModel.initialize(it) }
                }
                HomeScreen(
                    petProfile = selectedPet,
                    recentLogs = recentLogs,
                    onEditProfile = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLog = {
                        navController.navigate(Screen.Log.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToReminders = {
                        navController.navigate(Screen.Reminders.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToJournal = {
                        navController.navigate(Screen.Journal.route) {
                            launchSingleTop = true
                        }
                    },
                    onExportReport = { showExportDialog = true }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = petProfileViewModel,
                    petProfile = selectedPet,
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Log.route) {
                LaunchedEffect(selectedPet?.id) {
                    selectedPet?.id?.let { dailyLogViewModel.initialize(it, selectedPet!!.species) }
                }
                DailyLogScreen(
                    species = selectedPet?.species ?: Species.CAT,
                    viewModel = dailyLogViewModel,
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Reminders.route) {
                LaunchedEffect(selectedPet?.id) {
                    selectedPet?.id?.let { petReminderViewModel.initialize(it) }
                }
                ReminderScreen(viewModel = petReminderViewModel)
            }

            composable(Screen.Journal.route) {
                LaunchedEffect(selectedPet?.id) {
                    selectedPet?.id?.let { journalViewModel.initialize(it) }
                }
                JournalScreen(
                    viewModel = journalViewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
