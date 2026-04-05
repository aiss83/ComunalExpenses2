package ru.aiss83.comunalexpenses2.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Expected function to create platform-specific SQLDelight driver.
 */
expect fun createDriver(): SqlDriver

/**
 * Factory function to create the database instance.
 */
fun createDatabase(): ResourcesDatabase {
    return ResourcesDatabase(createDriver())
}
