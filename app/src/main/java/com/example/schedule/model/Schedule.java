package com.example.schedule.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.example.schedule.ui.MainActivity.scheduleFileName;
import static com.example.schedule.ui.MainActivity.url;

public class Schedule {

    private HashMap<Integer, List<List<String>>> unevenWeek, evenWeek;

    private Schedule() {}

    public List<Lesson> getLessons(Calendar date) {
        List<Lesson> lessons = new ArrayList<>();

        List<List<String>> stringLessons = isEven(date) ?
            evenWeek.get(date.get(Calendar.DAY_OF_WEEK)):
            unevenWeek.get(date.get(Calendar.DAY_OF_WEEK));

        if (stringLessons != null && stringLessons.size() != 0)
        for (List<String> lessonInfo: stringLessons) lessons.add(new Lesson(lessonInfo));

        return lessons;
    }

    public boolean isEven(Calendar date) { // true - Нижняя, false - Верхняя
        return (date.get(Calendar.WEEK_OF_YEAR) % 2) == 0;
    }


    @Override
    @NonNull
    public String toString() {
        return "Schedule{" +
                ", evenWeek=" + evenWeek +
                ", unevenWeek=" + unevenWeek +
                '}';
    }
}
