package ru.aiss83.comunalexpenses2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import ru.aiss83.comunalexpenses2.data.AppContextHolder
import ru.aiss83.comunalexpenses2.di.startKoinIfNeeded
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.screens.App

class MainActivity : ComponentActivity(), KoinComponent {

    private val resourcesDataViewModel: ResourcesDataViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize context holder for shared module
        AppContextHolder.context = applicationContext

        // Initialize Koin only if not already started (idempotent check via Koin itself)
        if (GlobalContext.getOrNull() == null) {
            startKoinIfNeeded()
        }

        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                App(
                    resourcesDataViewModel = resourcesDataViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
