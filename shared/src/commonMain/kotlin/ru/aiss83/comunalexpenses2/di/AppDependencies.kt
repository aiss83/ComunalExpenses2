package ru.aiss83.comunalexpenses2.di

import ru.aiss83.comunalexpenses2.data.*
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

/**
 * Legacy singleton DI container.
 *
 * @deprecated Use [AppContainer] with Compose-aware ViewModel providers instead.
 * Kept for backward compatibility with existing code.
 */
@Deprecated(
    message = "Use AppContainer with container.resourcesDataViewModel instead",
    replaceWith = ReplaceWith("AppContainer")
)
object AppDependencies {

    private val container by lazy { AppContainer() }

    val database: ResourcesDatabase get() = container.database
    val resourceDataDao: ResourceDataDao get() = container.resourceDataDao
    val resourceDataRepository: ResourceDataRepository get() = container.resourceDataRepository
    val settingsManager: SettingsManager get() = container.settingsManager

    fun createResourcesDataViewModel(): ResourcesDataViewModel {
        return container.resourcesDataViewModel
    }

    fun createSettingsViewModel(): SettingsViewModel {
        return container.settingsViewModel
    }
}
