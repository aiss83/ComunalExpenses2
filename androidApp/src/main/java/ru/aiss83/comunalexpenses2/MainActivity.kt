package ru.aiss83.comunalexpenses2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.aiss83.comunalexpenses2.di.AppDependencies
import ru.aiss83.comunalexpenses2.ui.screens.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val resourcesDataViewModel = AppDependencies.createResourcesDataViewModel()
                val settingsViewModel = AppDependencies.createSettingsViewModel()

                App(
                    resourcesDataViewModel = resourcesDataViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
