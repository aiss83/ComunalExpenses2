package ru.aiss83.comunalexpenses2

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import ru.aiss83.comunalexpenses2.di.AppContainer
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.screens.App

/**
 * Factory function to create the main UIViewController for iOS.
 * Uses AppContainer for dependency injection with ViewModel caching.
 *
 * Usage from Swift:
 * ```swift
 * import Shared
 * let vc = MainViewControllerKt.MainViewController()
 * ```
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    // AppContainer survives recomposition via remember, ViewModels are cached inside it
    val container = remember { AppContainer() }
    val resourcesDataViewModel = container.resourcesDataViewModel
    val settingsViewModel = container.settingsViewModel

    App(
        resourcesDataViewModel = resourcesDataViewModel,
        settingsViewModel = settingsViewModel
    )
}
