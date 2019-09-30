package com.example.schedule.ui;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.schedule.BuildConfig;
import com.example.schedule.R;
import com.example.schedule.logic.UpdateHelper;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        //private static final int FILE_PERMISSION_REQUEST_CODE = 1;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs);

            Preference updateAppPref = findPreference("check_update_app_pref");
            updateAppPref.setOnPreferenceClickListener(pref -> {
                checkUpdate();
                return false;
            });
        }

        private void checkUpdate() {
            UpdateHelper.checkUpdate(new Callback() {

                String newVersion;
                String newVersionUrl;

                Handler updCheckHandler = new Handler(msg -> {
                    if (newVersion.equals(BuildConfig.VERSION_NAME)) {
                        Toast.makeText(getContext(), "У вас установлена последняя версия приложения", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    openUpdateDialog(newVersion, newVersionUrl);
                    return true;
                });

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    MainActivity.showToast(getContext(), "Не удалось проверить обновление");
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
                        MainActivity.showToast(getContext(), "Не удалось прочитать ответ сервера");
                        e.printStackTrace();
                    }
                }
            });
        }

        private void openUpdateDialog(String newVersion, String newVersionUrl) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Текущая версия: " + BuildConfig.VERSION_NAME + "\n" + "Новая версия: " + newVersion)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Обновить", (dialogInterface, i) -> update(newVersionUrl))
                    //.setPositiveButton("Обновить", (dialogInterface, i) -> UpdateHelper.update(newVersionUrl, updateCallback))
                    .show();
        }

        private void update(String url) {
            /*ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            int permissionCheck = checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Разрешение получено");
            } else {
                System.out.println("Нет разрешения");
                return;
            }*/

            // Set path for file
            String destination = getContext().getExternalFilesDir("update") + "/";
            String fileName = "ScheduleUpdate.apk";
            String filePath = destination + fileName;

            // Check if file already exists
            File file = new File(filePath);
            if (file.exists()) file.delete();

            // Set Download Manager request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("ScheduleUpdate.apk");
            request.setDescription("Скачивание обновления");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(file));

            // Get download service and enqueue file
            DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = manager.enqueue(request);

            // Set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context context, Intent i) {

                    Intent intentInstall = new Intent(Intent.ACTION_VIEW);
                    Uri uri = FileProvider.getUriForFile(getContext(),
                            BuildConfig.APPLICATION_ID + ".provider", file);
                    intentInstall.setDataAndType(uri, "application/vnd.android.package-archive");
                    intentInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intentInstall);

                    getContext().unregisterReceiver(this);
                }
            };

            // Register receiver for when .apk download is compete
            getContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        /*@Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == FILE_PERMISSION_REQUEST_CODE) {
                if (grantResults[0] == -1)
                    Toast.makeText(getContext(), "Нет разрешений на скачивание файла", Toast.LENGTH_SHORT).show();
            }
            System.out.println("requestCode = [" + requestCode + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        }/*




        /*private void go() {
            String fileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/app-debug.apk";

            File file = new File(fileName);

            System.out.println(file.exists());
            System.out.println(file.setExecutable(true));
            System.out.println(file.canExecute());

            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }*/
    }
}