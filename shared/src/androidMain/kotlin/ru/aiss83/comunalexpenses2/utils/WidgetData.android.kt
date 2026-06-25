package ru.aiss83.comunalexpenses2.utils

import android.content.Context
import ru.aiss83.comunalexpenses2.data.AppContextHolder

actual fun updateWidgetData(coldWater: Long, hotWater: Long, dayElectricity: Long, nightElectricity: Long, date: String) {
    val context = AppContextHolder.context ?: return
    val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
    prefs.edit()
        .putLong("cold_water", coldWater)
        .putLong("hot_water", hotWater)
        .putLong("day_electricity", dayElectricity)
        .putLong("night_electricity", nightElectricity)
        .putString("last_date", date)
        .apply()
}
