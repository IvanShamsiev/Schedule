package com.example.schedule;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

public class ScheduleApplication extends Application {

    public static final String scheduleFileName = "schedule.json";
    public static final String branchesUrl = "getBranches.php";
    public static final String checkUpdateUrl = "checkUpdate.php";
    public static final String url = "https://schedule2171112.000webhostapp.com/";
    public static final List<String> dayOfWeek = Arrays.asList(
            "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
