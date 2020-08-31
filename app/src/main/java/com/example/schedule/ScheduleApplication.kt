package com.example.schedule

import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.core.content.edit
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.example.schedule.logic.ScheduleHelper.appContext
import com.example.schedule.logic.ServerHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class ScheduleApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        setCurrentTheme()
        setEveningChanged()
        checkScheduleVersion()
        appContext = this
    }

    private fun setEveningChanged() {
        eveningChanged = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(EVENING_CHANGED_PREF, false)
    }

    private fun setCurrentTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        isDarkTheme = sharedPreferences.getBoolean(THEME_PREF, false)
        currentTheme = if (isDarkTheme) R.style.AppTheme_Dark else R.style.AppTheme_Light

        /*sharedPreferences.registerOnSharedPreferenceChangeListener((sp, key) -> {
            if (!key.equals(THEME_PREF)) return;
            new RestartAppTask().execute();
        });*/

        COLOR_PRIMARY = if (isDarkTheme) R.color.darkColorPrimary else R.color.lightColorPrimary
        COLOR_PRIMARY_DARK = if (isDarkTheme) R.color.darkColorPrimaryDark else R.color.lightColorPrimaryDark
        COLOR_SECONDARY = if (isDarkTheme) R.color.darkColorSecondary else R.color.lightColorSecondary
        COLOR_ACCENT = if (isDarkTheme) R.color.darkColorAccent else R.color.lightColorAccent
    }

    class RestartAppTask : AsyncTask<SwitchPreference?, Void?, Void?>() {
        override fun doInBackground(vararg preferences: SwitchPreference?): Void? {
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            System.exit(0)
        }
    }

    private fun checkScheduleVersion() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val version = sharedPreferences.getInt(SCHEDULE_VERSION_PREF, -1)
        if (version == CURRENT_SCHEDULE_VERSION) return
        sharedPreferences.edit(commit = true) {
            putInt(SCHEDULE_VERSION_PREF, CURRENT_SCHEDULE_VERSION)
        }
        try {
            val outputStream = openFileOutput(GROUP_FILE, MODE_PRIVATE)
            outputStream.write(ByteArray(0))
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val GROUP_FILE = "group.json"
        const val LESSON_EXTRA = "lesson"
        const val CURRENT_DATE_EXTRA = "current_date"
        val dayOfWeek: List<String> = listOf(
                "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

        // Request code for StartActivity
        const val CHOSE_FILE_REQUEST_CODE = 1
        const val START_ACTIVITY_REQUEST_CODE = 2

        // Check JSON-Schedule version
        private const val CURRENT_SCHEDULE_VERSION = 2
        private const val SCHEDULE_VERSION_PREF = "schedule_version"

        // Check evening
        const val EVENING_CHANGED_PREF = "evening_changed"
        var eveningChanged: Boolean = false

        // Theme constants
        const val THEME_PREF = "theme_pref"
        var COLOR_PRIMARY = 0
        var COLOR_PRIMARY_DARK = 0
        var COLOR_SECONDARY = 0
        var COLOR_ACCENT = 0
        var isDarkTheme = false
        var currentTheme = 0



        fun checkEvening(preferences: SharedPreferences, onComplete: (() -> Unit)? = null): Disposable {
            return ServerHelper.checkEvening()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        preferences.edit {
                            putBoolean(EVENING_CHANGED_PREF, it)
                        }
                        eveningChanged = it
                        onComplete?.invoke()
                    }, {
                        eveningChanged = preferences.getBoolean(EVENING_CHANGED_PREF, false)
                        onComplete?.invoke()
                    })
        }
    }
}