package com.example.schedule.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.schedule.R;
import com.example.schedule.ScheduleApplication;
import com.example.schedule.logic.UpdateHelper;
import com.example.schedule.util.LoadDialog;

import static com.example.schedule.ScheduleApplication.currentTheme;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(currentTheme);
        setTitle("Настройки");

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        LoadDialog loadDialog;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            loadDialog = new LoadDialog(getFragmentManager());
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            SwitchPreference themePref = findPreference("theme_pref");
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                new ScheduleApplication.RestartAppTask().execute();
                return true;
            });

            Preference aboutAppPref = findPreference("about_app_pref");
            aboutAppPref.setOnPreferenceClickListener(preference -> {
                startActivity(AboutAppActivity.newIntent(getContext()));
                return true;
            });

            Preference updateAppPref = findPreference("check_update_app_pref");
            updateAppPref.setOnPreferenceClickListener(pref -> {
                UpdateHelper updateHelper = new UpdateHelper(getContext(), getFragmentManager());
                updateHelper.checkUpdate();
                return true;
            });

        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PreferencesActivity.class);
    }
}