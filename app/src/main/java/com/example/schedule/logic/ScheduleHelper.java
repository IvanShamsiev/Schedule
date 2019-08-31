package com.example.schedule.logic;

import com.example.schedule.model.Schedule;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.example.schedule.ui.MainActivity.scheduleFileName;
import static com.example.schedule.ui.MainActivity.url;

public class ScheduleHelper {


    private static String stringSchedule;
    public static String getStringSchedule() { return stringSchedule; }


    private static Schedule shortNamesWeek = null;
    private static Schedule fullNamesWeek = null;


    private static final MediaType JsonType = MediaType.parse("application/json; charset=utf-8");

    public static Schedule getInstance(boolean fullLessonsNames) { // false: short; true: full
        return fullLessonsNames ? fullNamesWeek : shortNamesWeek;
    }

    public static void setSchedule(String json) {
        stringSchedule = json;
        Schedule[] weeks = new Gson().fromJson(json, Schedule[].class);
        shortNamesWeek = weeks[0];
        fullNamesWeek = weeks[1];
    }

    public static void downloadSchedule(Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + scheduleFileName)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void sendSchedule(Callback callback, String json) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder().add("newSchedule", json).build();
        Request request = new Request.Builder()
                .url(url + "changeSchedule.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void loadSchedule(FileInputStream inputStream, Callback callback) {
        StringBuilder json = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String s;
            while ((s = br.readLine()) != null) json.append(s).append("\n");
            br.close();
        } catch (IOException e) {
            downloadSchedule(callback);
            return;
        }

        setSchedule(json.toString());
    }

    public static void saveSchedule(String json, FileOutputStream outputStream) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
