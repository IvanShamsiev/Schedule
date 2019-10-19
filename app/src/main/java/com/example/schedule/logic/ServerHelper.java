package com.example.schedule.logic;

import okhttp3.Callback;
import okhttp3.OkHttpClient;

import static com.example.schedule.ScheduleApplication.branchesUrl;
import static com.example.schedule.ScheduleApplication.url;

public class ServerHelper {

    public static void call(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

}
