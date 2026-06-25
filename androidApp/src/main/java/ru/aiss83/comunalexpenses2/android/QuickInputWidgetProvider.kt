package ru.aiss83.comunalexpenses2.android

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Home screen widget showing last readings and quick-add button.
 */
class QuickInputWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.quick_input_widget)

            // Read last readings from SharedPreferences
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val cold = prefs.getLong("cold_water", 0)
            val hot = prefs.getLong("hot_water", 0)
            val day = prefs.getLong("day_electricity", 0)
            val night = prefs.getLong("night_electricity", 0)
            val lastDate = prefs.getString("last_date", null)

            val coldLabel = context.getString(R.string.widget_cold)
            val hotLabel = context.getString(R.string.widget_hot)
            val dayLabel = context.getString(R.string.widget_day)
            val nightLabel = context.getString(R.string.widget_night)

            if (lastDate != null) {
                views.setTextViewText(
                    R.id.widget_last_reading,
                    "$lastDate\n$coldLabel: $cold  $hotLabel: $hot\n$dayLabel: $day  $nightLabel: $night"
                )
            }

            // Tap opens add-reading screen
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_add_screen", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add_button, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
