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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import comunalexpenses2.shared.generated.resources.*
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.AppRoute
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import ru.aiss83.comunalexpenses2.utils.toDayKey
import kotlinx.datetime.Clock

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
            // Saved so rotating the device / process death keeps the current screen.
            var currentRoute: AppRoute by rememberSaveable(stateSaver = AppRoute.RouteSaver) {
                mutableStateOf(startRoute)
            }

            val navigateBack = remember { { currentRoute = AppRoute.Home } }

            val errorMessage by resourcesDataViewModel.errorMessage.collectAsState()

            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { resourcesDataViewModel.clearError() },
                    title = { Text(stringResource(Res.string.error_title)) },
                    text = { Text(errorMessage!!) },
                    confirmButton = {
                        TextButton(onClick = { resourcesDataViewModel.clearError() }) {
                            Text(stringResource(Res.string.dialog_ok))
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
                            onNavigateToAddExpenses = remember { { currentRoute = AppRoute.EditResources() } },
                            onNavigateToEditExpenses = remember { { record -> currentRoute = AppRoute.EditResources(record) } },
                            onNavigateToSettings = remember { { currentRoute = AppRoute.Settings } },
                            onNavigateToStats = remember { { currentRoute = AppRoute.Stats } }
                        )
                    }

                    is AppRoute.EditResources -> {
                        val allData by resourcesDataViewModel.allResourcesData.collectAsState()
                        val sorted = remember(allData) { allData.sortedBy { it.date } }
                        // Exactly one card per day: an "add" intent is redirected to today's
                        // existing card, so the form always edits the single record of that day
                        // (also covers the widget deep link and process-death restore).
                        val target = remember(route, allData) {
                            route.existingRecord
                                ?: allData.firstOrNull { it.date == Clock.System.now().toEpochMilliseconds().toDayKey() }
                        }
                        val editIndex = target
                            ?.let { record -> sorted.indexOfFirst { it.id == record.id } }
                            ?: -1
                        val previousRecord = if (target != null) {
                            if (editIndex > 0) sorted[editIndex - 1] else null
                        } else {
                            sorted.lastOrNull()
                        }
                        // Values of an edited record must not exceed the following record either,
                        // otherwise the consumption chart gets a bogus spike.
                        val nextRecord = if (editIndex >= 0) sorted.getOrNull(editIndex + 1) else null
                        // Re-keyed when the target arrives asynchronously, so the form is
                        // initialised from the record instead of staying empty.
                        key(target?.id) {
                            AddResourcesDataScreen(
                                viewModel = resourcesDataViewModel,
                                existingRecord = target,
                                previousRecord = previousRecord,
                                nextRecord = nextRecord,
                                onNavigateBack = { navigateBack() }
                            )
                        }
                    }

                    is AppRoute.Settings -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            resourcesDataViewModel = resourcesDataViewModel,
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
