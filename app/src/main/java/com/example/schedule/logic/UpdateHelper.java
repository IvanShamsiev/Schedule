package com.example.schedule.logic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.example.schedule.BuildConfig;
import com.example.schedule.util.LoadDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.schedule.ScheduleApplication.checkUpdateUrl;
import static com.example.schedule.ScheduleApplication.showToast;
import static com.example.schedule.ScheduleApplication.url;

public class UpdateHelper {

    private Context context;
    private LoadDialog loadDialog;

    public UpdateHelper(Context context, FragmentManager fragmentManager) {
        this.context = context;

        loadDialog = new LoadDialog(fragmentManager);
    }

    public void checkUpdate() {
        loadDialog.show("Проверка обновлений");
        ServerHelper.call(url + checkUpdateUrl, checkUpdateCallback);
    }


    private Callback checkUpdateCallback = new Callback() {

        String newVersion;
        String newVersionUrl;

        Handler updCheckHandler = new Handler(msg -> {
            if (newVersion.equals(BuildConfig.VERSION_NAME)) {
                Toast.makeText(context, "У вас установлена последняя версия приложения",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            openUpdateDialog(newVersion, newVersionUrl);
            return true;
        });

        @Override
        public void onFailure(Call call, IOException e) {
            loadDialog.close();
            showToast(context, "Не удалось проверить обновление");
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
                showToast(context, "Не удалось прочитать ответ сервера");
                e.printStackTrace();
            }
        }
    };

    private void openUpdateDialog(String newVersion, String newVersionUrl) {
        new AlertDialog.Builder(context)
                .setMessage("Текущая версия: " + BuildConfig.VERSION_NAME + "\n" + "Новая версия: " + newVersion)
                .setPositiveButton("Обновить", (dialogInterface, i) -> {
                    update(context, newVersionUrl);
                    Toast.makeText(context, "Скачивание обновления", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void update(Context context, String url) {

        // Set path for file
        String destination = context.getExternalFilesDir("update") + "/";
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
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        // Set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {

                Intent intentInstall = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".provider", file);
                intentInstall.setDataAndType(uri, "application/vnd.android.package-archive");
                intentInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intentInstall);

                context.unregisterReceiver(this);
            }
        };

        // Register receiver for when .apk download is compete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        onDestroy();
    }

    private void onDestroy() {
        context = null;
        loadDialog = null;
    }

}
