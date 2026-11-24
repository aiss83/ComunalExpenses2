package ru.aiss83.comunalexpenses2.android

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import ru.aiss83.comunalexpenses2.utils.WidgetDataKeys

/**
 * Home screen widget showing the latest readings and a quick-add button.
 *
 * Data is written by the shared module ([ru.aiss83.comunalexpenses2.utils.updateWidgetData]);
 * this provider re-renders whenever it receives [WidgetDataKeys.REFRESH_ACTION].
 */
class QuickInputWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetDataKeys.REFRESH_ACTION) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, QuickInputWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                onUpdate(context, manager, ids)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, buildViews(context, widgetId))
        }
    }

    private fun buildViews(context: Context, widgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.quick_input_widget)

        // Read the latest readings written by the shared module
        val prefs = context.getSharedPreferences(WidgetDataKeys.PREFS, Context.MODE_PRIVATE)
        val cold = prefs.getLong(WidgetDataKeys.COLD_WATER, 0)
        val hot = prefs.getLong(WidgetDataKeys.HOT_WATER, 0)
        val day = prefs.getLong(WidgetDataKeys.DAY_ELECTRICITY, 0)
        val night = prefs.getLong(WidgetDataKeys.NIGHT_ELECTRICITY, 0)
        val lastDate = prefs.getString(WidgetDataKeys.LAST_DATE, null)

        if (lastDate != null) {
            val coldLabel = context.getString(R.string.widget_cold)
            val hotLabel = context.getString(R.string.widget_hot)
            val dayLabel = context.getString(R.string.widget_day)
            val nightLabel = context.getString(R.string.widget_night)

            views.setTextViewText(
                R.id.widget_last_reading,
                "$lastDate\n$coldLabel: $cold  $hotLabel: $hot\n$dayLabel: $day  $nightLabel: $night"
            )
        }

        // Tap opens the add-reading screen
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

        return views
    }
}
