package ru.aiss83.comunalexpenses2.ui

import androidx.compose.runtime.saveable.Saver
import ru.aiss83.comunalexpenses2.data.ResourceData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Navigation routes for the app screens.
 * EditResources carries optional existing record for editing.
 */
sealed class AppRoute {
    data object Home : AppRoute()
    data object Settings : AppRoute()
    data object Stats : AppRoute()
    data class EditResources(val existingRecord: ResourceData? = null) : AppRoute()

    companion object {
        /**
         * String-based saver so the current route survives activity recreation
         * (`rememberSaveable`) instead of dropping the user back to the home screen.
         */
        val RouteSaver: Saver<AppRoute, String> = Saver(
            save = { route -> route.encodeRoute() },
            restore = { encoded -> decodeRoute(encoded) }
        )
    }
}

private const val SEPARATOR = '|'
private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_STATS = "stats"
private const val ROUTE_EDIT = "edit"

@OptIn(ExperimentalUuidApi::class)
internal fun AppRoute.encodeRoute(): String = when (this) {
    AppRoute.Home -> ROUTE_HOME
    AppRoute.Settings -> ROUTE_SETTINGS
    AppRoute.Stats -> ROUTE_STATS
    is AppRoute.EditResources -> {
        val record = existingRecord
        if (record == null) {
            ROUTE_EDIT
        } else {
            listOf(
                ROUTE_EDIT,
                record.id.toString(),
                record.date.toString(),
                record.hotWater.toString(),
                record.coldWater.toString(),
                record.dayElectricity.toString(),
                record.nightElectricity.toString(),
                if (record.shared) "1" else "0"
            ).joinToString(SEPARATOR.toString())
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun decodeRoute(encoded: String): AppRoute = when {
    encoded == ROUTE_HOME -> AppRoute.Home
    encoded == ROUTE_SETTINGS -> AppRoute.Settings
    encoded == ROUTE_STATS -> AppRoute.Stats
    encoded == ROUTE_EDIT -> AppRoute.EditResources()
    encoded.startsWith("$ROUTE_EDIT$SEPARATOR") -> {
        val parts = encoded.split(SEPARATOR)
        val record = if (parts.size == 8) {
            runCatching {
                ResourceData(
                    id = Uuid.parse(parts[1]),
                    date = parts[2].toLong(),
                    hotWater = parts[3].toLong(),
                    coldWater = parts[4].toLong(),
                    dayElectricity = parts[5].toLong(),
                    nightElectricity = parts[6].toLong(),
                    shared = parts[7] == "1"
                )
            }.getOrNull()
        } else {
            null
        }
        AppRoute.EditResources(record)
    }

    else -> AppRoute.Home
}
