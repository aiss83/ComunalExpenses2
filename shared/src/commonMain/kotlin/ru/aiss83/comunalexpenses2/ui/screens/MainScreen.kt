package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.uuid.ExperimentalUuidApi
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.AppRoute
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@OptIn(ExperimentalUuidApi::class)
@Composable
fun App(
    resourcesDataViewModel: ResourcesDataViewModel,
    settingsViewModel: SettingsViewModel,
    startRoute: AppRoute = AppRoute.Home
) {
    val userSettings by settingsViewModel.settingsData.collectAsState()

    ComunalExpenses2Theme(themeMode = userSettings.themeMode) {
        Surface {
            var currentRoute: AppRoute by remember { mutableStateOf(startRoute) }

            val navigateBack = { currentRoute = AppRoute.Home }

            val errorMessage by resourcesDataViewModel.errorMessage.collectAsState()

            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { resourcesDataViewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(errorMessage!!) },
                    confirmButton = {
                        TextButton(onClick = { resourcesDataViewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    val direction = when (targetState) {
                        is AppRoute.Home -> -1
                        else -> 1
                    }
                    (slideInHorizontally { width -> direction * width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -direction * width } + fadeOut())
                }
            ) { route ->
                when (route) {
                    is AppRoute.Home -> {
                        val allResourcesData by resourcesDataViewModel.allResourcesData.collectAsState()
                        HomeScreen(
                            allResourceData = allResourcesData,
                            userSettings = userSettings,
                            viewModel = resourcesDataViewModel,
                            onNavigateToAddExpenses = { currentRoute = AppRoute.EditResources() },
                            onNavigateToEditExpenses = { record -> currentRoute = AppRoute.EditResources(record) },
                            onNavigateToSettings = { currentRoute = AppRoute.Settings },
                            onNavigateToStats = { currentRoute = AppRoute.Stats }
                        )
                    }

                    is AppRoute.EditResources -> {
                        val allData by resourcesDataViewModel.allResourcesData.collectAsState()
                        val sorted = allData.sortedBy { it.date }
                        val previousRecord = if (route.existingRecord != null) {
                            val idx = sorted.indexOfFirst { it.id == route.existingRecord.id }
                            if (idx > 0) sorted[idx - 1] else null
                        } else {
                            sorted.lastOrNull()
                        }
                        AddResourcesDataScreen(
                            viewModel = resourcesDataViewModel,
                            existingRecord = route.existingRecord,
                            previousRecord = previousRecord,
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    is AppRoute.Settings -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    is AppRoute.Stats -> {
                        val allResourcesData by resourcesDataViewModel.allResourcesData.collectAsState()
                        StatsScreen(
                            allResourceData = allResourcesData,
                            onNavigateBack = { navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
