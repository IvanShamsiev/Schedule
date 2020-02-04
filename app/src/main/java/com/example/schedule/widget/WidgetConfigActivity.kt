package com.example.schedule.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.schedule.R
import kotlinx.android.synthetic.main.widget_config.*


class WidgetConfigActivity : AppCompatActivity() {

    var widgetID = INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetID = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        if (widgetID == INVALID_APPWIDGET_ID) finish()

        val resultIntent = Intent().putExtra(EXTRA_APPWIDGET_ID, widgetID)
        setResult(Activity.RESULT_CANCELED, resultIntent)

        setContentView(R.layout.widget_config)

        btnCreateWidget.setOnClickListener { createWidget(resultIntent) }
    }

    private fun createWidget(intent: Intent) {
        val preferences = getSharedPreferences(WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        val theme = when (radioButtonsTheme.checkedRadioButtonId) {
            R.id.rbTransparent -> THEME_TRANSPARENT
            R.id.rbLight -> THEME_LIGHT
            R.id.rbDark -> THEME_DARK
            else -> THEME_LIGHT
        }
        editor.putInt(WIDGET_THEME + widgetID, theme)
        editor.commit()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        MainWidget.updateWidget(this, appWidgetManager, preferences, widgetID)

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val WIDGET_PREFERENCES = "widget_preferences"
        const val WIDGET_THEME = "widget_theme"

        const val THEME_TRANSPARENT = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
}
