package com.example.schedule.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.schedule.R
import com.example.schedule.ScheduleApplication
import com.example.schedule.ScheduleApplication.RestartAppTask
import com.example.schedule.logic.UpdateHelper
import com.example.schedule.util.LoadDialog

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ScheduleApplication.currentTheme)
        title = "Настройки"

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment: PreferenceFragmentCompat() {

        private lateinit var loadDialog: LoadDialog

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            loadDialog = LoadDialog(fragmentManager!!)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)

            val themePref = findPreference<SwitchPreference>("theme_pref")
            themePref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                RestartAppTask().execute()
                true
            }

            val aboutAppPref = findPreference<Preference>("about_app_pref")
            aboutAppPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, AboutAppActivity::class.java))
                true
            }

            val updateAppPref = findPreference<Preference>("check_update_app_pref")
            updateAppPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val updateHelper = UpdateHelper(context, fragmentManager)
                updateHelper.checkUpdate()
                true
            }
        }
    }
}