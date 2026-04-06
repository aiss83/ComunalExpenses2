package ru.aiss83.comunalexpenses2.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.aiss83.comunalexpenses2.data.AppContextHolder

/**
 * Android implementation of Koin initialization.
 * Must be called from Application.onCreate() or MainActivity.onCreate().
 */
actual fun startKoinIfNeeded() {
    val context = AppContextHolder.context
        ?: throw IllegalStateException("AppContextHolder.context must be initialized before Koin")

    startKoin {
        androidContext(context)
        modules(appModules)
    }
}
