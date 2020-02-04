package com.example.schedule.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.schedule.R
import com.example.schedule.model.Lesson
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_DARK
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_LIGHT
import com.example.schedule.widget.WidgetConfigActivity.Companion.THEME_TRANSPARENT
import com.example.schedule.widget.WidgetConfigActivity.Companion.WIDGET_THEME

class LessonFactory(private val context: Context,
                    private var data: List<Lesson>,
                    private val widgetId: Int): RemoteViewsService.RemoteViewsFactory {

    private var theme = THEME_LIGHT

    override fun onCreate() {
        val preferences = context.getSharedPreferences(WidgetConfigActivity.WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        theme = preferences.getInt(WIDGET_THEME + widgetId, THEME_LIGHT)
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onDataSetChanged() {

    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {

        val currentItemLayout = when (theme) {
            THEME_TRANSPARENT -> R.layout.widget_item_lesson_transparent
            THEME_LIGHT -> R.layout.widget_item_lesson_light
            THEME_DARK -> R.layout.widget_item_lesson_dark
            else -> R.layout.widget_item_lesson_light
        }
        val remoteViews = RemoteViews(context.packageName, currentItemLayout)
        val lesson = data[position]
        with(remoteViews) {
            setTextViewText(R.id.tvBeginTime, lesson.beginTime)
            setTextViewText(R.id.tvEndTime, lesson.endTime)
            setTextViewText(R.id.tvName, lesson.name)
            setTextViewText(R.id.tvTeacher, lesson.teacher)
            setTextViewText(R.id.tvLocation, lesson.location)
            setTextViewText(R.id.tvType, lesson.type)
        }
        return remoteViews
    }

    override fun getCount(): Int = data.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {

    }

}