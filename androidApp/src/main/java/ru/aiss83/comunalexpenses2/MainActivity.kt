package ru.aiss83.comunalexpenses2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.aiss83.comunalexpenses2.data.AppContextHolder
import ru.aiss83.comunalexpenses2.di.AppContainer
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.screens.App

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { AppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize context holder for shared module
        AppContextHolder.context = applicationContext

        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Use new DI approach
                val resourcesDataViewModel = appContainer.resourcesDataViewModel
                val settingsViewModel = appContainer.settingsViewModel

                App(
                    resourcesDataViewModel = resourcesDataViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
