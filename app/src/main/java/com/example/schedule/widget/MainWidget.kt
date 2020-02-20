package com.example.schedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.example.schedule.R
import com.example.schedule.logic.ScheduleHelper
import com.example.schedule.model.Group
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_DARK
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_LIGHT
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_TRANSPARENT
import com.example.schedule.widget.WidgetConfigActivity.Companion.WIDGET_PREFERENCES
import com.example.schedule.widget.WidgetConfigActivity.Companion.WIDGET_THEME
import java.util.*


class MainWidget: AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        weekEvenStyle = sharedPreferences.getString("week_even_style", "0") == "0"
        showNavigationLayout = sharedPreferences.getBoolean("show_navigation_layout", false)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        //println("MainWidget.onUpdate appWidgetIds = [${appWidgetIds.contentToString()}]")

        val preferences = context.getSharedPreferences(WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        appWidgetIds.forEach { widgetId -> updateWidget(context, appWidgetManager, preferences, widgetId) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        val editor: SharedPreferences.Editor = context.getSharedPreferences(WIDGET_PREFERENCES, Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { widgetId -> editor.remove(WIDGET_THEME + widgetId) }
        editor.commit()
    }




    companion object {
        const val DATE_EXTRA = "date"
        const val IS_EVEN_EXTRA = "is_even"

        private var weekEvenStyle: Boolean = true // true: В-Н; false: Ч-Н
        private var showNavigationLayout: Boolean = false // false: hide, true: show = false

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, preferences: SharedPreferences, widgetId: Int) {

            val locale = Locale.getDefault()
            val date: Calendar = GregorianCalendar.getInstance()
            val isEven = ScheduleHelper.isEven(date)

            val currentView = when (preferences.getInt(WIDGET_THEME + widgetId, THEME_LIGHT)) {
                THEME_TRANSPARENT -> R.layout.widget_main_transparent
                THEME_LIGHT -> R.layout.widget_main_light
                THEME_DARK -> R.layout.widget_main_dark
                else -> R.layout.widget_main_light
            }
            val remoteViews = RemoteViews(context.packageName, currentView)

            val updIntent = Intent(context, MainWidget::class.java)
            updIntent.action = ACTION_APPWIDGET_UPDATE
            updIntent.putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            val updPIntent = PendingIntent.getBroadcast(context, widgetId, updIntent, 0)

            var dayOfWeek = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Wrong day of week"
            if (dayOfWeek.isNotEmpty()) dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase(Locale.getDefault()) + dayOfWeek.substring(1)
            remoteViews.setTextViewText(R.id.tvDayOfWeek, dayOfWeek)
            remoteViews.setOnClickPendingIntent(R.id.tvDayOfWeek, updPIntent)

            var dateString = date.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) ?: "Wrong month name"
            dateString = dateString.substring(0, 1).toUpperCase(locale) + dateString.substring(1)
            remoteViews.setTextViewText(R.id.tvDate, String.format("%d %s", date[Calendar.DAY_OF_MONTH], dateString))

            val upWeek = if (weekEvenStyle) "Верхняя неделя" else "Нечётная неделя"
            val downWeek = if (weekEvenStyle) "Нижняя неделя" else "Чётная неделя"
            remoteViews.setTextViewText(R.id.tvEven, if (isEven) downWeek else upWeek)


            val group: Group? = ScheduleHelper.loadGroup(context)

            if (group == null) {
                remoteViews.setViewVisibility(R.id.lvLessons, View.GONE)
                remoteViews.setViewVisibility(R.id.tvNoLessons, View.VISIBLE)
                remoteViews.setTextViewText(R.id.tvNoLessons, "Группа ещё не выбрана")
            } else {
                val lessons = if (isEven) group.week.getDay(date).evenLessons
                else group.week.getDay(date).unevenLessons

                if (lessons.isEmpty()) {
                    remoteViews.setViewVisibility(R.id.lvLessons, View.GONE)
                    remoteViews.setViewVisibility(R.id.tvNoLessons, View.VISIBLE)
                } else {
                    remoteViews.setViewVisibility(R.id.lvLessons, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.tvNoLessons, View.GONE)

                    val adapter = Intent(context, LessonsService::class.java)
                    adapter.putExtra(EXTRA_APPWIDGET_ID, widgetId)
                    adapter.putExtra(DATE_EXTRA, date.time.time)
                    adapter.putExtra(IS_EVEN_EXTRA, isEven)
                    val data: Uri = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
                    adapter.data = data // Чтобы интент каждый раз различался
                    remoteViews.setRemoteAdapter(R.id.lvLessons, adapter)
                }
            }

            val updateIntent = Intent(context, MainWidget::class.java)
            updateIntent.action = ACTION_APPWIDGET_UPDATE
            updateIntent.putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            val pendingIntent = PendingIntent.getBroadcast(context, widgetId, updateIntent, 0)
            remoteViews.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }
}