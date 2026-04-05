package ru.aiss83.comunalexpenses2

import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ru.aiss83.comunalexpenses2.di.AppDependencies
import ru.aiss83.comunalexpenses2.ui.screens.App

/**
 * Factory function to create the main UIViewController for iOS.
 * This is exposed to Swift via Kotlin/Native interop.
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
    val dependencies = remember { AppDependencies }
    val resourcesDataViewModel = dependencies.createResourcesDataViewModel()
    val settingsViewModel = dependencies.createSettingsViewModel()

    App(
        resourcesDataViewModel = resourcesDataViewModel,
        settingsViewModel = settingsViewModel
    )
}
