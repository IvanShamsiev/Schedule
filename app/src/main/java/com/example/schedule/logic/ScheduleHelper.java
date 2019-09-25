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
import java.util.Calendar;

public class ScheduleHelper {


    private static String stringSchedule;

    private static Schedule mainSchedule = null;

    public static String getStringSchedule() { return stringSchedule; }

    public static Schedule getSchedule() { // false: short; true: full
        return mainSchedule;
    }

    public static void setSchedule(String json) {
        stringSchedule = json;
        mainSchedule = new Gson().fromJson(ScheduleHelper.getStringSchedule(), Schedule.class);
    }

    /*public static void downloadSchedule(Callback callback) {
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + scheduleUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }*/

    /*public static void sendSchedule(Callback callback, String json) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder().add("newSchedule", json).build();
        Request request = new Request.Builder()
                .url(url + changeScheduleName)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }*/

    public static void loadSchedule(FileInputStream inputStream) throws IOException {
        StringBuilder json = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String s;
        while ((s = br.readLine()) != null) json.append(s).append("\n");
        br.close();

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
