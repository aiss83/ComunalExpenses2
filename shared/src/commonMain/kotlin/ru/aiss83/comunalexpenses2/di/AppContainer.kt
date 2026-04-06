package ru.aiss83.comunalexpenses2.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.aiss83.comunalexpenses2.data.*
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

/**
 * Application-scoped dependency container.
 *
 * Provides singleton instances for database, repository, and settings.
 * ViewModels are cached per-container to survive recomposition.
 *
 * Usage:
 * ```kotlin
 * val container = remember { AppContainer() }
 * val viewModel = container.resourcesDataViewModel
 * ```
 */
class AppContainer {

    // --- Singletons (lazy, initialized once) ---

    val database: ResourcesDatabase by lazy { createDatabase() }

    val resourceDataDao: ResourceDataDao by lazy { ResourceDataDao(database) }

    val resourceDataRepository: ResourceDataRepository by lazy {
        ResourceDataRepository(resourceDataDao)
    }

    val settingsManager: SettingsManager by lazy { createSettingsManager() }

    // --- ViewModel cache (keyed by enum to avoid Class references) ---

    private val _viewModelCache = mutableMapOf<ViewModelKey, Any>()

    val resourcesDataViewModel: ResourcesDataViewModel
        get() = _viewModelCache.getOrPut(ViewModelKey.ResourcesData) {
            ResourcesDataViewModel(resourceDataRepository)
        } as ResourcesDataViewModel

    val settingsViewModel: SettingsViewModel
        get() = _viewModelCache.getOrPut(ViewModelKey.Settings) {
            SettingsViewModel(settingsManager)
        } as SettingsViewModel

    private enum class ViewModelKey {
        ResourcesData,
        Settings
    }
}

/**
 * Compose-aware helper to get the AppContainer.
 *
 * Usage inside @Composable:
 * ```kotlin
 * val container = rememberAppContainer()
 * val viewModel = container.resourcesDataViewModel
 * ```
 */
@Composable
fun rememberAppContainer(): AppContainer {
    return remember { AppContainer() }
}
