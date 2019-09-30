package com.example.schedule.logic;

import com.example.schedule.ui.MainActivity;

import okhttp3.Callback;
import okhttp3.OkHttpClient;

public class UpdateHelper {

    public static void checkUpdate(Callback callback) {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(MainActivity.url + MainActivity.checkUpdateUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void update(String url, Callback callback) {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

}
