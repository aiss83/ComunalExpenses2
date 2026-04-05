package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            var currentRoute by remember { mutableStateOf(AppRoute.Home) }

            val navigateTo = { route: AppRoute ->
                currentRoute = route
            }

            val navigateBack = {
                currentRoute = AppRoute.Home
            }

            when (currentRoute) {
                AppRoute.Home -> {
                    val allResourcesData by resourcesDataViewModel.allResourcesData
                    HomeScreen(
                        allResourceData = allResourcesData,
                        viewModel = resourcesDataViewModel,
                        onNavigateToAddExpenses = { navigateTo(AppRoute.EditResources) },
                        onNavigateToSettings = { navigateTo(AppRoute.Settings) }
                    )
                }

                AppRoute.EditResources -> {
                    AddResourcesDataScreen(
                        viewModel = resourcesDataViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }

                AppRoute.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
            }
        }
    }
}
