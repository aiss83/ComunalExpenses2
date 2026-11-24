package ru.aiss83.comunalexpenses2.utils

import android.content.Context
import android.content.Intent
import ru.aiss83.comunalexpenses2.data.AppContextHolder

/**
 * Shared storage contract between the shared module (writer) and the widget provider (reader).
 * Kept in one place so the keys cannot drift apart.
 */
object WidgetDataKeys {
    const val PREFS = "widget_data"
    const val COLD_WATER = "cold_water"
    const val HOT_WATER = "hot_water"
    const val DAY_ELECTRICITY = "day_electricity"
    const val NIGHT_ELECTRICITY = "night_electricity"
    const val LAST_DATE = "last_date"

    /**
     * Package-scoped broadcast telling the widget provider to re-render.
     * The provider also declares it in the manifest intent-filter.
     */
    const val REFRESH_ACTION = "ru.aiss83.comunalexpenses2.action.WIDGET_REFRESH"
}

actual fun updateWidgetData(
    coldWater: Long,
    hotWater: Long,
    dayElectricity: Long,
    nightElectricity: Long,
    date: String
) {
    val context = AppContextHolder.context ?: return
    context.getSharedPreferences(WidgetDataKeys.PREFS, Context.MODE_PRIVATE)
        .edit()
        .putLong(WidgetDataKeys.COLD_WATER, coldWater)
        .putLong(WidgetDataKeys.HOT_WATER, hotWater)
        .putLong(WidgetDataKeys.DAY_ELECTRICITY, dayElectricity)
        .putLong(WidgetDataKeys.NIGHT_ELECTRICITY, nightElectricity)
        .putString(WidgetDataKeys.LAST_DATE, date)
        .apply()

    notifyWidgetRefresh(context)
}

actual fun clearWidgetData() {
    val context = AppContextHolder.context ?: return
    context.getSharedPreferences(WidgetDataKeys.PREFS, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply()

    notifyWidgetRefresh(context)
}

/**
 * Ask [ru.aiss83.comunalexpenses2.android.QuickInputWidgetProvider] to re-read the stored
 * values. Without this the widget would only refresh on reboot or when re-added.
 */
private fun notifyWidgetRefresh(context: Context) {
    context.sendBroadcast(
        Intent(WidgetDataKeys.REFRESH_ACTION).setPackage(context.packageName)
    )
}
