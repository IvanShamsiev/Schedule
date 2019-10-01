package com.example.schedule.logic;

import okhttp3.Callback;
import okhttp3.OkHttpClient;

import static com.example.schedule.ScheduleApplication.branchesUrl;
import static com.example.schedule.ScheduleApplication.url;

public class StartHelper {

    public static void getBranches(Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + branchesUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getBranch(String branchUrl, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(branchUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }

}
