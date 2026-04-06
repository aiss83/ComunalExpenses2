package ru.aiss83.comunalexpenses2.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.aiss83.comunalexpenses2.data.*
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

/**
 * Platform-agnostic Koin modules.
 *
 * Contains:
 * - [dataModule]: Database, DAO, Repository, Settings
 * - [viewModelModule]: ViewModels (scoped to Compose lifecycle)
 */

// --- Data Layer ---

val dataModule = module {
    // Database (singleton)
    single<ResourcesDatabase> { createDatabase() }

    // DAO
    single<ResourceDataDao> { ResourceDataDao(get()) }

    // Repository
    single<ResourceDataRepository> { ResourceDataRepository(get()) }

    // Settings
    single<SettingsManager> { createSettingsManager() }
}

// --- ViewModel Layer ---

val viewModelModule = module {
    // ViewModels - created once per Koin scope
    // For Compose, use koinComposeViewModel() in UI code
    factory<ResourcesDataViewModel> { ResourcesDataViewModel(get()) }
    factory<SettingsViewModel> { SettingsViewModel(get()) }
}

/**
 * Complete Koin module list for the shared module.
 * Pass this to [startKoin] during app initialization.
 */
val appModules: List<Module> = listOf(
    dataModule,
    viewModelModule
)
