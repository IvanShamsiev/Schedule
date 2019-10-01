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

    private static Schedule mainSchedule = null;

    public static Schedule getSchedule() { // false: short; true: full
        return mainSchedule;
    }

    public static void setSchedule(String json) {
        mainSchedule = new Gson().fromJson(json, Schedule.class);
    }

    public static void loadSchedule(FileInputStream inputStream) throws IOException {
        StringBuilder json = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String s;
        while ((s = br.readLine()) != null) json.append(s);
        br.close();

        if (json.toString().isEmpty()) throw new IOException("Пустой файл с расписанием");

        setSchedule(json.toString());
    }

    public static void saveSchedule(String json, FileOutputStream outputStream) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
        bw.write(json);
        bw.close();
    }

    public static boolean isEven(Calendar date) { // true - Нижняя, false - Верхняя
        return (date.get(Calendar.WEEK_OF_YEAR) % 2) == 0;
    }
}
