package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class EchoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.widgets.SMASH_STREAK") {
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(ComponentName(context, EchoWidgetProvider::class.java))
            if (ids != null) {
                for (i in ids.indices) {
                    val id = ids[i]
                    val views = RemoteViews(context.packageName, R.layout.widget_layout)
                    views.setTextViewText(R.id.widget_streak, "⚡ FIRE STREAK: ACTIVE!")
                    widgetManager.updateAppWidget(id, views)
                }
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Set text
        views.setTextViewText(R.id.widget_title, "ECHO NOTES STUDY GRID")
        views.setTextViewText(R.id.widget_streak, "⚡ 1-DAY Streak: UNBREAKABLE")

        // Intent to launch MainActivity direct to STUDY TIMER
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("LAUNCH_TAB", "DASHBOARD")
            putExtra("FORCE_TIMER_START", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_smash_btn, pendingIntent)

        // Quick Repetition Battle intent
        val battleIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("LAUNCH_TAB", "REVISION")
        }
        val battlePendingIntent = PendingIntent.getActivity(
            context, appWidgetId + 1000, battleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_battle_btn, battlePendingIntent)

        // Instruct widget manager to update
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
