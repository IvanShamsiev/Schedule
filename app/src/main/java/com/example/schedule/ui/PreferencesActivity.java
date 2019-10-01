package com.example.schedule.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.schedule.BuildConfig;
import com.example.schedule.R;
import com.example.schedule.ScheduleApplication;
import com.example.schedule.logic.UpdateHelper;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Настройки");

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Preference updateAppPref = findPreference("check_update_app_pref");
            updateAppPref.setOnPreferenceClickListener(pref -> {
                UpdateHelper.checkUpdate(checkUpdateCallback);
                return true;
            });

            Preference aboutAppPref = findPreference("about_app_pref");
            aboutAppPref.setOnPreferenceClickListener(preference -> {
                startActivity(AboutAppActivity.newIntent(getContext()));
                return true;
            });

        }

        Callback checkUpdateCallback = new Callback() {

                String newVersion;
                String newVersionUrl;

                Handler updCheckHandler = new Handler(msg -> {
                    if (newVersion.equals(BuildConfig.VERSION_NAME)) {
                        Toast.makeText(getContext(), "У вас установлена последняя версия приложения",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    openUpdateDialog(newVersion, newVersionUrl);
                    return true;
                });

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ScheduleApplication.showToast(getContext(), "Не удалось проверить обновление");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    try {
                        String str = response.body().string();
                        String[] fromJson = new Gson().fromJson(str, String[].class);
                        newVersion = fromJson[0];
                        newVersionUrl = fromJson[1];
                        updCheckHandler.sendEmptyMessage(0);
                    }
                    catch (IOException e) {
                        ScheduleApplication.showToast(getContext(), "Не удалось прочитать ответ сервера");
                        e.printStackTrace();
                    }
                }
        };

        private void openUpdateDialog(String newVersion, String newVersionUrl) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Текущая версия: " + BuildConfig.VERSION_NAME + "\n" + "Новая версия: " + newVersion)
                    .setPositiveButton("Обновить", (dialogInterface, i) -> UpdateHelper.update(getContext(), newVersionUrl))
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }
}