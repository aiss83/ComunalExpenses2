package ru.aiss83.comunalexpenses2

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.viewmodel.koinViewModel
import ru.aiss83.comunalexpenses2.di.startKoinIfNeeded
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.ui.screens.App

/**
 * Factory function to create the main UIViewController for iOS.
 * Initializes Koin on first call and uses koinViewModel() for DI.
 *
 * Usage from Swift:
 * ```swift
 * import Shared
 * let vc = MainViewControllerKt.MainViewController()
 * ```
 */
private var koinInitialized = false

fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    // Initialize Koin on first composition
    if (!koinInitialized) {
        startKoinIfNeeded()
        koinInitialized = true
    }

    // Get ViewModels from Koin (cached per composition scope)
    val resourcesDataViewModel = koinViewModel<ResourcesDataViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    App(
        resourcesDataViewModel = resourcesDataViewModel,
        settingsViewModel = settingsViewModel
    )
}
