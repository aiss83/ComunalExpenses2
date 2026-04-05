package ru.aiss83.comunalexpenses2.di

import ru.aiss83.comunalexpenses2.data.*
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

/**
 * Simple dependency injection container.
 * Holds all shared instances of repositories, ViewModels, and services.
 * This is a manual DI approach to avoid framework dependencies in KMP.
 */
object AppDependencies {

    // Database
    val database: ResourcesDatabase by lazy { createDatabase() }

    // DAO
    val resourceDataDao: ResourceDataDao by lazy { ResourceDataDao(database) }

    // Repository
    val resourceDataRepository: ResourceDataRepository by lazy {
        ResourceDataRepository(resourceDataDao)
    }

    // Settings
    val settingsManager: SettingsManager by lazy { createSettingsManager() }

    // ViewModels (created with factories when needed)
    fun createResourcesDataViewModel(): ResourcesDataViewModel {
        return ResourcesDataViewModel(resourceDataRepository)
    }

    fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(settingsManager)
    }
}
