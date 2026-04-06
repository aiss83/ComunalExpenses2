package ru.aiss83.comunalexpenses2.di

import org.koin.core.context.startKoin

/**
 * iOS implementation of Koin initialization.
 */
actual fun startKoinIfNeeded() {
    startKoin {
        modules(appModules)
    }
}
