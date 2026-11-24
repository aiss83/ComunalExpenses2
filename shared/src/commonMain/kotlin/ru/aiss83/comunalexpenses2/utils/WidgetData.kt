package ru.aiss83.comunalexpenses2.utils

/**
 * Saves the latest reading data for the home screen widget and refreshes the widget.
 */
expect fun updateWidgetData(coldWater: Long, hotWater: Long, dayElectricity: Long, nightElectricity: Long, date: String)

/**
 * Clears the widget data (e.g. when all records are deleted) and refreshes the widget.
 */
expect fun clearWidgetData()
