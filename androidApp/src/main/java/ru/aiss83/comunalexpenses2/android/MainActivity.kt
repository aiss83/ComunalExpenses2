package ru.aiss83.comunalexpenses2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import ru.aiss83.comunalexpenses2.di.startKoinIfNeeded
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.AppRoute
import ru.aiss83.comunalexpenses2.ui.screens.App

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Koin only if not already started (idempotent check via Koin itself)
        if (GlobalContext.getOrNull() == null) {
            startKoinIfNeeded()
        }

        // Check if launched from widget to open add screen directly
        val openAddScreen = intent?.getBooleanExtra("open_add_screen", false) ?: false

        super.onCreate(savedInstanceState)

        setContent {
            // koinViewModel() stores the ViewModel in the Activity's ViewModelStore, so it is
            // retained across configuration changes and its viewModelScope is cancelled when
            // the Activity is finished (previously `by inject()` leaked a DB collector each time).
            val resourcesDataViewModel = koinViewModel<ResourcesDataViewModel>()
            val settingsViewModel = koinViewModel<SettingsViewModel>()

            App(
                resourcesDataViewModel = resourcesDataViewModel,
                settingsViewModel = settingsViewModel,
                startRoute = if (openAddScreen) AppRoute.EditResources() else AppRoute.Home
            )
        }
    }
}
