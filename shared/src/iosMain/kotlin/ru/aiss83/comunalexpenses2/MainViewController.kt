package ru.aiss83.comunalexpenses2

import androidx.compose.ui.window.ComposeUIViewController
import ru.aiss83.comunalexpenses2.di.AppDependencies
import ru.aiss83.comunalexpenses2.ui.screens.App

/**
 * Factory function to create the main UIViewController for iOS.
 * This is exposed to Swift via Kotlin/Native interop.
 */
fun MainViewController() = ComposeUIViewController {
    val resourcesDataViewModel = AppDependencies.createResourcesDataViewModel()
    val settingsViewModel = AppDependencies.createSettingsViewModel()

    App(
        resourcesDataViewModel = resourcesDataViewModel,
        settingsViewModel = settingsViewModel
    )
}
