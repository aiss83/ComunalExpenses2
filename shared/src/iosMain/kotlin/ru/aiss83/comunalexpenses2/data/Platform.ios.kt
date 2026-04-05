package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of SQLDelight driver factory.
 */
actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(ResourcesDatabase.Schema, "resources_data_database.db")
}

/**
 * iOS implementation of Settings factory.
 */
actual fun createSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
