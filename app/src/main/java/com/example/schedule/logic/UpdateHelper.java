package com.example.schedule.logic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.schedule.BuildConfig;

import java.io.File;

import okhttp3.Callback;
import okhttp3.OkHttpClient;

import static com.example.schedule.ScheduleApplication.checkUpdateUrl;
import static com.example.schedule.ScheduleApplication.url;

public class UpdateHelper {

    public static void checkUpdate(Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + checkUpdateUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void update(Context context, String url) {

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
    }

}
