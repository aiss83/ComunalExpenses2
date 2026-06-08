package ru.aiss83.comunalexpenses2.ui

import ru.aiss83.comunalexpenses2.data.ResourceData

/**
 * Navigation routes for the app screens.
 * EditResources carries optional existing record for editing.
 */
sealed class AppRoute {
    data object Home : AppRoute()
    data object Settings : AppRoute()
    data class EditResources(val existingRecord: ResourceData? = null) : AppRoute()
}
