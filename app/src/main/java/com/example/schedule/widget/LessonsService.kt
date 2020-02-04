package com.example.schedule.widget

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.widget.RemoteViewsService
import com.example.schedule.logic.ScheduleHelper
import com.example.schedule.model.Group
import com.example.schedule.widget.MainWidget.Companion.DATE_EXTRA
import com.example.schedule.widget.MainWidget.Companion.IS_EVEN_EXTRA
import java.util.*

class LessonsService: RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val date = intent.getSerializableExtra(DATE_EXTRA) as Calendar
        val isEven = intent.getBooleanExtra(IS_EVEN_EXTRA, false)

        val group: Group? = ScheduleHelper.loadGroup(applicationContext)

        val lessons = if (isEven) group?.week?.getDay(date)?.evenLessons ?: listOf()
        else group?.week?.getDay(date)?.unevenLessons ?: listOf()

        return LessonFactory(applicationContext, lessons.toList(), intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID))
    }
}