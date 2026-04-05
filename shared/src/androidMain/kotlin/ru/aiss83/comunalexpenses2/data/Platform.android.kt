package ru.aiss83.comunalexpenses2.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android implementation of SQLDelight driver factory.
 */
actual fun createDriver(): SqlDriver {
    val context = getAppContext()
    return AndroidSqliteDriver(ResourcesDatabase.Schema, context, "resources_data_database.db")
}

/**
 * Android implementation of Settings factory.
 */
actual fun createSettings(): Settings {
    val context = getAppContext()
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPreferences)
}

/**
 * Get application context using Koin (or fallback to reflection).
 * Since we don't use DI framework, we use a simple holder pattern.
 */
private fun getAppContext(): Context {
    return AppContextHolder.context
        ?: throw IllegalStateException("AppContextHolder.context must be initialized before using the database")
}

/**
 * Holder for application context. Must be initialized from Android platform code.
 */
object AppContextHolder {
    var context: Context? = null
}
