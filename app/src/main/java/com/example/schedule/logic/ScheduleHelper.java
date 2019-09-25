package com.example.schedule.logic;

import com.example.schedule.model.Lesson;
import com.example.schedule.model.Schedule;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.example.schedule.ui.MainActivity.changeScheduleName;
import static com.example.schedule.ui.MainActivity.scheduleUrl;
import static com.example.schedule.ui.MainActivity.url;

public class ScheduleHelper {


    private static String stringSchedule;

    private static Schedule mainSchedule = null;

    public static String getStringSchedule() { return stringSchedule; }

    public static Schedule getInstance() { // false: short; true: full
        return mainSchedule;
    }

    public static void setSchedule(String json) {
        stringSchedule = json;
        HashMap<Integer, List<Lesson>> scheduleMap = new Gson().fromJson(ScheduleHelper.getStringSchedule(), HashMap.class);
        mainSchedule = new Schedule(scheduleMap);
    }

    public static void downloadSchedule(Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + scheduleUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void sendSchedule(Callback callback, String json) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder().add("newSchedule", json).build();
        Request request = new Request.Builder()
                .url(url + changeScheduleName)
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
            e.printStackTrace();
            //downloadSchedule(callback);
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

    public static boolean isEven(Calendar date) { // true - Нижняя, false - Верхняя
        return (date.get(Calendar.WEEK_OF_YEAR) % 2) == 0;
    }
}
