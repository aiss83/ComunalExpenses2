package ru.aiss83.comunalexpenses2.ui.screens

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
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.AppRoute
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

/**
 * Main screen composable that handles navigation between app screens.
 * Uses simple state-based navigation instead of Navigation Compose for KMP compatibility.
 */
@Composable
fun App(
    resourcesDataViewModel: ResourcesDataViewModel,
    settingsViewModel: SettingsViewModel
) {
    ComunalExpenses2Theme {
        Surface {
            var currentRoute: AppRoute by remember { mutableStateOf(AppRoute.Home) }

            val navigateBack = {
                currentRoute = AppRoute.Home
            }

            // Collect error state from ViewModel
            val errorMessage by resourcesDataViewModel.errorMessage.collectAsState()

            // Error dialog
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

            when (val route = currentRoute) {
                is AppRoute.Home -> {
                    val allResourcesData by resourcesDataViewModel.allResourcesData.collectAsState()
                    val userSettings by settingsViewModel.settingsData.collectAsState()
                    HomeScreen(
                        allResourceData = allResourcesData,
                        userSettings = userSettings,
                        viewModel = resourcesDataViewModel,
                        onNavigateToAddExpenses = { currentRoute = AppRoute.EditResources() },
                        onNavigateToEditExpenses = { record -> currentRoute = AppRoute.EditResources(record) },
                        onNavigateToSettings = { currentRoute = AppRoute.Settings }
                    )
                }

                is AppRoute.EditResources -> {
                    AddResourcesDataScreen(
                        viewModel = resourcesDataViewModel,
                        existingRecord = route.existingRecord,
                        onNavigateBack = { navigateBack() }
                    )
                }

                is AppRoute.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
            }
        }
    }
}
