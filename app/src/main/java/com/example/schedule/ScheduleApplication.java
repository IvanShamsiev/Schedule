package com.example.schedule;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.example.schedule.logic.ScheduleHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ScheduleApplication extends MultiDexApplication {

    public static final String GROUP_FILE = "group.json";

    public static final String LESSON_EXTRA = "lesson";
    public static final String CURRENT_DATE_EXTRA = "current_date";

    public static final List<String> dayOfWeek = Arrays.asList(
            "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");


    // Request code for StartActivity
    public static final int CHOSE_FILE_REQUEST_CODE = 1;
    public static final int START_ACTIVITY_REQUEST_CODE = 2;

    // Check JSON-Schedule version
    private static final int CURRENT_SCHEDULE_VERSION = 2;
    private static final String SCHEDULE_VERSION_PREF = "schedule_version";

    // Theme constants
    public static final String THEME_PREF = "theme_pref";
    public static int COLOR_PRIMARY, COLOR_PRIMARY_DARK, COLOR_SECONDARY, COLOR_ACCENT;
    public static boolean isDarkTheme;
    public static int currentTheme;

    @Override
    public void onCreate() {
        super.onCreate();

        setCurrentTheme();

        checkScheduleVersion();

        ScheduleHelper.INSTANCE.setContext(this);
    }


    private void setCurrentTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        isDarkTheme = sharedPreferences.getBoolean(THEME_PREF, false);
        currentTheme = isDarkTheme ? R.style.AppTheme_Dark : R.style.AppTheme_Light;

        /*sharedPreferences.registerOnSharedPreferenceChangeListener((sp, key) -> {
            if (!key.equals(THEME_PREF)) return;
            new RestartAppTask().execute();
        });*/

        COLOR_PRIMARY = isDarkTheme ? R.color.darkColorPrimary : R.color.lightColorPrimary;
        COLOR_PRIMARY_DARK = isDarkTheme ? R.color.darkColorPrimaryDark : R.color.lightColorPrimaryDark;
        COLOR_SECONDARY = isDarkTheme ? R.color.darkColorSecondary : R.color.lightColorSecondary;
        COLOR_ACCENT = isDarkTheme ? R.color.darkColorAccent : R.color.lightColorAccent;
    }

    public static class RestartAppTask extends AsyncTask<SwitchPreference, Void, Void> {

        @Override
        protected Void doInBackground(SwitchPreference... preferences) {
            try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            System.exit(0);
        }
    }


    private void checkScheduleVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int version = sharedPreferences.getInt(SCHEDULE_VERSION_PREF, -1);
        if (version == CURRENT_SCHEDULE_VERSION) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SCHEDULE_VERSION_PREF, CURRENT_SCHEDULE_VERSION);
        editor.commit();

        try {
            FileOutputStream outputStream = openFileOutput(GROUP_FILE, MODE_PRIVATE);
            outputStream.write(new byte[0]);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
