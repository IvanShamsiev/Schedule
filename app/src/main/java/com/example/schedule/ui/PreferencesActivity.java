package com.example.schedule.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.schedule.BuildConfig;
import com.example.schedule.R;
import com.example.schedule.logic.UpdateHelper;
import com.example.schedule.util.LoadDialog;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.schedule.ScheduleApplication.showToast;

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

            Preference aboutAppPref = findPreference("about_app_pref");
            aboutAppPref.setOnPreferenceClickListener(preference -> {
                startActivity(AboutAppActivity.newIntent(getContext()));
                return true;
            });

            Preference updateAppPref = findPreference("check_update_app_pref");
            updateAppPref.setOnPreferenceClickListener(pref -> {
                loadDialog.show("Проверка обновлений");
                UpdateHelper.checkUpdate(checkUpdateCallback);
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
                public void onFailure(Call call, IOException e) {
                    loadDialog.close();
                    showToast(getContext(), "Не удалось проверить обновление");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    loadDialog.close();
                    try {
                        String str = response.body().string();
                        String[] fromJson = new Gson().fromJson(str, String[].class);
                        newVersion = fromJson[0];
                        newVersionUrl = fromJson[1];
                        updCheckHandler.sendEmptyMessage(0);
                    }
                    catch (IOException e) {
                        showToast(getContext(), "Не удалось прочитать ответ сервера");
                        e.printStackTrace();
                    }
                }
        };

        private void openUpdateDialog(String newVersion, String newVersionUrl) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Текущая версия: " + BuildConfig.VERSION_NAME + "\n" + "Новая версия: " + newVersion)
                    .setPositiveButton("Обновить", (dialogInterface, i) -> {
                        UpdateHelper.update(getContext(), newVersionUrl);
                        Toast.makeText(getContext(), "Скачивание обновления", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }
}